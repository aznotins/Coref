package lv.coref.mf;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.data.Token;
import lv.coref.lv.Constants.Type;

public class MentionCleaner {

	public static final boolean VERBOSE = false;

	public static void cleanSentenceMentions(Sentence sentence) {
		List<Mention> mentions = sentence.getMentions();
		Collection<Mention> unnecessaryMentions = new HashSet<Mention>();

		for (int i = 0; i < mentions.size(); i++) {
			Mention m1 = mentions.get(i);
			for (int j = i + 1; j < mentions.size(); j++) {
				Mention m2 = mentions.get(j);

				Mention lessImportantMention = getLessImportantMention(m1, m2);
				Mention moreImportantMention = m1 == lessImportantMention ? m2
						: m1;
				if (true || m2.getType() == Type.NE) {
					// System.err.println(m1 + " " + m2);
				}

				// same mention borders
				if (m1.getTokens().equals(m2.getTokens())) {
					unnecessaryMentions.add(lessImportantMention);
					if (VERBOSE)
						System.err.println("CLEAN Same borders: " + m1 + ", "
								+ m2 + ":    "
								+ getLessImportantMention(m1, m2) + " removed");
					continue;
				}

				// same mention heads
				if (!m1.getHeads().isEmpty() && !m2.getHeads().isEmpty()) {
					if (m1.getHeads().equals(m2.getHeads())) {

						// List<Token> tokens = moreImportantMention
						// .getTokens();
						//
						// boolean isConj = false;
						// for (Token t : tokens) {
						// if (t.isConj()) {
						// isConj = true;
						// break;
						// }
						// }
						// if (!isConj) {}
						unnecessaryMentions.add(lessImportantMention);
						if (VERBOSE)
							System.err.println("CLEAN Same heads: " + m1 + ", "
									+ m2 + ":    " + lessImportantMention
									+ " removed");
						continue;
					}
				}

				// mention head equals whole other mention
				if (m1.getHeads().isEmpty() && !m2.getHeads().isEmpty()) {
					if (m2.getHeads().equals(m1.getHeads())) {
						unnecessaryMentions.add(lessImportantMention);
						if (VERBOSE)
							System.err.println("CLEAN head is other mention: "
									+ m1 + ", " + m2 + " : "
									+ lessImportantMention + " removed");
						continue;
					}
				}

				// the same, but other way round
				if (!m2.getHeads().isEmpty() && !m1.getHeads().isEmpty()) {
					if (m1.getHeads().equals(m2.getHeads())) {
						unnecessaryMentions.add(lessImportantMention);
						if (VERBOSE)
							System.err.println("CLEAN head is other mention: "
									+ m1 + ", " + m2 + " : "
									+ lessImportantMention + " removed");
						continue;

					}
				}

				// intersection
				boolean intersect = false;

				Set<Token> notInM1 = new HashSet<Token>(m2.getTokens());
				notInM1.removeAll(m1.getTokens());
				if (notInM1.size() < m2.getTokens().size())
					intersect = true;

				Set<Token> notInM2 = new HashSet<Token>(m1.getTokens());
				notInM2.removeAll(m2.getTokens());
				if (notInM2.size() < m1.getTokens().size())
					intersect = true;

				// if (intersect)
				// System.out.println(m1+","+m2);

				if (intersect && !notInM1.isEmpty() && !notInM2.isEmpty()) {
					unnecessaryMentions.add(lessImportantMention);
					if (VERBOSE)
						System.err.println("CLEAN intersection!" + m1 + ", "
								+ m2 + ":    "
								+ getLessImportantMention(m1, m2) + " removed");
					continue;
				}

				// Nested in Named Enitity
				if (intersect && notInM1.isEmpty() && m1.getType() == Type.NE) {
					unnecessaryMentions.add(m2);
					if (VERBOSE)
						System.err.println("CLEAN Nested in NE!" + m1 + ", "
								+ m2 + ":    " + m2 + " removed");
				}
				// if (intersect && notInM2.isEmpty() && m2.getType() ==
				// Type.NE) {
				// unnecessaryMentions.add(m1);
				// System.err.println("CLEAN Nested in NE!" + m1 + ", " + m2
				// + ":    " + m1
				// + " removed");
				// }
			}
		}
		for (Mention m : unnecessaryMentions) {
			sentence.removeMention(m);
		}
	}

	private static Mention getLessImportantMention(Mention m1, Mention m2) {
		if (m1.getTokens().size() > m2.getTokens().size())
			return m2;
		else
			return m1;
	}
}
