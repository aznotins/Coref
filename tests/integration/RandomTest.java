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
package integration;

import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.coref.io.PipeClient;
import lv.label.Annotation;
import lv.pipe.Pipe;
import lv.util.StringUtils;

public class RandomTest {
	private final static Logger log = Logger.getLogger(RandomTest.class.getName());

	public static boolean USE_PIPE_CLIENT = true;

	public static void main(String[] args) {
		Config.logInit();
		try {
			problems_23032015();

		} catch (Exception e) {
			e.printStackTrace();
		}
		Pipe.close();
		System.exit(0);
	}

	public static void problems_23032015() {
		debug("Turpinot investēt uzņēmuma attīstībā , \" Latvijas Maiznieks \" "
				+ "ieguldījis teju 50 000 latus jaunu produktu izstrādē , kā arī "
				+ "veicis investīcijas ražošanas iekārtās un ievērojami paplašinājis "
				+ "ražošanas telpas , informē AS \" Latvijas Maiznieks \" "
				+ "tirdzniecības un mārketinga vadītājs Artūrs Ilziņš .");
		debug("\" Domina Shopping \" tehniskais vadītājs Kaspars Riters informēja , "
				+ "ka \" Domina Shopping \" katru dienu apseko astoņu cilvēku "
				+ "komanda - tehniskie darbinieki un piesaistīts apakšuzņēmums "
				+ "SIA \" Caverion Latvija \" , pārliecinoties par tirdzniecības "
				+ "centra tehnisko stāvokli un tā drošību .");
		debug("Šodien , 5. jūnijā , notika SIA “ Daugavpils reģionālā slimnīca ” "
				+ "dibinātāju – Daugavpils pilsētas domes , Daugavpils novada domes "
				+ "un Ilūkstes novada domes – pārstāvju sapulce , kurā ar dibinātāju "
				+ "un DRS vadītāja Aivara Zdanovska koplēmumu viņu šajā amatā "
				+ "nomainīja Inta Vaivode , poliklīnikas “ Olvi ” direktore .");
		debug("Uz izmeklēšanas laiku no amata pienākumu pildīšanas atbrīvotais \" "
				+ "Daugavpils siltumtīklu' ' valdes loceklis Māris Laudiņš iecelts "
				+ "par uzņēmuma galveno enerģētiķi , to šorīt intervijā Latvijas "
				+ "Radio apstiprināja Daugavpils pilsētas domes priekšsēdētāja "
				+ "Žanna Kulakova ( RP ) .");
		debug("Cits Jānis Annuss atklāsies galerijas Daugava vadītājas Andas Treijas "
				+ "producētā filmā Projekcijas .");
		debug("KNAB ne apstiprina , ne noliedz , ka šajā lietā figurētu bijusī VID "
				+ "vadītāja Jezdakova .");
		debug("lv \" atklāj biznesa laboratorijas \" eegloo \" ģenerāldirektors "
				+ "Žans Mauris .");
		debug("lv \" atzina maizes un konditorejas izstrādājumu ražotāja AS \" Latvijas "
				+ "maiznieks \" tirdzniecības un mārketinga vadītājs Artūrs Ilziņš .");
		debug("\" Cido grupa \" ģenerāldirektors Marijus Kirstuks stāsta , ka būtiska "
				+ "uzmanība ir pievērsta inovāciju ieviešanai ražošanas procesā , kas "
				+ "ļauj uzņēmumam būt soli priekšā citiem ražotājiem .");
		debug("\" Latvenergo \" kā Nacionālā kapitāla balvas ieguvējs saņems mākslinieka "
				+ "Aivara Vilipsōna veidotu bronzas skulptūru ar uzņēmuma vadītāja Āra "
				+ "Žīgura rokas nospiedumu .");
		debug("J. Zaķa , kurš ievēlēts Latvijas Universitātes rektora amatā , vietu "
				+ "Saeimā varētu ieņemt Dailes teātra aktieris Ivars Kalniņš .");
		debug("Partiju ' Saskaņa ' turpinās vadīt Jānis Urbanovičs");
		debug("\" Ir gandarījums , ka tiekam novērtēti , \" norāda uzņēmuma \" Blind "
				+ "Save \" vadītājs Andis Blinds .");
		debug("8. septembrī aizvadītajās Maskavas mēra vēlēšanās Navaļnijs ieguva 27 % "
				+ "balsu , piekāpjoties līdzšinējam pilsētas vadītājam Sergejam "
				+ "Sobjaņinam .");
		

		debug("Par iekārotāko darba devēju 2012. gadā atzīta AS \"Latvenergo\", "
				+ "secināts personāla atlases uzņēmuma SIA \"WorkingDay Latvia\" "
				+ "aptaujā.");		
		debug("Lielākie uzņēmumi pēc 2010. gadā gūtās peļņas ir SIA \"Latvija "
				+ "Statoil\" (5,36 milj. latu), SIA \"First Data Latvia\" "
				+ "(2,33 milj. latu), AS \"AGROFIRMA TĒRVETE\" (1,37 milj. latu), "
				+ "SIA \"Agrofirma Zelta Druva\" (0,98 milj. latu) un SIA "
				+ "‘ SABIEDRĪBA MĀRUPE \"(0,47 milj. latu).");		
		debug("'' Aldara ' ' produktu portfelī ir aptuveni 40 dažādu dzērienu , "
				+ "kas pārstāv sešas grupas - alu , dzeramo ūdeni , alkoholiskos "
				+ "kokteiļus , sidru , bezalkoholiskos dzērienus un enerģijas "
				+ "dzērienus .");
		debug("\" Grindeks ' ' koncernā ir četri meitasuzņēmumi Latvijā , Igaunijā "
				+ "un Krievijā , kā arī pārstāvniecības 13 valstīs .");
		debug("AS '' Latvijas balzams ' ' tiešā eksporta apgrozījums 2013. gada "
				+ "pirmajā pusgadā bija 3,4 miljoni eiro jeb 2,38 miljoni latu , "
				+ "no kā lielākais pieaugums 41 % apjomā bijis uz Krieviju , "
				+ "liecina uzņēmuma paziņojums \" NASDAQ OMX Riga \" biržā .");
	}

