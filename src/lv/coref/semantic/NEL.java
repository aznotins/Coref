/*******************************************************************************
 * Copyright 2014,2015 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package lv.coref.semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.MentionChain;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.coref.io.PipeClient;
import lv.coref.lv.Constants.Category;
import lv.coref.semantic.KNB.EntityData;
import lv.coref.semantic.KNB.FrameData;
import lv.util.Pair;

public class NEL {

	private final static Logger log = Logger.getLogger(NEL.class.getName());

	private static NEL nel = null;

	boolean REAL_UPLOAD = false;
	boolean SHOW_DISAMBUGATION = false;

	public static NEL getInstance() {
		if (nel == null) {
			nel = new NEL();
			nel.init(Config.getInstance().filter(Config.PREFIX_NEL));
		}
		return nel;
	}

	public void init(Properties prop) {
		if (prop.getProperty(Config.PROP_NEL_UPLOAD, "false").equalsIgnoreCase("true"))
			REAL_UPLOAD = true;
		if (prop.getProperty(Config.PROP_NEL_SHOW_DISAMBIGUATION, "false").equalsIgnoreCase("true"))
			SHOW_DISAMBUGATION = true;
	}

	public List<Entity> link(Text text) {
		List<Entity> entities = new ArrayList<>(text.getMentionChains().size());
		for (MentionChain mc : text.getMentionChains()) {
			Entity entity = Entity.makeEntity(mc);
			if (entity != null) {
				entities.add(entity);
				mc.setEntity(entity);
			}
		}
		fetchGlobalIds(text, entities);
		if (SHOW_DISAMBUGATION) {
			for (Entity e : entities) {
				System.err.println(e);
			}
		}
		return entities;
	}

	public Set<String> getGlobalIdCandidates(Entity e) {
		String title = NELUtils.fixname(e.getTitle());
		Set<String> ids = new HashSet<>();
		ids.addAll(KNB.getInstance().getEntityIdsByName(title));
		if (ids.size() == 0) {
			for (String alias : e.getAliases()) {
				if (!NELUtils.goodAlias(alias, e.getCategory()))
					continue;
				ids.addAll(KNB.getInstance().getEntityIdsByName(alias));
				ids.addAll(KNB.getInstance().getEntityIdsByName(NELUtils.clearOrgName(alias)));
			}
		}
		return ids;
	}

	public void fetchGlobalIds(Text text, List<Entity> entities) {
		List<Pair<Entity, Set<String>>> toDisambiguate = new ArrayList<>(entities.size());
		for (Entity e : entities) {
			Set<String> ids = getGlobalIdCandidates(e);
			if (ids.size() == 0) {
				// ieliek DB jaunu entītiju
				e.setId("_NEW_ENTITY_");
				continue;
			}
			// inWhitelist = False
			// for candidateID in matchedEntities:
			// if candidateID in disambiguationWhitelist: # Ja ir blessed
			// norāde, ka tieši šī globālā entītija ir šajā dokumentā
			// entity['GlobalID'] = candidateID
			// inWhitelist = True
			// if inWhitelist: # Šajā gadījumā neskatamies kā disambiguēt
			// continue

			// Disabiguējam un augšupielādējam tikai personas un organizācijas
			if (ids.size() > 1
					&& (e.getCategory().equals(Category.person) || e.getCategory().equals(Category.organization))) {
				toDisambiguate.add(new Pair<Entity, Set<String>>(e, ids));
			} else {
				// klasifikatoriem tāpat daudzmaz vienalga, vai pie kaut kā
				// piesaista vai veido jaunu
				e.setId(ids.iterator().next());
				if (REAL_UPLOAD
						&& (e.getCategory().equals(Category.person) || e.getCategory().equals(Category.organization))) {
					// api.insertMention(matchedEntities[0], documentId,
					// locations=entity.get('locations'))
				}
			}
		}

		// TODO - šobrīd mentionbag visiem ir vienāds un tādēļ iekļauj arī pašas
		// disambiguējamās entītijas vārdus
		Bag mentionBag = CDCBags.makeMentionBag(entities);

		for (Pair<Entity, Set<String>> pair : toDisambiguate) {
			Entity entity = pair.first;
			Set<String> candidates = pair.second;
			String id = dissambiguate(entity, candidates, text, mentionBag);
			if (id == null) {
				log.log(Level.WARNING, "Failed to resolve entity {0} {1} from candidates {2}",
						new Object[] { entity.getTitle(), entity.getAliases(), candidates });
				continue;
			}
			entity.setId(id);
		}
	}

	/**
	 * Disambiguē dokumenta entītijas
	 * 
	 * @param entity
	 * @param candidates
	 * @param text
	 * @param mentionBag
	 */
	public String dissambiguate(Entity entity, Collection<String> candidates, Text text, Bag mentionBag) {

		CDCBags eBags = new CDCBags();
		eBags.mentionBag = mentionBag;
		eBags.nameBag = CDCBags.makeNameBag(entity);
		eBags.contextBag = CDCBags.makeContextBag(entity);

		if (SHOW_DISAMBUGATION) {
			System.err.printf("\n--- Disambiguate %s (%s)\n", entity.getTitle(), entity.getCategory());
			System.err.printf("nameBag : %s\n", eBags.nameBag);
			System.err.printf("mentionBag : %s\n", eBags.mentionBag);
			System.err.printf("contextBag : %s\n", eBags.contextBag);
			System.err.println("----");
		}

		double maxSim = -99999;
		String maxId = null;
		Map<String, Double> cosineSim = new HashMap<>();
		for (String candidateId : candidates) {
			CDCBags bags = KNB.getInstance().getCDCBags(candidateId);

			if (bags == null) {
				/*
				 * šai entītijai nekad nav ģenerēti CDC bagi... uzģenerēsim!
				 * TODO - varbūt šo efektīvāk veikt kā batch job kautkur citur,
				 * piemēram, pie freimu summarizācijas šai entītei
				 */
				log.log(Level.INFO, "Make global entity bags for {0}", new Object[] { candidateId });
				bags = NEL.makeGlobalEntityBags(candidateId);
			}

			if (SHOW_DISAMBUGATION) {
				EntityData ed = KNB.getInstance().getEntityData(candidateId, false);
				System.err.printf("Candidate: #%s %s\n", candidateId, ed.name);
				System.err.printf("%.6f name match %s\n", CDCBags.cosineSimilarity(eBags.nameBag, bags.nameBag),
						eBags.nameBag);
				System.err.printf("%.6f mention match %s\n",
						CDCBags.cosineSimilarity(eBags.mentionBag, bags.mentionBag), eBags.mentionBag);
				System.err.printf("%.6f context match %s\n",
						CDCBags.cosineSimilarity(eBags.contextBag, bags.contextBag), eBags.contextBag);
			}

			double sim = CDCBags.cosineSimilarity(bags, eBags);
			cosineSim.put(candidateId, sim);
			if (sim > maxSim) {
				maxSim = sim;
				maxId = candidateId;
			}
		}

		if (maxId == null) {
			log.log(Level.INFO, "Did not resolve candidates for {0} {1}: maxId = {2}, maxSim = {3} ", new Object[] {
					entity.getTitle(), candidates, maxId, maxSim }); // parameter
																		// log
																		// messages
																		// cannot
																		// contain
																		// "'"
		}

		if (SHOW_DISAMBUGATION) {
			System.err.printf("\nChosed: %s (%.4f)\n\n", maxId, maxSim);
		}

		return maxId;
	}

	/**
	 * Savāc visu vajadzīgo lai uztaisītu globālajai entītijai CDC datus
	 * 
	 * @param entityId
	 */
	public static CDCBags makeGlobalEntityBags(String entityId) {
		KNB knb = KNB.getInstance();

		/*
		 * namebag liksim aliasus, kā arī amatus/nodarbošanās - jo tie nonāk
		 * dokumenta entītijas aliasos mentionbag liksim visas 'ID-entītes' kas
		 * labajos freimos ir saistītas ar šo ID - personas, organizācijas,
		 * vietas
		 */
		Bag nameBag = new Bag();
		Bag mentionBag = new Bag();
		Bag contextBag = new Bag();

		EntityData ed = knb.getEntityData(entityId, false);

		if (ed == null) {
			log.log(Level.WARNING, "No such entityId found while building entity bags: {0}", entityId);
			return new CDCBags();
		}

		// vairākvārdu aliasiem ieliekam katru atsevišķo vārdu
		for (String alias : ed.aliases) {
			nameBag.add(alias.split("\\s+"));
		}

		// paņemam no DB visus summarizētos freimus par šo entīti
		List<FrameData> fds = knb.getSummaryFrameDataById(entityId);

		for (FrameData fd : fds) {
			// Ņemam vērā manuāli blesotos freimus - tie ir autoritāte par to,
			// ka saite attiecas tieši uz šo konkrēto 'vārdabrāli'
			// TODO maz blessoto, sourceId nesaskaņas

			// if (!fd.blessed) continue;
			// if (!fd.sourceId.equals("LETA CV dati"))
			if (!fd.sourceId.startsWith("LETA CV"))
				continue;

			// System.err.println(fd);

			String frameType = KNBUtils.getFrameName(fd.frameType);

			if (frameType.equals("Being_employed")) {
				String amatsId = fd.elements.get("Position");
				if (amatsId != null) {
					EntityData amats = knb.getEntityData(amatsId, false);
					if (amats != null)
						nameBag.add(amats.name.split("\\s+"));
				}
			}
			if (frameType.equals("People_by_vocation")) {
				String amatsId = fd.elements.get("Vocation");
				if (amatsId != null) {
					EntityData amats = knb.getEntityData(amatsId, false);
					if (amats != null)
						nameBag.add(amats.name.split("\\s+"));
				}
			}
			// Unstructured - piemēram, abstraktā info no CV
			if (frameType.equals("Unstructured")) {
				String aprakstsId = fd.elements.get("Property");
				if (aprakstsId != null) {
					EntityData apraksts = knb.getEntityData(aprakstsId, false);
					if (aprakstsId != null) {
						nameBag.add(apraksts.name.split("\\s+"));
						// Pieņemam, ka brīvā teksta apraksti labi korelē ar
						// kontekstu reālajos rakstos
						contextBag.add(apraksts.name.split("\\s+"));
					}
				}
			}

			for (String relatedId : fd.elements.values()) {

				/*
				 * TODO Šis aizkomentētais būtu production variants - mazāk
				 * korekti nekā tiešās entīšu kategorijas, bet vajadzētu (nav
				 * pārbaudīts) būt būtiski ātrāk jo nav lieku DB request
				 * defaultroletype = getDefaultRole(frame['FrameType'],
				 * element['Key']) if (defaultroletype == 'person') or
				 * (defaultroletype == 'organization') or (defaultroletype ==
				 * 'location'): mentionbag.add(entityID)
				 */

				EntityData red = knb.getEntityData(relatedId, false);
				if (red == null)
					continue;
				// 1=location 2=organization 3=person
				if (red.category <= 3 && !relatedId.equals(entityId)) {
					mentionBag.add(red.name);
				}
			}
		}
		// Analogs freima tipa #26 (Unstructured) datiem, tikai atsevišķa tabula
		// pēc 2014 struktūras izmaiņām
		for (String fact : knb.getEntityTextFacts(entityId)) {
			// Pieņemam, ka brīvā teksta apraksti labi korelē ar kontekstu
			// reālajos rakstos
			contextBag.add(fact.split("\\s+"));
		}

		CDCBags cdcBags = new CDCBags();
		cdcBags.nameBag = nameBag;
		cdcBags.mentionBag = mentionBag;
		cdcBags.contextBag = contextBag;
		// System.err.printf("\n--- Vācam whitelst datus entītijai: %s %s\n",
		// entityId, ed);
		// System.err.printf("nameBag: %s\n", nameBag);
		// System.err.printf("mentionBag: %s\n", mentionBag);
		// System.err.printf("contextBag: %s\n", contextBag);
		//
		// System.err.println(fds);
		return cdcBags;
	}

	public static void main(String[] args) throws IOException {
		Config.logInit();
		Text text = PipeClient.getInstance().read("resource/sample/test_taube.txt");
		// Text text =
		// Annotation.makeText(Pipe.getInstance().read("resource/sample/test_taube.txt"));
		CorefPipe.getInstance().process(text);
		System.err.println(text);
		NEL.getInstance().link(text);
		KNB.getInstance().close();
		log.warning("hello");
	}
}
