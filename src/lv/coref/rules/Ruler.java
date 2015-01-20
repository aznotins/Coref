package lv.coref.rules;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.tests.CorefTest;
import lv.coref.util.Pair;

public class Ruler {
	public static final boolean VERBOSE = false;
	public static final boolean POST_PROCESS = false;
	private List<Rule> rules = new ArrayList<>();

	public Ruler resolve(Text t) {
		List<Rule> rules = new ArrayList<>();
		// rules.add(new AllInOne());
//		rules.add(new HeadMatch());

		rules.add(new ExactMatch());
		rules.add(new Appositive());
		rules.add(new Predicate());
		rules.add(new Acronym());
		rules.add(new StrictHeadMatch());
		rules.add(new SubClause());
		rules.add(new Pronoun());

		for (Rule r : rules) {
			List<Pair<Mention, Mention>> merge = new ArrayList<>();
			for (Paragraph p : t) {
				for (Sentence s : p) {
					for (Mention m : s.getMentions()) {
						Mention a = r.getFirst(m);
						if (a != null) {
							merge.add(Pair.create(a, m));
							// MentionChain mc = m.getMentionChain();
							// a.getMentionChain().add(mc);
							// t.dropMentionChain(mc);
							if (VERBOSE)
								getDescription(r, m, a);
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
		if (POST_PROCESS) postProcess(t);
		return this;
	}
	
	public static Text postProcess(Text text) {
		text.removeCommonSingletons();
		return text;
	}

	public static void solveAppositives(Text t) {
		Rule appositive = new Appositive();
		for (Sentence s : t.getSentences()) {
			for (Mention m : s.getMentions()) {
				Mention a = appositive.getFirst(m);
				if (a == null)
					continue;
				if (VERBOSE)
					getDescription(appositive, m, a);
				MentionChain mc = m.getMentionChain();
				a.getMentionChain().add(mc);
				t.dropMentionChain(mc);
				// a.setLinked(true);
				// m.getLinkedMentions().add(a);
			}
		}
	}

	public List<Rule> getRules() {
		return rules;
	}

	public void setRules(List<Rule> rules) {
		this.rules = rules;
	}

	public void addRule(Rule r) {
		rules.add(r);
	}

	public static void getDescription(Rule r, Mention m, Mention a) {
		boolean correctExactSpans = m.getMention(true) != null && a.getMention(true) != null
				&& m.getMention(true).getMentionChain() == a.getMention(true).getMentionChain();
		boolean correctHeads = m.getMention(false) != null && a.getMention(false) != null
				&& m.getMention(false).getMentionChain() == a.getMention(false).getMentionChain();

		StringBuilder sb = new StringBuilder();
		sb.append(correctExactSpans ? "+" : "-");
		sb.append(correctHeads ? "+" : "-");
		sb.append(r.getName());
		sb.append(" MERGE ");
		sb.append("\n  - ").append(m).append(m.toParamString());
		sb.append("\n  - ").append(a).append(a.toParamString());
		if (!correctHeads) {
			sb.append("\n\t").append(m.getSentence());
			if (m.getSentence().getPairedSentence() != null)
				sb.append("\n\t").append(m.getSentence().getPairedSentence());
			if (a.getSentence() != m.getSentence())
				sb.append("\n\t").append(a.getSentence());
			if (a.getSentence() != m.getSentence() && a.getSentence().getPairedSentence() != null)
				sb.append("\n\t").append(a.getSentence().getPairedSentence());
		}
		if (VERBOSE)
			System.err.println(sb.toString());
	}

	public static void main(String[] args) {
		CorefTest.test("RULER", "Dzejnieks Jānis Bērziņš sacerēja dzejoli.",
				"Lai arī viņš nebija plaši pazīstams, dzejnieks bija ļoti talantīgs.");
	}

}
