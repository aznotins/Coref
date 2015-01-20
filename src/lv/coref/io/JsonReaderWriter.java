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
package lv.coref.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Type;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;
import lv.coref.util.Pair;
import lv.coref.util.Triple;
//import lv.lumii.expressions.Expression;
import lv.lumii.expressions.Expression;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonReaderWriter extends ReaderWriter {

	private JSONObject json; // original JSON

	public Text read(BufferedReader in) throws Exception {
		readJSON(in);
		Text text = read(json);
		return text;
	}

	public JSONObject readJSON(BufferedReader in) throws IOException {
		// log.fine("Read json stream");
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = in.readLine()) != null;) {
			if (line.trim().length() == 0)
				break;
			builder.append(line).append("\n");
		}
		json = (JSONObject) JSONValue.parse(builder.toString());
//		if (json == null)
//			System.err.println("Empty document");
		return json;
	}

	public Text read(JSONObject json) throws Exception {
		if (json == null)
			return null;
		Text text = new Text();
		try {
			JSONArray sentencesArr = (JSONArray) json.get("sentences");
			if (sentencesArr == null)
				throw new Exception("Sentences key not set");

			Paragraph p = new Paragraph(0);
			text.add(p);
			for (int iSent = 0; iSent < sentencesArr.size(); iSent++) {
				JSONObject jsonSentence = (JSONObject) sentencesArr.get(iSent);
				JSONArray tokens = (JSONArray) jsonSentence.get("tokens");
				Sentence sentence = new Sentence(iSent);
				p.add(sentence);
				List<String> sentenceNer = new ArrayList<>(tokens.size());
				List<Pair<Triple<Integer, Integer, String>, String>> sentenceMentions = new ArrayList<>();
				for (int iTok = 0; iTok < tokens.size(); iTok++) {
					JSONObject jsonToken = (JSONObject) tokens.get(iTok);
					String word = jsonToken.containsKey("form") ? jsonToken
							.get("form").toString() : "_";
					String lemma = jsonToken.containsKey("lemma") ? jsonToken
							.get("lemma").toString() : "_";
					String tag = jsonToken.containsKey("tag") ? jsonToken.get(
							"tag").toString() : "_";
					String pos = jsonToken.containsKey("pos") ? jsonToken.get(
							"pos").toString() : tag.substring(0, 1);
					String morphoFeatures = jsonToken.containsKey("features") ? jsonToken
							.get("features").toString() : "_";
					// Integer position = jsonToken.containsKey("index") ?
					// Integer
					// .parseInt(jsonToken.get("index").toString()) : -1;
					Integer parentPosition = jsonToken
							.containsKey("parentIndex") ? Integer
							.parseInt(jsonToken.get("parentIndex").toString())
							: -1;
					String ner = "O";
					if (jsonToken.containsKey("namedEntityType")) {
						ner = (String) jsonToken.get("namedEntityType");
					}
					String dep = "_";
					if (jsonToken.containsKey("dependencyLabel")) {
						dep = (String) jsonToken.get("dependencyLabel");
					}

					if (jsonToken.containsKey("mentions")) {
						JSONArray jsonMentions = (JSONArray) jsonToken
								.get("mentions");
						for (int iMent = 0; iMent < jsonMentions.size(); iMent++) {
							JSONObject jsonMention = (JSONObject) jsonMentions
									.get(iMent);
							Integer start = jsonMention.containsKey("start") ? Integer
									.parseInt(jsonMention.get("start")
											.toString()) - 1 : iTok;
							Integer end = jsonMention.containsKey("start") ? Integer
									.parseInt(jsonMention.get("end").toString()) - 1
									: iTok;
							String id = jsonMention.containsKey("id") ? jsonMention
									.get("id").toString() : null;
							String type = jsonMention.containsKey("type") ? jsonMention
									.get("type").toString() : null;

							sentenceMentions
									.add(new Pair<Triple<Integer, Integer, String>, String>(
											new Triple<Integer, Integer, String>(
													start, end, id), type));
						}
					}
					Token token = new Token(word, lemma, tag);
					token.setPosition(iTok);
					token.setMorphoFeatures(morphoFeatures);
					token.setParent(parentPosition);
					token.setDependency(dep);
					token.setPos(pos);

					sentence.add(token);
					sentenceNer.add(ner);
				}

				sentence.initializeNodeTree();
				sentence.initializeNamedEntities(getClassSpans(sentenceNer, "O"));


			}

			// initializeBaseCoreference();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public List<Triple<Integer, Integer, String>> getClassSpans(
			List<String> tokens, String defaultMarker) {
		List<Triple<Integer, Integer, String>> spans = new ArrayList<Triple<Integer, Integer, String>>();
		String prev = defaultMarker;
		int prevStart = 0;
		for (int wordPos = 0; wordPos < tokens.size(); wordPos++) {
			String val = tokens.get(wordPos);
			if (!prev.equals(val)) {
				if (!defaultMarker.equals(prev)) {
					spans.add(Triple.makeTriple(prevStart, wordPos - 1, prev));
				}
				prev = val;
				prevStart = wordPos;
			}
		}
		if (!defaultMarker.equals(prev)) {
			spans.add(Triple.makeTriple(prevStart, tokens.size() - 1, prev));
		}
		return spans;
	}

	public void write(PrintStream out, Text text) throws IOException {
		initialize(text);
		if (json == null)
			System.err.println("Writing json == null");
		else
			out.println(json.toString());
		out.flush();
	}

	protected void initialize(Text text) {
		if (json == null) {
			jsonInitializeBase(text);
		}
		jsonUpdateSentences(text);
		jsonUpdateNE(text);
		jsonUpdateFrames(text);
	}

	/**
	 * Initialize base json (not coreferences)
	 */
	@SuppressWarnings("unchecked")
	private void jsonInitializeBase(Text text) {
		JSONArray jsonSentences = new JSONArray();
		for (Sentence sentence : text.getSentences()) {
			JSONObject jsonSentence = new JSONObject();
			JSONArray jsonTokens = new JSONArray();
			for (Token token : sentence) {
				JSONObject jsonToken = new JSONObject();
				jsonToken.put("index", token.getPosition() + 1);
				jsonToken.put("form", token.getWord());
				jsonToken.put("lemma", token.getLemma());
				jsonToken.put("pos", token.getPos());
				jsonToken.put("tag", token.getTag());
				jsonToken.put("features", token.getMorphoFeatures());
				jsonToken.put("parentIndex", token.getParent());
				if (token.getDependency() != null)
					jsonToken.put("dependencyLabel", token.getDependency());
				if (token.getNamedEntity() != null)
					jsonToken.put("namedEntityType", token.getNamedEntity()
							.getLabel());
				jsonTokens.add(jsonToken);
			}
			jsonSentence.put("tokens", jsonTokens);
			jsonSentence.put("text", sentence.getTextString());
			jsonSentence.put("frames", new JSONArray());
			jsonSentence.put("detachedNamedEntityMarkers", new JSONArray());
			jsonSentences.add(jsonSentence);
		}
		JSONObject jsonDocument = new JSONObject();
		jsonDocument.put("document", new JSONObject());
		jsonDocument.put("sentences", jsonSentences);
		json = jsonDocument;
	}

	/**
	 * Traverses and updates sentence token coreference fields (idType and neID)
	 */
	@SuppressWarnings("unchecked")
	public void jsonUpdateSentences(Text text) {
		if (json == null)
			return;
		try {
			JSONArray jsonSentences = (JSONArray) json.get("sentences");
			if (jsonSentences == null)
				throw new Exception("Sentences key not set");

			List<Sentence> sentences = text.getSentences();
			for (int iSent = 0; iSent < jsonSentences.size(); iSent++) {
				JSONObject jsonSentence = (JSONObject) jsonSentences.get(iSent);
				JSONArray jsonTokens = (JSONArray) jsonSentence.get("tokens");
				Sentence sentence = sentences.get(iSent);
				for (int iTok = 0; iTok < jsonTokens.size(); iTok++) {
					Token t = sentence.get(iTok);
					JSONObject jsonToken = (JSONObject) jsonTokens.get(iTok);

					if (jsonToken.containsKey("mentions"))
						jsonToken.remove("mentions");

					// TODO add warnings about overwriting, deleting base
					// mentions
					// if (n.namedEntityID > 0 && n.mention == null) {
					// log.warning("Deleted base mention " + n);
					// }

					Collection<Mention> headMentions = t.getHeadMentions();
					if (headMentions.size() > 0) {
						JSONArray jsonMentions = new JSONArray();
						for (Mention m : t.getHeadMentions()) {
							// System.err.println("Head mention " + t + " " +
							// m);
							JSONObject jsonMention = new JSONObject();
							jsonMention.put("end", m.getLastToken()
									.getPosition() + 1);
							jsonMention.put("start", m.getFirstToken()
									.getPosition() + 1);
							jsonMention.put("id", m.getMentionChain().getID());
							if (!m.getCategory().equals(Category.unknown))
								jsonMention.put("type", m.getCategory()
										.toString());
							jsonMentions.add(jsonMention);
						}
						jsonToken.put("mentions", jsonMentions);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR while updating tokens");
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void jsonUpdateNE(Text text) {
		if (json == null)
			return;
		try {
			if (json.containsKey("namedEntities"))
				json.remove("namedEntities"); // remove old namedEntity data
			JSONObject jsonNEs = new JSONObject();
			json.put("namedEntities", jsonNEs);

			for (MentionChain mc : text.getMentionChains()) {
				// System.err.println(mc);
				JSONObject jsonNE = new JSONObject();
				jsonNE.put("id", mc.getID());
				Set<String> aliases = new HashSet<String>();

				for (Mention m : mc) {
					if (m.getType().equals(Type.PRON))
						continue; // Vietniekvārdus aliasos neliekam
					Expression e;
					try {
						e = new Expression(m.getString(), m.getCategory()
								.toString(), false);
						String normalised = e.inflect("Nominatīvs");
						if (normalised != null)
							aliases.add(normalised);
						else
							aliases.add(m.getString());
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					// System.err.printf("head:%s ner:%s\n", m.headString,
					// m.nerString);
				}
				// System.err.println(aliases.toString());
				JSONArray jsonAliases = new JSONArray();
				jsonAliases.addAll(aliases);
				jsonNE.put("aliases", jsonAliases);
				jsonNE.put("type", mc.getCategory().toString());
				// if (category == null)
				// System.err.println("Empty cluster category " +
				// cluster.representative);
				if (mc.getRepresentative() != null
						&& mc.getRepresentative().getType().equals(Type.NE))
					jsonNE.put("isTitle", 1);
				JSONObject oInflections = new JSONObject();
				String representativeString = mc.getRepresentative()
						.getString();
				try {
					Expression e = new Expression(representativeString, mc
							.getCategory().toString(), false);
					Map<String, String> inflections = e.getInflections();
					// System.err.printf("Saucam getInflections vārdam '%s' ar kategoriju '%s'\n",
					// cluster.representative.nerString,
					// cluster.firstMention.category);
					for (String i_case : inflections.keySet()) {
						oInflections.put(i_case, inflections.get(i_case));
					}
					representativeString = e.inflect("Nominatīvs");
					// System.err.printf("Locījām frāzi '%s' ar kategoriju '%s', dabūjām '%s'\n",
					// cluster.representative.nerString,
					// cluster.firstMention.category, representative);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (representativeString == null)
					representativeString = mc.getRepresentative().getString();
				jsonNE.put("inflections", oInflections);
				jsonNE.put("representative", representativeString);
				jsonNEs.put(mc.getID(), jsonNE);
			}
		} catch (Exception e) {
			System.err.println("ERROR while updating json named entities");
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Traverses frames and updates the ids of the nameEntities, based on
	 * original namedEntityID annotation for token Could rewrite this based on
	 * frame tokenIndex
	 */
	@SuppressWarnings("unchecked")
	public void jsonUpdateFrames(Text text) {
		// create baseNeId => newNeId mapping
		// Map<Integer, Integer> nes = new HashMap<>();
		// for (Node n : tree) {
		// if (n.namedEntityID > 0) {
		// if (n.mention != null) {
		// nes.put(n.namedEntityID, n.mention.corefClusterID);
		// } else {
		// // log.warning(String.format("Deleted base mention %s", n));
		// }
		// }
		// }
		// try {
		// if (json == null)
		// throw new Exception("Empty document");
		// JSONArray sentencesArr = (JSONArray) json.get("sentences");
		// if (sentencesArr == null)
		// throw new Exception("Sentences key not set");
		//
		// for (int s_id = 0; s_id < sentencesArr.size(); s_id++) {
		// JSONObject sentence = (JSONObject) sentencesArr.get(s_id);
		// JSONArray framesArr = (JSONArray) sentence.get("frames");
		// // int sent_start = sentences.get(s_id); // sentence start node
		// // id
		//
		// for (int f_id = 0; f_id < framesArr.size(); f_id++) {
		// JSONObject frame = (JSONObject) framesArr.get(f_id);
		//
		// if (frame.containsKey("namedEntityID")) {
		// int nameEntityID = Integer.parseInt(frame.get(
		// "namedEntityID").toString());
		// if (nes.containsKey(nameEntityID)) {
		// frame.put("namedEntityID", nes.get(nameEntityID));
		// } else {
		// log.warning(String
		// .format("Frame without existing namedEntity: sentID=%d, frameID=%d [%s]",
		// s_id, f_id, sentence.get("text")));
		// }
		// }
		//
		// JSONArray frameElements = (JSONArray) frame.get("elements");
		// for (int el_id = 0; el_id < frameElements.size(); el_id++) {
		// JSONObject element = (JSONObject) frameElements
		// .get(el_id);
		// if (element.containsKey("namedEntityID")) {
		// int nameEntityID = Integer.parseInt(element.get(
		// "namedEntityID").toString());
		// if (nes.containsKey(nameEntityID)) {
		// element.put("namedEntityID",
		// nes.get(nameEntityID));
		// } else {
		// log.warning(String
		// .format("Frame element without existing namedEntity: sentID=%d, frameID=%d, elementID=%d [%s]",
		// s_id, f_id, el_id,
		// sentence.get("text")));
		// }
		// }
		// }
		// }
		// }
		// } catch (Exception e) {
		// log.severe("ERROR parsing input json data");
		// e.printStackTrace(System.err);
		// }
	}

	public static void main(String[] args) throws Exception {
		// ConllReaderWriter rw = new ConllReaderWriter();
		// Text t = rw.getText("data/test.corefconll");

		ReaderWriter jrw = new JsonReaderWriter();
		Text t = jrw.read("resource/tests/dzejnieks.json");

		new MentionFinder().findMentions(t);
		new Ruler().resolve(t);

		System.out.println(t);
		for (MentionChain mc : t.getMentionChains()) {
			if (mc.size() > 1)
				System.out.println(mc);
		}

		new JsonReaderWriter().write(System.out, t);
		//new JsonReaderWriter().write("test3.json", t);

		// System.out.println(t);
		// for (MentionChain mc : t.getMentionChains()) {
		// if (mc.size() > 1)
		// System.out.println(mc);
		// }
		//
		// Text t2 = rw.getText("data/test.corefconll");
		// rw.write("tmp/twofiles.out", Arrays.asList(t, t2));

	}

}
