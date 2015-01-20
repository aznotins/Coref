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
			if (filter(m, a) && score(m, a) > 0.49) {
				//System.out.println(getName()+ "FILTER" + m + "\n" + a);
				first = a;
				break;
			}
		}
		return first;
	}

}
