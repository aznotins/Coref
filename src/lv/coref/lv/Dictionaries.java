package lv.coref.lv;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lv.coref.data.MentionCategory;

public class Dictionaries {

	public static final Set<String> personPronouns = new HashSet<>(
			Arrays.asList("kurš", "kura", "es", "tu", "viņš", "viņa", "mēs",
					"jūs", "savs", "sava"));

	public static final Set<String> excludeWords = new HashSet<>();

	public static final Set<String> relativeClauseIntroducers = new HashSet<>(
			Arrays.asList("jo", "ja", "kas", "ka", "lai", "vai", "kas", "kurš",
					"kura", "kurš", "kāds", "kāda", "cik", "kā", "kad", "kur",
					"tiklīdz", "līdz", "kopš"));

	public static final Set<String> unclearGender = new HashSet<>(
			Arrays.asList("savs", "sava"));

	static {
		
	}
	
	public static Set<String> getRelativeClauseIntroducers() {
		return relativeClauseIntroducers;
	}

	private static void getWordsFromFile(String filename,
			Set<String> resultSet, boolean lowercase) throws IOException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
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

	public static String getCategory(String s) {
		if (personPronouns.contains(s)) return MentionCategory.Category.person.toString();
		return MentionCategory.UNKNOWN;
	}

}