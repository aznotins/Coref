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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.NamedEntity;
import lv.coref.data.Node;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.lv.Constants.Case;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.PosTag;
import lv.coref.lv.Constants.Type;
import lv.coref.lv.Dictionaries;
import lv.coref.lv.MorphoUtils;
import lv.coref.tests.CorefTest;

public class MentionFinder {
	private final static Logger log = Logger.getLogger(MentionFinder.class.getName());

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
		// addCoordinationsFlat(sentence);

		addNamedEntityMentions(sentence);
		addAcronymMentions(sentence);
		addQuoteMentions(sentence);
		addNounPhraseMentions2(sentence);
		// //addCoordinations(sentence);
		addPronounMentions(sentence);
		MentionCleaner.cleanSentenceMentions(sentence);
		updateMentionHeads(sentence);
		updateMentionBoundaries(sentence);
		// addCoordinationsFlat(sentence);
		removeAbstractMentions(sentence);
		removeNestedMentions(sentence); // last step

	}

	private void updateMentionHeads(Sentence sentence) {
		for (Mention m : sentence.getMentions())
			if (m.getHeads().isEmpty())
				m.addHead(m.getLastToken());
	}

	public void removeAbstractMentions(Sentence sentence) {
		for (Mention m : sentence.getMentions()) {
			boolean remove = false;
			if (m.isProperMention() && !m.isAcronym())
				continue;
			if (Dictionaries.abstractMentions.match(m.getHeadLemmaString()) != null)
				remove = true;

			if (remove) {
				log.log(Level.INFO, "Remove abstract {0}", m);
				sentence.removeMention(m);
			}
		}
	}

	private void updateMentionBoundaries(Sentence sentence) {
		for (Mention m : sentence.getMentions()) {
			int start = m.getFirstToken().getPosition();
			int end = m.getLastToken().getPosition();
			if (start - 1 >= 0 && sentence.get(start - 1).isQuote())
				start--;
			if (end + 1 < sentence.size() && sentence.get(end + 1).isQuote())
				end++;
			// System.err.println(m + " " + sentence.get(start) +" " +
			// sentence.get(end));
			if (sentence.get(start).isQuote() && sentence.get(end).isQuote()) {
				m.setTokens(sentence.subList(start, end + 1));
				log.log(Level.INFO, "Added quotes to {0}", m);
			}
		}
		List<String> introducers = Arrays.asList("valsts SIA", "SIA");
		for (String s : introducers) {
			List<Token> tokens = sentence.matchTokensByLemmaText(s);
			// System.err.println(tokens);
			if (tokens == null)
				continue;
			int end = tokens.get(tokens.size() - 1).getPosition();
			if (end + 1 >= sentence.size())
				continue;
			Set<Mention> mentions = sentence.get(end + 1).getStartMentions();
			for (Mention m : mentions) {
				tokens.addAll(m.getTokens());
				m.setTokens(tokens);
				m.setType(Type.NE);
				m.setCategory(Category.organization);
				log.log(Level.INFO, "Added introducer {0} to {1}", new Object[] { s, m });
			}
		}

		for (Mention m : sentence.getMentions()) {
			Token t = m.getFirstToken().getPrev();
			if (t != null && t.getEndMentions().size() > 0) {
				Mention max = null;
				for (Mention mm : t.getEndMentions()) {
					if (mm.isProperMention() && (max == null || max.getTokens().size() < mm.getTokens().size()))
						max = mm;
				}
				if (max != null && max.getCase().equals(Case.GEN)) {
					List<Token> tokens = new ArrayList<>();
					tokens.addAll(max.getTokens());
					tokens.addAll(m.getTokens());
					m.setTokens(tokens);
					log.log(Level.INFO, "Added genitive mention {0} to {1}", new Object[] { max, m });
				}
			}
		}
	}

	private void removeNestedMentions(Sentence sentence) {
		// System.err.println(sentence.getTextString());
		for (Mention mention : sentence.getMentions()) {
			boolean remove = false;
			// Token first = mention.getFirstToken();
			// System.err.println("consider nested" + mention);
			for (Mention m : sentence.getMentions()) {
				// TODO don't process all mentions?
				// System.err.println("consider nested " + mention + " inside "
				// + m);
				if (m == mention)
					continue;
				if (mention.isNestedInside(m)) {
					// System.err.println("IS NESTED " + mention + " inside " +
					// m);
					if (m.getCategory().equals(Category.person) && m.isProperMention())
						remove = true;
					// if (m.isStrong()) remove = true;
					if (mention.isPronoun()
							&& Dictionaries.isDemonstrativePronoun(mention.getLemmaString().toLowerCase()))
						remove = true;
				}

			}
			if (remove) {
				log.log(Level.INFO, "Removed nested mention {0}", mention);
				sentence.removeMention(mention);
			}
		}
	}

	private void addNamedEntityMentions(Sentence sent) {
		for (NamedEntity n : sent.getNamedEntities()) {
			List<Token> tokens = n.getTokens();
			List<Token> heads = new ArrayList<>();
			heads.add(tokens.get(tokens.size() - 1));
			Mention m = new Mention(sent.getText().getNextMentionID(), tokens, heads);
			sent.addMention(m);
			sent.getText().addMentionChain(new MentionChain(m));

			m.setCategory(n.getLabel());
			m.setFinal(true);
			if (!m.getCategory().equals(Category.unknown) && !m.getCategory().equals(Category.profession)
					&& !m.getCategory().equals(Category.time) && !m.getCategory().equals(Category.sum)) {
				m.setType(Type.NE);
			} else {
				m.setType(Type.NP);
			}
			log.log(Level.INFO, "Added NER mention {0}", m);
		}
	}

	private void addNounMentions(Sentence sent) {
		for (Token t : sent) {
			if (t.getTag().startsWith("n")) {
				Mention m = new Mention(sent.getText().getNextMentionID(), t);
				sent.addMention(m);
				sent.getText().addMentionChain(new MentionChain(m));
				log.log(Level.INFO, "Added Noun [tag=n] mention {0}", m);
			}
		}
	}

	private void addAcronymMentions(Sentence sent) {
		for (Token t : sent) {
			if (t.isAcronym()) {
				Mention m = new Mention(sent.getText().getNextMentionID(), t);
				sent.addMention(m);
				sent.getText().addMentionChain(new MentionChain(m));
				m.setType(Type.NE);
				log.log(Level.INFO, "Added acronym mention {0}", m);
			}
		}
	}

	private void addQuoteMentions(Sentence sent) {
		for (int iTok = 0; iTok < sent.size(); iTok++) {
			Token t = sent.get(iTok);
			if (!t.isQuote())
				continue;
			int jTokMax = Math.min(sent.size(), iTok + 10);
			boolean hasProperToken = false;
			for (int jTok = iTok + 1; jTok < jTokMax; jTok++) {
				Token jt = sent.get(jTok);
				if (jt.getPosTag().equals(PosTag.V) && !jt.isProper())
					break;
				if (jt.isProper())
					hasProperToken = true;
				// TODO all uppercase letters means that this mention is proper
				if (jt.isQuote()) {
					List<Token> tokens = sent.subList(iTok + 1, jTok);
					if (tokens.size() == 0)
						break; // Piemēram " „ Tas , kas
					if (tokens.get(0).getTag().equals("zc"))
						break; // comma
					if (tokens.get(0).getTag().equals("ccs"))
						break; // saiklis

					List<Token> heads = Arrays.asList(sent.get(jTok - 1));

					Mention m = new Mention(sent.getText().getNextMentionID(), tokens, heads);
					if (hasProperToken)
						m.setType(Type.NE);
					else
						m.setType(Type.NP);
					sent.addMention(m);
					sent.getText().addMentionChain(new MentionChain(m));
					log.log(Level.INFO, "Added quote mention {0}", m);
					break;
				}
			}
		}
	}

	// while (++i < d.tree.size()) {
	// Node n = d.tree.get(i);
	// if (n.isQuote()) {
	// int j = i;
	// while (++j - i <= max_l && j < d.tree.size()) {
	// if (d.tree.get(j).sentence.getID() != n.sentence.getID()) break;
	// if (d.tree.get(j).tag.charAt(0) == 'v' && !Character.isUpperCase(
	// d.tree.get(j).word.charAt(0))) break; //nesatur d.v.
	// if (d.tree.get(j).isQuote()) {
	// if (i + 1 <= j-1) {
	// String s = d.getSubString(i+1, j-1);
	// boolean add = false;
	// for (int k = 0; k < s.length(); k++) {
	// if (Character.isUpperCase(s.charAt(k)) ){
	// add = true;
	// break;
	// }
	// }

	private void addCoordinations(Sentence sent) {
		Node n = sent.getRootNode();
		addCoordinations(sent, n);
	}

	private void addCoordinationsFlat(Sentence sent) {
		int start = -1;
		int end = -1;
		boolean coord = false;
		for (Mention mention : sent.getMentions()) {
			Token next = mention.getLastToken().getNext();
			if (next == null)
				continue;
			if (!next.getLemma().equals("un"))
				continue;
			next = next.getNext();
			if (next == null)
				continue;
			List<Mention> mentions = next.getOrderedStartMentions();
			if (mentions.size() == 0)
				continue;
			// Collections.reverse(mentions);
			Mention mention2 = mentions.get(0);
			log.log(Level.INFO, "Coordination candidate: {0} {1}", new Object[] { mention, mention2 });

			List<Token> tokens = new ArrayList<>();
			List<Token> heads = new ArrayList<>();
			tokens.addAll(mention.getTokens());
			tokens.addAll(mention2.getTokens());
			heads.addAll(mention.getHeads());
			heads.addAll(mention2.getHeads());
			Mention m = new Mention(sent.getText().getNextMentionID(), tokens, heads);
			sent.addMention(m);
			sent.getText().addMentionChain(new MentionChain(m));
			m.setType(Type.CONJ);
			log.log(Level.INFO, "Added flat coordination {0}", m);
		}
	}

	private void addCoordinations(Sentence sent, Node n) {
		for (Node child : n.getChildren()) {
			if (n.getLabel().endsWith("crdParts:crdPart") && n.getHeads().get(0).getPosTag() == PosTag.N) {
				List<Token> tokens = n.getTokens();
				List<Token> heads = new ArrayList<>();
				for (Token t : tokens) {
					if (t.getDependency().endsWith("crdPart")) {
						heads.add(t);
					}
				}
				if (heads.size() > 1) {
					Mention m = new Mention(sent.getText().getNextMentionID(), tokens, heads);
					sent.addMention(m);
					sent.getText().addMentionChain(new MentionChain(m));
					m.setType(Type.CONJ);
					log.log(Level.INFO, "Added coordination {0}", m);
				}
			} else {
				addCoordinations(sent, child);
			}
		}

	}

	private void addPronounMentions(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.P) {
				Mention m = new Mention(sent.getText().getNextMentionID(), n.getTokens(), n.getHeads());
				sent.addMention(m);
				sent.getText().addMentionChain(new MentionChain(m));
				String text = n.getHeads().get(0).getLemma();
				m.setCategory(Dictionaries.getCategory(text));
				m.setType(Type.PRON);
				m.getLastHeadToken().setPronounType(MorphoUtils.getPronounType(m.getLastHeadToken().getTag()));
				m.getLastHeadToken().setPerson(MorphoUtils.getPerson(m.getLastHeadToken().getTag()));
				log.log(Level.INFO, "Added pronoun mention {0}", m);
			}
		}
	}

	private void addNounPhraseMentions(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.N) {
				Mention m = new Mention(sent.getText().getNextMentionID(), n.getTokens(), n.getHeads());
				sent.addMention(m);
				sent.getText().addMentionChain(new MentionChain(m));
			}
		}
	}

	private void addNounPhraseMentions2(Sentence sent) {
		for (Node n : sent.getNodes(false)) {
			if (n.getHeads().get(0).getPosTag() == PosTag.N) {
				List<Token> tokens = sent.subList(n.getStart(),
						n.getHeads().get(n.getHeads().size() - 1).getPosition() + 1);

				// simple filter out incorrect borders due conjunctions,
				// punctuation
				int start = 0, end = tokens.size();

				Set<String> fillerLemmas = new HashSet<String>(Arrays.asList("un", ",", "."));
				// filter out verbs
				for (int i = start; i < end; i++) {
					Token t = tokens.get(i);
					if (t.getPosTag() == PosTag.V)
						start = i + 1;
				}
				// filter out fillers
				while (start < tokens.size()) {
					Token t = tokens.get(start);
					if (fillerLemmas.contains(t.getLemma())) {
						start++;
					} else if (t.getPosTag().equals(PosTag.ADJ) && !MorphoUtils.isDefAdj(t.getTag())) {
						// System.err.println("REMOVE ADJ " + n + " " +
						// t.getPosTag());
						// TODO definite adjective as proper mention attribute
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
				if (start > end)
					continue;
				tokens = tokens.subList(start, end);

				Mention m = new Mention(sent.getText().getNextMentionID(), tokens, n.getHeads());

				sent.addMention(m);
				sent.getText().addMentionChain(new MentionChain(m));

				m.setType(Type.NP);
				if (m.getFirstToken().isProper())
					m.setType(Type.NE);
				m.setCategory(Dictionaries.getCategory(m.getLemmaString()));

				log.log(Level.INFO, "Added NP mention {0}", m);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		log.info("test");
		CorefTest.test("MENTIONS",
				"Kopš 2001. gada viņš strādājis dažādos amatos valsts SIA \" Psihiatrijas centrs \" ,"
						+ " Garīgās veselības valsts aģentūrā un valsts SIA \" Rīgas Psihiatrijas un narkoloģijas "
						+ "centrs \" , bijis arī docents RSU un vadījis lekcijas Latvijas Universitātē .");
		log.info("test");
	}

}
