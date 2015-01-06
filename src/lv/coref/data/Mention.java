package lv.coref.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import lv.coref.lv.Constants.Case;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Gender;
import lv.coref.lv.Constants.Number;
import lv.coref.lv.Constants.PosTag;
import lv.coref.lv.Constants.PronType;
import lv.coref.lv.Constants.Type;

public class Mention implements Comparable<Mention> {
	private MentionChain mentionChain;
	private List<Token> tokens = new ArrayList<>();
	private List<Token> heads = new ArrayList<>();
	private String id;
	private Type type = Type.UNKNOWN;
	private Category category = Category.unknown;
	private Node parent;

	public Node getParent() {
		if (parent != null)
			return parent;
		else {
			return getLastHeadToken().getNode();
		}
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	// public Mention() {
	// // TODO null id for mention chain
	// MentionChain mc = new MentionChain(this);
	// setMentionChain(mc);
	// }

	public Mention(String id, List<Token> tokens, List<Token> heads) {
		for (Token t : tokens) {
			t.addMention(this);
			this.tokens.add(t);
		}
		this.heads = heads;
		this.id = id;
	}

	public Mention(List<Token> tokens, List<Token> heads) {
		this(null, tokens, heads);
	}

	public Mention(String id, Token token) {
		this.id = id;
		this.tokens.add(token);
		token.addMention(this);
		this.heads.add(token);
	}

	public boolean isProperMention() {
		if (type == Type.NE)
			return true;
		return false;
	}

	public boolean isAcronym() {
		if (tokens.size() > 1)
			return false;
		Token t = getFirstToken();
		return t.isAcronym();
	}

	public boolean isAcronymOf(String acronym) {
		if (!isProperMention())
			return false;
		Set<String> exclude = new HashSet<String>(Arrays.asList("un", ",",
				"\"", "'"));
		String s = "";
		for (Token t : getTokens()) {
			if (!exclude.contains(t.getLemma()))
				s += t.getWord().charAt(0);
		}
		return s.toUpperCase().equals(acronym.toUpperCase());
	}

	public boolean isBefore(Mention o) {
		if (o == null)
			return false;
		if (getParagraph().getPosition() > o.getParagraph().getPosition())
			return true;
		if (getParagraph().getPosition() < o.getParagraph().getPosition())
			return false;
		if (getSentence().getPosition() > o.getSentence().getPosition())
			return true;
		if (getSentence().getPosition() < o.getSentence().getPosition())
			return false;
		if (getLastToken().getPosition() > o.getLastToken().getPosition())
			return true;
		if (getLastToken().getPosition() < o.getLastToken().getPosition())
			return false;
		return false;
	}

	public boolean isMoreRepresentativeThan(Mention o) {
		if (o == null)
			return true;
		// System.err.println(nerString +
		// "("+(category!=null?category:"null")+")"+ " : " + p.nerString +
		// "("+(p.category!=null?p.category:"null")+")");
		if (getType().equals(Type.PRON))
			return false; // PP - lai nav vietniekvārdi kā reprezentatīvākie
		if (o.getType().equals(Type.PRON))
			return true;
		if (!o.getCategory().isUnkown() && !getCategory().isUnkown()
				&& o.getCategory().equals(Category.profession)
				&& getCategory().equals(Category.person)
				&& getType().equals(Type.NE))
			return true;
		if (!o.getCategory().isUnkown() && !getCategory().isUnkown()
				&& o.getCategory().equals(Category.person)
				&& getCategory().equals(Category.profession)
				&& o.getType().equals(Type.NE))
			return false;
		if (!o.getType().equals(Type.NE) && getType().equals(Type.NE))
			return true;
		else if (o.getType().equals(Type.NE) && !getType().equals(Type.NE))
			return false;
		if (getString().length() > o.getString().length())
			return true;
		else if (getString().length() < o.getString().length())
			return false;
		if (isBefore(o))
			return true; // TODO what is better indicator: length or position
		return false;
	}

	public void setTokens(List<Token> tokens) {
		for (Token t : this.tokens) {
			t.removeMention(this);
		}
		this.tokens = tokens;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = Category.get(category);
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public Token getFirstToken() {
		return tokens.get(0);
	}

	public Token getLastToken() {
		return tokens.get(tokens.size() - 1);
	}

	public List<Token> getHeads() {
		return heads;
	}

	public Token getLastHeadToken() {
		if (heads.size() > 0) {
			return heads.get(heads.size() - 1);
		} else {
			return getLastToken();
		}
	}

	public void addHead(Token head) {
		heads.add(head);
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public Sentence getSentence() {
		return getFirstToken().getSentence();
	}

	public Paragraph getParagraph() {
		return getSentence().getParagraph();
	}

	public Text getText() {
		return getFirstToken().getSentence().getText();
	}

	public MentionChain getMentionChain() {
		return mentionChain;
	}

	public void setMentionChain(MentionChain mentionChain) {
		this.mentionChain = mentionChain;
	}

	// TODO how to tell if is a pronoun
	public boolean isPronoun() {
		if (type == Type.PRON || getLastHeadToken().getPosTag() == PosTag.P)
			return true;
		return false;
	}

	public PronType getPronounType() {
		Token t = getLastHeadToken();
		if (isPronoun())
			return getLastHeadToken().getPronounType();
		return PronType.UNKNOWN;
	}

	public Gender getGender() {
		return getLastHeadToken().getGender();
	}

	public Number getNumber() {
		return getLastHeadToken().getNumber();
	}

	public Case getCase() {
		return getLastHeadToken().getTokenCase();
	}

	// public String getLemma() {
	// StringBuffer sb = new StringBuffer();
	// for (Token t : tokens)
	// sb.append(" " + t.getLemma());
	// return sb.toString().trim();
	// }
	//
	// public String getHeadLemma() {
	// StringBuffer sb = new StringBuffer();
	// for (Token t : heads)
	// sb.append(" " + t.getLemma());
	// return sb.toString().trim();
	// }

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Token t : getTokens()) {
			if (!first)
				sb.append(" ");
			else
				first = false;
			sb.append(t.getWord());
		}
		return sb.toString();
	}

	public String getLemmaString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Token t : getTokens()) {
			if (!first)
				sb.append(" ");
			else
				first = false;
			sb.append(t.getLemma());
		}
		return sb.toString();
	}

	public String getHeadString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Token t : getHeads()) {
			if (!first)
				sb.append(" ");
			else
				first = false;
			sb.append(t.getWord());
		}
		return sb.toString();
	}

	public String getHeadLemmaString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Token t : getHeads()) {
			if (!first)
				sb.append(" ");
			else
				first = false;
			sb.append(t.getLemma());
		}
		return sb.toString();
	}

	public Set<String> getProperTokens() {
		Set<String> properTokens = new HashSet<>();
		if (isProperMention()) {
			for (Token t : getTokens()) {
				if (t.isProper())
					properTokens.add(t.getLemma().toLowerCase());
			}
		}
		return properTokens;
	}

	public List<Mention> getPotentialAntecedents(int par, int sent, int ment) {
		List<Mention> r = new ArrayList<>();
		int parC = 0, sentC = 0, mentC = 1;
		int pos = getLastToken().getPosition();
		Sentence curSentence = this.getSentence();
		Paragraph curParagraph = curSentence.getParagraph();
		if (curSentence == null || curParagraph == null)
			return r;
		Text text = curParagraph.getText();
		ListIterator<Paragraph> pit = text.listIterator(curParagraph
				.getPosition() + 1);
		main: while (pit.hasPrevious()) {
			Paragraph p = pit.previous();
			ListIterator<Sentence> sit = p.listIterator(p.size());
			while (sit.hasPrevious()) {
				Sentence s = sit.previous();
				if (p == curParagraph
						&& s.getPosition() > curSentence.getPosition())
					continue;
				List<Mention> mm = s.getMentions();
				ListIterator<Mention> mit = mm.listIterator(mm.size());
				while (mit.hasPrevious()) {
					Mention m = mit.previous();
					if (s == curSentence
							&& pos <= m.getLastToken().getPosition())
						continue;
					r.add(m);
					if (mentC++ >= ment && ment >= 0)
						break main;
				}
				if (sentC++ >= sent && sent >= 0)
					break main;
			}
			if (parC++ >= par && par >= 0)
				break main;
		}
		return r;
	}

	// public int hashCode() {
	// final int prime = 31;
	// int result = 1;
	// result = prime * result + ((heads == null) ? 0 : heads.hashCode());
	// result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
	// return result;
	// }

	public int compareTo(Mention other) {
		// TODO test performance
		// System.err.println("COMPARE " + this + other);
		Token thisLastToken = getLastToken();
		Token anotherLastToken = other.getLastToken();

		Sentence thisSentence = thisLastToken.getSentence();
		Sentence anotherSentence = anotherLastToken.getSentence();

		Paragraph thisParagraph = thisSentence == null ? null : thisSentence
				.getParagraph();
		Paragraph anotherParagraph = anotherSentence == null ? null
				: anotherSentence.getParagraph();

		String thisTextId = thisParagraph == null ? null : thisParagraph
				.getText().getId();
		String anotherTextId = anotherParagraph == null ? null
				: anotherParagraph.getText().getId();

		int compare;
		// first, compare by ids of texts
		if (thisTextId != null && anotherTextId != null) {
			compare = thisTextId.compareTo(anotherTextId);
			if (compare != 0)
				return compare;
		}

		// second, compare by paragraph position
		if (thisParagraph != null && anotherParagraph != null) {
			compare = thisParagraph.getPosition().compareTo(
					anotherParagraph.getPosition());
			if (compare != 0)
				return compare;

			// third, compare by sentence position
			compare = thisSentence.getPosition().compareTo(
					anotherSentence.getPosition());
			if (compare != 0)
				return compare;
		}

		// fourth, compare by last segments
		compare = thisLastToken.getPosition().compareTo(
				anotherLastToken.getPosition());
		if (compare != 0)
			return compare;

		// fifth, compare by size
		Integer thisSize = getTokens().size();
		Integer anotherSize = other.getTokens().size();
		compare = thisSize.compareTo(anotherSize);
		if (compare != 0)
			return compare;

		// sixth, compare by last head segments
		Token thisLastHeadSegment = getLastHeadToken();
		Token anotherLastHeadSegment = other.getLastHeadToken();
		if (thisLastHeadSegment != null && anotherLastHeadSegment != null) {
			compare = thisLastHeadSegment.getPosition().compareTo(
					anotherLastHeadSegment.getPosition());
		}

		// seventh, compare by head segments size
		thisSize = getHeads().size();
		anotherSize = other.getHeads().size();
		compare = thisSize.compareTo(anotherSize);

		return compare;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mention other = (Mention) obj;
		if (heads == null) {
			if (other.heads != null)
				return false;
		} else if (!heads.equals(other.heads))
			return false;
		if (tokens == null) {
			if (other.tokens != null)
				return false;
		} else if (!tokens.equals(other.tokens))
			return false;
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		sb.append(getMentionChain().getID());
		if (!getCategory().equals(Category.unknown))
			sb.append("-").append(getCategory());
		sb.append(" ").append(heads).append(" ");
		for (Token t : tokens) {
			sb.append(t.toString() + " ");
		}
		sb.append("|").append(getType());
		if (isPronoun())
			sb.append("-" + getPronounType());
		sb.append("|").append(getID());
		sb.append("|").append(getGender());
		sb.append("|").append(getNumber());
		sb.append("|").append(getCase());
		sb.append("]");
		sb.append(toParamString());
		return sb.toString();
	}
	
