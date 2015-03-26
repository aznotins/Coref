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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.coref.io.PipeClient;
import lv.coref.lv.AnalyzerUtils;
import lv.coref.lv.Constants.Category;
import lv.coref.semantic.KNB.EntityData;
import lv.coref.semantic.KNB.EntityMentionData;
import lv.coref.semantic.KNB.FrameData;
import lv.util.Pair;

public class NEL {

	private final static Logger log = Logger.getLogger(NEL.class.getName());

	private static NEL nel = null;

	protected boolean REAL_UPLOAD = false;
	protected boolean VERBOSE = false;
	protected boolean SHOW_DISAMBIGUGATION = false;
	protected boolean SHOW_INSERTS = false;
	protected boolean SHOW_ENTITIES = false; // show created entities

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
		if (prop.getProperty(Config.PROP_NEL_VERBOSE, "false").equalsIgnoreCase("true"))
			VERBOSE = true;
		if (prop.getProperty(Config.PROP_NEL_SHOW_DISAMBIGUATION, "false").equalsIgnoreCase("true"))
			SHOW_DISAMBIGUGATION = true;
		if (prop.getProperty(Config.PROP_NEL_SHOW_INSERTS, "false").equalsIgnoreCase("true"))
			SHOW_INSERTS = true;
		if (prop.getProperty(Config.PROP_NEL_SHOW_ENTITIES, "false").equalsIgnoreCase("true"))
			SHOW_ENTITIES = true;
	}	

	public void setRealUpload(boolean realUpload) {
		this.REAL_UPLOAD = realUpload;
	}
	
	public void setShowDisambiguation(boolean showDisambiguation) {
		this.SHOW_DISAMBIGUGATION = showDisambiguation;
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
		if (SHOW_ENTITIES || VERBOSE) {
			for (Entity e : entities) {
				System.err.printf("\nNEL_ENTITY: #%d \"%s\" (%s) %s %s\n", e.getId(), e.getTitle(), e.getCategory(), e.getAliases(), e.getLocations());
				if (VERBOSE)
					for (Mention m : e.mentions) {
//						System.err.printf("\t%s\n", m);
						System.err.printf("\t%s\t\t\t%s\n", m, m.getSentence().getTextString());
					}
			}
		}
		return entities;
	}

	public Set<Integer> getGlobalIdCandidates(Entity e) {
		String title = NELUtils.fixname(e.getTitle());
		Set<Integer> ids = new HashSet<>();
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
		String documentId = text.getId();
		List<Pair<Entity, Set<Integer>>> toDisambiguate = new ArrayList<>(entities.size());
		
		Set<Integer> disambiguationWhitelist = KNB.getInstance().getBlessedEntityMentions(documentId);
		
		for (Entity entity : entities) {
			Set<Integer> ids = getGlobalIdCandidates(entity);
			if (ids.size() == 0) {
				// ieliek DB jaunu entītiju
				EntityData ed = new EntityData();
				
				ed.name = entity.getTitle();
				if (entity.category.equals(Category.person)) {
					ed.aliases = NELUtils.personAliases(ed.name);
				} else if (entity.category.equals(Category.organization)) {
					ed.aliases = NELUtils.orgAliases(ed.name);
					// Organizācijām te var izveidoties pilnāka pamatforma
					ed.name = ed.aliases.get(0);
				} else {
					// Šeit ņemam tikai representative, nevis visus aliasus ko koreferences 
					// atrod. Ja ņemtu visus, tad te būtu interesanti jāfiltrē lai nebūtu 
					// nekorektas apvienošanas kā direktors -> skolas direktors un 
					// gads -> 1983. gads
					ed.aliases.add(ed.name);
				}
				ed.inflections = AnalyzerUtils.inflectJson(ed.name, entity.getCategory().toString()).toJSONString();
				ed.category = KNBUtils.getEntityTypeCode(entity.getCategory().toString());
				if (ed.category == 3) {
					ed.outerIds.add("FP-" + UUID.randomUUID());
				}
				if (ed.category == 2) {
					ed.outerIds.add("JP-" + UUID.randomUUID());
				}
				ed.source = String.format("Upload %s, %s at %s", documentId, entity.titleMention.getComment(), NELUtils.isoDateFormat.format(new Date()));
				
				if (SHOW_INSERTS || VERBOSE) {
					System.err.printf("\nUPLOAD_ENTITY: \"%s\" (%s) %s\n", entity.getTitle(), entity.getCategory(), entity.getAliases());
					if (VERBOSE)
						for (Mention m : entity.mentions) {
							System.err.printf("\t%s\t\t%s\n", m, m.getSentence().getTextString());
						}
				}
				entity.setId(-1);
				if (REAL_UPLOAD) {
					entity.id = KNB.getInstance().putEntity(ed, false);
					EntityMentionData em = new EntityMentionData(entity.id, documentId);
					em.locations = entity.locations;
					KNB.getInstance().putEntityMention(em);
				}
				continue;
			}
			
			// Pārbaudām blessed entītijas šajā dokumentā
			boolean inWhiteList = false;
			for (int candidateId : ids) {
				if (disambiguationWhitelist.contains(candidateId)) {
					// Ja ir blessed norāde, ka tieši šī globālā entītija ir šajā dokumentā
					entity.setId(candidateId);
					log.log(Level.INFO, "Found {0} candidate id in blessed mentions: {1}", 
							new Object[] {entity.getTitle(), candidateId});
					inWhiteList = true;
				}
			}
			if (inWhiteList)
				continue; // Šajā gadījumā neskatamies kā disambiguēt
			
			
			// Disabiguējam un augšupielādējam; tikai personas un organizācijas
			if (ids.size() > 1 && (entity.getCategory().equals(Category.person)
					|| entity.getCategory().equals(Category.organization))) {
				toDisambiguate.add(new Pair<Entity, Set<Integer>>(entity, ids));
			} else {
				// klasifikatoriem tāpat daudzmaz vienalga, vai pie kaut kā
				// piesaista vai veido jaunu
				entity.setId(ids.iterator().next());
				
				// REAL_UPLOAD
				if (REAL_UPLOAD && (entity.getCategory().equals(Category.person)
						|| entity.getCategory().equals(Category.organization))) {
					EntityMentionData em = new EntityMentionData(entity.getId(), documentId);
					em.locations = entity.locations; // TODO paļaujamies, ka dokumenti DB ir vai tiks pievienoti
					KNB.getInstance().putEntityMention(em);
				}
			}
		}

		// TODO - šobrīd mentionbag visiem ir vienāds un tādēļ iekļauj arī pašas
		// disambiguējamās entītijas vārdus
		Bag mentionBag = CDCBags.makeMentionBag(entities);

		for (Pair<Entity, Set<Integer>> pair : toDisambiguate) {
			Entity entity = pair.first;
			Set<Integer> candidates = pair.second;
			Integer id = dissambiguate(entity, candidates, text, mentionBag);
			if (id == null) {
				log.log(Level.WARNING, "Failed to resolve entity {0} {1} from candidates {2}",
						new Object[] { entity.getTitle(), entity.getAliases(), candidates });
			} else {
				if (REAL_UPLOAD) {
					EntityMentionData em = new EntityMentionData(id, documentId);
					em.locations = ""; // TODO paļaujamies, ka dokumenti DB ir vai tiks pievienoti
					em.chosen = true;
					em.cos_similarity = entity.cosineSimilarity;
					// TODO vajadzētu ielikt arī informāciju par citiem atrastajiem kandidātiem
					KNB.getInstance().putEntityMention(em);
				}
			}
		}
	}

	/**
	 * Disambiguē dokumenta entītijas
	 * Rediģē Entity datus: uzstāda globālo id un cosineSimilarity
	 */
	public Integer dissambiguate(Entity entity, Collection<Integer> candidates, Text text, Bag mentionBag) {

		CDCBags eBags = new CDCBags();
		eBags.mentionBag = mentionBag;
		eBags.nameBag = CDCBags.makeNameBag(entity);
		eBags.contextBag = CDCBags.makeContextBag(entity);

		if (SHOW_DISAMBIGUGATION || VERBOSE) {
			System.err.printf("\nDISAMBIGUATE: %s (%s) %s\n", entity.getTitle(), entity.getCategory(), entity.getAliases());
			System.err.printf("\tnameBag : %s\n", eBags.nameBag);
			System.err.printf("\tmentionBag : %s\n", eBags.mentionBag);
			System.err.printf("\tcontextBag : %s\n", eBags.contextBag);
		}

		double maxSim = -99999;
		Integer maxId = null;
		Map<Integer, Double> cosineSim = new HashMap<>();
		for (int candidateId : candidates) {
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
//			bags = NEL.makeGlobalEntityBags(candidateId);

			if (SHOW_DISAMBIGUGATION || VERBOSE) {
				EntityData ed = KNB.getInstance().getEntityData(candidateId, false);
				System.err.printf("\tCandidate: #%s \"%s\" %s\n", candidateId, ed.name, ed.aliases);
				System.err.printf("\t\t%.6f name match %s\n", CDCBags.cosineSimilarity(eBags.nameBag, bags.nameBag),
						bags.nameBag);
				System.err.printf("\t\t%.6f mention match %s\n",
						CDCBags.cosineSimilarity(eBags.mentionBag, bags.mentionBag), bags.mentionBag);
				System.err.printf("\t\t%.6f context match %s\n",
						CDCBags.cosineSimilarity(eBags.contextBag, bags.contextBag), bags.contextBag);
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
					entity.getTitle(), candidates, maxId, maxSim });
		}

		if (SHOW_DISAMBIGUGATION || VERBOSE) {
			System.err.printf("\tChosed: %s (%.4f)\n", maxId, maxSim);
		}
		entity.id =maxId;
		entity.cosineSimilarity = maxSim;

		return maxId;
	}
	
	
