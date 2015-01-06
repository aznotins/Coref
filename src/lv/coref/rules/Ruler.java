package lv.coref.rules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.Pipe;
import lv.coref.mf.MentionFinder;
import lv.coref.util.FileUtils;
import lv.coref.util.Pair;
import lv.coref.util.StringUtils;
import lv.coref.visual.Viewer;

public class Ruler {

	public static final boolean VERBOSE = true;

	public void resolve(Text t) {
		List<Rule> rules = new ArrayList<>();
//		rules.add(new AllInOne());
//		 rules.add(new HeadMatch());
		
		
		rules.add(new ExactMatch());
		 rules.add(new Appositive());
		 rules.add(new Predicate());

		rules.add(new Acronym());
		 rules.add(new StrictHeadMatch());

		 rules.add(new SubClause());
		 rules.add(new Pronoun());


		// initialize mention chains
		for (Paragraph p : t) {
			for (Sentence s : p) {
				for (Mention m : s.getMentions()) {
					MentionChain mc = new MentionChain(m);
					m.setMentionChain(mc);
				}
			}
		}

		for (Rule r : rules) {
			List<Pair<Mention,Mention>> merge = new ArrayList<>();
			for (Paragraph p : t) {
				for (Sentence s : p) {
					for (Mention m : s.getMentions()) {
						Mention a = r.getFirst(m);
						if (a != null) {
							merge.add(Pair.create(a, m));
//							MentionChain mc = m.getMentionChain();
//							a.getMentionChain().add(mc);							
//							t.dropMentionChain(mc);
							if (VERBOSE) getDescription(r, m, a);
						}
					}
				}
			}
			
			for (Pair<Mention, Mention> pair : merge) {
				Mention m = pair.second;
				Mention a = pair.first;
				MentionChain mc = m.getMentionChain();
				a.getMentionChain().add(mc);							
				t.dropMentionChain(mc);
			}
			
		}
		//System.out.println(t);
		t.removeCommonSingletons();
	}
	
	public static void getDescription(Rule r, Mention m, Mention a) {
		boolean correct = m.getMention(false) != null
				&& a.getMention(false) != null
				&& m.getMention(false).getMentionChain() == a
						.getMention(false)
						.getMentionChain();
		
		StringBuilder sb = new StringBuilder();
		sb.append(correct? "+" : "-");
		sb.append(r.getName());
		sb.append(" MERGE ");
		sb.append("\n  - ").append(m).append(m.toParamString());
		sb.append("\n  - ").append(a).append(a.toParamString());
		if (!correct) {
			sb.append("\n\t").append(m.getSentence());
			if (m.getSentence().getPairedSentence() != null) sb.append("\n\t").append(m.getSentence().getPairedSentence());
			if (a.getSentence() != m.getSentence()) sb.append("\n\t").append(a.getSentence());
			if (a.getSentence() != m.getSentence() && a.getSentence().getPairedSentence() != null) sb.append("\n\t").append(a.getSentence().getPairedSentence());
		}
		System.err.println(sb.toString());
	}
	
	
	public static String stringTest(String... strings) {
		String s = stringTests(strings);
		System.out.println(s);
		return s;
	}
	
	public static void fileTest(String file) {
		String fileText = null;
		try {
			fileText = FileUtils.readFile(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		Text t = new Pipe().getText(fileText);
		new MentionFinder().findMentions(t);
		//new Ruler().resolve(t);
		for (Sentence s : t.getSentences()) {
			sb.append(s).append("\n");
//			for (Mention m : s.getOrderedMentions()) {
//				sb.append(" - " + m + "\t\t" + m.toParamString());
//				sb.append("\n");
		}
		System.out.println(sb.toString());
		SwingUtilities.invokeLater(new Viewer(t));
	}

	public static String stringTests(String... strings) {
		String stringText = StringUtils.join(strings, "\n");
		StringBuilder sb = new StringBuilder();
		Text t = new Pipe().getText(stringText);
		new MentionFinder().findMentions(t);
		new Ruler().resolve(t);
		for (Sentence s : t.getSentences()) {
			sb.append(s).append("\n");
			for (Mention m : s.getOrderedMentions()) {
				sb.append(" - " + m + "\t\t" + m.toParamString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	public static void tests() {
		
		System.out.println("ACRONYMS");
		
		
		stringTest("Eiropas Savienība (ES) uzsāka.");
		stringTest("Šodien skolotājs Jānis Kalniņš.");

		stringTest("Izglītības un zinātnes ministra amata kandidātes Mārītes Seiles biogrāfija - aģentūras LETA apkopotie dati\n"
				+ "Aģentūra LETA publicē Minitru prezidenta amata kandidātes Laimbodas Straujumas (V) jaunveidojamās valdības izglītības un zinātnes ministra amata kandidātes Mārītes Seiles biogrāfiju.\n"
				+ "Seile dzimusi 1996. gada 26. februārī Preiļos.\n"
				+ "Viņai ir dēls.");
		
		
//		System.out.println("APPOSITIVES");
//		stringTest("Šodien skolotājs Jānis Kalniņš mācīja ausgtāko matemātiku.");
//		stringTest("Šodien skolotājs Jānis Kalniņš.");
//		stringTest("Uzņēmuma vadītājs un priekšsēdētājs Pēteris Rudzītis.");
//		stringTest("Ministrs Andris Bērziņš. Ministrs Pēteris Kalniņš");
//		
//		System.out.println("PREDICATES");
//		stringTest("No šodienas Jānis Bērziņš ir valdes priekšsēdētājs, kura atbildība ietver.");
//		
//		System.out.println("PRONOUN");
//		stringTest("Pēteris devās mājup. Viņš bija izsalcis.");
//		stringTest("Pēteris devās uz savu māju.");
//		stringTest("Andris Kalniņš bija galvenais ministra pretinieks. Esošais ministrs uzsāka pret viņu vērstu kampaņu.");
//		
//		stringTest("Andris bija noguris. Pēteris devās pie viņa ciemos.");
//		
//		System.out.println("STRICTHEADMATCH");
//		stringTest("Ministrs Andris Bērziņš. Ministrs Pēteris Kalniņš");
//		stringTest("Jaunais Rīgas teātris uzsāka. Latvijas Nacionālais teātris uzsāka.");
	}
	
	public static void main(String[] args) {
		tests();
		//fileTest("data/test/putins_korumpants.txt");
	}

}
