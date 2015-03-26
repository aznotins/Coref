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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.util.StringUtils;
import lv.coref.lv.Constants.Category;
import lv.coref.lv.Constants.Person;

public class NELUtils {

	private final static Logger log = Logger.getLogger(NELUtils.class.getName());

	public static final Set<String> commons = new HashSet<String>(Arrays.asList("viņš", "viņs", "viņa", "viņam",
			"viņu", "viņā", "viņas", "viņai", "viņās", "viņi", "viņiem", "viņām", "es", "mēs", "man", "mūs", "mums",
			"tu", "tev", "jūs", "jums", "jūsu", "tas", "tā", "tie", "tās", "tajā", "kas", "kam", "tam", "tām", "ko",
			"to", "tos", "tai", "tiem", "sava", "savu", "savas", "savus", "kurš", "kuru", "kura", "kuram", "kuri",
			"kuras", "kurai", "kuriem", "kurām", "kurā", "kurās", "būs", "arī", "jau", "kā arī", "ka_arī"));

	public static final Set<String> orgCommons = new HashSet<String>(Arrays.asList("uzņēmums", "kompānija", "firma",
			"firmas", "aģentūra", "portāls", "tiesa", "banka", "fonds", "koncerns", "komisija", "partija", "apvienība",
			"frakcija", "birojs", "dome", "organizācija", "augstskola", "studentu sabiedrība", "studija", "žurnāls",
			"sabiedrība", "iestāde", "skola"));

	public static final Set<String> descriptors = new HashSet<String>(Arrays.asList("investori", "cilvēki", "personas",
			"darbinieki", "vadība", "pircēji", "vīrieši", "sievietes", "konkurenti", "latvija iedzīvotāji",
			"savienība biedrs", "skolēni", "studenti", "personība", "viesi", "viesis", "ieguvējs", "klients", "vide",
			"amats", "amati", "domas", "idejas", "vakars", "norma", "elite", "būtisks", "tālākie", "guvēji"));

	public static final Set<String> badNames = new HashSet<String>(Arrays.asList("gads", "gada", "a/s", "sia", "as"));

	public static final Pattern goodSmallWords = Pattern.compile("[A-ZĀČĒĢĪĶĻŅŠŪŽ]+|\\d+$", Pattern.UNICODE_CHARACTER_CLASS);
	
