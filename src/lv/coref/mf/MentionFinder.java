package lv.coref.mf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.data.MentionCategory;
import lv.coref.data.NamedEntity;
import lv.coref.data.Node;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;
import lv.coref.io.Pipe;
import lv.coref.lv.Dictionaries;
import lv.coref.lv.Constants.PosTag;
import lv.coref.lv.Constants.Type;
import lv.coref.rules.Ruler;
import lv.coref.score.SummaryScorer;
import lv.coref.util.FileUtils;
import lv.coref.util.StringUtils;

public class MentionFinder {

	public static final boolean VERBOSE = false;

	private int nextID = 1;

	public String getnextID() {
		return Integer.toString(nextID++);
	}

	public void findMentions(Text text) {
		for (Paragraph p : text) {
			for (Sentence s : p) {
				findMentionInSentence(s);
			}
		}
	}

	public void findMentionInSentence(Sentence sentence) {
		// addNounMentions(sentence);
		// addNounPhraseMentions(sentence);
		addNamedEntityMentions(sentence);
		addNounPhraseMentions2(sentence);
		addCoordinations(sentence);
		//addCoordinationsFlat(sentence);
		addPronounMentions(sentence);
		
		MentionCleaner.cleanSentenceMentions(sentence);
		updateMentionHeads(sentence);
		updateMentionBoundaries(sentence);
	}

	private void updateMentionHeads(Sentence sentence) {
		for (Mention m : sentence.getMentions())
			if (m.getHeads().isEmpty())
				m.addHead(m.getLastToken());
	}

	private void updateMentionBoundaries(Sentence sentence) {
		int l = sentence.size();
		for (Mention m : sentence.getMentions()) {

		}
	}

	private void addNamedEntityMentions(Sentence sent) {
		for (NamedEntity n : sent.getNamedEntities()) {
			List<Token> tokens = n.getTokens();
			List<Token> heads = new ArrayList<>();
			heads.add(tokens.get(tokens.size() - 1));
			Mention m = new Mention(tokens, heads, getnextID());
			sent.addMention(m);

			m.setCategory(n.getLabel());

			if (!m.getCategory().equals(MentionCategory.Category.unknown)
					&& !m.getCategory().equals(
							MentionCategory.Category.profession)
					&& !m.getCategory().equals(MentionCategory.Category.time)
					&& !m.getCategory().equals(MentionCategory.Category.sum)) {
				m.setType(Type.NE);
			} else {
				m.setType(Type.NP);
				if (VERBOSE)
					System.err.println("NER not named entity " + m);
			}
		}
	}

	private void addNounMentions(Sentence sent) {
		for (Token t : sent) {
			if (t.getTag().startsWith("n")) {
				Mention m = new Mention(t);
				m.setID(getnextID());
				sent.addMention(m);
			}
		}
	}

	private void addCoordinations(Sentence sent) {
		Node n = sent.getRootNode();
		addCoordinations(sent, n);
	}
	
	private void addCoordinationsFlat(Sentence sent) {
		int start = -1;
		int end = -1;
		boolean coord = false;
		for (int i = 0; i < sent.size(); i++) {
			Token t = sent.get(i);
			if (t.getStartMentions().size() > 0) {
				//TODO
			}
		}
	}

	private void addCoordinations(Sentence sent, Node n) {
		for (Node child : n.getChildren()) {
			if (n.getLabel().endsWith("crdParts:crdPart")
					&& n.getHeads().get(0).getPosTag() == PosTag.N) {
				List<Token> tokens = n.getTokens();
				List<Token> heads = new ArrayList<>();
				for (Token t : tokens) {
					if (t.getDependency().endsWith("crdPart")) {
						heads.add(t);
					}
				}
				if (heads.size() > 1) {
					Mention m = new Mention(tokens, heads, getnextID());
					sent.addMention(m);
					m.setType(Type.CONJ);
					// if (VERBOSE)
					System.err.println("MENTION COORDINATION: " + m);
				}
			} else {
				addCoordinations(sent, child);
			}
		}

	}

