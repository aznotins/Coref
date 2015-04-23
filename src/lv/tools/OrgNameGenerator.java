package lv.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.pipe.Tokenizer;
import edu.stanford.nlp.util.StringUtils;

public class OrgNameGenerator {
	
	private final static Logger log = Logger.getLogger(OrgNameGenerator.class.getName());
	
	public static class NamePattern {
		public static String NAME_PLACEHOLDER = "_NAME_";
		public List<String> patterns = new ArrayList<>();
		public String shortType;
		public String longType;
		public NamePattern(String pattern, String shortType) {
			this(pattern, shortType, null);
		}
		public NamePattern(String pattern, String shortType, String longType) {
			this(Arrays.asList(pattern), shortType, longType);
		}
		
		public NamePattern(String[] patterns, String shortType, String longType) {
			this(Arrays.asList(patterns), shortType, longType);
		}
		
		public NamePattern(List<String> patterns, String shortType, String longType) {
			this.patterns.addAll(patterns);
			this.shortType = shortType;
			this.longType = longType;
		}
		
		public String toString() {
			return String.format("{%s %-40s %s}", shortType, longType, patterns);
		}
	}
	
	public static List<NamePattern> orgPatterns = new ArrayList<>();
	
//	public static String[][] orgTypes = {
//		{"Sabiedrība ar ierobežotu atbildību", "SIA", "sabiedrība ar ierobežotu atbildību"},
//	    {"Akciju sabiedrība", "AS", "A/S", "a/s", "akciju sabiedrība"},
//	    {"Individuālais komersants", "IK", "individuālais komersants"},
//	    {"Individuālais uzņēmums", "IU", "individuālais uzņēmums"},
//	    {"Zemnieka saimniecība", "ZS", "Z/S", "zemnieku saimniecība"},
//	    {"Nodibinājums", "nodibinājums"},	    
//	    {"Kooperatīvā sabiedrība", "kooperatīvā sabiedrība"},
//		{"Pilnsabiedrība", "PS", "pilnsabiedrība"},
//		{"Biedrība", "biedrība"},
//		{"Ārvalsts komersanta filiāle", "ārvalsts komersanta filiāle"},
//		{"Filiāle", "filiāle"},
//		{"Šķīrējtiesa", "sķīrējtiesa"},
//		{"Politiskā partija", "politiskā partija"},
//		{"Ārvalsts komersanta pārstāvniecība", "ārvalsts komersanta pārstāvniecība"},
//		{"Katoļu baznīcas publisko tiesību juridiskā persona", "katoļu baznīcas publisko tiesību juridiskā persona"},
//		{"Pašvaldības uzņēmums", "pašvaldības uzņēmums"},
//		{"Draudze", "draudze"},
//		{"Draudze (jaunā)", "draudze (jaunā)"},
//		{"Paju sabiedrība", "paju sabiedrība"},
//		{"Sabiedriskā organizācija", "sabiedriskā organizācija"},
//		{"Komandītsabiedrība", "komandītsabiedrība"},
//		{"Politisko partiju apvienība", "politisko partiju apvienība"},
//		{"Eiropas komercsabiedrība"},
//		{"Politiska organizācija (partija)", "Politiskā organizācija", "politiskā organizācija"},
//		{"Līgumsabiedrība ar pilnu atbildību", "līgumsabiedrība ar pilnu atbildību"},
//		{"Pārstāvis", "pārstāvis"},
//		{"Zvejnieku saimniecība", "zvejnieku saimniecība"},
//		{"Ārvalsts organizācijas pārstāvniecība", "ārvalsts organizācijas pārstāvniecība"},
//		{"Arodbiedrība", "arodbiedrība"},
//		{"Ģimenes uzņēmums", "ģimenes uzņēmums"},
//		{"Iestāde", "iestāde"},
//		{"Valsts uzņēmums", "VU", "valsts uzņēmums"},
//		{"Kooperatīvo biedrību savienība", "kooperatīvo biedrību savienība"},
//		{"Uzņēmējsabiedrības uzņēmums", "uzņēmējsabiedrības uzņēmums"},
//		{"Kooperatīvo biedrību savienības uzņēmums", "kooperatīvo biedrību savienības uzņēmums"},
//		{"Sabiedriskās organizācijas uzņēmums", "sabiedriskās organizācijas uzņēmums"},
//		{"Kooperatīvo biedrību uzņēmums", "kooperatīvo biedrību uzņēmums"},
//		{"Sabiedrība ar papildu atbildību", "sabiedrība ar papildu atbildību"},
//		{"Reliģiskas organizācijas uzņēmums", "reliģiskas organizācijas uzņēmums"},
//		{"Savienība", "savienība"},
//		{"Baznīca", "baznīca"},
//		{"Misija", "misija"},
//		{"Eiropas ekonomisko interešu grupa"},
//		{"Klosteris", "klosteris"},
//		{"Biedrība (rel.)", "biedrība", "Biedrība"},
//		{"Masu informācijas līdzeklis", "masu informācijas līdzeklis"}	    
//	};
	
