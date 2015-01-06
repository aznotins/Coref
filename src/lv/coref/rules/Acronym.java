package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;

public class Acronym extends Rule {

	public String getName() {
		return "ACRONYM";
	}
	
	public boolean filter(Mention m, Mention a) {
		if(!m.isAcronym()) return false;
		if (m.getFirstToken().getWord().equals("SIA")) return false;
		if (m.getFirstToken().getWord().equals("AS")) return false;
		return true;
	}

	public double score(Mention m, Mention a) {
		if (a.isAcronymOf(m.getFirstToken().getWord())) return 1.0;
		return 0.0;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 10, 100);
	}

	public static void main(String[] args) {

	}

}
