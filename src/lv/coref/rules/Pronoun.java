package lv.coref.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.Person;
import lv.coref.lv.Constants.PronType;

public class Pronoun extends Rule {

	public String getName() {
		return "PRONOUN";
	}

	public boolean filter(Mention m, Mention a) {

		if (!m.isPronoun() || a.isPronoun())
			return false;
		PronType pronType = m.getPronounType();
		if (pronType == PronType.DEMONSTRATIVE) return false;
		if (pronType == PronType.INDEFINITE) return false;
		if (pronType == PronType.INTERROGATIVE) return false;
		if (pronType == PronType.DEFINITE) return false;
		
		Person person = m.getLastHeadToken().getPerson();
		if (person == Person.FIRST) return false;
		if (person == Person.SECOND) return false;
		
		Set<String> exclude = new HashSet<>();
		
//		exclude.addAll(Arrays.asList("tas", "tā", "viss", "es", "tu", "mēs",
//				"katrs", "kāds", "kāda", "šis", "šī", "šāds", "šāda", "nekāds",
//				"nekāda", "daudzi", "daudzas", "abi", "neviens", "neviena",
//				"katrs", "katra", "pats", "pati", "cits", "cita", "tāds",
//				"tāda"));

		if (exclude.contains(m.getLastHeadToken().getLemma()))
			return false;

		if (m.getSentence() == a.getSentence()) {
			int ms = m.getFirstToken().getPosition();
			int me = m.getLastToken().getPosition();
			int as = a.getFirstToken().getPosition();
			int ae = a.getLastToken().getPosition();
			
			if ((as >= ms && as <= me) || (ms >= as && ms <= ae))
				return false; // intersects, nested
			
			if (m.getParent() != null && m.getParent() == a.getParent())
			return false;
			
			int d = ms - ae;
		}



		return true;
	}

	public double score(Mention m, Mention a) {
		double prob = 1.0;
		if (m.getPronounType() != PronType.POSSESIVE && !m.getGender().equals(a.getGender()))
			prob *= 0.05;
		if (m.getPronounType() != PronType.POSSESIVE && !m.getNumber().equals(a.getNumber()))
			prob *= 0.05;
		if (!m.getCategory().weakEquals(a.getCategory()))
			prob *= 0.05;

		return prob;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 3, 100);
	}

	public static void main(String[] args) {

	}

}
