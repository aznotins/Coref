package lv.coref.lv;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.lv.Constants.Category;
import lv.lumii.expressions.Expression;
import lv.lumii.expressions.Expression.Gender;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.attributes.AttributeNames;

public class AnalyzerUtils {

	private final static Logger log = Logger.getLogger(AnalyzerUtils.class.getName());

	// public static void init() {
	// try {
	// analyzer = new Analyzer("dist/Lexicon.xml", false);
	// analyzer.setCacheSize(1000);
	// } catch (Exception e) {
	// log.log(Level.SEVERE, "Unable to initilize normalizer", e);
	// }
	// }

	public static String normalize(String name, String category) {
		try {
			Analyzer analyzer = Expression.getAnalyzer();
			if (analyzer == null) {
				Expression.initClassifier();
				analyzer = Expression.getAnalyzer();
			}
			analyzer.enableGuessing = true;
			analyzer.enableVocative = true;
			analyzer.guessVerbs = false;
			analyzer.guessParticiples = false;
			analyzer.guessAdjectives = false;
			analyzer.guessInflexibleNouns = true;
			analyzer.enableAllGuesses = true;

			// analyzer.describe(new PrintWriter(System.err));

			Expression expr = new Expression(name, category, false);
			String normalised = expr.normalize();
			analyzer.defaultSettings();
			return normalised;
		} catch (Exception e) {
			log.log(Level.WARNING, "Error normalizing: " + name, e);
		}
		return null;
	}

	public static Map<String, String> inflect(String name, String category) {
		try {
			Analyzer analyzer = Expression.getAnalyzer();
			if (analyzer == null) {
				Expression.initClassifier();
				analyzer = Expression.getAnalyzer();
			}
			analyzer.enableGuessing = true;
			analyzer.enableVocative = true;
			analyzer.guessVerbs = false;
			analyzer.guessParticiples = false;
			analyzer.guessAdjectives = true;
			analyzer.guessInflexibleNouns = true;
			analyzer.enableAllGuesses = true;

			// analyzer.describe(new PrintWriter(System.err));

			// Pieņemam, ka klients padod pamatformu
			Expression e = new Expression(name, category, true);
			// e.describe(new PrintWriter(System.err)); // ko tageris sadomājis

			Map<String, String> inflections = e.getInflections();

			// System.err.println(e.category + " " + e.gender);
			if (e.category.equals(lv.lumii.expressions.Expression.Category.hum)) {
				if (e.gender == Gender.masculine)
					inflections.put(AttributeNames.i_Gender, AttributeNames.v_Masculine);
				if (e.gender == Gender.feminine)
					inflections.put(AttributeNames.i_Gender, AttributeNames.v_Feminine);
			}

			analyzer.defaultSettings();

			return inflections;
		} catch (Exception e) {
			log.log(Level.WARNING, "Error inflecting: " + name, e);
		}
		return null;
	}

	public static void main(String[] args) {
		System.err.println(normalize("Andra Vilka", Category.person.toString()));
		System.err.println(normalize("Andra Ambaiņa", Category.person.toString()));

		System.err.println(inflect("Andris Vilks", Category.person.toString()));
		System.err.println(inflect("Andra Vilks", Category.person.toString()));
		System.err.println(inflect("Andris Ambainis", Category.person.toString()));
	}

}