	private void addPronounMentions(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.P) {
				Mention m = new Mention(n.getTokens(), n.getHeads(),
						getnextID());
				sent.addMention(m);
				String text = n.getHeads().get(0).getLemma();
				m.setCategory(Dictionaries.getCategory(text));
				m.setType(Type.PRON);
			}
		}
	}

	private void addNounPhraseMentions(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.N) {
				Mention m = new Mention(n.getTokens(), n.getHeads(),
						getnextID());
				sent.addMention(m);
			}
		}
	}

	private void addNounPhraseMentions2(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.N) {
				List<Token> tokens = sent.subList(n.getStart(), n.getHeads()
						.get(n.getHeads().size() - 1).getPosition() + 1);

				// simple filter out incorrect borders due conjunctions,
				// punctuation
				int start = 0, end = tokens.size();
				Set<String> fillerLemmas = new HashSet<String>(Arrays.asList(
						"un", ",", "."));
				while (start < tokens.size()) {
					Token t = tokens.get(start);
					if (fillerLemmas.contains(t.getLemma())) {
						start++;
					} else
						break;
				}
				while (end > 0) {
					Token t = tokens.get(end - 1);
					if (fillerLemmas.contains(t.getLemma())) {
						end--;
					} else
						break;
				}
				tokens = tokens.subList(start, end);

				Mention m = new Mention(tokens, n.getHeads(), getnextID());
				m.setType(Type.NP);
				sent.addMention(m);
			}
		}
	}

	public static void compare() {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text text;
		// t = rw.getText("news_63.conll");
		// t = rw.getText("sankcijas.conll");
		text = rw.getText("data/corpus/conll/interview_16.conll");
		Text goldText = rw
				.getText("data/corpus/corefconll/interview_16.corefconll");
		text.setPairedText(goldText);
		goldText.setPairedText(text);

		MentionFinder mf = new MentionFinder();
		mf.findMentions(text);

		// System.out.println(text);
		for (Sentence s : text.getSentences()) {
			System.out.println(s);
			// System.out.println("\t@" + s.getMentions());
			System.out.println(s.getPairedSentence());

			for (Mention m : s.getPairedSentence().getMentions()) {
				if (m.getMention(true) == null) {
					if (m.getMention(false) == null) {
						System.out.println("\t-- " + m);
					} else {
						System.out.println("\t-+ " + m);
					}
				} else {
					if (m.getMention(false) == null) {
						System.out.println("\t+- " + m);
					} else {
						System.out.println("\t++ " + m);
					}
				}

			}
		}
		SummaryScorer scorer = new SummaryScorer();
		scorer.add(text);
		System.err.println(scorer);
	}

	public static String stringTest(String... strings) {
		String s = stringTests(strings);
		System.out.println(s);
		return s;
	}

	public static String stringTests(String... strings) {
		String stringText = StringUtils.join(strings, "\n");
		StringBuilder sb = new StringBuilder();
		Text t = new Pipe().getText(stringText);
		MentionFinder mf = new MentionFinder();
		mf.findMentions(t);

		for (Sentence s : t.getSentences()) {
			sb.append(s).append("\n");
			for (Mention m : s.getOrderedMentions()) {
				sb.append(" - " + m + "\t\t" + m.toParamString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static void fileTests(BufferedWriter bw, List<String> files) throws IOException {
		
		for (String file : files) {
			bw.write("\n\n\n==== " +  file + " ===== \n");
			
			String fileText = FileUtils.readFile(file, StandardCharsets.UTF_8);
			//System.err.println(stringText);
			
			String[] parText = fileText.split("\n");
			for (String stringText : parText) {
				stringText = stringText.trim();
				if (stringText.length() == 0 ) continue;
				Text t = new Pipe().getText(stringText);
				MentionFinder mf = new MentionFinder();
				mf.findMentions(t);
				
				for (Sentence s : t.getSentences()) {
					bw.write(s.toString()); bw.write("\n");
					for (Mention m : s.getOrderedMentions()) {
						bw.write(" - " + m + "\t\t" + m.toParamString());
						bw.write("\n");
					}
				}
				bw.write("\n\n");
			}
		}
		bw.close();
	}

	public static void main(String[] args) throws IOException {
		
		//fileTests(new BufferedWriter(new FileWriter("mentionFinder.out")), FileUtils.getFiles("data/mentionTest", -1, -1, ""));
		
		stringTest("Jānis Kalniņš devās mājup.", "Šodien J.K. devās mājup.",
				"J. Kalniņš devās mājup.",
				"Profesors Jānis Kalniņš devās mājup.");

		stringTest("Latvija, Rīga un Liepāja iestājās par.",
			"Jānis un Pēteris devās mājup.",
			"Uzņēmuma vadītājs un valdes priekšēdētājs Jānis Krūmiņš izteica sašutumu.");

		stringTest("SIA \"Cirvis\". ");
	}

}
