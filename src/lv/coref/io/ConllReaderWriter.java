package lv.coref.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import lv.coref.data.Mention;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;
import lv.coref.util.Triple;

public class ConllReaderWriter extends ReaderWriter {

	public static enum TYPE {
		CONLL, LETA
	};

	private TYPE type = TYPE.CONLL;

	private static final int CONLL_POSITION = 0;
	private static final int CONLL_WORD = 1;
	private static final int CONLL_LEMMA = 2;
	private static final int CONLL_SPOS = 3;
	private static final int CONLL_POS = 4;
	private static final int CONLL_MORPHO = 5;
	private static final int CONLL_PARENT = 6;
	private static final int CONLL_DEP = 7;
	private static final int CONLL_NER = 8;
	private static final int CONLL_COREF_CAT = 9;
	private static final int CONLL_COREF = 10;
	private static final int LETA_BOUNDARIES = 11;

	private static final int CONLL_MAX = 10;
	private static final int LETA_MAX = 11;

	private static final String CONLL_DEFAULT = "_";

	public ConllReaderWriter() {
	}

	public ConllReaderWriter(TYPE type) {
		this.type = type;
	}

	/**
	 * Paragraph => Sentence => Tokens
	 */
	private List<List<List<List<String>>>> conll;

	public Text read(BufferedReader in) throws IOException {
		Text text = null;
		readCONLL(in);
		text = read(conll, getFileID());
		return text;
	}

	public Text read(List<List<List<List<String>>>> conll, String id) {
		this.conll = conll;
		setFileID(id);
		Text text = new Text(id);
		for (int iPar = 0; iPar < conll.size(); iPar++) {
			List<List<List<String>>> par = conll.get(iPar);
			Paragraph paragraph = new Paragraph(iPar);
			paragraph.setText(text);
			for (int iSent = 0; iSent < par.size(); iSent++) {
				List<List<String>> sent = par.get(iSent);
				Sentence sentence = new Sentence(iSent);
				boolean corefColumn = false;
				for (int iTok = 0; iTok < sent.size(); iTok++) {
					List<String> tok = sent.get(iTok);
					// int position = Integer.parseInt(tok.get(CONLL_POSITION));
					int parentPosition = Integer
							.parseInt(tok.get(CONLL_PARENT));
					String word = tok.get(CONLL_WORD);
					String lemma = tok.get(CONLL_LEMMA);
					String tag = tok.get(CONLL_POS);
					String pos = tok.get(CONLL_SPOS);
					String morphoFeatures = tok.get(CONLL_MORPHO);
					// String ner = tok.get(CONLL_NER);
					String dep = tok.get(CONLL_DEP);
					pos = (pos.length() > 0) ? pos : tag.substring(0, 1);

					Token token = new Token(word, lemma, tag);
					token.setPosition(iTok);
					token.setMorphoFeatures(morphoFeatures);
					token.setParent(parentPosition);
					token.setDependency(dep);
					token.setPos(pos);
					sentence.add(token);
					if (tok.size() > CONLL_COREF)
						corefColumn = true;
				}
				paragraph.add(sentence);
				sentence.initializeNodeTree();
				sentence.initializeNamedEntities(getClassSpans(sent, CONLL_NER,
						"O"));
				// sentence.initializeNamedEntities(getSpans(sent, CONLL_NER,
				// "-", false));

				if (corefColumn) {
					if (type.equals(TYPE.LETA)) {
						List<Triple<Integer, Integer, String>> spans = new ArrayList<>();
						List<Integer> heads = new ArrayList<>();
						List<String> categories = new ArrayList<>();

						for (int iTok = 0; iTok < sent.size(); iTok++) {
							List<String> tok = sent.get(iTok);
							String idString = tok.get(CONLL_COREF);
							if (!idString.equals("_"))
								continue;
							String[] ids = idString.split("\\|");
							String[] cats = tok.get(CONLL_COREF_CAT).split(
									"\\|");
							String[] bounds = tok.get(LETA_BOUNDARIES).split(
									"\\|");
							for (int mi = 0; mi < ids.length; mi++) {
								Integer start = Integer.parseInt(bounds[mi]
										.split(",")[0]);
								Integer end = Integer.parseInt(bounds[mi]
										.split(",")[1]);
								spans.add(new Triple<Integer, Integer, String>(
										start, end, ids[mi]));
								heads.add(iTok);
								categories.add(cats[mi]);
							}
						}
						sentence.initializeCoreferences(spans, heads,
								categories);
					} else {
						assert type.equals(TYPE.CONLL);
						sentence.initializeCoreferences(
								getSpans(sent, CONLL_COREF, CONLL_DEFAULT, true),
								null, null);
						sentence.initializeMentionAttributes(
								getSpans(sent, CONLL_COREF_CAT, CONLL_DEFAULT,
										true), "category");
					}
				}

			}
			text.add(paragraph);
		}
		return text;
	}