	public static boolean goodName(Category category, String name) {
		if (name.length() <= 2 && !goodSmallWords.matcher(name).matches())
			return false; // tik īsi var būt tikai cipari vai saīsinājumi
		if (commons.contains(name.toLowerCase()))
			return false;
		if (category.equals(Category.organization) && orgCommons.contains(name.toLowerCase()))
			return false;
		if (badNames.contains(name.toLowerCase()))
			return false;
		return true;
		
		// TODO
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
	
	public static DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//	static {
//		TimeZone tz = TimeZone.getTimeZone("UTC");
//		isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
//		isoDateFormat.setTimeZone(tz);
//	}
	
	public static Set<String> days = new HashSet<>(Arrays.asList("šodien", "patlaban", "tagad", "pašlaik", "šonedēļ"));
	
	public static String updateName(String name, String date) {
		if (days.contains(name.toLowerCase())) {
			return isoDateFormat.format(date.split(" ")[0]);
		}
		// TODO
		return null;
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
	
	public static Pattern personName = Pattern.compile("([A-ZĀČĒĢĪĶĻŅŠŪŽ])\\w+ [A-ZČĒĢĪĶĻŅŠŪŽ]\\w+$", Pattern.UNICODE_CHARACTER_CLASS);
	public static Pattern personFirstName = Pattern.compile("([A-ZĀČĒĢĪĶĻŅŠŪŽ])\\w+ ", Pattern.UNICODE_CHARACTER_CLASS);
	
	public static List<String> personAliases(String name) {
		List<String> aliases = new LinkedList<>();
		aliases.add(name);
		if (personName.matcher(name).matches()) {
			String extraAlias = personFirstName.matcher(name).replaceFirst("$1. ");
			aliases.add(extraAlias);
		}
		return aliases;
	}
	
	public static String[][] orgTypes = {
		{"SIA", "Sabiedrība ar ierobežotu atbildību"},
	    {"AS", "A/S", "Akciju sabiedrība"},
	    {"apdrošināšanas AS", "Apdrošināšanas akciju sabiedrība"},
	    {"ZS", "Z/S", "Zemnieka saimniecība"},
	    {"IU", "Individuālais uzņēmums"},
	    {"Zvejnieka saimniecība"},
	    {"UAB"},
	    {"VAS"},
	    {"valsts aģentūra"},
	    {"biedrība"},
	    {"fonds"},
	    {"mednieku biedrība"},
	    {"mednieku klubs"},
	    {"mednieku kolektīvs"},
	    {"kooperatīvā sabiedrība"},
	    {"nodibinājums"},
	    {"komandītsabiedrība"},
	    {"zvērinātu advokātu birojs"},
	    {"advokātu birojs"},
	    {"partija"},
	    {"dzīvokļu īpašnieku kooperatīvā sabiedrība"},
	    {"dzīvokļu īpašnieku biedrība"},
	    {"Pilnsabiedrība", "PS"}
	};
	
	public static Pattern firstLastQuote = Pattern.compile("^\"[^\"]+\"$", Pattern.UNICODE_CHARACTER_CLASS);
	
	// "Kautkas", SIA
	static Pattern[] org_sia;
	static {
		org_sia = new Pattern[orgTypes.length];
		int i = 0;
		for (String[] orgGroup : orgTypes) {
			org_sia[i++] = Pattern.compile(String.format("^\"([\\w\\s\\.,\\-\\'\\+/!:\\(\\)@&]+)\" ?, (%s)$", StringUtils.join(orgGroup, "|")), Pattern.UNICODE_CHARACTER_CLASS); 
		}
	}
	
	// SIA "Kautkas"
	static Pattern[] sia_org;
	static {
		sia_org = new Pattern[orgTypes.length];
		int i = 0;
		for (String[] orgGroup : orgTypes) {
			sia_org[i++] = Pattern.compile(String.format("^^(%s) \" ?([\\w\\s\\.,\\-\\'\\+/!:\\(\\)@&]+) ?\"$", StringUtils.join(orgGroup, "|")), Pattern.UNICODE_CHARACTER_CLASS); 
		}
	}
	
	// 'hardkodētie' nosaukumi kuriem bez standartformas citu aliasu nebūs
	static Pattern orgNames = Pattern.compile(" (partija|pārvalde|dome|iecirknis|aģentūra|augstskola|koledža|vēstniecība|asociācija|apvienība|savienība|centrs|skola|federācija|fonds|institūts|biedrība|teātris|pašvaldība|arodbiedrība|[Šš]ķīrējtiesa)$", Pattern.UNICODE_CHARACTER_CLASS);
	// šādus nevar normāli normalizēt
	static Pattern orgSpecific = Pattern.compile("(filiāle Latvijā|Latvijas filiāle|korporācija|biedrība|krājaizdevu sabiedrība|klubs|kopiena|atbalsta centrs|asociācija)$", Pattern.UNICODE_CHARACTER_CLASS);
	
	public static List<String> orgAliases(String name) {
		Set<String> aliases = new HashSet<>();
		aliases.add(name);
		String representative = name;
		String fixname = fixname(name);
		aliases.add(fixname);
		if (firstLastQuote.matcher(fixname).matches()) {
			fixname = fixname.substring(1, fixname.length()-1);
			aliases.add(fixname);
		}
		if (fixname.contains("vidusskola")) {
			aliases.add(fixname.replaceFirst("vidusskola", "vsk."));
			aliases.add(fixname.replaceFirst("vidusskola", "vsk"));
		}
		
		boolean understood = false;
		for (int i = 0; i < orgTypes.length; i++) {
			String[] orgGroup = orgTypes[i];
			String maintitle = orgGroup[0];
			String clearname = null;
			Pattern p;
			Matcher m;
			
			p = org_sia[i];
			m = p.matcher(fixname);
			if (m.matches()) {
				clearname = m.group(1);
			}
			
			p = sia_org[i];
			m = p.matcher(fixname);
			if (m.matches()) {
				clearname = m.group(2);
			}
			
			if (clearname != null) {
				understood = true;
				representative = String.format("%s %s", maintitle, clearname);
				aliases.add(representative);
				// Visiem uzņēmējdarbības veida variantiem
				for (String title : orgGroup) {
					aliases.add(String.format("%s \"%s\"", title, clearname)); // SIA "Nosaukums"
					aliases.add(String.format("%s %s",     title, clearname)); // SIA Nosaukums
					aliases.add(String.format("%s, %s",    clearname, title)); // Nosaukums, SIA
					aliases.add(String.format("\"%s\" %s", clearname, title)); // "Nosaukums", SIA
					aliases.add(String.format("\"%s\"",    clearname));        // "Nosaukums"
					// TODO - šis ir bīstams!   A/S "Dzintars" pārvērtīsies par Dzintars, kas konfliktēs ar personvārdiem, līdzīgi ļoti daudz firmu kam ir vietvārdi, utml
					// aliases.add(String.format("%s",        clearname));        // Nosaukums 
					
					// modifikācijas ar atstarpēm, kādas liek morfotageris
					aliases.add(String.format("\" %s \" , %s", clearname, title)); // " Nosaukums " , SIA  
	                aliases.add(String.format("%s \" %s \"",   title, clearname)); // SIA " Nosaukums "
	                aliases.add(String.format("\" %s \"",      clearname));        // " Nosaukums "
					
				}
				break; // nemeklējam tālāk
			}
		}
		if (!understood) {
			// 'hardkodētie' nosaukumi kuriem bez standartformas citu aliasu nebūs
			if (!fixname.contains("\"") && orgNames.matcher(fixname).find()) {
				aliases.add(clearOrgName(fixname));
				understood = true;
			}
			// šādus nevar normāli normalizēt
			if (orgSpecific.matcher(fixname).find()) {
				aliases.add(clearOrgName(fixname));
				understood = true;
			}
		}
		
		if (!understood) {
			// ja ir "labs" avots kur itkā vajadzētu būt 100% sakarīgiem nosaukumiem
			log.log(Level.INFO, "Did not understood org name {0}", fixname);
			aliases.add(clearOrgName(fixname));
		}

		List<String> res = new ArrayList<>(aliases.size());
		res.add(representative);
		aliases.remove(representative);
		res.addAll(aliases);
		return res;
	}
	
	public static void main(String[] args) {
		String name = "‘‘SIA «Divi radi»’";
		System.err.println(fixname(name));
		System.err.println(name);

		System.err.println(personAliases("Artis Svece"));
		System.err.println(personAliases("Jānis Bērziņš"));
		System.err.println(personAliases("Ūle Bērziņš"));
		
		System.err.println(orgAliases("«Cirvis»"));
		System.err.println(orgAliases("1. vidusskola"));
		
		System.err.println(orgAliases("\"Cirvis\", SIA"));
		System.err.println(orgAliases("SIA \"Cirvis\""));
		System.err.println(orgAliases("«Cirvja» korporācija"));

		System.err.println(orgAliases("Akciju sabiedrība Cirvis"));
		
//		System.err.println(updateName("šodien", "2015-03-24 15:59:34.250750"));
		
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//
//        Date date;
//        String dateformat = "";
//        try {
//            date = sdf.parse("2012-05-04 00:00:00");
//            sdf.applyPattern("dd-MMM-yyyy");
//            dateformat = sdf.format(date);
//            System.err.println(dateformat);
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
		System.err.println(isoDateFormat.format(new Date()));
	}
}
