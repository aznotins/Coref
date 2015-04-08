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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Person;
import lv.coref.lv.Constants.PronType;

public class Pronoun extends Rule {

	public String getName() {
		return "PRONOUN";
	}

	public boolean filter(Mention m, Mention a) {

		if (!m.isPronoun() || a.isPronoun())
			return false;
		
		String word = m.getLastHeadToken().getWord().toLowerCase();
		String lemma = m.getLastHeadToken().getLemma().toLowerCase();
		
		PronType pronType = m.getPronounType();
		if (pronType == PronType.DEMONSTRATIVE) return false;
		if (pronType == PronType.INDEFINITE) return false;
		if (pronType == PronType.INTERROGATIVE) return false;
		if (pronType == PronType.DEFINITE) return false;
		
		Person person = m.getLastHeadToken().getPerson();
		if (person == Person.FIRST) return false;
		if (person == Person.SECOND) return false;
		
		if (pronType == PronType.DEFINITE) return false;
		if ((lemma.equals("tas") || lemma.equals("tā") || lemma.equals("tie") || lemma.equals("tās"))
				&& (a.getMentionChain().getCategory().equalsEither(Category.person, Category.profession)))
			return false;
		
		Set<String> exclude = new HashSet<>();
		
//		exclude.addAll(Arrays.asList("tas", "tā", "viss", "es", "tu", "mēs",
//				"katrs", "kāds", "kāda", "šis", "šī", "šāds", "šāda", "nekāds",
//				"nekāda", "daudzi", "daudzas", "abi", "neviens", "neviena",
//				"katrs", "katra", "pats", "pati", "cits", "cita", "tāds",
//				"tāda"));

		if (exclude.contains(m.getLastHeadToken().getLemma()))
			return false;

		if (m.getSentence() == a.getSentence()) {
			int ms = m.getFirstToken().getPosition();
			int me = m.getLastToken().getPosition();
			int as = a.getFirstToken().getPosition();
			int ae = a.getLastToken().getPosition();
			
			if ((as >= ms && as <= me) || (ms >= as && ms <= ae))
				return false; // intersects, nested
			
			if (m.getParent() != null && m.getParent() == a.getParent())
			return false;
			
			int d = ms - ae;
			
		}
		if (m.getPronounType().equals(PronType.POSSESIVE) && !a.getMentionChain().isProper()) return false;
		return true;
	}

	public double score(Mention m, Mention a) {
		double prob = 1.0;
		if (!(m.getPronounType().equals(PronType.POSSESIVE) || m.getGender().strictEquals(a.getGender()))) //TODO weakEquals?
			prob *= 0.05;
		if (!(m.getPronounType().equals(PronType.POSSESIVE) || m.getNumber().strictEquals(a.getNumber()))) //TODO weakEquals?
			prob *= 0.05;
		if (m.getPronounType().equals(PronType.PERSONAL) && !m.getCategory().strictEquals(a.getMentionChain().getCategory()))
			prob *= 0.05;
		if ((m.getLemmaString().equals("savs") || m.getLemmaString().equals("sava")) 
				&& !(a.getMentionChain().getCategory().strictEquals(Category.organization) 
						|| a.getMentionChain().getCategory().strictEquals(Category.person))) prob *= 0.05;
		if (!m.getCategory().weakEquals(a.getCategory()))
			prob *= 0.05;

		return prob;
	}

	public List<Mention> getPotentialAntecedents(Mention m) {
		return m.getPotentialAntecedents(-1, 3, 100);
	}

	public static void main(String[] args) {

	}

}
