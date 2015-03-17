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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Sentence;
import lv.coref.lv.Constants.Category;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CDCBags {

	private final static Logger log = Logger.getLogger(CDCBags.class.getName());

	public Bag mentionBag = new Bag();
	public Bag nameBag = new Bag();
	public Bag contextBag = new Bag();

	public CDCBags() {
	}

	@SuppressWarnings("unchecked")
	public CDCBags(String jsonString) {
		JSONObject json = (JSONObject) JSONValue.parse(jsonString);
		if (json.containsKey("mentionbag"))
			mentionBag.putAll((Map<String, Long>) json.get("mentionbag"));
		if (json.containsKey("namebag"))
			nameBag.putAll((Map<String, Long>) json.get("namebag"));
		if (json.containsKey("contextbag"))
			contextBag.putAll((Map<String, Long>) json.get("contextbag"));
	}

	public static Bag makeNameBag(Entity entity) {
		Bag bag = new Bag();
		for (String alias : entity.getAliases()) {
			bag.add(alias.split("\\s+"));
		}
		for (String titleWord : entity.getTitle().split("\\s+"))
			bag.put(titleWord, 0l);
		return bag;
	}

	public static Bag makeMentionBag(Collection<Entity> entities) {
		Bag bag = new Bag();
		for (Entity entity : entities) {
			Category cat = entity.getCategory();
			if (cat.equals(Category.person) || cat.equals(Category.organization) || cat.equals(Category.location)) {
				// bag.add(entity.getUid());
				bag.add(entity.getTitle()); // only for debug
			}
		}
		return bag;
	}

	public static Bag makeMentionBagFromMentions(List<MentionChain> clusters) {
		List<Entity> entities = new ArrayList<>();
		for (MentionChain mc : clusters) {
			Entity e = Entity.makeEntity(mc);
			if (e != null)
				entities.add(e);
		}
		return makeMentionBag(entities);
	}

	private static final int CONTEXT = 5;

	public static Bag makeContextBag(Entity entity) {
		Bag bag = new Bag();
		Mention titleMention = entity.getTitleMention();
		if (titleMention == null) {
			log.warning(String.format("NULL titleMention for %s", entity));
			return bag;
		}
		MentionChain mc = titleMention.getMentionChain();
		if (mc == null) {
			log.warning(String.format("NULL mention chain for %s", entity));
			return bag;
		}
		for (Mention m : mc) {
			Sentence s = m.getSentence();
			int cStart = Math.max(0, m.getHeads().get(0).getPosition() - CONTEXT);
			int cEnd = Math.min(s.size() - 1, m.getHeads().get(0).getPosition() + CONTEXT);
			for (int i = cStart; i <= cEnd; i++) {
				bag.add(s.get(i).getLemma());
			}
		}
		return bag;
	}

	public static double cosineSimilarity(Bag a, Bag b) {
		long numerator = 0;
		for (String w : a.keySet()) {
			if (!b.containsKey(w))
				continue;
			numerator += a.get(w) * b.get(w);
		}
		int sumA = 0, sumB = 0;
		for (long x : a.values())
			sumA += x * x;
		for (long x : b.values())
			sumB += x * x;
		double denominator = Math.sqrt(sumA) * Math.sqrt(sumB);
		if (denominator == 0)
			return 0.0;
		else
			return numerator / denominator;
	}

	public static double cosineSimilarity(CDCBags a, CDCBags b) {
		double res = 0;
		res += cosineSimilarity(a.nameBag, b.nameBag);
		res += cosineSimilarity(a.mentionBag, b.mentionBag);
		res += cosineSimilarity(a.contextBag, b.contextBag);
		return res;
	}

	public String toString() {
		return String.format("{mentionbag=%s namebag=%s contextbag=%s}", mentionBag, nameBag, contextBag);
	}

}

class Bag extends HashMap<String, Long> {

	private static final long serialVersionUID = 1L;

	public void add(String[] words) {
		for (String word : words) {
			if (word.length() == 0)
				continue;
			add(word);
		}
	}

	public void add(String word) {
		if (!containsKey(word))
			put(word, 1l);
		else
			put(word, get(word) + 1);
	}

	public void set(String word, long value) {
		put(word, value);
	}
}
