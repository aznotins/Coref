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
package lv.coref.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import lv.coref.util.Triple;

public class Sentence extends ArrayList<Token> {

	private static final long serialVersionUID = 6452651828665864779L;

	private Paragraph paragraph;
	private int position;

	// private Set<Mention> mentions = new TreeSet<>(); // TODO nodzēš nepareizi
	// pievienojot jaunus m
	private Set<Mention> mentions = new HashSet<>();
	private Node root;
	private Set<NamedEntity> namedEntities = new TreeSet<>();

	public Sentence() {
	}

	public Sentence(int position) {
		this.position = position;
	}
	
	public List<Token> matchTokensByLemmaText(String lemmaText) {
		String[] bits = lemmaText.split(" ");
		for (int i = 0; i < this.size(); i++) {
			boolean ok = true;
			for (int j = 0; j < bits.length; j++ ) {
				if (j + i >= size()) { ok = false; break; }
				if (!get(i + j).getWord().equals(bits[j])) { ok = false; break; }
			}
			if (ok) {
				return subList(i, i + bits.length);
			}
		}
		return null;		
	}

	public boolean add(Token token) {
		token.setPosition(this.size());
		token.setSentence(this);
		return super.add(token);
	}
	
	@Override
	public List<Token> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return new ArrayList<>(super.subList(fromIndex, toIndex));
	}
	
	public Paragraph getParagraph() {
		return paragraph;
	}

	public void setParagraph(Paragraph paragraph) {
		this.paragraph = paragraph;
	}

	public Sentence getPairedSentence() {
		Sentence s = null;
		Text pairedText = getParagraph().getText().getPairedText();
		if (pairedText != null) {
			Paragraph pairedParagraph = pairedText.get(getParagraph()
					.getPosition());
			if (pairedParagraph != null) {
				s = pairedParagraph.get(getPosition());
			}
		}
		return s;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<Mention> getMentions() {
		//return new ArrayList<>(mentions);
		List<Mention> mentions = new ArrayList<>(this.mentions);
		Collections.sort(mentions);
		return mentions;
	}
	
	public Set<Mention> getMentionSet() {
		return mentions;
	}

	public List<Mention> getOrderedMentions() {
		List<Mention> mentions = new ArrayList<>();
		mentions.addAll(getMentions());
		Collections.sort(mentions);
		return mentions;
	}

	public void addMention(Mention mention) {
		mentions.add(mention);
		// TODO what is this
		// if (!this.mentions.add(mention))
		// this.removeMention(mention);
	}

	public void removeMention(Mention mention) {
		mentions.remove(mention);
		// System.err.println("SENTECE remove mention " + mention + " "
		// + mentions.contains(mention));
		for (Token t : mention.getTokens()) {
			t.removeMention(mention);
			// for (Mention m : t.getMentions()) {
			// System.err.println(mention + " " + m + " " + mention.equals(m)
			// + mention.compareTo(m) + " " + mention.hashCode() + ":"
			// + m.hashCode());
			// }
			// System.err.println("TOKEN remove mention "
			// + t.getMentions().contains(mention) + t.getMentions());
		}
		if (mention.getMentionChain() != null) {
			if (mention.getMentionChain().size() == 1) {
				getText().removeMentionChain(mention.getMentionChain());
			} else {
				mention.getMentionChain().remove(mention);
			}
		}
	}

	public List<NamedEntity> getNamedEntities() {
		return new ArrayList<>(namedEntities);
	}

	public void addNamedEntity(NamedEntity namedEntity) {
		namedEntities.add(namedEntity);
	}

	public Node getRootNode() {
		return root;
	}

	public void setRootNode(Node root) {
		this.root = root;
	}

	/**
	 * Initialize syntactic node tree using CONLL parent field
	 * 
	 * @return Sentence root node
	 */
	public Node initializeNodeTree() {
		Node root = new Node("_ROOT_", 0, size());
		root.setHead(new Token("_sentroot_", "_", "_"));
		Map<Token, Node> nodes = new HashMap<>();
		for (Token t : this) {
			Node n;
			if (nodes.containsKey(t))
				n = nodes.get(t);
			else {
				n = new Node(t.getDependency(), t.getPosition(),
						t.getPosition() + 1);
				n.setHead(t);
				nodes.put(t, n);
			}
			Token p = t.getParentToken();
			if (p == null) {
				n.setParent(root);
				root.addChild(n);
			} else {
				Node pn;
				if (nodes.containsKey(p))
					pn = nodes.get(p);
				else {
					pn = new Node(p.getDependency(), p.getPosition(),
							p.getPosition() + 1);
					pn.setHead(p);
					nodes.put(p, pn);
				}
				n.setParent(pn);
				pn.addChild(n);
			}
		}
		// get correct span borders for nodes
		initializeNodeBorders(root);
		this.root = root;
		return root;
	}

	/**
	 * Initialize node span borders, assuming correctly set up tree (all
	 * children and parents) TODO optimize - for borders use ordered children
	 * list last and first element
	 */
	public int[] initializeNodeBorders(Node n) {
		for (Node nn : n.getChildren()) {
			int[] arr = initializeNodeBorders(nn);
			n.setStart(Math.min(n.getStart(), arr[0]));
			n.setEnd(Math.max(n.getEnd(), arr[1]));
		}
		return new int[] { n.getStart(), n.getEnd() };
	}

	/**
	 * Get all ordered nodes
	 * 
	 * @return
	 */
	public List<Node> getNodes(boolean rootIncluded) {
		return root.getNestedNodes(rootIncluded);
	}

	/**
	 * Initialize Named Entities from spans (start, end, label)
	 * 
	 * @param spans
	 */
	public void initializeNamedEntities(
			List<Triple<Integer, Integer, String>> spans) {
		for (Triple<Integer, Integer, String> span : spans) {
			NamedEntity n = new NamedEntity(span.third, this.subList(
					span.first, span.second + 1));
			namedEntities.add(n);
		}
	}

	/**
	 * Initialize coreferences from spans (start, end, id), categories TODO what
	 * to do with multiple head mentions? - could specify separate mentions for
	 * each head
	 * 
	 * @param spans
	 * @param mf
	 */
	public void initializeCoreferences(
			List<Triple<Integer, Integer, String>> mSpans,
			List<Integer> mHeads, List<String> mCategories) {
		Text text = getText();
		for (int i = 0; i < mSpans.size(); i++) {
			assert mHeads == null || mSpans.size() == mHeads.size();
			assert mCategories == null || mSpans.size() == mCategories.size();
			Triple<Integer, Integer, String> span = mSpans.get(i);
			String category = null;
			if (mCategories != null)
				category = mCategories.get(i);
			Integer head = span.second;
			if (mHeads != null && mHeads.get(i) != null)
				head = mHeads.get(i);
			String id = span.third;
			List<Token> tokens = this.subList(span.first, span.second + 1);
			List<Token> heads = this.subList(head, head + 1);
			Mention m = new Mention(text.getNextMentionID(), tokens, heads);
			if (category != null)
				m.setCategory(category);
			addMention(m);
			if (text.getMentionChain(id) == null) {
				text.addMentionChain(new MentionChain(id, m));
			} else {
				text.getMentionChain(id).add(m);
			}
			// System.err.println("Initialize coreference mention " + span +
			// " to " + m.getMentionChain().getID() + " " + m);
		}
	}

	/**
	 * Initialize coreferences using only mention heads and categories
	 * 
	 * @param headMentions
	 *            : Triple(position, id, category)
	 */
	// public void initalizeCoreferencesFromHeads(
	// List<Triple<Integer, String, String>> headMentions) {
	// Text text = getText();
	// for (Triple<Integer, String, String> headMention : headMentions) {
	// Token head = get(headMention.first());
	// String id = headMention.second();
	// List<Token> heads = new ArrayList<>();
	// heads.add(head);
	// List<Token> tokens = head.getNode().getTokens();
	// // TODO filter wrong tokens in head subtree
	//
	// Mention m = new Mention(getText().getNextMentionID(), tokens, heads);
	// m.setCategory(headMention.third());
	// addMention(m);
	// if (text.getMentionChain(id) == null) {
	// text.addMentionChain(new MentionChain(id, m));
	// } else {
	// text.getMentionChain(id).add(m);
	// }
	// }
	// }

	// TODO make mention category indetification faster
	public void initializeMentionCategories(
			List<Triple<Integer, Integer, String>> spans) {
		initializeMentionAttributes(spans, "category");
	}

	/**
	 * For testing only. Used after initializeCoreferences() to explicitly set
	 * some attributes (category, type)
	 */
	public void initializeMentionAttributes(
			List<Triple<Integer, Integer, String>> spans, String attribute) {
		for (Triple<Integer, Integer, String> span : spans) {
			try {
				Token first = this.get(span.first);
				Token last = this.get(span.second);
				Set<Mention> spanMentions = first.getStartMentions();
				spanMentions.retainAll(last.getEndMentions());
				if (spanMentions.size() == 0)
					throw new Exception("No such mention");
				for (Mention m : spanMentions) {
					if (attribute.equals("category")) {
						m.setCategory(span.third);
					}
				}
			} catch (Exception e) {
				System.err.println(e.getMessage()
						+ " : Unable to initilize attribute " + "\""
						+ attribute + "\" in sentence position="
						+ getPosition() + ": " + span);
			}
		}
	}

	public Text getText() {
		return getParagraph().getText();
	}

	public String getTextString() {
		StringBuilder sb = new StringBuilder();
		// Set<String> noGapBefore = new HashSet<String>(Arrays.asList(".", ",",
		// ":", ";", "!", "?", ")", "]", "}", "%"));
		// Set<String> noGapAfter = new HashSet<String>(Arrays.asList("(", "[",
		// "{"));
		// Set<String> quoteSymbols = new HashSet<String>(Arrays.asList("'",
		// "\""));
		for (Token t : this) {
			sb.append(t.getWord());
			sb.append(" ");
			// TODO uzlabot teksta veidošanu no sadalītiem tokeniem
		}
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Token t : this) {
			for (@SuppressWarnings("unused")
			Mention m : t.getStartMentions()) {
				sb.append("[");
			}
			sb.append(t.toString());
			for (Mention m : t.getEndMentions()) {
				sb.append(" |").append(m.getCategory());
				sb.append("|").append(m.getType());
				if (m.getMentionChain() != null)
					sb.append("|").append(m.getMentionChain().getID());
				// sb.append("|").append(m.getID());
				// sb.append(" ").append(m.toParamString());
				sb.append("]");
			}
			sb.append(" ");
		}
		String tmpText = sb.toString();
		// tmpText = WordUtils.wrap(tmpText, 150, "\n\t", true);
		sb = new StringBuilder(tmpText);

		// for (Mention m : getOrderedMentions()) {
		// sb.append("\n\t").append(m.getMentionChain().getID());
		// if (m.getMention(false) != null)
		// sb.append("+");
		// else
		// sb.append("-");
		// if (m.getMention(true) != null)
		// sb.append("+");
		// else
		// sb.append("-");
		// sb.append(m);
		// }
		return sb.toString();
	}

	public String getMentionString() {
		Sentence paired = getPairedSentence();
		StringBuilder sb = new StringBuilder();
		sb.append(getTextString());
		
		for (Mention m : getOrderedMentions()) {
			sb.append("\n  ");
			if (paired != null) {
				sb.append(m.getMention(true) == null ? "-" : "+");
				sb.append(m.getMention(false) == null ? "-" : "+");				
			}
			sb.append(" ").append(m.toString());
			sb.append("\t - ").append(m.getParent());
			//sb.append("\t").append(m.toParamString());
		}
		if (paired != null) {			
			for (Mention m : paired.getOrderedMentions()) {				
				if (m.getMention(true) == null || m.getMention(false) == null) {
					sb.append("\n *");
					sb.append(m.getMention(true) == null ? "-" : "+");
					sb.append(m.getMention(false) == null ? "-" : "+");					
					sb.append(" ").append(m.toString());
					//sb.append("\t").append(m.toParamString());
				}				
			}
		}
		return sb.toString();
	}

}
