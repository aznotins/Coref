package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;

public class AllInOne extends Rule {

	public String getName() {
		return "ALLINONE";
	}

	public boolean filter(Mention m, Mention a) {
		return true;
	}

	public double score(Mention m, Mention a) {
		return 1.0;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, -1, 1);
	}

	public static void main(String[] args) {

	}

}
