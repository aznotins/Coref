package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.Type;

public class Appositive extends Rule {

	public String getName() {
		return "APPOSITIVE";
	}

	public boolean filter(Mention m, Mention a) {
		if (m.getType() == Type.PRON || a.getType() == Type.PRON)
			return false;

		if (m.getType() != Type.NE && a.getType() != Type.NE)
			return false;

		if (m.getSentence().getPosition() != a.getSentence().getPosition())
			return false;
		int d = m.getFirstToken().getPosition()
				- a.getLastToken().getPosition();
		if (d <= 0 || d > 2)
			return false;
		return true;
	}

	public double score(Mention m, Mention a) {
		double prob = 1.0;
		// if (m.getLemma().equals(a.getLemma())) return 1.0;
		if (!m.getCase().equals(a.getCase())) prob = 0;
		if (!m.getGender().equals(a.getGender())) prob *= 0.5;
		if (!m.getNumber().equals(a.getNumber())) prob *= 0.5;
		
		
		return prob;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 0, 100);
	}

	public static void main(String[] args) {

	}

}
