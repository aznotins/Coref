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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lv.coref.lv.Constants.Category;

public class Dictionaries {

	public static final Set<String> personPronouns = new HashSet<>(Arrays.asList("kurš", "kura", "es", "tu", "viņš",
			"viņa", "mēs", "jūs", "mans", "mana", "tavs", "tava", "mūsu", "jūsu"));

	public static final Set<String> excludeWords = new HashSet<>();

	public static final Set<String> relativeClauseIntroducers = new HashSet<>(Arrays.asList("jo", "ja", "kas", "ka",
			"lai", "vai", "kas", "kurš", "kura", "kurš", "kāds", "kāda", "cik", "kā", "kad", "kur", "tiklīdz", "līdz",
			"kopš"));

	public static final Set<String> demonstrativePronouns = new HashSet<>(Arrays.asList("šis", "šī", "tas", "tā"));

	public static final Set<String> unclearGender = new HashSet<>(Arrays.asList("savs", "sava"));
	
	public static Set<String> orgSubdivisions = new HashSet<>(Arrays.asList("departaments", "birojs", "nodaļa", "daļa"));

	public static Dictionary abstractMentions = new Dictionary(false, false)
			.readFile("resource/dictionaries/abstract.txt");
	
	public static Dictionary commonPersons = new Dictionary(false, true)
			.readFile("resource/dictionaries/pers_common.txt");
	public static Dictionary commonOrganizations = new Dictionary(false, true)
			.readFile("resource/dictionaries/org_common.txt");
	public static Dictionary commonLocations = new Dictionary(false, true)
			.readFile("resource/dictionaries/loc_common.txt");
	
	public static Dictionary namedEntities = new Dictionary(true, true)
			.readFile("resource/dictionaries/named_entities.txt")
			.readFile("resource/dictionaries/PP_Onomastica_geonames_lem.txt")
			.readFile("resource/dictionaries/DB_locations.txt");
	
	public static Dictionary exact = new Dictionary(true, true);
	
	public static Dictionary orgIntroducers = new Dictionary(false, true)
			.addStrings(new String[] { "valsts SIA", "SIA" } , "orgIntroducer");

	public static boolean isDemonstrativePronoun(String s) {
		if (demonstrativePronouns.contains(s))
			return true;
		return false;
	}

	public static Set<String> getRelativeClauseIntroducers() {
		return relativeClauseIntroducers;
	}

	private static void getWordsFromFile(String filename, Set<String> resultSet, boolean lowercase) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			while (reader.ready()) {
				if (lowercase)
					resultSet.add(reader.readLine().toLowerCase());
				else
					resultSet.add(reader.readLine());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Category getCategory(String s) {
		if (personPronouns.contains(s))
			return Category.person;

		String cat = null;

		cat = namedEntities.matchLongest(s);
		if (cat != null)
			return Category.get(cat);

		cat = commonPersons.matchLongest(s);
		if (cat != null)
			return Category.get(cat);

		cat = commonOrganizations.matchLongest(s);
		if (cat != null)
			return Category.get(cat);

		cat = commonLocations.matchLongest(s);
		if (cat != null)
			return Category.get(cat);

		return Category.unknown;
	}

	public static void main(String[] args) {
		System.err.println(getCategory("skolotājs"));
		System.err.println(getCategory("Jānis"));
		System.err.println(getCategory("Pēteris"));
	}

}