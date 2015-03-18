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
package lv.coref.semantic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.lv.AnalyzerUtils;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Type;

public class Entity {

	private final static Logger log = Logger.getLogger(Entity.class.getName());

	public static boolean SHOW_DISAMBIGUATION = true;

	private String title;

	private Set<String> aliases = new HashSet<>();

	private Category category = Category.unknown;

	// DB ID
	private String id;

	// Unikāls ārējais ID
	private String uid;

	private Mention titleMention;

	private Collection<Mention> mentions;

	public Entity() {
	}

	public Entity(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Set<String> getAliases() {
		return aliases;
	}

	public void addAlias(String alias) {
		aliases.add(alias);
	}

	public int getAliasCount() {
		return aliases.size();
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Mention getTitleMention() {
		return titleMention;
	}

	public void setTitleMention(Mention titleMention) {
		this.titleMention = titleMention;
	}

	public Collection<Mention> getMentions() {
		return mentions;
	}

	public void setMentions(Collection<Mention> mentions) {
		this.mentions = mentions;
	}

	/**
	 * Returns NULL if mentionChain is not representative
	 * 
	 * @param mc
	 * @return
	 */
	public static Entity makeEntity(MentionChain mc) {
		Mention titleMention = mc.getRepresentative();
		// interesējamies tikai par noteiktajām entītijām
		if (!titleMention.getType().equals(Type.NE))
			return null;
		Category cat = mc.getCategory();
		if (cat.isUnkown())
			return null;
		String title = AnalyzerUtils.normalize(titleMention.getString(), cat.toString());
		Entity entity = new Entity(title);
		boolean needNewTitle = false;
		if (!NELUtils.goodName(cat, title)) {
			log.log(Level.WARNING, "Bad title mention {0}", titleMention);
			needNewTitle = true;
		}
		for (Mention m : mc) {
			if (m.isPronoun())
				continue;
			String str = AnalyzerUtils.normalize(m.getString(), cat.toString());
			if (!NELUtils.goodName(cat, str))
				continue;
			entity.addAlias(str);
			if (needNewTitle) {
				// ja nav labs nosaukums, paņemam pirmo atrasto derīgo aliasu
				entity.setTitle(str);
				titleMention = m;
				needNewTitle = false;
			}
		}
		if (entity.getAliasCount() == 0)
			return null;
		entity.setCategory(cat);
		entity.setTitleMention(mc.getRepresentative());
		entity.setMentions(mc);
		return entity;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("{");
		s.append("#").append(id);
		s.append(" title:").append(title);
		s.append(", aliases:").append(aliases);
		s.append(", type:").append(category);
		s.append("}");
		return s.toString();
	}
	
}
