package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.Type;

public class Predicate extends Rule {

	public String getName() {
		return "PREDICATE";
	}

	public boolean filter(Mention m, Mention a) {
		if (m.getSentence().getPosition() != a.getSentence().getPosition())
			return false;

		if (m.isPronoun() || a.isPronoun())
			return false;

		if (!m.isProperMention() && !a.isProperMention())
			return false;

		int ms = m.getFirstToken().getPosition();
		int me = m.getLastToken().getPosition();
		int as = a.getFirstToken().getPosition();
		int ae = a.getLastToken().getPosition();
		if ((as >= ms && as <= me) || (ms >= as && ms <= ae))
			return false; // intersects, nested

		int d = ms - ae;

		if ((m.getParent() == null || m.getParent() != a.getParent() || !m
				.getParent().getHead().getLemma().equals("būt"))
				&& (d > 2 || m.getSentence().size() <= ae + 1 || !m
						.getSentence().get(ae + 1).getLemma().equals("būt")))
			return false;

		return true;
	}

	public double score(Mention m, Mention a) {
		double prob = 1.0;
		if (!m.getCase().equals(a.getCase()))
			prob = 0;
		if (!m.getGender().equals(a.getGender()))
			prob *= 0.5;
		if (!m.getNumber().equals(a.getNumber()))
			prob *= 0.5;
		if (!m.getCategory().weakEquals(a.getCategory()))
			prob *= 0.5;

		return prob;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 0, 100);
	}

	public static void main(String[] args) {

	}

}
