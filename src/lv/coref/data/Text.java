package lv.coref.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Text extends ArrayList<Paragraph> implements Comparable<Text> {
	
	private static final long serialVersionUID = -5828501273010235785L;
	private String id;
	private Map<String, MentionChain> mentionChains = new HashMap<>();
	private Text pairedText;
	
	public Text() {}
	
	public Text(String id) {
		this.id = id;
	}
	
	public boolean add(Paragraph p) {
		p.setPosition(this.size());
		p.setText(this);
		return super.add(p);
	}
	
	public String getId() { return id; }
	
	public void setId(String id) { this.id = id; }
	
	public Map<String, MentionChain> finalizeMentionChains() {
		Map<String, MentionChain> mc = new HashMap<>();
		for (Paragraph p : this) {
			for (Sentence s : p) {
				for (Mention m : s.getMentions()) {
					if (m.getMentionChain() == null) {
						System.err.println("WARNING: no chain for " + m);
					} else	mc.put(m.getMentionChain().getID(), m.getMentionChain());
				}
			}
		}
		mentionChains = mc;
		return mc;
	}
	
	public void  removeSingletons() {
		for (Mention m : getMentions()) {
			if (m.getMentionChain().size() < 2) {
				for (Mention mm : m.getMentionChain()) {
					for (Token t : mm.getTokens()) {
						t.removeMention(mm);
					}
					mm.getSentence().removeMention(mm);
				}
				removeMentionChain(m.getMentionChain());
			}
		}
	}
	
	public MentionChain getMentionChain(String id) {
		return mentionChains.get(id);
	}
	
	public List<MentionChain> getMentionChains() {
		List<MentionChain> sorted = new ArrayList<MentionChain>(mentionChains.values());
		Collections.sort(sorted, MentionChain.getMentionChainComparator());
		return sorted;
	}
	
	public void addMentionChain(MentionChain mentionChain) {
		this.mentionChains.put(mentionChain.getID(), mentionChain);
	}
	
	public void removeMentionChain(MentionChain mc) {
		mentionChains.remove(mc);
	}
	
	public void clearMentionChains() {
		for (MentionChain mc : mentionChains.values()) {
			for (Mention m : mc) {
				m.setMentionChain(null);
			}
			mc.clear();
		}
		mentionChains.clear();
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