	public static String[][] orgTypes = {
		{"sabiedrība ar ierobežotu atbildību", "SIA"},
	    {"akciju sabiedrība", "AS", "A/S"},
	    {"individuālais komersants", "IK"},
	    {"individuālais uzņēmums", "IU"},
	    {"zemnieka saimniecība", "ZS", "Z/S"},
	    {"nodibinājums"},
	    {"kooperatīvā sabiedrība"},
		{"pilnsabiedrība", "PS"},
		{"biedrība"},
		{"ārvalsts komersanta filiāle"},
		{"filiāle"},
		{"šķīrējtiesa"},
		{"politiskā partija"},
		{"ārvalsts komersanta pārstāvniecība"},
		{"pašvaldības uzņēmums"},
		{"paju sabiedrība"},
		{"sabiedriskā organizācija"},
		{"komandītsabiedrība"},
		{"politisko partiju apvienība"},
		{"Eiropas komercsabiedrība"},
		{"politiska organizācija (partija)", "politiskā organizācija", "partija", "politiskā partija"},
		{"līgumsabiedrība ar pilnu atbildību"},
		{"zvejnieku saimniecība"},
		{"ārvalsts organizācijas pārstāvniecība"},
		{"arodbiedrība"},
		{"ģimenes uzņēmums"},
		{"valsts uzņēmums", "VU"},
		{"kooperatīvo biedrību savienība"},
		{"uzņēmējsabiedrības uzņēmums"},
		{"kooperatīvo biedrību savienības uzņēmums"},
		{"sabiedriskās organizācijas uzņēmums"},
		{"sabiedrība ar papildu atbildību"}
	};
	
	public static List<String> getDefaultPatterns(String title) {
		if (title == null) {
			log.log(Level.SEVERE, "NULL title");
		}
		List<String> res = new ArrayList<>();
		res.add(String.format("%s \" %s \"", title,  NamePattern.NAME_PLACEHOLDER));	// SIA "Nosaukums"
		res.add(String.format("%s %s", title,  NamePattern.NAME_PLACEHOLDER));		// SIA Nosaukums
//		not really safe:
//		res.add(String.format("%s , %s", NAME_PLACEHOLDER, title));		// Nosaukums, SIA
//		res.add(String.format("\" %s \" , %s", NAME_PLACEHOLDER, title));	// "Nosaukums", SIA
		res.add(String.format("\" %s \"", NamePattern.NAME_PLACEHOLDER));			// "Nosaukums"
		return res;
	}
	
	static {
		for (int i = 0; i < orgTypes.length; i++) {
			String[] orgGroup = orgTypes[i];
			String mainTitle = orgGroup[0];
			
			List<String> patternStrings = new ArrayList<>();
			for (int iTitle = 0; iTitle < orgGroup.length; iTitle++) {
				String title = orgGroup[iTitle];
				patternStrings.addAll(getDefaultPatterns(title));
			}
			NamePattern np = new NamePattern(patternStrings, null, mainTitle);
//			System.err.println(np);
			orgPatterns.add(np);
		}			
	};
	
