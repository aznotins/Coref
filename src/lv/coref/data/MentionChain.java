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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.lv.Constants.Category;
import lv.coref.semantic.Entity;

/**
 * Adding mention, changes mention (sets mentionChain to current)
 * 
 * @author Artūrs
 *
 */
public class MentionChain extends HashSet<Mention> {

	private static final long serialVersionUID = 6267185173283516621L;
	private Mention representative;
	private Mention first;
	private String id;
	private Entity entity;	

	public MentionChain(String id) {
		this.id = id;
	}

	public MentionChain(String id, Mention m) {
		this(id);
		add(m);
		this.id = id;
	}

	public MentionChain(Mention m) {
		this(m.getID(), m);
	}

	// public Text getText() {
	// return text;
	// }
	//
	// public void setText(Text text) {
	// this.text = text;
	// }

	public Category getCategory() {
		if (representative != null) return representative.getCategory();
		if (first != null) return first.getCategory();
		return Category.unknown;
	}

	public Set<String> getProperTokens() {
		Set<String> attr = new HashSet<>();
		for (Mention m : this) {
			attr.addAll(m.getProperTokens());
		}
		return attr;
	}

	public Set<String> getAttributeTokens() {
		Set<String> attr = new HashSet<>();
		for (Mention m : this) {
			attr.addAll(m.getAttributeTokens());
		}
		return attr;
	}

	public boolean weakAgreement(MentionChain o) {
		boolean ok = true;
		Category mcat = getCategory();
		// TODO
		if ((mcat.equals(Category.organization) || o.getCategory().equals(Category.organization))
				&& !mcat.compatible(o.getCategory()))
			return false;
		if (!mcat.weakEquals(o.getCategory()))
			return false;
		Set<String> properTokens = getProperTokens();
		for (String oProperToken : o.getProperTokens()) {
			if (!properTokens.contains(oProperToken))
				return false;
		}
		Set<String> attributeTokens = getAttributeTokens();
		for (String oProperToken : o.getAttributeTokens()) {
			if (!attributeTokens.contains(oProperToken))
				return false;
		}
		return ok;
	}

	public boolean strictAgreement(MentionChain o) {
		boolean ok = true;
		Category mcat = getCategory();
		if (!mcat.weakEquals(o.getCategory()))
			return false;
		Set<String> properTokens = getProperTokens();
		for (String oProperToken : o.getProperTokens()) {
			if (!properTokens.contains(oProperToken))
				return false;
		}
		return ok;
	}

	public boolean isProper() {
		for (Mention m : this) {
			if (m.isProperMention())
				return true;
		}
		return false;
	}

	public boolean add(Mention m) {
		m.setMentionChain(this);
		if (first == null || m.isBefore(first)) {
			first = m;
		}
		if (representative == null || m.isMoreRepresentativeThan(representative)) {
			representative = m;
		}
		return super.add(m);
	}

	public boolean remove(Object o) {
		return super.remove(o);
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

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
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
		s.append("=== Cluster #").append(getID());
		s.append(getRepresentative()).append(" ===\n");

		for (Mention m : this) {
			s.append("  ");
			s.append(m);
			s.append("\t\t").append(m.getSentence().getTextString());
			s.append("\n");
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
