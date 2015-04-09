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

import java.util.List;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.Category;

public class Acronym extends Rule {

	public String getName() {
		return "ACRONYM";
	}
	
	public boolean filter(Mention m, Mention a) {
		if(!m.isAcronym()) return false;
		if (!a.isProperMention()) return false;
		if (!a.hasCategory(Category.unknown, Category.organization, Category.media))
			return false;
		if (m.getFirstToken().getWord().equals("SIA")) return false;
		if (m.getFirstToken().getWord().equals("AS")) return false;
		return true;
	}

	public double score(Mention m, Mention a) {
		if (a.isAcronymOf(m.getFirstToken().getWord())) return 1.0;
		return 0.0;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 10, 100);
	}

	public static void main(String[] args) {

	}

}
