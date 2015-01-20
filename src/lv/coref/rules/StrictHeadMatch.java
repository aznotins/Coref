/*******************************************************************************
 * Copyright 2014,2015 Institute of Mathematics and Computer Science, University of Latvia
 * Author: Artūrs Znotiņš
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
