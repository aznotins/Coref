package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.PronType;

public class SubClause extends Rule {

	public String getName() {
		return "SUBCLAUSE";
	}

	public boolean filter(Mention m, Mention a) {
		if (!m.isPronoun() || a.isPronoun())
			return false;
		PronType pronType = m.getPronounType();
		if (pronType != PronType.RELATIVE && pronType != PronType.INTERROGATIVE) return false;
		
		int ms = m.getFirstToken().getPosition();
		int me = m.getLastToken().getPosition();
		int as = a.getFirstToken().getPosition();
		int ae = a.getLastToken().getPosition();
		if ((as >= ms && as <= me) || (ms >= as && ms <= ae))
			return false; // intersects, nested

		int d = ms - ae;
		if (d > 3) return false;

		return true;
	}

	public double score(Mention m, Mention a) {
		double prob = 1.0;
		if (!m.getGender().equals(a.getGender()))
			prob *= 0.5;
		if (!m.getNumber().equals(a.getNumber()))
			prob *= 0.5;
		if (!m.getCategory().weakEquals(a.getCategory()))
			prob *= 0.5;

		return prob;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 0, 1);
	}

	public static void main(String[] args) {

	}

}
