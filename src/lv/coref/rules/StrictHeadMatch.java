package lv.coref.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.Category;

public class StrictHeadMatch extends Rule {
	
	public String getName() { return "STRICTHEADMATCH"; }

	public boolean filter(Mention m, Mention a) {
		if (m.isPronoun() || a.isPronoun()) return false;
		if (!m.getHeadLemmaString().equals(a.getHeadLemmaString())) return false;
		if (m.getCategory().equals(Category.sum)) return false;
		if (m.getCategory().equals(Category.time)) return false;
		if (a.getCategory().equals(Category.sum)) return false;
		if (a.getCategory().equals(Category.time)) return false;
		
		
		Set<String> exclude = new HashSet<>();
		exclude.addAll(Arrays.asList("gads", "lats"));
		if (exclude.contains(m.getLastHeadToken().getLemma()))
			return false;

		if (!a.getMentionChain().isProper()) return false; // test this
		if (!a.getMentionChain().weakAgreement(m.getMentionChain())) return false;
		return true;
	}
	
	public double score(Mention m, Mention a) {
		double prob = 1.0;
		return prob;
	}
	
	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 10, 100);
	}
	
	public static void main(String[] args) {
		
	}

}
