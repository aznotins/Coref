package lv.coref.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.lv.Constants.Case;
import lv.coref.lv.Constants.Gender;
import lv.coref.lv.Constants.Number;
import lv.coref.lv.Constants.Person;
import lv.coref.lv.Constants.PosTag;
import lv.coref.lv.Constants.PronType;
import lv.coref.lv.MorphoUtils;

public class Token implements Comparable<Token> {
	private String word;
	private String tag;
	private String pos;
	private String lemma;
	private String morphoFeatures;	
	private int position;
	private int parent;
	private String dep;
	
	private Node node;
	private Sentence sentence;
	private NamedEntity namedEntity = null;
	private Set<Mention> mentions = new HashSet<>();

	private PosTag posTag = PosTag.UNKNOWN;
	private Number number = Number.UNKNOWN;
	private Case tokenCase = Case.UNKNOWN;
	private Gender gender = Gender.UNKNOWN;
	private Person person = Person.UNKNOWN;
	private PronType pronounType = PronType.UNKNOWN;

	public Token() {
	}

	public Token(String word, String lemma, String tag) {
		this.word = word;
		this.lemma = lemma;
		this.tag = tag;
		this.posTag = MorphoUtils.getPosTag(tag);
		this.number = MorphoUtils.getNumber(tag);
		this.tokenCase = MorphoUtils.getCase(tag);
		this.gender = MorphoUtils.getGender(tag);
		this.person = MorphoUtils.getPerson(tag);
	}
	
	public boolean isProper() {
		if (getLemma().length() > 0 && Character.isUpperCase(getLemma().charAt(0))) return true;
		return false;
	}
	
	public boolean isAcronym(){
    	if (getTag().charAt(0) == 'z') return false;
    	if (getWord().length() < 2) return false;
    	for (int i = 0; i < getWord().length(); i++) {
    		char c = getWord().charAt(i);
            if (!Character.isLetter(c)) return false;
            if (!Character.isUpperCase(c)) return false;
        }
    	return true;
    }
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public String getMorphoFeatures() {
		return morphoFeatures;
	}

	public void setMorphoFeatures(String morphoFeatures) {
		this.morphoFeatures = morphoFeatures;
	}
	
	public void setNamedEntity(NamedEntity namedEntity) {
		this.namedEntity = namedEntity;
	}

	public NamedEntity getNamedEntity() {
		return namedEntity;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Sentence getSentence() {
		return sentence;
	}
	
	public int getTextPosition() {
		Text text = getSentence().getParagraph().getText();
		int idx = 1;
		int sent = getSentence().getPosition();
		int par = getSentence().getParagraph().getPosition();
		
		main:
		for (Paragraph p : text) {
			for (Sentence s : p) {
				if (p.getPosition() == par && s.getPosition() >= sent) break main;
				idx += s.size();
			}
		}
		idx += getPosition();
		return idx;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public Token getParentToken() {
		int p = getParent();
		if (p == 0)
			return null;
		return getSentence().get(p - 1);
	}

	public String getDependency() {
		return dep;
	}

	public void setDependency(String dependency) {
		this.dep = dependency;
	}

	public PosTag getPosTag() {
		return posTag;
	}

	public Number getNumber() {
		return number;
	}

	public Case getTokenCase() {
		return tokenCase;
	}

	public Gender getGender() {
		return gender;
	}

	public Person getPerson() {
		return person;
	}
	
	public void setPerson(Person person) {
		this.person = person;
	}

	public PronType getPronounType() {
		return pronounType;
	}

	public void setPronounType(PronType pronounType) {
		this.pronounType = pronounType;
	}

	public Set<Mention> getMentions() {
		return mentions;
	}

	public void addMention(Mention mention) {
		mentions.add(mention);
	}

	public void removeMention(Mention mention) {
		mentions.remove(mention);
	}

	public Set<Mention> getStartMentions() {
		Set<Mention> result = new HashSet<>();
		if (getMentions() != null) {
			for (Mention m : getMentions()) {
				if (m.getFirstToken().equals(this)) {
					result.add(m);
				}
			}
		}
		return result;
	}
	
	public List<Mention> getOrderedStartMentions() {
		List<Mention> mentions = new ArrayList<>();
		mentions.addAll(getStartMentions());
		Collections.sort(mentions);
		Collections.reverse(mentions);
		return mentions;
	}

	public Set<Mention> getEndMentions() {
		Set<Mention> result = new HashSet<>();
		if (getMentions() != null) {
			for (Mention m : getMentions()) {
				if (m.getLastToken().equals(this)) {
					result.add(m);
				}
			}
		}
		return result;
	}
	
	public List<Mention> getOrderedEndMentions() {
		List<Mention> mentions = new ArrayList<>();
		mentions.addAll(getEndMentions());
		Collections.sort(mentions);		
		return mentions;
	}
	
	/**
	 * Get mentions with this token as the last head token
	 * @return
	 */
	public Set<Mention> getHeadMentions() {
		Set<Mention> result = new HashSet<>();
		if (getMentions() != null) {
			for (Mention m : getMentions()) {
				if (m.getHeads() == null) continue;
				Token head = m.getHeads().get(m.getHeads().size()-1);
				if (head == this) result.add(m);
			}
		}
		return result;
	}
	
	public Mention getHeadMention() {
		Set<Mention> result = getHeadMentions();
		if (result.size() > 0) {
			if (result.size() > 1)
				System.err.println("Multiple mentions for same head " + this + " " + getHeadMentions());
			return result.iterator().next();
		}
		return null;
	}
	
	public int compareTo(Token o) {
		return getPosition().compareTo(o.getPosition());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(word);
		// sb.append("[");
		// sb.append(getLemma());
		// sb.append(" ").append(getTag());
		// sb.append(" ").append(getPosTag());
		// sb.append(" ").append(getNumber());
		// sb.append(" ").append(getTokenCase());
		// sb.append(" ").append(getGender());
		// sb.append(" ").append(getPerson());
		// sb.append("]");
		return sb.toString();
	}

	public static void main(String[] args) {
		Token t = new Token("mašīnu", "mašīna", "n_____");
		System.out.println(t);
		System.err.println(t.getClass().getMethods()[2].toString());
	}

}
