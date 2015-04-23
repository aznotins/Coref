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
package lv.coref.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lv.coref.data.NamedEntity;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;
import lv.coref.io.ConllReaderWriter.TYPE;
import lv.coref.io.PipeClient;
import lv.coref.tests.TextCorpus.Query.QueryResult;
import lv.util.FileUtils;
import lv.util.StringUtils;

public class TextCorpus {

	public static class Query {
		public static class QueryResult {
			public String text;
			public String lemmaText;
			public String context;
			public String file;

			QueryResult(String text, String lemmaText, String context, String file) {
				this.text = text;
				this.lemmaText = lemmaText;
				this.context = context;
				this.file = file;
			}

			public String toString() {
				return text + StringUtils.repeat(" ", Math.max(0, 20 - text.length() % 20)) + " ["  + context + "]          (" + file + ")";
			}
			
			public String toStringPretty() {
				return text + " [" + context.replaceAll(Pattern.quote(text), " @[" + text.toUpperCase() + "]@ ") + "]          (" + file + ")";
			}
		}

		public static enum BitType {
			WORD, LEMMA, TAG, NER, BEFORE, AFTER, DEP
		};

		private List<List<BitPattern>> pattern = new ArrayList<>();
		
		public static class BitPattern {
			private BitType type;
			private String string;
			private boolean negative = false;

			public BitPattern(BitType type, String string) {
				this.setType(type);
				this.setString(string);
			}
			public boolean isNegative() {
				return negative;
			}
			public void setNegative(boolean negative) {
				this.negative = negative;
			}
			public BitType getType() {
				return type;
			}
			public void setType(BitType type) {
				this.type = type;
			}
			public String getString() {
				return string;
			}
			public void setString(String string) {
				this.string = string;
			}
			public String toString() {
				return String.format("(%s%s %s)", negative ? "!" : "", type, string);
			}
		}

		public Query(String queryStrign) {
			String[] wordBits = queryStrign.split("\\s");
			for (String partString : wordBits) {
				List<BitPattern> wordPattern = new ArrayList<>();
				String[] partBits = partString.split("@");
				for (String bit : partBits) {
					boolean negative = false;
					if (bit.startsWith("!")) {
						negative = true;
						bit = bit.substring(1);
					}
					BitPattern bp = null;
					if (bit.startsWith("W-")) {
						bp = new BitPattern(BitType.WORD, bit.substring(2));
					} else if (bit.startsWith("L-")) {
						bp = new BitPattern(BitType.LEMMA, bit.substring(2));
					} else if (bit.startsWith("T-")) {
						bp = new BitPattern(BitType.TAG, bit.substring(2));
					} else if (bit.startsWith("N-")) {
						bp = new BitPattern(BitType.NER, bit.substring(2));
					} else if (bit.startsWith("D-")) {
						bp = new BitPattern(BitType.DEP, bit.substring(2));
					} else if (bit.startsWith(">")) {
						bp = new BitPattern(BitType.BEFORE, "");
					} else if (bit.startsWith("<")) {
						bp = new BitPattern(BitType.AFTER, "");
					}
					if (bp != null) {
						if (negative) bp.setNegative(true);
						wordPattern.add(bp);
					}
				}
				pattern.add(wordPattern);
			}
		}

		public List<QueryResult> match(Sentence s) {
			List<QueryResult> res = new ArrayList<>();

			for (int i = 0; i < s.size(); i++) {
				boolean ok = true;
				StringBuilder text = new StringBuilder();
				StringBuilder lemmaText = new StringBuilder();
				int j = i;
				boolean inside = true;
				for (List<BitPattern> wordPattern : pattern) {
					if (j >= s.size()) {
						ok = false;
						break;
					}
					Token t = s.get(j);
					if (wordPattern.get(0).getType().equals(BitType.AFTER)) {
						text = new StringBuilder();
						lemmaText = new StringBuilder();
						continue;
					} else if (wordPattern.get(0).getType().equals(BitType.BEFORE)) {
						inside = false;
						continue;
					} else {
						for (BitPattern p : wordPattern) {
							if (p.getType().equals(BitType.WORD) && !t.getWord().matches(p.getString()) ^ p.isNegative()) {
								ok = false;
								break;
							} else if (p.getType().equals(BitType.LEMMA) && !t.getLemma().matches(p.getString()) ^ p.isNegative()) {
								ok = false;
								break;
							} else if (p.getType().equals(BitType.TAG) && !t.getTag().matches(p.getString()) ^ p.isNegative()) {
								ok = false;
								break;
							} else if (p.getType().equals(BitType.NER)
									&& !(t.getNamedEntity() != null ? t.getNamedEntity().getLabel() : "O")
											.matches(p.getString()) ^ p.isNegative()) {
								ok = false;
								break;
							} else if (p.getType().equals(BitType.DEP) && !t.getDependency().matches(p.getString()) ^ p.isNegative()) {
								ok = false;
								break;
							}
						}
					}
					if (!ok)
						break;
					if (inside) {
						text.append(t.getWord()).append(" ");
						lemmaText.append(t.getLemma()).append(" ");
					}
					j++;
				}
				if (ok) {
					StringBuilder sText = new StringBuilder();
					for (Token t : s) {
						sText.append(t.getWord());
						if (t.getNamedEntity() != null)
							sText.append("/").append(t.getNamedEntity().getLabel());
						sText.append(" ");
					}
					
					res.add(new QueryResult(text.toString(), lemmaText.toString(), sText.toString(), s.getText()
							.getId()));
				}
			}

			return res;
		}

		public String toString() {
			return pattern.toString();
		}
	}

	List<Text> texts = new ArrayList<>();