	public static List<String> getOrgAliases(String name, String type) {
		List<String> aliases = new ArrayList<>();
		for (NamePattern np : orgPatterns) {
			if (np.longType != null && np.longType.equalsIgnoreCase(type) || np.shortType != null && np.shortType.equalsIgnoreCase(type)) {
				for (String pattern : np.patterns) {
					//if (!isCommonWord(name) || !pattern.equals(String.format("\" %s \"", NamePattern.NAME_PLACEHOLDER)))
					boolean add = true;
					if (isCommonWord(name) && pattern.equals(String.format("\" %s \"", NamePattern.NAME_PLACEHOLDER))) add = false;
					if (add) aliases.add(pattern.replace(NamePattern.NAME_PLACEHOLDER, name));
				}
			}
		}
		return aliases;
	}
	
//	public static Set<String> commonWords = new HashSet<>(Arrays.asList("uz", "no", "ar"));
	public static Set<String> commonWords = new HashSet<>(Arrays.asList(",", ".", "un", "\"", "būt", "par", "ar", "Latvija", "ka", "gads", "tas", ")", "-", "(", "no", "kas", ">", "<", "viņš", "/", "arī", "Ziedonis", "valsts", "kā", ":", "tā", "uz", "nebūt", "bet", "tikt", "Rīga", "ministrs", "td", "varēt", "es", "kurš", "s", "mēs", "darbs", "laiks", "=", "šis", "Jānis", "pēc", "vai", "jau", "a", "saeima", "viss", "l", "lai", "līdz", "?", "sava", "cilvēks", "deputāts", "kura", "vieta", "vide", "gan", "dzejnieks", "ja", "notikt", "šī", "partija", "diena", "amats", "tr", "c", "Imanta", "–", "pasaule", "jo", "savs", "latvietis", "viņa", "tikai", "informācija", "vēl", "kultūra", "valdība", "tauta", "daudz", "priekšsēdētājs", "Eiropa", "jautājums", "pie", "viens", "teātris", "koncerts", "kad", "Balva", "hermanis", "dome", ";", "prezidents", "cits", "—", "jauns", "kā_arī", "projekts", "attīstība", "jauna", "i", "tad", "kļūt", "pats", "ministrija", "taču", "«", "»", "padome", "!", "piedalīties", "n", "pašvaldība", "saņemt", "ļoti", "mūzika", "izglītība", "kāds", "liela", "vārds", "vadītājs", "krēmers", "imants", "kopā", "šodien", "deklarācija", "ne", "norādīt", "labs", "liels", "fonds", "savukārt", "iespēja", "informēt", "sabiedrība", "plkst.", "aģentūra", "uzskatīt", "dzīve", "visa", "strādāt", "teikt", "pirmais", "viena", "rezultāts", "pret", "komisija", "atzīt", "tapt", "pasākums", "Andris", "centrs", "grāmata", "gidons", "maijs", "spēle", "grupa", "pirms", "radīt", "piešķirt", "savienība", "rodins", "1.", "kur", "augsta", "programma", "Juris", "trīs", "čempionāts", "pirmā", "Raimonds", "frakcija", "sākt", "lats", "emsis", "izrāde", "veltīt", "sports", "vējonis", "sagatavot", "cita", "g", "sadarbība", "tik", "sacīt", "Marts", "nauda", "pilsēta", "...", "nevarēt", "tomēr", "pārstāvis", "tāds", "urbanovičs", "tiesa", "iespējams", "domāt", "posms", "vadīt", "jūs", "darīt", "autors", "zināt", "divi", "veidot", "aizsardzība", "sudrabs", "dziesma", "Krievija", "zinātne", "ceļš", "laba", "daļa", "likums", "sacensība", "iegūt", "darbība", "saskaņa", "pat", "labi", "doma", "vulfsons", "Aivars", "valoda", "aicināt", "režisors", "bērns", "veids", "tāpēc", "sevis", "veikt", "paust", "miņins", "i.", "nākt", "v", "māja", "ievēlēt", "dzeja", "Alvis", "iesniedzējs", "māksla", "kandidāts", "bez", "Indulis", "baltica", "paredzēt", "viedoklis", "loceklis", "gods", "festivāls", "ziņot", "runāt", "lēmums", "3.", "kāda", "atbalsts", "sportists", "sezona", "summa", "otrā", "izcīnīt", "tagad", "nacionāla", "izcils", "pasaka", "kremerata", "o", "valde", "Jan", "dot", "uzņēmums", "Ieva", "a.", "d", "tele", "palikt", "mūziķis", "TeLeGr", "sēde", "lieta", "piemiņa", "spēks", "spēlēt", "attiecības", "tu", "aiziet", "politiska", "pieņemt", "nekā", "redzēt", "prese", "piemērs", "ščerbatihs", "uzsvērt", "p", "līdzeklis", "biedrs", "Ls", "problēma", "Mar", "līgums", "darbinieks", "direktors", "pa", "kaut", "situācija", "universitāte", "ekipāža", "zeme", "politika", "jubileja", "orķestris", "Askolds", "sabiedriska", "2013", "tur", "t", "reģionāla", "satversme", "satiksme", "lv", "atklāt", "skola", "Matīss", "tāda", "reize", "īpašums", "apvienība", "šogad", "šmēdiņš", "olimpiska", "muzejs", "6.", "laikraksts", "februāris", "baltija", "šāds", "turpināt", "politiķis", "mēnesis", "rakstīt", "2.", "vēlēšana", "DPS", "tikties", "kauss", "tieši", "organizācija", "sekretārs", "persona", "apstiprināt", "vadība", "novads", "nams", "plānot", "nodaļa", "ES", "Viktors", "SIA", "konference", "katra", "ziņa", "starptautiska", "filma", "dalībnieks", " ", "turlais", "komanda", "katrs", "izmantot", "tiesības", "skaits", "ekonomika", "20", "iedzīvotājs", "pienākums", "politisks", "ģimene", "tikšanās", "ienākums", "Dainis", "izveidot", "gadījums", "daudzi", "tukums", "mākslinieks", "interese", "šobrīd", "dzīvot", "žurnālists", "viegli", "starp", "galvens", "brīdis", "albums", "Feb", "2", "vietnieks", "vajadzēt", "puse", "cik", "paiet", "ieņemt", "intervija", "braukt", "augstskola", "%", "Vācija", "sniegt", "policija", "ordenis", "mērķis", "neviens", "liepāja", "10", "m", "saistīt", "republika", "premjers", "h", "aprīlis", "amatpersona", "zvaigzne", "kopš", "atbalstīt", "iela", "5.", "vēsture", "vairāki", "iekšlietas", "krievs", "komponists", "septembris", "pēdējs", "nevis", "gribēt", "biedrība", "akadēmija", "2010", "01", "rakstnieks", "saruna", "lnnk", "vienotība", "novembris", "aktieris", "plāns", "mūžība", "koris", "zaķis", "nosaukums", "medaļa", "13", "vēlēties", "varbūt", "stāstīt", "nekas", "iepriekš", "dienests", "zalāns", "pārvalde", "izstāde", "vērtēt", "tāpat", "publicists", "vidusskola", "jūnijs", "dzimt", "dažāds", "Edgars", "ņemt", "trešā", "papildu", "nodrošināt", "dēļ", "vienmēr", "May", "saprast", "nedēļa", "2011", "sākties", "ne_tikai", "mana", "skolotājs", "process", "Māris", "konkurss", "celmiņš", "punkts", "iet", "sieviete", "akuratere", "vasiļevskis", "turklāt", "paziņot", "pasniegt", "nākotne", "lasīt", "gūt", "TSP", "Lietuva", "kabinets", "ap", "sākums", "piedāvāt", "'", "izvirzīt", "vakars", "pati", "ideja", "darboties", "augt", "j.", "9.", "literatūra", "epifānija", "alga", "w", "studija", "divas", "nodoklis", "8.", "mavriks", "dambis", "brauciens", "sistēma", "raidījums", "raksts", "kāpēc", "4.", "vairs", "pludmale", "Maskava", "nosaukt", "draugs", "nu", "iesniegt", "7.", "četri", "g.", "dzejolis", "doties", "sasniegt", "janvāris", "ieguldījums", "2010.", "15", "lietus", "kuldīga", "iekļaut", "zāle", "vien", "11.", "Inguns", "Apr", "brīvība", "vadītāja", "liecināt", "apliecināt", "vērtība", "beigt", "treneris", "rīcība", "budžets", "atrasties", "akcija", "čempions", "piederēt", "īpaši", "vadims", "televīzija", "personība", "notikums", "uzvarēt", "kilograms", "baznīca", "Sep", "decembris", "radio", "minēt", "balss", "atrast", "šāda", "bet_arī", "zaļa", "drošība", "LC", "metrs", "doms", "banka", "ari", "stunda", "birojs", "2009", "augsts", "12", "reģions", "freimanis", "vecums", "bibliotēka", "vidus", "vasara", "beigas", "vācietis", "ieraksts", "daba", "automašīna", "klase", "gaidīt", "TP", "TB", "līmenis", "0", "AIP", "netikt", "likt", "joma", "Dzintars", "pārstāvēt", "ietvars", "Nov", "spēt", "atbildēt", "Augusts", "ASV", "2012", "izlase", "e-pasts", "starptautisks", "skanēt", "izteikt", "vijolnieks", "pieredze", "1997", "ventspils", "zelts", "10.", "vētra", "sekunde", "priekšsēdētāja", "pildīt", "kārtība", "27.", "1", "ūdens", "rajons", "oktobris", "koalīcija", "izdarīt", "gaita", "20.", "biļete", "saraksts", "ierasties", "finanses", "atkal", "”", "uzvara", "bobsleja", "e", "Maija", "veidošana", "vecs", "tālr.", "pašlaik", "Jul", "vēli", "numurs", "nolemt", "foto", "izdot", "1996", "sakars", "cīņa", "Jun", "+", "rīkot", "jaunietis", "rektors", "roka", "oct", "neatkarība", "4", "2007.", "pārsniegt", "kamēr", "Valdis", "17", "14", "mūžs", "1970", "vizīte", "2000", "portāls", "kārta", "dzejnieka", "SC", "valdmanis", "tādējādi", "pamats", "balsot", "28", "2004", "veselība", "publicēt", "īstenot", "vērā", "stāsts", "reforma", "līderis", "jūra", "2008.", "ēka", "vēstule", "uzvarētājs", "volejbols", "skatītājs", "nozare", "nacionāls", "ikviens", "vēlēšanās", "tirgus", "jā", "departaments", "2004.", "četrinieks", "zaudēt", "r", "nekad", "endziņš", "opera", "mans", "lauks", "atzīmēt", "LZP", "ļaut", "uzņēmējs", "turnīrs", "prast", "eksperts", "Gunārs", "3", "*", "priekšnieks", "priekšlikums", "dažāda", "šķēle", "turpmāk", "sastāvs", "mērs", "telpa", "100", "noteikt", "kontroliere", "šmēdiņs", "v.", "tēls", "laukums", "izdoties", "institūcija", "akmens", "25.", "2007", "19", "rasties", "tuvs", "tiesnesis", "prasība", "komiteja", "zīme", "uzdevums", "r.", "mārtiņš", "krēmera", "federācija", "atvadīties", "15.", "uzmanība", "nepieciešams", "literārs", "iestāde", "atcerēties", "ozols", "novērtēt", "mežs", "investīcija", "grūti", "1997.", "skatuve", "parlaments", "otrs", "otrais", "kvalitāte", "abi", "JL", "tādēļ", "saistība", "pateikt", "noteikums", "dzirdēt", "auto", "aleksandrs", "uzsākt", "startēt", "gandrīz", "uzstāties", "pakalpojums", "maza", "1998.", "vairākas", "te", "pagasts", "abas", "18.", "īpašs", "vara", "pļaviņš", "pastāstīt", "krājums", "institūts", "duets", "2002.", "rīts", "pārrunāt", "pāris", "galvena", "dzimšana", "Andrejs", "vēstnieks", "skaņdarbs", "organizēt", "kontrole", "avīze", "atzinība", "Dec", "sasniegums", "pozīcija", "būvniecība", "2009.", "table", "saukt", "internets", "runa", "mazs", "apmērs", "30", "profesors", "diriģents", "dažs", "censties", "28.", "25", "17.", "ārlietas", "sudraba", "iemesls", "fakultāte", "ceremonija", "bieži", "atgriezties", "aizvadīt", "ministre", "klubs", "12.", "ārvalsts", "pērn", "nafta", "joprojām", "apmeklēt", "šķēps", "trase", "atbildība", "24", "papildināt", "mācība", "miljons", "atmoda", "atkārtot", "14.", "“", "meklēt", "bizness", "22", "palīdzēt", "asociācija", "īstenošana", "·", "vismaz", "sieva", "prēmija", "mēģināt", "krāsaina", "bērziņš", "atvērt", "16.", "vienkārši", "veicināt", "kopums", "izsacīt", "18", "tāli", "svētki", "apbalvot", "īpaša", "pēdēja", "proza", "panākums", "ekonomiska", "atklāšana", "2013.", "prezidente", "kamerorķestris", "šeit", "sigulda", "sekot", "robeža", "pāri", "pilots", "pieci", "mīlestība", "kongress", "celtniecība", "svars", "francija", "bronza", "rast", "pulksten", "lasījums", "starts", "jeb", "emšs", "13.", "tēvs", "pārliecināt", "pateikties", "parakstīt", "nonākt", "nekustams", "izdevniecība", "iespējama", "2008", "students", "princips", "mēģinājums", "iestudēt", "brālis", "apgalvot", "Kaspars", "nepieciešama", "desmit", "aktīvs", "svarīgs", "pavasaris", "jūrmala", "izstrādāt", "iepriekšējs", "fakts", "panākt", "nezināt", "mainīt", "atgādināt", "2011.", "spēja", "skaidrs", "prieks", "pieminēt", "nolikt", "krīze", "izcīņa", "diskusija", "attieksme", "valdīt", "trešdien", "speciālists", "populārs", "pilsonis", "nē", "krāsa", "komentēt", "karš", "gadsimts", "vakar", "treniņš", "tradīcija", "sociāla", "sirds", "pieļaut", "piebilst", "patikt", "iecerēt", "1998", "16", "„", "šķist", "zemnieks", "sorokins", "priekša", "patiesība", "nozīmīgs", "noteikta", "klausītājs", "apstāklis", "Inguna", "vēlētājs", "teksts", "padomnieks", "medijs", "cerība", "transportlīdzeklis", "svarcēlājs", "paziņojums", "mačs", "ieguvējs", "dati", "cieņa", "2003.", "uzticība", "svarcelšana", "pagaidām", "mašīna", "valdījums", "meistars", "m.", "laikam", "grozījums", "cerēt", "zieds", "zaļš", "paaudze", "nakts", "lācis", "kandidatūra", "atstāt"));
	public static boolean isCommonWord(String s) {
		boolean filter = false;
		if (s.matches("^\\d+$")) filter = true;
		if (commonWords.contains(s.toLowerCase())) filter = true;
		if (filter) System.err.println("Filter out simple patterns for " + s);
		return filter;
	}
	
