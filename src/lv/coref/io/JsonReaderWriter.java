package lv.coref.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
import lv.coref.util.Triple;
//import lv.lumii.expressions.Expression;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class JsonReaderWriter {

	private JSONObject json; // original JSON

	public Text getText(BufferedReader in) {
		Text text = null;
		try {
			readJSON(in);
			text = getText(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public Text getText(String filename) {
		Text text = null;
		try {
			readJSON(filename);
			text = getText(json);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public JSONObject readJSON(String filename) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filename));
		readJSON(in);
		in.close();
		return json;
	}

	public JSONObject readJSON(BufferedReader in) throws IOException {
		// log.fine("Read json stream");
		StringBuilder builder = new StringBuilder();
		for (String line = null; (line = in.readLine()) != null;) {
			if (line.trim().length() == 0)
				break;
			builder.append(line).append("\n");
		}
		String jsonString = builder.toString();

		if (jsonString.length() == 0)
			throw new IOException("Empty document");
		json = (JSONObject) JSONValue.parse(builder.toString());
		if (json == null)
			throw new IOException("Empty document");
		return json;
	}

	public Text getText(JSONObject json) throws Exception {
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
//					Integer namedEntityID = jsonToken
//							.containsKey("namedEntityID") ? Integer
//							.parseInt(jsonToken.get("namedEntityID").toString())
//							: -1;
//					String idType = jsonToken.containsKey("idType") ? jsonToken
//							.get("idType").toString() : "";

					Token token = new Token(word, lemma, tag);
					token.setPosition(iTok);
					token.setMorphoFeatures(morphoFeatures);
					token.setParent(parentPosition);
					token.setDependency(dep);
					token.setPos(pos);

					// node.namedEntityID = namedEntityID;
					// node.idType = idType;
					sentence.add(token);
					sentenceNer.add(ner);
				}

				sentence.initializeNodeTree();
				sentence.initializeNamedEntities(getClassSpans(sentenceNer, "O"));

				// s.initializeNamedEntities(getClassSpans(sent, CONLL_NER,
				// "O"));
				// if (corefColumn) {
				// sentence.initializeCoreferences(
				// getSpans(sent, CONLL_COREF, CONLL_DEFAULT, true), mf);
				// sentence.initializeMentionAttributes(
				// getSpans(sent, CONLL_COREF_CAT, CONLL_DEFAULT, true),
				// "category");

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

	public void write(PrintStream out, Text text) {
		if (json == null)
			initialize(text);
		jsonUpdateSentences(text);
		jsonUpdateNE(text);
		jsonUpdateFrames(text);
		out.println(json.toString());
		out.flush();
	}

	public void write(String filename, Text t) {
		write(filename, Arrays.asList(t));
	}

	public void write(String filename, List<Text> texts) {
		try {
			PrintStream ps = new PrintStream(new File(filename), "UTF8");
			for (Text t : texts) {
				write(ps, t);
			}
			ps.close();
		} catch (IOException ex) {
			System.err.println("Problem writing output to " + filename);
		}
	}

	/**
	 * Initialize JSON structure (sentences and named entities)
	 */
	@SuppressWarnings("unchecked")
	public void initialize(Text text) {
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
		jsonUpdateNE(text);
	}

	/**
	 * Traverses and updates sentence token coreference fields (idType and neID)
	 */
	@SuppressWarnings("unchecked")
	public void jsonUpdateSentences(Text text) {
		try {
			if (json == null)
				throw new Exception("Empty document");
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

					if (jsonToken.containsKey("idType"))
						jsonToken.remove("idType");
					if (jsonToken.containsKey("namedEntityID"))
						jsonToken.remove("namedEntityID");

					// TODO add warnings about overwriting, deleting base
					// mentions
					// if (n.namedEntityID > 0 && n.mention == null) {
					// log.warning("Deleted base mention " + n);
					// }

					Mention m = t.getHeadMention();
					if (m != null) {
						// System.err.println("Head mention " + t + " " + m);
						jsonToken.put("namedEntityID", m.getMentionChain()
								.getID());
						if (!m.getMentionChain().getCategory()
								.equals(Category.unknown))
							jsonToken.put("idType", m.getMentionChain()
									.getCategory().toString());
					}
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR parsing input json data");
			e.printStackTrace(System.err);
		}
	}

	@SuppressWarnings("unchecked")
	public void jsonUpdateNE(Text text) {
		if (json.containsKey("namedEntities"))
			json.remove("namedEntities"); // remove old namedEntity data
		JSONObject jsonNEs = new JSONObject();
		json.put("namedEntities", jsonNEs);

		for (MentionChain mc : text.getMentionChains()) {
			//System.err.println(mc);
			JSONObject jsonNE = new JSONObject();
			jsonNE.put("id", mc.getID());
			Set<String> aliases = new HashSet<String>();

			// for (Mention m : mc) {
			// if (m.getType().equals(Type.PRON))
			// continue; // Vietniekvārdus aliasos neliekam
			// Expression e;
			// try {
			// e = new Expression(m.getString(), m.getCategory(), false);
			// String normalised = e.inflect("Nominatīvs");
			// if (normalised != null)
			// aliases.add(normalised);
			// else
			// aliases.add(m.getString());
			// } catch (Exception e1) {
			// e1.printStackTrace();
			// }
			// // System.err.printf("head:%s ner:%s\n", m.headString,
			// // m.nerString);
			// }
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
			String representativeString = mc.getRepresentative().getString();
			try {
				// Expression e = new Expression(representativeString,
				// mc.getCategory(), false);
				// Map<String, String> inflections = e.getInflections();
				// //
				// System.err.printf("Saucam getInflections vārdam '%s' ar kategoriju '%s'\n",
				// // cluster.representative.nerString,
				// // cluster.firstMention.category);
				// for (String i_case : inflections.keySet()) {
				// oInflections.put(i_case, inflections.get(i_case));
				// }
				// representativeString = e.inflect("Nominatīvs");
				// //
				// System.err.printf("Locījām frāzi '%s' ar kategoriju '%s', dabūjām '%s'\n",
				// // cluster.representative.nerString,
				// // cluster.firstMention.category, representative);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (representativeString == null)
				representativeString = mc.getRepresentative().getString();
			jsonNE.put("inflections", oInflections);
			jsonNE.put("representative", representativeString);
			jsonNEs.put(mc.getID(), jsonNE);
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

	public static void main(String[] args) throws IOException {
//		 ConllReaderWriter rw = new ConllReaderWriter();
//		 Text t = rw.getText("data/test.corefconll");

		JsonReaderWriter jrw = new JsonReaderWriter();
		Text t = jrw.getText("test2.json");
		
		
		new MentionFinder().findMentions(t);
		new Ruler().resolve(t);
		System.out.println(t);
		new JsonReaderWriter().write(System.out, t);
		new JsonReaderWriter().write("test3.json", t);

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