	/**
	 * Enclosed using bracket notation (x|(y) )
	 * 
	 * @param tokens
	 * @param fieldIndex
	 * @param defaultMarker
	 * @param checkEndLabel
	 * @return
	 */
	public List<Triple<Integer, Integer, String>> getSpans(
			List<List<String>> tokens, int fieldIndex, String defaultMarker,
			boolean checkEndLabel) {

		if (fieldIndex < 0)
			fieldIndex = tokens.get(0).size() - fieldIndex;
		List<Triple<Integer, Integer, String>> spans = new ArrayList<Triple<Integer, Integer, String>>();
		Stack<Triple<Integer, Integer, String>> openSpans = new Stack<Triple<Integer, Integer, String>>();
		for (int wordPos = 0; wordPos < tokens.size(); wordPos++) {
			String val = tokens.get(wordPos).get(fieldIndex);
			if (!defaultMarker.equals(val)) {
				int openParenIndex = -1;
				int lastDelimiterIndex = -1;
				for (int j = 0; j < val.length(); j++) {
					char c = val.charAt(j);
					boolean isDelimiter = false;
					if (c == '(' || c == ')' || c == '|') {
						if (openParenIndex >= 0) {
							String s = val.substring(openParenIndex + 1, j);
							// if (removeStar) {
							// s = starPattern.matcher(s).replaceAll("");
							// }
							openSpans
									.push(new Triple<Integer, Integer, String>(
											wordPos, -1, s));
							openParenIndex = -1;
						}
						isDelimiter = true;
					}
					if (c == '(') {
						openParenIndex = j;
					} else if (c == ')') {
						Triple<Integer, Integer, String> t = openSpans.pop();
						if (checkEndLabel) {
							// NOTE: end parents may cross (usually because
							// mention either start or end on the same token
							// and it is just an artifact of the ordering
							String s = val.substring(lastDelimiterIndex + 1, j);
							if (!s.equals(t.third())) {
								Stack<Triple<Integer, Integer, String>> saved = new Stack<Triple<Integer, Integer, String>>();
								while (!s.equals(t.third())) {
									// find correct match
									saved.push(t);
									if (openSpans.isEmpty()) {
										throw new RuntimeException(
												"Cannot find matching labelled span for ["
														+ s + "] : " + tokens);
									}
									t = openSpans.pop();
								}
								while (!saved.isEmpty()) {
									openSpans.push(saved.pop());
								}
								assert (s.equals(t.third()));
							}
						}
						t.setSecond(wordPos);
						spans.add(t);
					}
					if (isDelimiter) {
						lastDelimiterIndex = j;
					}
				}
				if (openParenIndex >= 0) {
					String s = val.substring(openParenIndex + 1, val.length());
					// if (removeStar) {
					// s = starPattern.matcher(s).replaceAll("");
					// }
					openSpans.push(new Triple<Integer, Integer, String>(
							wordPos, -1, s));
				}
			}
		}
		if (openSpans.size() != 0) {
			throw new RuntimeException(
					"Error extracting labelled spans for column " + fieldIndex
							+ ": " + tokens);
		}

		return spans;
	}

