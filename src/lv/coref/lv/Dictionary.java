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
package lv.coref.lv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

public class Dictionary {

	// public static final Logger log = Logger.getLogger(KeywordTrie.class);

	private Entry root = new Entry("", null);
	private boolean caseSensitive = true;
	private boolean reversed = false;
	
	public Dictionary(boolean caseSensitive, boolean reversed) {
		this.caseSensitive = caseSensitive;
		this.reversed = reversed;
	}

	public class Entry {
		public String key = null;
		public String category = null;
		public Map<String, Entry> children = new HashMap<>();

		Entry(String key, String category) {
			this.key = key;
			this.category = category;
		}
	}

	public void readFile(String filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String s;
			while (reader.ready()) {
				s = reader.readLine();
				if (s.trim().length() > 0) {
					String[] bits = s.split("\t");
					if (bits.length > 1)
						add(bits[1], bits[0]);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void add(List<String> wordsList, String category) {
		for (String words : wordsList) {
			add(words, category);
		}
	}

	public void add(String words, String category) {
		if (!caseSensitive)
			words = words.toLowerCase();
		String[] path = words.split(" ");
		if (reversed) ArrayUtils.reverse(path);
		Entry cur = root;
		for (String word : path) {
			if (!cur.children.containsKey(word)) {
				cur.children.put(word, new Entry(word, null));
			}
			cur = cur.children.get(word);
		}
		if (cur.category != null && !cur.category.equals(category))
			System.err.println("Overwrite category " + cur.category + " -> "
					+ category + " [" + words + "]");
		cur.category = category;
	}

	public void print() {
		printLevel(root, 0);
	}
	
	public String match(String words) {
		return match(words.split(" "));
	}
	
	public String match(String[] words) {
		Entry cur = root;
		if (reversed) {
			for (int i = words.length - 1; i >= 0; i--) {
				String word = words[i];
				if (!caseSensitive) word = word.toLowerCase();
				if (!cur.children.containsKey(word)) return null;
				cur = cur.children.get(word);
			}
		} else {
			for (String word : words) {
				if (!caseSensitive) word = word.toLowerCase();
				if (!cur.children.containsKey(word)) return null;
				cur = cur.children.get(word);
			}
		}
		return cur.category;
	}
	
	public String matchLongest(String words) {
		return matchLongest(words.split(" "));
	}
	
	public String matchLongest(String[] words) {
		String category = null;
		Entry cur = root;
		if (reversed) {
			for (int i = words.length - 1; i >= 0; i--) {
				String word = words[i];
				if (!caseSensitive) word = word.toLowerCase();
				if (!cur.children.containsKey(word)) break;
				cur = cur.children.get(word);
				if (cur.category != null) category = cur.category;
			}
		} else {
			for (String word : words) {
				if (!caseSensitive) word = word.toLowerCase();
				if (!cur.children.containsKey(word)) break;
				cur = cur.children.get(word);
				if (cur.category != null) category = cur.category;
			}
		}
		return category;
	}

	public void printLevel(Entry n, int level) {
		Entry cur = n;
		for (int i = 0; i < level; i++)
			System.out.print("  ");
		System.out.print(cur.key);
		System.out.print(": ");
		if (cur.category != null)
			System.out.print(cur.category);
		System.out.println();
		for (Entry child : cur.children.values()) {
			printLevel(child, level + 1);
		}
	}

	public static void main(String[] args) {
		Dictionary d = new Dictionary(false, true);
		d.add(Arrays.asList("Latvija Radio", "Radio", "Pasts", "Latvija Pasts",
				"Liepājas Metalurgs", "L N A"), "organization");
		System.err.println(d.match(new String[] {"Latvija", "Pasts"}));
		System.err.println(d.matchLongest(new String[] {"Pasts"}));
		System.err.println(d.matchLongest(new String[] {"Latvija"}));
		d.print();
	}
}