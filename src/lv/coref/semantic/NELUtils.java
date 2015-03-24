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
package lv.coref.semantic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.lv.Constants.Category;
import lv.lumii.expressions.Expression;

public class NELUtils {
	
	//public 

	private final static Logger log = Logger.getLogger(NELUtils.class.getName());

	public static final Set<String> commons = new HashSet<String>(Arrays.asList("viņš", "viņs", "viņa", "viņam",
			"viņu", "viņā", "viņas", "viņai", "viņās", "viņi", "viņiem", "viņām", "es", "mēs", "man", "mūs", "mums",
			"tu", "tev", "jūs", "jums", "jūsu", "tas", "tā", "tie", "tās", "tajā", "kas", "kam", "tam", "tām", "ko",
			"to", "tos", "tai", "tiem", "sava", "savu", "savas", "savus", "kurš", "kuru", "kura", "kuram", "kuri",
			"kuras", "kurai", "kuriem", "kurām", "kurā", "kurās", "būs", "arī", "dr.", "jau", "tur"));

	public static final Set<String> orgCommons = new HashSet<String>(Arrays.asList("uzņēmums", "kompānija", "firma",
			"firmas", "aģentūra", "portāls", "tiesa", "banka", "fonds", "koncerns", "komisija", "partija", "apvienība",
			"frakcija", "birojs", "dome", "organizācija", "augstskola", "studentu sabiedrība", "studija", "žurnāls",
			"sabiedrība", "iestāde", "skola"));

	public static final Set<String> descriptors = new HashSet<String>(Arrays.asList("investori", "cilvēki", "personas",
			"darbinieki", "vadība", "pircēji", "vīrieši", "sievietes", "konkurenti", "latvija iedzīvotāji",
			"savienība biedrs", "skolēni", "studenti", "personība", "viesi", "viesis", "ieguvējs", "klients", "vide",
			"amats", "amati", "domas", "idejas", "vakars", "norma", "elite", "būtisks", "tālākie", "guvēji"));

	public static final Set<String> badNames = new HashSet<String>(Arrays.asList("gads", "gada", "a/s", "sia", "as"));

	public static boolean goodName(Category category, String name) {
		// TODO regex small words
		if (commons.contains(name.toLowerCase()))
			return false;
		if (category.equals(Category.organization) && orgCommons.contains(name.toLowerCase()))
			return false;
		if (badNames.contains(name.toLowerCase()))
			return false;
		return true;
	}

	public static final Set<String> aliasHeading = new HashSet<String>(Arrays.asList("izglītība", "karjera"));

	public static final Set<String> aliasGeneral = new HashSet<String>(Arrays.asList("direktors", "deputāts",
			"loceklis", "ministrs", "latvietis", "domnieks", "sociālists", "latvietis", "premjers", "sportists",
			"vietnieks", "premjerministrs", "prezidents", "vīrs", "sieva", "māte", "deputāte"));

	public static final Set<String> aliasBad = new HashSet<String>(Arrays.asList("dome"));

	public static boolean goodAlias(String name, Category category) {
		// TODO filter these when creating Entity
		if (aliasHeading.contains(name.toLowerCase()))
			return false;
		if (aliasGeneral.contains(name.toLowerCase()))
			return false;
		if (aliasBad.contains(name.toLowerCase()))
			return false;
		return true;
	}
	
	public static String fixname(String name) {
		name = name.replaceAll("[«»“”„‟‹›〝〞〟＂]", "\"");
		name = name.replaceAll("[‘’‚`‛]", "'");
		name = name.replaceAll(" /$", "");
		name = name.replaceAll("_", " ");
		return name;
	}

	public static String clearOrgName(String name) {
		String norm = name.replaceAll("[«»“”„‟‹›〝〞〟＂\"‘’‚‛']", "");
		norm = norm.replaceAll("(AS|SIA|A/S|VSIA|VAS|Z/S|Akciju sabiedrība) ", "");
		norm = norm.replaceAll(", (AS|SIA|A/S|VSIA|VAS|Z/S|Akciju sabiedrība)", "");
		norm = norm.replaceAll(" (AS|SIA|A/S|VSIA|VAS|Z/S|Akciju sabiedrība)", "");
		norm = norm.replaceAll("\\s\\s+", " ");
		return norm;
	}
	
	public static void main(String[] args) {
		String name = "‘‘SIA «Divi radi»’";
		System.err.println(fixname(name));
		System.err.println(name);
	}
}
