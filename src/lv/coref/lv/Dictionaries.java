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

	public static Dictionary abstractMentions = new Dictionary(false, false);
	
	public static Dictionary commonPersons = new Dictionary(false, true);
	public static Dictionary commonOrganizations = new Dictionary(false, true);
	public static Dictionary commonLocations = new Dictionary(false, true);

	public static Dictionary namedEntities = new Dictionary(true, true);
	
	public static Dictionary exact = new Dictionary(true, true);

	static {
		abstractMentions.readFile("resource/dictionaries/abstract.txt");
		commonPersons.readFile("resource/dictionaries/pers_common.txt");
		commonOrganizations.readFile("resource/dictionaries/org_common.txt");
		commonLocations.readFile("resource/dictionaries/loc_common.txt");
		
		namedEntities.readFile("resource/dictionaries/named_entities.txt");
		namedEntities.readFile("resource/dictionaries/PP_Onomastica_geonames_lem.txt");
		namedEntities.readFile("resource/dictionaries/DB_locations.txt");
	}

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