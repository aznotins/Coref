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
package lv.label;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.JsonReaderWriter;
import lv.coref.lv.AnalyzerUtils;
import lv.coref.lv.Constants.Type;
import lv.coref.semantic.Entity;
import lv.label.Labels.LabelAliases;
import lv.label.Labels.LabelDependency;
import lv.label.Labels.LabelDocumentDate;
import lv.label.Labels.LabelDocumentId;
import lv.label.Labels.LabelEntities;
import lv.label.Labels.LabelEntityIsTitle;
import lv.label.Labels.LabelId;
import lv.label.Labels.LabelIdGlobal;
import lv.label.Labels.LabelIdxEnd;
import lv.label.Labels.LabelIdxStart;
import lv.label.Labels.LabelIndex;
import lv.label.Labels.LabelInflections;
import lv.label.Labels.LabelLemma;
import lv.label.Labels.LabelList;
import lv.label.Labels.LabelMentions;
import lv.label.Labels.LabelMorphoFeatures;
import lv.label.Labels.LabelNer;
import lv.label.Labels.LabelParagraphs;
import lv.label.Labels.LabelParent;
import lv.label.Labels.LabelPosTag;
import lv.label.Labels.LabelPosTagSimple;
import lv.label.Labels.LabelSDP;
import lv.label.Labels.LabelSDPLabel;
import lv.label.Labels.LabelSDPTarget;
import lv.label.Labels.LabelSentences;
import lv.label.Labels.LabelText;
import lv.label.Labels.LabelTokens;
import lv.label.Labels.LabelType;
import lv.util.SimpleTypeSafeMap;
import lv.util.Triple;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Annotation extends SimpleTypeSafeMap {

	private final static Logger log = Logger.getLogger(JsonReaderWriter.class.getName());

	private static final long serialVersionUID = 1L;

	public Annotation() {

	}

	public Annotation(String text) {
		set(LabelText.class, text);
	}

	public void setText(String text) {
		set(LabelText.class, text);
	}

	public String getText() {
		return get(LabelText.class);
	}

	public void setLemma(String text) {
		set(LabelLemma.class, text);
	}

	public String getLemma() {
		return get(LabelLemma.class);
	}

	public void setNer(String text) {
		set(LabelNer.class, text);
	}

	public String getNer() {
		return get(LabelNer.class);
	}

	public static List<Annotation> flatten(Annotation a, String BOUNDARY) {
		List<Annotation> r = new ArrayList<>();
		main: if (a.has(LabelParagraphs.class)) {
			for (Annotation p : a.get(LabelParagraphs.class)) {
				if (!p.has(LabelSentences.class))
					break main;
				for (Annotation s : p.get(LabelSentences.class)) {
					if (!s.has(LabelTokens.class))
						break main;
					for (Annotation t : s.get(LabelTokens.class)) {
						r.add(t);
					}
					r.add(new Annotation(BOUNDARY));
				}
				r.add(new Annotation(BOUNDARY));
			}
		} else if (a.has(LabelSentences.class)) {
			for (Annotation s : a.get(LabelSentences.class)) {
				if (!s.has(LabelTokens.class))
					break main;
				for (Annotation t : s.get(LabelTokens.class)) {
					r.add(t);
				}
				r.add(new Annotation(BOUNDARY));
			}
		} else {
			if (!a.has(LabelTokens.class))
				break main;
			for (Annotation t : a.get(LabelTokens.class)) {
				r.add(t);
			}
		}
		return r;
	}

	public static String getConllString(Annotation a) {
		StringBuilder sb = new StringBuilder();
		if (a.has(LabelParagraphs.class)) {
			for (Annotation p : a.get(LabelParagraphs.class)) {
				if (!p.has(LabelSentences.class))
					continue;
				for (Annotation s : p.get(LabelSentences.class)) {
					if (!s.has(LabelTokens.class))
						continue;
					for (Annotation t : s.get(LabelTokens.class)) {
						sb.append(t.get(LabelIndex.class));
						sb.append("\t").append(t.getText());
						sb.append("\t").append(t.getLemma());
						sb.append("\t").append(t.get(LabelPosTagSimple.class));
						sb.append("\t").append(t.get(LabelPosTag.class));
						sb.append("\t").append(t.get(LabelMorphoFeatures.class));
						sb.append("\t").append(t.has(LabelParent.class) ? t.get(LabelParent.class) : "_");
						sb.append("\t").append(t.has(LabelDependency.class) ? t.get(LabelDependency.class) : "_");
						sb.append("\t").append(t.has(LabelNer.class) ? t.get(LabelNer.class) : "O");
						sb.append("\n");
					}
					sb.append("\n");
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public String toStringPretty() {
		StringBuilder s = new StringBuilder();
		s.append("Annotation [\n");
		s.append(toStringPretty(1));
		s.append("]");
		return s.toString();
	}

	private static final String PRETTY_SEPERATOR = "  ";

	private String toStringPretty(int level) {
		StringBuilder s = new StringBuilder();
		for (Map.Entry<Class<?>, Object> x : entrySet()) {
			String key = x.getKey().getSimpleName();
			Object oVal = x.getValue();			
			String type = oVal != null ? oVal.getClass().getSimpleName() : null;
			String value = oVal != null ? oVal.toString() : null;

			for (int i = 0; i < level; i++)
				s.append(PRETTY_SEPERATOR);
			s.append(key).append(": ");
			
			if (oVal == null) {
				s.append("null").append("\n");
				continue;
			}

			if ((Collection.class).isAssignableFrom(x.getValue().getClass())) {
				try {
					@SuppressWarnings("unchecked")
					Collection<Annotation> ca = (Collection<Annotation>) oVal;
					boolean first = true;
					for (Annotation a : ca) {
						if (first) {
							s.append("<").append(type).append(">");
							s.append("\n");
							first = false;
						} else {
							for (int i = 0; i <= level; i++)
								s.append(PRETTY_SEPERATOR);
							s.append("-");
							s.append("\n");
						}
						s.append(a.toStringPretty(level + 1));
					}
				} catch (ClassCastException e) {
					value = value.replaceAll("\n", "_NEWLINE_");
					s.append(value).append("\n");
				}
			} else {
				value = value.replaceAll("\n", "_NEWLINE_");
				s.append(value).append("\n");
			}
		}
		return s.toString();
	}

	public static Text makeText(Annotation a) {
		Text text = new Text();
		if (a.has(LabelParagraphs.class)) {
			for (Annotation p : a.get(LabelParagraphs.class)) {
				Paragraph par = new Paragraph();
				if (!p.has(LabelSentences.class))
					continue;
				for (Annotation s : p.get(LabelSentences.class)) {
					if (!s.has(LabelTokens.class))
						continue;
					Sentence sent = new Sentence();
					for (Annotation t : s.get(LabelTokens.class)) {
						Token tok = new Token(t.get(LabelText.class), t.get(LabelLemma.class), t.get(LabelPosTag.class));
						tok.setPos(t.get(LabelPosTagSimple.class));
						tok.setMorphoFeatures(t.get(LabelMorphoFeatures.class));
						tok.setDependency(t.get(LabelDependency.class));
						tok.setParent(t.get(LabelParent.class));
						sent.add(tok);
					}
					sent.initializeNodeTree();
					sent.initializeNamedEntities(getNerSpans(s, "O"));
					par.add(sent);
				}
				text.add(par);
			}
		}
		return text;
	}

	public static Annotation makeAnnotationFromText(Annotation doc, Text text) {
		if (!doc.has(LabelParagraphs.class)) {
			// initialize annotation from text
			doc.setText(text.getTextString());
			List<Annotation> pLabels = new ArrayList<>(text.size());
			for (Paragraph paragraph : text) {
				if (paragraph.size() == 0) continue;
				Annotation pLabel = new Annotation();
				pLabel.setText(paragraph.getTextString());
				pLabels.add(pLabel);
				List<Annotation> sLabels = new ArrayList<>(paragraph.size());
				for (Sentence sentence : paragraph) {
					if (sentence.size() == 0) continue;
					Annotation sLabel = new Annotation();
					sLabel.setText(sentence.getTextString());
					sLabels.add(sLabel);
					List<Annotation> tLabels = new ArrayList<>(sentence.size());
					for (Token t : sentence) {
						Annotation tLabel = new Annotation();
						tLabel.set(LabelIndex.class, t.getPosition() + 1);
						tLabel.setText(t.getWord());
						tLabel.setLemma(t.getLemma());
						tLabel.set(LabelPosTag.class, t.getTag());
						tLabel.set(LabelPosTagSimple.class, t.getPos());
						tLabel.set(LabelMorphoFeatures.class, t.getMorphoFeatures());
						tLabel.set(LabelParent.class, t.getParent());
						tLabel.set(LabelDependency.class, t.getDependency());
						tLabel.set(LabelNer.class, t.getNamedEntity() != null ? t.getNamedEntity().getLabel() : "O");
						tLabels.add(tLabel);						
					}
					sLabel.set(LabelTokens.class, tLabels);
				}
				pLabel.set(LabelSentences.class, sLabels);
			}
			doc.set(LabelParagraphs.class, pLabels);
		}
		List<Annotation> entities = new ArrayList<>(text.getMentionChains().size());

		List<Annotation> pars = doc.get(LabelParagraphs.class);
		for (int iPar = 0; iPar < text.size(); iPar++) {
			Paragraph p = text.get(iPar);
			List<Annotation> setences = pars.get(iPar).get(LabelSentences.class);
			for (int iSent = 0; iSent < p.size(); iSent++) {
				Sentence s = p.get(iSent);
				List<Annotation> tokens = setences.get(iSent).get(LabelTokens.class);

				for (int iTok = 0; iTok < s.size(); iTok++) {
					Token t = s.get(iTok);
					Annotation token = tokens.get(iTok);

					Collection<Mention> headMentions = t.getMentions();
					if (headMentions.size() > 0) {
						List<Annotation> mentions = new ArrayList<>(headMentions.size());
						for (Mention m : t.getHeadMentions()) {
							Annotation ma = new Annotation();
							ma.set(LabelId.class, m.getID());
							ma.set(LabelIdxStart.class, m.getFirstToken().getPosition());
							ma.set(LabelIdxEnd.class, m.getLastToken().getPosition());
							if (!m.getCategory().isUnkown())
								ma.set(LabelType.class, m.getCategory().toString());
							mentions.add(ma);
						}
						token.set(LabelMentions.class, mentions);
					}
				}
			}
		}

		for (MentionChain mc : text.getMentionChains()) {
			Annotation mce = new Annotation();
			mce.set(LabelId.class, mc.getID());
			if (!mc.getCategory().isUnkown())
				mce.set(LabelType.class, mc.getCategory().toString());
			Entity e = mc.getEntity();
			String title = null;
			if (e != null) {
				title = e.getTitle();
				if (e.getId() != null)
					mce.set(LabelIdGlobal.class, e.getId());
			} else {
				title = AnalyzerUtils.normalize(mc.getRepresentative().getString(), mc.getCategory().toString());
			}
			if (title != null) {
				if (mc.getRepresentative().getType().equals(Type.NE))
					mce.set(LabelEntityIsTitle.class, true);
				mce.set(LabelInflections.class, AnalyzerUtils.inflect(title, mc.getCategory().toString()));
			}

			List<String> aliases = new ArrayList<>();
			for (Mention m : mc) {
				if (m.getType().equals(Type.PRON))
					continue; // Vietniekvārdus aliasos neliekam
				String alias = AnalyzerUtils.normalize(m.getString(), mc.getCategory().toString());
				aliases.add(alias);
			}
			mce.set(LabelAliases.class, aliases);

			entities.add(mce);
		}
		doc.set(LabelEntities.class, entities);
		return doc;
	}

	public void printJson(PrintStream out) {
		JSONObject json = toJson();
		out.println(json.toJSONString());
		out.flush();
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		Annotation doc = this;
		JSONObject jsonDocument = new JSONObject();
		try {
			if (doc.has(LabelDocumentId.class))
				jsonDocument.put("document", doc.get(LabelDocumentId.class));
			if (doc.has(LabelDocumentDate.class))
				jsonDocument.put("date", doc.get(LabelDocumentDate.class));
			JSONArray jsonSentences = new JSONArray();
			jsonDocument.put("sentences", jsonSentences);
			if (doc.has(LabelParagraphs.class)) {
				for (Annotation p : doc.get(LabelParagraphs.class)) {
					if (!p.has(LabelSentences.class))
						continue;
					for (Annotation s : p.get(LabelSentences.class)) {
						if (!s.has(LabelTokens.class))
							continue;
						JSONObject jsonSentence = new JSONObject();
						JSONArray jsonTokens = new JSONArray();
						for (Annotation t : s.get(LabelTokens.class)) {
							JSONObject jsonToken = new JSONObject();
							jsonToken.put("index", t.get(LabelIndex.class));
							jsonToken.put("form", t.getText());
							jsonToken.put("lemma", t.getLemma());
							jsonToken.put("pos", t.get(LabelPosTag.class));
							jsonToken.put("tag", t.get(LabelPosTagSimple.class));
							jsonToken.put("features", t.get(LabelMorphoFeatures.class));
							jsonToken.put("parentIndex", t.get(LabelParent.class));
							jsonToken.put("dependencyLabel", t.get(LabelDependency.class));
							String ner = t.get(LabelNer.class);
							if (ner != null)
								jsonToken.put("namedEntityType", ner);
							List<Annotation> mentions = t.get(LabelMentions.class);
							if (mentions != null && mentions.size() > 0) {
								JSONArray jsonMentions = new JSONArray();
								for (Annotation m : mentions) {
									JSONObject jsonMention = new JSONObject();
									jsonMention.put("end", m.get(LabelIdxEnd.class) + 1);
									jsonMention.put("start", m.get(LabelIdxStart.class) + 1);
									jsonMention.put("id", m.get(LabelId.class));
									if (m.has(LabelType.class))
										jsonMention.put("type", m.get(LabelType.class));
									jsonMentions.add(jsonMention);
								}
								jsonToken.put("mentions", jsonMentions);
							}

							JSONArray sdps = new JSONArray();
							for (Annotation sdpLabel : t.get(LabelSDP.class)) {
								JSONObject sdp = new JSONObject();
								sdp.put("label", sdpLabel.get(LabelSDPLabel.class));
								sdp.put("target", sdpLabel.get(LabelSDPTarget.class));
								sdps.add(sdp);
							}
							jsonToken.put("sdp", sdps);

							jsonTokens.add(jsonToken);
						}
						jsonSentence.put("tokens", jsonTokens);
						jsonSentence.put("text", s.getText());
						jsonSentences.add(jsonSentence);
					}
				}
			}
			JSONObject jsonNEs = new JSONObject();
			jsonDocument.put("namedEntities", jsonNEs);
			if (doc.has(LabelEntities.class)) {
				for (Annotation e : doc.get(LabelEntities.class)) {
					JSONObject jsonNE = new JSONObject();
					String id = e.get(LabelId.class);
					jsonNE.put("id", id);
					jsonNE.put("representative", e.get(LabelText.class));
					JSONArray jsonAliases = new JSONArray();
					jsonAliases.addAll(e.get(LabelAliases.class));
					jsonNE.put("aliases", jsonAliases);
					if (e.has(LabelType.class))
						jsonNE.put("type", e.get(LabelType.class));
					if (e.has(LabelEntityIsTitle.class))
						jsonNE.put("isTitle", e.get(LabelEntityIsTitle.class));
					if (e.has(LabelIdGlobal.class)) {
						jsonNE.put("globalId", e.get(LabelIdGlobal.class));
					}
					JSONObject oInflections = new JSONObject();
					if (e.has(LabelInflections.class)) {
						Map<String, String> inflections = e.get(LabelInflections.class);
						for (String i_case : inflections.keySet()) {
							oInflections.put(i_case, inflections.get(i_case));
						}
					}
					jsonNE.put("inflections", oInflections);
					jsonNEs.put(id, jsonNE);
				}
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "ERROR while creating json from annotation", e);
			jsonDocument = new JSONObject();
		}
		return jsonDocument;
	}

	/**
	 * Enclosed using simple categories ( o x x o y y )
	 */
	public static List<Triple<Integer, Integer, String>> getNerSpans(Annotation sentence, String defaultMarker) {
		List<Triple<Integer, Integer, String>> spans = new ArrayList<Triple<Integer, Integer, String>>();
		if (!sentence.has(LabelTokens.class))
			return spans;
		String prev = defaultMarker;
		int prevStart = 0;
		int wordPos = 0;
		for (Annotation t : sentence.get(LabelTokens.class)) {
			String val = t.get(LabelNer.class);
			if (val == null)
				val = defaultMarker;
			if (!prev.equals(val)) {
				if (!defaultMarker.equals(prev)) {
					spans.add(Triple.makeTriple(prevStart, wordPos - 1, prev));
				}
				prev = val;
				prevStart = wordPos;
			}
			wordPos++;
		}
		if (!defaultMarker.equals(prev)) {
			spans.add(Triple.makeTriple(prevStart, wordPos - 1, prev));
		}
		return spans;
	}
	
//	public Annotation getMention(Properties p, String mentionString) {
//		Annotation m = null;
//		int mPar = Integer.parseInt(p.getProperty("par","-1"));
//		int mSent = Integer.parseInt(p.getProperty("sent","-1"));
//		int mTok = Integer.parseInt(p.getProperty("tok","-1"));
//		String mType = p.getProperty("type");
//		return m;
//	}
	
	public Annotation getMention(String mentionString, String type, int par, int sent, int tok) {
		Annotation m = null;
		String[] mTokens = mentionString.split("\\s+");
		if (!this.has(LabelParagraphs.class)) return null;
		int iPar = 0;
		for (Annotation aPar : this.get(LabelParagraphs.class)) {
			if (par >= 0 && par != iPar++) continue;
			if (!aPar.has(LabelSentences.class)) continue;
			
			int iSent = 0;
			for (Annotation aSent : aPar.get(LabelSentences.class)) {
				if (sent >= 0 && sent != iSent++) continue;
				if (!aSent.has(LabelTokens.class)) continue;
				
				int iTok = 0;
				for (Annotation aTok : aSent.get(LabelTokens.class)) {
					if (tok >= 0 && tok != iTok++) continue;					
					if (!aTok.has(LabelMentions.class)) continue;
					
					for (Annotation aMent : aTok.get(LabelMentions.class)) {
						//System.err.println(aMent);
						int start = aMent.get(LabelIdxStart.class);
						int end = aMent.get(LabelIdxEnd.class);
						if (end - start + 1 != mTokens.length) continue;
						boolean match = true;
						for (int i = start; i <= end; i++) {
							String tokStr = aSent.get(LabelTokens.class).get(i).getText();
							if (!tokStr.equals(mTokens[i-start])) {
								match = false;
								break;								
							}
						}
						if (type != null && !aMent.get(LabelType.class, "_NOT_A_TYPE_").equals(type)) continue;
						if (match) return aMent;
					}
				}
			}
		}
		return m;
	}

	public static void main(String[] args) {
		Annotation d = new Annotation();

		d.set(LabelText.class, "dokumenta teksts");

		d.set(LabelList.class, Arrays.asList("1", "2"));

		Annotation t1_1 = new Annotation();
		t1_1.set(LabelText.class, "jauns");
		Annotation t1_2 = new Annotation();
		t1_2.set(LabelText.class, "auto");
		d.set(LabelSentences.class, Arrays.asList(t1_1, t1_2));

		System.err.println(d);
		System.err.println(d.toStringPretty());

		try {
			FileOutputStream fileOut = new FileOutputStream("tmp.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(d);
			out.close();
			fileOut.close();
			System.err.println("Serialized data is saved");
		} catch (IOException i) {
			i.printStackTrace();
		}

		Annotation aa;
		try {
			FileInputStream fileIn = new FileInputStream("tmp.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			aa = (Annotation) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return;
		} catch (ClassNotFoundException c) {
			System.out.println("Employee class not found");
			c.printStackTrace();
			return;
		}

		System.err.println(aa.toStringPretty());
	}

}
