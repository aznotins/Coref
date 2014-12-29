package lv.coref.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class MentionChain extends HashSet<Mention> {

	private static final long serialVersionUID = 6267185173283516621L;
	private Mention representative;
	private Mention first;
	private String id;

	public MentionChain(String id) {
		this.id = id;
	}

	public MentionChain(Mention m) {
		add(m);
	}

	public boolean add(Mention m) {
		m.setMentionChain(this);
		if (isLaterMention(first, m)) {
			first = m;
			this.id = m.getID();
		}
		return super.add(m);
	}

	public boolean isLaterMention(Mention m, Mention t) {
		if (m == null)
			return true;
		if (t == null)
			return false;
		if (m.getParagraph().getPosition() > t.getParagraph().getPosition())
			return true;
		if (m.getParagraph().getPosition() < t.getParagraph().getPosition())
			return false;
		if (m.getSentence().getPosition() > t.getSentence().getPosition())
			return true;
		if (m.getSentence().getPosition() < t.getSentence().getPosition())
			return false;
		if (m.getLastToken().getPosition() > t.getLastToken().getPosition())
			return true;
		if (m.getLastToken().getPosition() < t.getLastToken().getPosition())
			return false;
		return false;
	}

	public boolean add(MentionChain mc) {
		boolean r = true;
		for (Mention m : mc) {
			r &= this.add(m);
			m.setMentionChain(this);
		}
		return r;
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public Mention getRepresentative() {
		return representative;
	}

	public void setRepresentative() {
		representative = null;
	}

	public List<Mention> getOrderedMentions() {
		List<Mention> mentions = new ArrayList<>();
		mentions.addAll(this);
		Collections.sort(mentions);
		return mentions;
	}

	public static Comparator<MentionChain> getMentionChainComparator() {
		return new Comparator<MentionChain>() {
			public int compare(MentionChain o1, MentionChain o2) {
				Integer first1 = Integer.MAX_VALUE;
				for (Mention m : o1) {
					Integer pos = m.getFirstToken().getPosition();
					first1 = first1 < pos ? first1 : pos;
				}
				Integer first2 = Integer.MAX_VALUE;
				for (Mention m : o2) {
					Integer pos = m.getFirstToken().getPosition();
					first2 = first2 < pos ? first2 : pos;
				}
				return first1.compareTo(first2);
			}
		};
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("=== Cluster #").append(getID()).append(" ===\n");

		for (Mention m : this) {
			s.append("  ");
			s.append(m).append("\n");
		}
		return s.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
