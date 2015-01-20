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
package lv.coref.mf;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Type;

public class MentionCleaner {

	public static final boolean VERBOSE = false;

	public static void cleanSentenceMentions(Sentence sentence) {
		List<Mention> mentions = sentence.getOrderedMentions();
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
				
				boolean intersect = m1.intersects(m2);
//				boolean intersect = false;
//				Set<Token> notInM1 = new HashSet<Token>(m2.getTokens());
//				notInM1.removeAll(m1.getTokens());
//				if (notInM1.size() < m2.getTokens().size())
//					intersect = true;
//				Set<Token> notInM2 = new HashSet<Token>(m1.getTokens());
//				notInM2.removeAll(m2.getTokens());
//				if (notInM2.size() < m1.getTokens().size())
//					intersect = true;
//				if (intersect && !notInM1.isEmpty() && !notInM2.isEmpty())

				if (intersect) {
					unnecessaryMentions.add(lessImportantMention);
					if (VERBOSE)
						System.err.println("CLEAN intersection!" + m1 + ", "
								+ m2 + ":    "
								+ getLessImportantMention(m1, m2) + " removed");
					continue;
				}

				// Nested in Named Enitity
//				if (intersect && notInM1.isEmpty() && m1.getType() == Type.NE) {
//					unnecessaryMentions.add(m2);
//					if (VERBOSE)
//						System.err.println("CLEAN Nested in NE!" + m1 + ", "
//								+ m2 + ":    " + m2 + " removed");
//				}
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
		if (m2.isFinal() && !m1.isFinal()) {
			return m1;
		} else if (!m2.isFinal() && m1.isFinal()) {
			return m2;
		}
		
		if (m2.isProperMention() && !m1.isProperMention()) {
			return m1;
		} else if (!m2.isProperMention() && m1.isProperMention()) {
			return m2;
		}
		
		if (m1.getTokens().size() > m2.getTokens().size()) {
			return m2;
		} else if (m1.getTokens().size() < m2.getTokens().size()) {
			return m1;
		}
		
		if (!m2.getCategory().equals(Category.unknown) && m1.getCategory().equals(Category.unknown)) {
			return m1;
		} else if (!m2.getCategory().equals(Category.unknown) && m1.getCategory().equals(Category.unknown)) {
			return m2;
		}
		
		return m2;
	}
}