//	public String getContext(int tokens, int maxWidth) {
//		Sentence s = getFirstToken().getSentence();
//		StringBuilder left = new StringBuilder();
//		StringBuilder right = new StringBuilder();
//		int iTok = 1;
//		for (int i = getFirstToken().getPosition() - 1; i > 0 &&  (iTok < 0 || iTok < tokens); i--) {
//			left.insert(0, s.get(i) + " ");
//			if (left.length() > maxWidth && maxWidth >= 0) {
//				left = new StringBuilder(".. " + left.substring(left.length() - maxWidth - 3));
//				break;
//			}
//			iTok++;
//		}
//		iTok = 1;
//		for (int i = getLastToken().getPosition() - 1; i < s.size() &&  (iTok < 0 || iTok < tokens); i++) {
//			right.append(s.get(i)).append(" ");
//			if (right.length() > maxWidth && maxWidth >= 0) {
//				right = new StringBuilder(".. " + right.substring(0, maxWidth - 3));
//				break;
//			}
//			iTok++;
//		}
//		left.append(this).append(right);
//		return left.toString();		
//	}

	public String toParamString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("#").append(getID());
		sb.append("*MCne=").append(getMentionChain().isProper());
		sb.append("*MCprop=").append(getMentionChain().getProperTokens());
		sb.append("*").append(getHeadLemmaString());
		sb.append("*").append(getLemmaString());
		sb.append("*").append(getNumber());
		sb.append("*").append(getGender());
		sb.append("*").append(getCase());
		sb.append(String.format("*(%d,%d,%d)", getParagraph().getPosition(),
				getSentence().getPosition(), getLastHeadToken().getPosition()));
		sb.append("*").append(getLastHeadToken().getDependency());
		sb.append("*").append(getLastHeadToken().getParentToken());
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Get Mention from paired Document
	 * 
	 * @return
	 */
	public Mention getMention(boolean exact) {
		Text paired = getSentence().getText().getPairedText();
		if (paired == null)
			return null;
		Sentence pairedSentence = paired.get(getParagraph().getPosition()).get(
				getSentence().getPosition());
		// System.out.println(getSentence().getPosition() + " " +
		// pairedSentence.getPosition());
		for (Mention m : pairedSentence.getMentions()) {
			boolean equal = true;
			if (exact) {
				if (tokens.size() == m.getTokens().size()) {
					for (int i = 0; i < tokens.size(); i++) {
						if (tokens.get(i).getPosition() != m.getTokens().get(i)
								.getPosition()) {
							equal = false;
							break;
						}
					}
				} else
					equal = false;
			} else {
				if (heads.size() == m.getHeads().size()) {
					for (int i = 0; i < heads.size(); i++) {
						if (heads.get(i).getPosition() != m.getHeads().get(i)
								.getPosition()) {
							equal = false;
							break;
						}
					}
				} else
					equal = false;
			}
			if (equal)
				return m;
		}
		return null;
	}
}
