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
import lv.coref.lv.Constants.Number;
import lv.coref.lv.Constants.Type;

public class Appositive extends Rule {

	public String getName() {
		return "APPOSITIVE";
	}

	public boolean filter(Mention m, Mention a) {
		if (m.getType() == Type.PRON || a.getType() == Type.PRON)
			return false;
		if (!m.isProperMention())
			return false;
		if (!m.getCategory().compatible(a.getCategory()))
			return false;
		// labi zinām profesijas un personas: neļaujam iemaldīties citām kategorijām
		if (m.hasCategory(Category.person, Category.profession) && !a.hasCategory(Category.person, Category.profession))
			return false;
		if (!m.hasCategory(Category.person, Category.profession) && a.hasCategory(Category.person, Category.profession))
			return false;
		if (a.isAcronym())
			return false;
		if (m.hasCategory(Category.organization, Category.media) && !a.hasCategory(Category.organization, Category.media))
			return false;
		if (m.getCategory().equals(Category.location) || a.getCategory().equals(Category.location)) return false;
		int d = m.getFirstToken().getPosition()
				- a.getLastToken().getPosition();
		if (d <= 0 || d > 1)
			return false;
		return true;
	}

	public double score(Mention m, Mention a) {
		double prob = 1.0;
		// if (m.getLemma().equals(a.getLemma())) return 1.0;
		if (!m.getCase().weakEquals(a.getCase())) prob = 0;
		if (!m.getGender().weakEquals(a.getGender())) prob *= 0.5;
		if (!m.getNumber().weakEquals(a.getNumber())) prob *= 0.5;
		if (m.getNumber().equals(Number.PL) || a.getNumber().equals(Number.PL)) prob *= 0.05;
		
		
		return prob;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 0, 100);
	}

	public static void main(String[] args) {

	}

}
