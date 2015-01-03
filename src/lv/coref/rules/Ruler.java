package lv.coref.rules;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.*;

public class Ruler {

	public static final boolean VERBOSE = true;

	public void resolve(Text t) {
		List<Rule> rules = new ArrayList<>();
		rules.add(new ExactMatch());
		// rules.add(new HeadMatch());
//		 rules.add(new Appositive());

		 
		 
		 
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
		sb.append("\n\t").append(m).append(m.toParamString());
		sb.append("\n\t").append(a).append(a.toParamString());
		if (!correct) {
			sb.append("\n\t").append(m.getSentence());
			sb.append("\n\t").append(m.getSentence().getPairedSentence());
			sb.append("\n\t").append(a.getSentence());
			sb.append("\n\t").append(a.getSentence().getPairedSentence());
		}
		System.err.println(sb.toString());
	}

}
