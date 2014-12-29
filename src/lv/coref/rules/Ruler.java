package lv.coref.rules;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.*;

public class Ruler {

	public void resolve(Text t) {
		List<Rule> rules = new ArrayList<>();
		rules.add(new HeadMatch());
		rules.add(new Appositive());

		
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
							boolean correct = m.getMention(false) != null
									&& a.getMention(false) != null
									&& m.getMention(false).getMentionChain() == a
											.getMention(false)
											.getMentionChain();
							System.err.println((correct ? "+" : "-")
									+ r.getName() + " MERGE\n\t" + m
									+ m.toParamString() + "\n\t" + a
									+ a.toParamString());
						}
					}
				}
			}
		}
	}

}
