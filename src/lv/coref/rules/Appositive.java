package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;

public class Appositive extends Rule {
	
	public String getName() { return "APPOSITIVE"; }

	public boolean filter(Mention m, Mention a) {
		if (m.getSentence().getPosition() != a.getSentence().getPosition()) return false;
		int d = m.getFirstToken().getPosition() - a.getLastToken().getPosition();
		if (d <= 0 || d > 1) return false;
		return true;
	}
	
	public double score(Mention m, Mention a) {
		//if (m.getLemma().equals(a.getLemma())) return 1.0;
		if (m.getCase().equals(a.getCase())) return 1.0;
		return 0.0;
	}
	
	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 0, 100);
	}
	
	public static void main(String[] args) {
		
	}

}
