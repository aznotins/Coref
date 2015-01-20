package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;

public class ExactMatch extends Rule {

	public String getName() {
		return "EXACTMATCH";
	}
	
	public boolean filter(Mention m, Mention a) {
		if (m.isPronoun() || a.isPronoun()) return false;
		if (!m.isProperMention() && !a.isProperMention()) return false;
		//if (m.isAcronym() || a.isAcronym()) return false;
		return true;
	}

	public double score(Mention m, Mention a) {
		// if (m.getLemma().equals(a.getLemma())) return 1.0;

		if (m.getLemmaString().equals(a.getLemmaString()))
			return 1.0;
		return 0.0;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 10, 100);
	}

	public static void main(String[] args) {

	}

}
