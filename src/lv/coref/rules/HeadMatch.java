package lv.coref.rules;

import java.util.List;

import lv.coref.data.Mention;

public class HeadMatch extends Rule {
	
	public String getName() { return "HEADMATCH"; }

	public boolean filter(Mention m, Mention a) {
		return true;
	}
	
	public double score(Mention m, Mention a) {
		//if (m.getLemma().equals(a.getLemma())) return 1.0;
		if (m.getHeadLemmaString().equals(a.getHeadLemmaString())) return 1.0;
		return 0.0;
	}
	
	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 10, 100);
	}
	
	public static void main(String[] args) {
		
	}

}