	public void create(String baseDir, int limit, int skip, String endsWith, String outputDir) {
		List<String> files = FileUtils.getFiles(baseDir, limit, skip, endsWith);
		create(files, outputDir);
	}

	public void create(List<String> filePaths, String outputDir) {
		for (String filePath : filePaths) {
			try {
				File file = new File(filePath);
				String name = StringUtils.getBaseName(file.getName(), ".txt");
				Text text = PipeClient.getInstance().read(filePath);
				String outFile = outputDir + name + ".conll";
				new ConllReaderWriter(TYPE.CONLL).write(outFile, text);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void load(String baseDir, int limit, int skip, String endsWith) {
		List<String> files = FileUtils.getFiles(baseDir, limit, skip, endsWith);
		for (String file : files) {
			try {
				Text t = new ConllReaderWriter().read(file);
				texts.add(t);
				t.setId(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void search(Query q) {
		System.err.println("=====\nSEARCH" + q);
		List<QueryResult> res = new ArrayList<>();
		for (Text t : texts) {
			// System.err.println(t.getId());
			for (Sentence s : t.getSentences()) {
				res.addAll(q.match(s));
			}
		}
		for (QueryResult r : res) {
			System.err.println(r);
		}
	}

	public List<String> getNerSpans(Text t, String cat) {
		List<String> spans = new ArrayList<String>();
		for (Sentence s : t.getSentences()) {
			for (NamedEntity n : s.getNamedEntities()) {
				// spans.add(n.toString());
				if (n.getTokens().size() < 2)
					continue;
				if (cat == null || n.getLabel().equals(cat))
					System.err.println(n);
			}
		}
		return spans;
	}

	public List<String> getNerSpans(String cat) {
		List<String> spans = new ArrayList<String>();
		for (Text t : texts) {
			spans.addAll(getNerSpans(t, cat));
		}
		return spans;
	}

	public static void testNerProfessions() {
		TextCorpus c;

		// base NER
		// c = new TextCorpus();
		// c.create("data/text_corpus/txt/", -1, -1, ".txt",
		// "D:/work/Coref/data/text_corpus/conll/");

		// only profession whiteList
		// c = new TextCorpus();
		// c.create("data/text_corpus/txt/", -1, -1, ".txt",
		// "D:/work/Coref/data/text_corpus/conll_ner_test/");
		// c.load("data/text_corpus/conll_ner_test/", -1, -1, ".conll");
		// c.getNerSpans("profession");

		// base ner with profession whiteList
		c = new TextCorpus();
		// c.create("data/text_corpus/txt/", -1, -1, ".txt",
		// "D:/work/Coref/data/text_corpus/conll_ner_test2/");
		c.load("data/text_corpus/conll_ner_test2/", -1, -1, ".conll");
		c.getNerSpans("profession");
	}

	public static void main(String[] args) {

		// testNerProfessions();

		TextCorpus c = new TextCorpus();
		// c.create("data/text_corpus/txt/", -1, -1, ".txt",
		// "D:/work/Coref/data/text_corpus/conll/");

		 c.load("data/text_corpus/conll/", -1, -1, ".conll");
		// c.search(new Query("L-\\( W-[^\\d]*@N-^((?!location).)*$ L-\\)"));
		// c.search(new Query("N-profession W-un N-profession"));
		// c.search(new Query("N-person W-un"));
		// c.search(new Query("N-organization W-un"));
		// c.search(new Query("D-.*App.*"));

		// c.search(new Query("L-prese L-sekretāre"));
		// c.search(new Query("L-tiesa L-priekšsēdētājs"));
		// c.search(new Query("W-pienākumu L-izpildītājs"));
		// c.search(new Query("W-biroja L-vadītājs"));

		// c.search(new Query("W-nodaļas L-vadītājs"));
		// c.search(new Query("W-departamenta L-vadītājs"));
		// c.search(new Query("W-biroja L-vadītājs"));

		// c.search(new Query("W-.* L-departaments N-profession"));
		// c.search(new Query("W-.* L-nodaļa N-profession"));
		// c.search(new Query("W-.* L-birojs N-profession"));
		//
		// c.search(new
		// Query("W-.* L-departaments@N-organization N-profession"));
		// c.search(new Query("W-.* L-nodaļa@N-organization N-profession"));
		// c.search(new Query("W-.* L-birojs@N-organization N-profession"));

		// c.search(new Query("N-organization L-departaments"));
		// c.search(new Query("!N-organization L-departaments"));

		// c.search(new Query("N-organization L-birojs N-profession"));
		// c.search(new Query("!N-organization L-birojs N-profession"));

		// c.search(new Query("N-organization L-nodaļa N-profession"));
		// c.search(new Query("!N-organization L-nodaļa N-profession"));

		// c.search(new Query("L-departaments N-profession"));
		// c.search(new Query("L-departaments@N-organization N-profession"));
		//
		// c.search(new Query("N-organization L-departaments@N-organization N-profession"));
		// c.search(new Query("N-organization N-organization L-departaments@N-organization N-profession"));
		// c.search(new Query("N-organization N-organization N-organization L-departaments@N-organization N-profession"));

		// c.search(new Query("W-Mūsu"));
		// c.search(new Query("W-Katr.*"));

		// c.search(new Query("W-.*@T-(x|y).* > W-\\\" N-organization"));
		// c.search(new Query("W-ES > W-\)"));
		// c.search(new Query("W-projekts > W-korupcijas"));

		// c.search(new Query("W-RPP"));
		// c.search(new Query("L-Latvija"));
		// c.search(new Query("N-media"));

		// c.search(new Query("L-\\( W-.* W-.* L-\\)"));
		
		c.search(new Query("W-SIA"));
		// c.search(new Query("L-ierobožotu L-atbildību"));
	}
}
