package lv.coref.rules;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.Mention;

public abstract class Rule {
	
	public String getName() { return "ABSTRASCT RULE"; }
	
	public boolean filter(Mention m, Mention a) {
		return true;
	}
	
	public double score(Mention m, Mention a) {
		return 0.0;
	}
	
	public List<Mention> getPotentialAntecedents(Mention m) {
		return new ArrayList<Mention>();
	}
	
	public Mention getFirst(Mention m) {
		Mention first = null;
		for (Mention a : getPotentialAntecedents(m)) {
			if (filter(m, a) && score(m, a) > 0.5) {
				//System.out.println(getName()+ "FILTER" + m + "\n" + a);
				first = a;
				break;
			}
		}
		return first;
	}

}
