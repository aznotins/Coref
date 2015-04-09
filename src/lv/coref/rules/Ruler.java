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
package lv.coref.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.tests.CorefTest;
import lv.util.Pair;

public class Ruler {
	private final static Logger log = Logger.getLogger(Ruler.class.getName());
	
	private boolean POST_PROCESS = false;
	private Set<String> DEBUG_STRINGS = !Config.getInstance().containsKey(Config.PROP_COREF_DEBUG_MENTION_STRINGS) ? null
			: new HashSet<String>(Arrays.asList(Config.getInstance().get(Config.PROP_COREF_DEBUG_MENTION_STRINGS, "")
					.split("\\s*\\|\\s*")));

	private List<Rule> rules = new ArrayList<>();
	
	public Ruler() {
		initRules();
	}
	
	public Ruler(boolean postProcess) {
		POST_PROCESS = postProcess;
		initRules();
	}
	
	public void initRules() {
		rules = new ArrayList<>();
		
		// rules.add(new AllInOne());
		// rules.add(new HeadMatch());
		
		rules.add(new ExactMatch());
		rules.add(new Appositive());
		rules.add(new Predicate());
		rules.add(new Acronym());
		rules.add(new StrictHeadMatch());
		rules.add(new SubClause());
		rules.add(new Pronoun());
	}

	public Ruler resolve(Text t) {
		for (Rule r : rules) {
			List<Pair<Mention, Mention>> merge = new ArrayList<>();
			if (DEBUG_STRINGS != null) {
				System.err.printf("=== %s\n", r.getName());
			}
			for (Paragraph p : t) {
				for (Sentence s : p) {
					for (Mention m : s.getMentions()) {
						Mention a = r.getFirst(m);
						if (a != null && a.getMentionChain() !=  m.getMentionChain()) {
							merge.add(Pair.create(a, m));
							getDescription(r, m, a);
							//m.addComment(String.format("%s<%s>",r.getName(), a.getString()));
							if (DEBUG_STRINGS != null && 
									(DEBUG_STRINGS.contains(m.getString()) || DEBUG_STRINGS.contains(m.getLastHeadToken().getWord())
									 || DEBUG_STRINGS.contains(a.getString()) || DEBUG_STRINGS.contains(a.getLastHeadToken().getWord()))) {
								System.err.printf("    RESOLVE: %s\t\t\t%s\n", m, m.getSentence().getTextString());
								System.err.printf("    -> %s\t\t\t%s\n", a, a.getSentence().getTextString());
							}
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
		log.log(Level.INFO, "{0}", sb.toString());
	}

	public static void main(String[] args) {
		CorefTest.test("RULER", "Dzejnieks Jānis Bērziņš sacerēja dzejoli.",
				"Lai arī viņš nebija plaši pazīstams, dzejnieks bija ļoti talantīgs.");
	}

}
