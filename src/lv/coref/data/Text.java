package lv.coref.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lv.coref.lv.Constants.Type;

public class Text extends ArrayList<Paragraph> implements Comparable<Text> {

	private static final long serialVersionUID = -5828501273010235785L;
	private String id;
	private Map<String, MentionChain> mentionChains = new HashMap<>();
	private Text pairedText;
	private int nextMentionID = 1;

	public Text() {
	}

	public Text(String id) {
		this.id = id;
	}
	
	public boolean isEmpty() {
		if (size() == 0) return true;
		if (get(0).size() == 0) return true;
		if (get(0).get(0).size() == 0) return true;
		return false;
	}

	public boolean add(Paragraph p) {
		p.setPosition(this.size());
		p.setText(this);
		return super.add(p);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNextMentionID() {
		return Integer.toString(nextMentionID++);
	}

	public void setNextMentionID(int nextId) {
		this.nextMentionID = nextId;
	}

//	public Map<String, MentionChain> finalizeMentionChains() {
//		Map<String, MentionChain> mc = new HashMap<>();
//		for (Paragraph p : this) {
//			for (Sentence s : p) {
//				for (Mention m : s.getMentions()) {
//					if (m.getMentionChain() == null) {
//						System.err.println("WARNING: no chain for " + m);
//					} else
//						mc.put(m.getMentionChain().getID(), m.getMentionChain());
//				}
//			}
//		}
//		mentionChains = mc;
//		return mc;
//	}

	public void removeSingletons() {
		for (Mention m : getMentions()) {
			if (m.getMentionChain().size() < 2) {
				removeMentionChain(m.getMentionChain());
			}
		}
	}
	
	public void removeCommonSingletons() {
		for (Mention m : getMentions()) {
			if (m.getMentionChain().size() < 2 && !m.getType().equals(Type.NE)) {
				removeMentionChain(m.getMentionChain());
			}
		}
	}

	public MentionChain getMentionChain(String id) {
		return mentionChains.get(id);
	}

	public List<MentionChain> getMentionChains() {
		// TODO load only ones (finalizeMentionChains)
		Map<String, MentionChain> mentionChains = new HashMap<>();
		for (Sentence s : getSentences()) {
			for (Mention m : s.getOrderedMentions()) {
				if (m.getMentionChain() == null) {
					System.err.println("WARNING: no chain for " + m);
				} else
					mentionChains.put(m.getMentionChain().getID(),
							m.getMentionChain());
			}
		}

		List<MentionChain> sorted = new ArrayList<MentionChain>(
				mentionChains.values());
		Collections.sort(sorted, MentionChain.getMentionChainComparator());
		return sorted;
	}

	public void addMentionChain(MentionChain mentionChain) {
		this.mentionChains.put(mentionChain.getID(), mentionChain);
	}

	public void removeMentionChain(MentionChain mc) {
		mentionChains.remove(mc.getID());
		for (Mention m : mc) {
			m.getSentence().removeMention(m);
			mentionChains.remove(mc.getID());
		}
	}
	
	public void dropMentionChain(MentionChain mc) {
		mentionChains.remove(mc.getID());
	}

	public void clearMentionChains() {
		for (MentionChain mc : mentionChains.values()) {
			removeMentionChain(mc);
		}
	}

	public List<Mention> getMentions() {
		List<Mention> r = new ArrayList<>();
		for (Paragraph p : this) {
			r.addAll(p.getMentions());
		}
		return r;
	}

	public List<Sentence> getSentences() {
		List<Sentence> r = new ArrayList<>();
		for (Paragraph p : this) {
			r.addAll(p);
		}
		return r;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Paragraph par : this)
			sb.append(par.toString() + "\n");
		return sb.toString();
	}

	public int compareTo(Text o) {
		return getId().compareTo(o.getId());
	}

	public Text getPairedText() {
		return pairedText;
	}

	public void setPairedText(Text pairedText) {
		this.pairedText = pairedText;
	}

}