	public static void orgRegisterToNerList(String infile, String outfile, int limit) {
		try {
			//BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(infile), "utf8"));
			BufferedReader in = new BufferedReader(new FileReader(infile));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outfile), "utf8"));
			int counter = 0;
			boolean header = true;
			boolean firstOutLine = true;
			for (String line = null; (line = in.readLine()) != null; ) {
				if (header) {
					header = false;
					continue;
				}
				String[] bits = line.split("\t");
				if (bits.length > 4) {
					if (limit >= 0 && counter++ >= limit)
						break;
					String name = bits[2];
					String name_in_quotes = bits[4];
					String type_text = bits[10];
					String type = bits[9];
					String source = bits[8];
					
					if (source.equals("MIL reģistrs")) {
						// masu informācijas līdzeklis - ļoti netīrs
						continue;
					}
					if (!source.equals("Komercreģistrs")) {
						// biedrību reģistrs un uzņēmumu reģistrs dažkārt var radīt problēmas
						continue;
					}
					
					
					List<String> aliases = new ArrayList<>();
					if (name_in_quotes != null && !name_in_quotes.equals("")) {
						aliases.addAll(getOrgAliases(name_in_quotes, type_text));
					}
//					System.err.println(name + "            " + name_in_quotes);
//					System.err.println(type_text);
					if (aliases.size() == 0) {
						aliases.add(StringUtils.join(Tokenizer.getInstance().tokenize(name), " "));
//						System.err.println(name + "            " + name_in_quotes);
					}
					
					for (String alias : aliases) {
						if (!firstOutLine) out.println();
						else firstOutLine = false;
						out.printf("organization\t%s", alias);
//						System.err.println(alias);
					}
				}
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// for (NamePattern np : orgPatterns) {
		// System.err.println(np);
		// }

		// System.err.println(getOrgAliases("Cirvis",
		// "Sabiedrība ar ierobežotu atbildību"));
		// orgRegisterToNerList("D:/work/data/ur/register.tab", "D:/work/data/ur/UR_nocase.txt", -1);
		orgRegisterToNerList("D:/work/data/ur/register.tab", "D:/work/Coref/Gazetteer/UR_nocase.txt", -1);
	}
}

