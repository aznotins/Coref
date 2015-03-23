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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Type;

public class Text extends ArrayList<Paragraph> implements Comparable<Text> {
	private final static Logger log = Logger.getLogger(Text.class.getName());

	private static final long serialVersionUID = -5828501273010235785L;
	private String id;
	private Map<String, MentionChain> mentionChains = new HashMap<>();
	private Text pairedText;
	private int nextMentionID = 1;
	private String date;

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
	
	public Token getToken(int absolutePosition) {
		int iTok = 0;
		int iPar = 0;
		int iSent = 0;		
		Paragraph p = this.get(0);
		Sentence s = p.get(0);		
		while (iTok + s.size() <= absolutePosition) {
			iTok += s.size();
			if (iSent < p.size()) s = p.get(++iSent);
			else {
				p = this.get(++iPar);
				iSent = 0;
				s = p.get(iSent);
			}
		}	
		return s.get(absolutePosition - iTok);
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
			if (m.getCategory().equals(Category.time)) continue;
			if (m.getCategory().equals(Category.sum)) continue;
			if (m.getMentionChain().size() < 2 && !m.getType().equals(Type.NE)) {
				removeMentionChain(m.getMentionChain());
			}
		}
	}
	
	public void removeCommonUnknownSingletons() {
		for (Mention m : getMentions()) {
			if (m.isPronoun() && m.getCategory().equals(Category.person)) continue;
			if (m.getMentionChain().size() < 2 && !m.getType().equals(Type.NE) && m.getCategory().equals(Category.unknown)) {
				removeMentionChain(m.getMentionChain());
			}
		}
	}
	
	public void removeDescriptorMentionTokens() {
		for (Mention m : getMentions()) {
			if (m.getCategory().equals(Category.profession)) {
				if (m.getDescriptorMentions() == null) continue;
				m.addComment("remove descriptor mention");
				log.log(Level.INFO, "Remove descriptors {0} from {1}", new Object[]{m.getDescriptorMentions(), m});
				
				List<Token> newTokens = new ArrayList<>();
				for (Token t : m.getTokens()) {
					boolean ok = true;
					for (Mention dm : m.getDescriptorMentions()) {
						if (dm.getTokens() != null && dm.getTokens().contains(t)) {
							ok = false;
							break;
						}
					}
					if (ok) newTokens.add(t);
				}
				m.setTokens(newTokens);				
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
			m.setMentionChain(null);
			if (m.getSentence().getMentionSet().contains(m))
				m.getSentence().removeMention(m);
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
	
	public String getTextString() {
		StringBuilder sb = new StringBuilder();
		for (Paragraph p : this) {
			sb.append(p.getTextString());
			sb.append("\n");
		}
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}