	/**
	 * Enclosed using simple categories ( o x x o y y )
	 * 
	 * @param tokens
	 * @param fieldIndex
	 * @param defaultMarker
	 * @return
	 */
	public List<Triple<Integer, Integer, String>> getClassSpans(
			List<List<String>> tokens, int fieldIndex, String defaultMarker) {

		if (fieldIndex < 0)
			fieldIndex = tokens.get(0).size() - fieldIndex;
		List<Triple<Integer, Integer, String>> spans = new ArrayList<Triple<Integer, Integer, String>>();
		String prev = defaultMarker;
		int prevStart = 0;
		for (int wordPos = 0; wordPos < tokens.size(); wordPos++) {
			String val = tokens.get(wordPos).get(fieldIndex);
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

	public List<List<List<List<String>>>> readCONLL(BufferedReader in)
			throws IOException {
		// System.err.println("START READ");
		conll = new ArrayList<>();
		List<List<List<String>>> par = new ArrayList<>();
		List<List<String>> sent = new ArrayList<>();

		int emptyLines = 0;
		String line;
		while ((line = in.readLine()) != null) {
			line = line.trim();
			if (line.length() > 0) {
				if (line.startsWith("#end document")) {
					break;
				}
				if (line.startsWith("#begin document (")) {
					setFileID(line.substring(17, line.length() - 1));
					continue;
				}

				String[] bits = line.split("\t");
				int position = Integer.parseInt(bits[CONLL_POSITION]);

				if (position == 1) {
					if (sent.size() > 0) {
						par.add(sent);
						sent = new ArrayList<>();
					}
				}
				List<String> tok = new ArrayList<>(Arrays.asList(bits));
				sent.add(tok);
				emptyLines = 0;
			} else {
				emptyLines++;
				if (emptyLines == 1 && sent.size() > 0) {
					par.add(sent);
					sent = new ArrayList<>();
				}
				if (emptyLines == 2 && par.size() > 0) {
					conll.add(par);
					par = new ArrayList<>();
				}
				if (emptyLines > 2)
					break;
			}
		}
		if (sent.size() > 0)
			par.add(sent);
		if (par.size() > 0)
			conll.add(par);
		// System.err.println(conll.size() + "  " + par.size() + " " +
		// sent.size());
		// System.err.println("FINISHED READ");
		return conll;
	}

	protected void initialize(Text text) {
		conll = new ArrayList<>();
		for (Paragraph p : text) {
			List<List<List<String>>> sList = new ArrayList<>();
			if (p.size() > 0)
				conll.add(sList);
			for (Sentence s : p) {
				List<List<String>> tList = new ArrayList<>();
				if (s.size() > 0)
					sList.add(tList);
				int tCounter = 0;
				for (Token t : s) {
					int max = CONLL_MAX;
					if (type.equals(TYPE.LETA))
						max = LETA_MAX;
					List<String> bits = new ArrayList<>(max);
					tList.add(bits);
					for (int i = 0; i <= max; i++)
						bits.add(CONLL_DEFAULT);

					bits.set(CONLL_POSITION, Integer.toString(++tCounter));
					bits.set(CONLL_WORD, t.getWord());
					bits.set(CONLL_LEMMA, t.getLemma());
					bits.set(CONLL_LEMMA, t.getLemma());
					bits.set(CONLL_SPOS, t.getPos());
					bits.set(CONLL_POS, t.getTag());
					bits.set(CONLL_MORPHO, t.getMorphoFeatures());
					bits.set(CONLL_PARENT, t.getParent().toString());
					bits.set(CONLL_DEP, t.getDependency());
					bits.set(CONLL_NER, t.getNamedEntity() != null ? t
							.getNamedEntity().getLabel() : "O");

					bits.set(CONLL_NER, t.getNamedEntity() != null ? t
							.getNamedEntity().getLabel() : "O");

				}

				// for (NamedEntity ne : s.getNamedEntities()) {
				// for (Token t : ne.getTokens()) {
				// tList.get(t.getPosition()).set(CONLL_NER, ne.getLabel());
				// }
				// }
				// System.err.println(s);
				// System.err.println(tList);
			}
		}
	}

	public void setLETACoreferences(Text t) {
		for (int iPar = 0; iPar < conll.size(); iPar++) {
			List<List<List<String>>> par = conll.get(iPar);
			Paragraph paragraph = t.get(iPar);
			for (int iSen = 0; iSen < par.size(); iSen++) {
				StringBuilder s = new StringBuilder();
				List<List<String>> sen = par.get(iSen);
				Sentence sentence = paragraph.get(iSen);
				for (int iTok = 0; iTok < sen.size(); iTok++) {
					List<String> tok = sen.get(iTok);
					for (int i = tok.size(); i <= LETA_MAX; i++)
						tok.add(CONLL_DEFAULT);
					Token token = sentence.get(iTok);
					if (token.getHeadMentions().size() == 0)
						continue;
					StringBuilder idString = new StringBuilder();
					StringBuilder catString = new StringBuilder();
					StringBuilder boundString = new StringBuilder();
					for (Mention m : token.getHeadMentions()) {
						idString.append(m.getMentionChain().getID())
								.append("|");
						catString.append(m.getCategory()).append("|");
						boundString.append(m.getFirstToken().getPosition() + 1)
								.append(",")
								.append(m.getLastToken().getPosition() + 1)
								.append("|");
					}
					idString.deleteCharAt(idString.length() - 1);
					catString.deleteCharAt(catString.length() - 1);
					boundString.deleteCharAt(boundString.length() - 1);
					tok.set(CONLL_COREF, idString.toString());
					tok.set(CONLL_COREF_CAT, catString.toString());
					tok.set(LETA_BOUNDARIES, boundString.toString());
				}
			}
		}
	}

	public void setCONLLCoreferences(Text t) {
		for (int iPar = 0; iPar < conll.size(); iPar++) {
			List<List<List<String>>> par = conll.get(iPar);
			Paragraph paragraph = t.get(iPar);
			for (int iSen = 0; iSen < par.size(); iSen++) {
				StringBuilder s = new StringBuilder();
				List<List<String>> sen = par.get(iSen);
				Sentence sentence = paragraph.get(iSen);
				for (int iTok = 0; iTok < sen.size(); iTok++) {
					List<String> tok = sen.get(iTok);
					for (int i = tok.size(); i <= CONLL_MAX; i++)
						tok.add(CONLL_DEFAULT);
					Token token = sentence.get(iTok);
					StringBuilder coref = new StringBuilder();
					StringBuilder corefCat = new StringBuilder();

					boolean first = true;
					for (Mention m : token.getOrderedStartMentions()) {
						if (m.getMentionChain() != null) {
							if (!first) {
								coref.append("|");
								corefCat.append("|");
							} else {
								first = false;
							}
							coref.append("(").append(
									m.getMentionChain().getID());
							corefCat.append("(").append(m.getCategory());
						}
					}
					boolean second = true;
					for (Mention m : token.getOrderedEndMentions()) {
						if (m.getMentionChain() != null) {
							if (second) {
								if (first) {
									coref.append(m.getMentionChain().getID());
									corefCat.append(m.getCategory());
								}
								coref.append(")");
								corefCat.append(")");
								second = false;
							} else {
								coref.append("|")
										.append(m.getMentionChain().getID())
										.append(")");
								corefCat.append("|").append(m.getCategory())
										.append(")");
							}
						}
					}
					if (coref.length() == 0) {
						coref.append(CONLL_DEFAULT);
						corefCat.append(CONLL_DEFAULT);
					}
					tok.set(CONLL_COREF, coref.toString());
					tok.set(CONLL_COREF_CAT, corefCat.toString());
				}
			}
		}
	}

	public void write(PrintStream out, Text t) throws IOException {
		if (conll == null)
			initialize(t);
		if (type.equals(TYPE.LETA)) {
			setLETACoreferences(t);
		} else if (type.equals(TYPE.CONLL)) {
			setCONLLCoreferences(t);
			out.println("#begin document (" + t.getId() + "); part 000\n");
		}
		for (List<List<List<String>>> par : conll) {
			for (List<List<String>> sent : par) {
				for (List<String> tok : sent) {
					StringBuilder sb = new StringBuilder();
					for (String bit : tok) {
						sb.append(bit).append("\t");
					}
					sb.deleteCharAt(sb.length() - 1);
					out.println(sb.toString());
				}
				out.println();
			}
			out.println();
		}

		if (type.equals(TYPE.CONLL))
			out.println("#end document");
		out.flush();
	}

	public static void main(String[] args) throws Exception {
		Text t;
		ReaderWriter rw = new ConllReaderWriter(TYPE.CONLL);
		// t = rw.getText("data/test.corefconll");
		t = rw.read("data/test.conll");

		new MentionFinder().findMentions(t);
		new Ruler().resolve(t);
		rw.write(System.out, t);
		System.out.println(t);
		// for (MentionChain mc : t.getMentionChains()) {
		// if (mc.size() > 1)
		// System.out.println(mc);
		// }

		// Text t2 = rw.getText("data/test.corefconll");
		// rw.write("tmp/twofiles.out", Arrays.asList(t, t2));

	}

}