	public static Annotation getAnnotation(String... strings) {
		String stringText = StringUtils.join(strings, "\n");
		Annotation a = null;
		if (USE_PIPE_CLIENT) {
			Text t = PipeClient.getInstance().getText(stringText);
			CorefPipe.getInstance().process(t);
			a = Annotation.makeAnnotationFromText(new Annotation(), t);
		} else {
			a = Pipe.getInstance().process(new Annotation(stringText));
		}
		return a;
	}

	public static Text getText(String... strings) {
		String stringText = StringUtils.join(strings, "\n");
		Text t = null;
		if (USE_PIPE_CLIENT) {
			t = PipeClient.getInstance().getText(stringText);
			CorefPipe.getInstance().process(t);
		} else {
			t = Pipe.getInstance().processText(new Annotation(stringText));
		}
		return t;
	}

	public static Text debug(String... strings) {
		System.err.println("\n =========");
		Text t = getText(strings);
		if (t == null) {
			log.log(Level.SEVERE, "NULL TEXT during debug");
			return null;
		}
		System.err.println(" === " + t.getTextString().trim());
		System.err.println(" === " + t.toString().trim());
		for (Sentence s : t.getSentences()) {
			for (Mention m : s.getOrderedMentions()) {
				System.err.printf("\t\t@%s %s %s\n", m.getMentionChain().getID(), m.getGlobalId() == null ? "" : "#"
						+ m.getGlobalId(), m);
			}
		}
		// Annotation a = Annotation.makeAnnotationFromText(new Annotation(),
		// t);
		// System.err.println(a.toStringPretty());
		return t;
	}

	public static void debug(Text t) {
		System.err.println(t);
		for (Sentence s : t.getSentences()) {
			System.err.println(s.getTextString());
			System.err.println(" " + s);
			for (Mention m : s.getOrderedMentions()) {
				System.err.println(" @" + m.getMentionChain().getID() + " " + m);
			}
		}
	}

}
