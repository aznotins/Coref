package lv.coref.rules;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.*;
import lv.coref.io.Pipe;
import lv.coref.mf.MentionFinder;
import lv.coref.util.StringUtils;

public class Ruler {

	public static final boolean VERBOSE = true;

	public void resolve(Text t) {
		List<Rule> rules = new ArrayList<>();
		rules.add(new ExactMatch());
//		 rules.add(new HeadMatch());
		 rules.add(new Appositive());
		 rules.add(new Predicate());
		 rules.add(new Pronoun());
		 rules.add(new SubClause());


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
			for (Paragraph p : t) {
				for (Sentence s : p) {
					for (Mention m : s.getMentions()) {
						Mention a = r.getFirst(m);
						if (a != null) {
							MentionChain mc = m.getMentionChain();
							a.getMentionChain().add(mc);
							t.removeMentionChain(mc);
							if (VERBOSE) getDescription(r, m, a);
						}
					}
				}
			}
		}
		//System.out.println(t);
//		t.removeSingletons();
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
		System.out.println("APPOSITIVES");
		stringTest("Šodien skolotājs Jānis Kalniņš mācīja ausgtāko matemātiku.");
		stringTest("Šodien skolotājs Jānis Kalniņš.");
		stringTest("Uzņēmuma vadītājs un priekšsēdētājs Pēteris Rudzītis.");
		stringTest("Ministrs Andris Bērziņš. Ministrs Pēteris Kalniņš");
		
		System.out.println("PREDICATES");
		stringTest("No šodienas Jānis Bērziņš ir valdes priekšsēdētājs, kura atbildība ietver.");
		
		System.out.println("PRONOUN");
		stringTest("Pēteris devās mājup. Viņš bija izsalcis.");
		stringTest("Pēteris devās uz savu māju.");
		stringTest("Andris Kalniņš bija galvenais ministra pretinieks. Esošais ministrs uzsāka pret viņu vērstu kampaņu.");
		
		stringTest("Andris bija noguris. Pēteris devās pie viņa ciemos.");
	}
	
	public static void main(String[] args) {
		tests();
	}

}