//	/**
//	 * Entītiju disambiguācijai - konkrētā ID 'a priori' ticamība
//	 */
//	public Double getOuterIdScore(String id) {
//		// TODO - ideālā gadījumā mums būtu dati par to, cik dokumentos kura entīte parādās
//	    // Piemēram, ImantsZiedonis1 ir 3300 dokumentos, ImantsZiedonis2 ir 0 dokumentos.
//	
//		if (id == "F6A8C3B7-AC39-11D4-9D85-00A0C9CFC2DB") return 100.0; // Imants Ziedonis, vienīgais pieminētais
//		if (id == "3AFC2FB8-4879-11D5-AE84-0010B5A3DE2F") return -100.0; //šis neparādās reālos dokumentos
//
//		if (id == "F5C17E17-AC39-11D4-9D85-00A0C9CFC2DB") return 100.0; //Gunārs Upenieks, vienīgais pieminētais
//	    if (id == "8E336987-D834-47F8-A590-D0D473FABADE") return -100.0; //šis neparādās reālos dokumentos
//	
//	    if (id == "F6A8BD85-AC39-11D4-9D85-00A0C9CFC2DB") return 50.0; //'primārais' Gunārs Freimanis (ir citi kam nav LETA-profilu)
//	    if (id == "CD900493-1E9A-11D5-AE08-0010B594D402") return -50.0; //šis gandrīz neparādās reālos dokumentos
//	
//	    if (id == "F6A8B27C-AC39-11D4-9D85-00A0C9CFC2DB") return 50.0; //'primārais' Jānis Freimanis (ir citi kam nav LETA-profilu)
//	    if (id == "F6A8B27F-AC39-11D4-9D85-00A0C9CFC2DB") return -100.0; //šis neparādās reālos dokumentos
//	    if (id == "F6A8B279-AC39-11D4-9D85-00A0C9CFC2DB") return -1000.0; //tukšs profils - gļuks LETA sourcedatos http://www.leta.lv/archive/search/?patern=Freimanis%20J%C4%81nis&item=F6A8B279-AC39-11D4-9D85-00A0C9CFC2DB
//	
//	    if (id == "F6A8BDCF-AC39-11D4-9D85-00A0C9CFC2DB") return 50.0; //biežākais Jānis Gailis (ir arī citi kam nav LETA-profilu)
//	    if (id == "F6A8BDD2-AC39-11D4-9D85-00A0C9CFC2DB") return 30.0; //retāk pieminēts 
//	    if (id == "F6A8BDD5-AC39-11D4-9D85-00A0C9CFC2DB") return 10.0; //pavisam reti pieminēts 
//	
//	    if (id == null) return -100.0;// ja nu ir izvēle starp tādu entītiju kam ir profils un tādu, kam nav - liekam pie 'zināmās'
//	    if (id.startsWith("FP-") || id.startsWith("JP-")) return -10.0; // Kamēr nav entītiju blesošana, šādi prioritizējam LETA iepriekšējos profilus (VIP) no automātiski veidotajiem
//	    
//	    return 0.0;
//	}

	/**
	 * Savāc visu vajadzīgo lai uztaisītu globālajai entītijai CDC datus
	 * 
	 * @param entityId
	 */
	public static CDCBags makeGlobalEntityBags(int entityId) {
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
				Integer amatsId = fd.elements.get("Position");
				if (amatsId != null) {
					EntityData amats = knb.getEntityData(amatsId, false);
					if (amats != null)
						nameBag.add(amats.name.split("\\s+"));
				}
			}
			if (frameType.equals("People_by_vocation")) {
				Integer amatsId = fd.elements.get("Vocation");
				if (amatsId != null) {
					EntityData amats = knb.getEntityData(amatsId, false);
					if (amats != null)
						nameBag.add(amats.name.split("\\s+"));
				}
			}
			// Unstructured - piemēram, abstraktā info no CV
			if (frameType.equals("Unstructured")) {
				Integer aprakstsId = fd.elements.get("Property");
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

			for (Integer relatedId : fd.elements.values()) {

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
	
}
