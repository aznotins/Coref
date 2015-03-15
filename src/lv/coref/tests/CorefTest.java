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
package lv.coref.tests;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.PipeClient;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;
import lv.coref.util.FileUtils;
import lv.coref.util.StringUtils;

public class CorefTest {
	private final static Logger log = Logger.getLogger(CorefTest.class.getName());

	private static PipeClient pipe;
	
	public static void main(String[] args) {
		Config.logInit();
		appositiveTests();
		mentionTests();
	}
	
	public static void init() {
		pipe = new PipeClient();
	}

	public static void appositiveTests() {
		String ok = "APPOSITIVE";
		String not = "NOT_APPOSITIVE";

		test(ok, "Šodien aģentūrai LETA pastāstīja.");
		test(ok, "Ārsta psihiatra specialitātē Rīgas Stradiņa universitātē.");
		
		
		test(ok, "Veselības ministre Ingrīda Circene.");
		test(ok, "Nacionālā veselības dienesta vadītājs Māris Taube.");
		test(ok, "Nacionālā veselības dienesta (NVD) vadītājs Māris Taube.");
		test(ok, "Brīvdienās skolotājs Jānis Kalniņš mācīja.");
		test(ok, "Uzņēmuma vadītājs un priekšsēdētājs Pēteris Rudzītis.");
		test(ok, "Valsts SIA \"Psihiatrijas Centrs\" uzsāka.");

		test(ok + "_attributeAgreement", "Ministrs Andris Bērziņš. Ministrs Pēteris Kalniņš");

		test(not, "Bijis docents RSU.");
		test(not, "Vadīja lekcijas Latvijas Universitātē.");
	}

	public static void mentionTests() {
		String ok = "MENTION";
		String not = "NOT_MENTION";
		test(ok, "Viņš 1999. gadā absolvēja Latvijas Universitāti.");
		test(ok,
				"Viņš 1999. gadā absolvējis Latvijas Medicīnas akadēmiju , 2002. gadā beidzis"
				+ " rezidentūru ārsta psihiatra specialitātē Rīgas Stradiņa universitātē ( RSU ) , "
				+ "bet 2005. gadā ieguvis medicīnas zinātņu doktora grādu psihiatrijā.");

	}

	public static void tests() {

		// stringTest("Kopš 2001. gada viņš strādājis dažādos amatos valsts SIA \" Psihiatrijas centrs \" ,"
		// +
		// " Garīgās veselības valsts aģentūrā un valsts SIA \" Rīgas Psihiatrijas un narkoloģijas "
		// +
		// "centrs \" , bijis arī docents RSU un vadījis lekcijas Latvijas Universitātē .");
		//
		// stringTest("Kā pastāstīja Veselības ministrijas (VM) preses sekretāre Aija Bukova-Žideļūna, "
		// + "Taubes atlūgums ir saņemts un apstiprināts.");
		//
		// stringTest("Jānis Putniņš un Pēteris Bērziņš dziedāja. Viņi bija pārsteigti.");
		//
		// stringTest("Uzņēmuma vadītājs un galvenais izpildītājs Jānis Putniņš un Pēteris Bērziņš dziedāja. Viņi bija pārsteigti.");
		// stringTest("Finanšu ministrs Jānis Bērziņš un ārlietu ministrs Pēteris Krūmiņš.");

		// System.out.println("ACRONYMS");
		//
		//
		// stringTest("Eiropas Savienība (ES) uzsāka.");
		// stringTest("Šodien skolotājs Jānis Kalniņš.");
		//
		// stringTest("Izglītības un zinātnes ministra amata kandidātes Mārītes Seiles biogrāfija - aģentūras LETA apkopotie dati\n"
		// +
		// "Aģentūra LETA publicē Minitru prezidenta amata kandidātes Laimbodas Straujumas (V) jaunveidojamās valdības izglītības un zinātnes ministra amata kandidātes Mārītes Seiles biogrāfiju.\n"
		// + "Seile dzimusi 1996. gada 26. februārī Preiļos.\n"
		// + "Viņai ir dēls.");
		// stringTest("Valstī ir jauns veselības ministrs, kurš respektē NVD viedokli, tomēr līdz galam nav skaidra turpmākā globālā "
		// +
		// "stratēģija veselības jomā, un es nezinu, vai mani iekļaus nākotnes komandā.");
		//
		// System.out.println("APPOSITIVES");

		//
		// System.out.println("PREDICATES");
		// stringTest("No šodienas Jānis Bērziņš ir valdes priekšsēdētājs, kura atbildība ietver.");
		//
		// System.out.println("PRONOUN");
		// stringTest("Pēteris devās mājup. Viņš bija izsalcis.");
		// stringTest("Pēteris devās uz savu māju.");
		// stringTest("Andris Kalniņš bija galvenais ministra pretinieks. Esošais ministrs uzsāka pret viņu vērstu kampaņu.");
		//
		// stringTest("Andris bija noguris. Pēteris devās pie viņa ciemos.");
		//
		// System.out.println("STRICTHEADMATCH");
		// stringTest("Ministrs Andris Bērziņš. Ministrs Pēteris Kalniņš");
		// stringTest("Jaunais Rīgas teātris uzsāka. Latvijas Nacionālais teātris uzsāka.");

		// stringTest("Kā pastāstīja Veselības ministrijas (VM) preses sekretāre Aija Bukova-Žideļūna, "
		// + "Taubes atlūgums ir saņemts un apstiprināts.");
		//
		// stringTest("Jānis un Pēteris dziedāja.");

		// stringTest("Jānis Kalniņš devās mājup.", "Šodien J.K. devās mājup.",
		// "J. Kalniņš devās mājup.",
		// "Profesors Jānis Kalniņš devās mājup.",
		// "Šodien skolotājs Jānis Kalniņš mācīja ausgtāko matemātiku.");
		//
		// stringTest("Latvija, Rīga un Liepāja iestājās par.",
		// "Jānis un Pēteris devās mājup.",
		// "Uzņēmuma vadītājs un valdes priekšēdētājs Jānis Krūmiņš izteica sašutumu.");
		//
		// stringTest("SIA \"Cirvis\". ");
	}

	public static Text test(String descr, String... strings) {
		log.info("CorefTest test");
		System.err.println("===== " + descr + " TEST ======");
		Text t = solve(strings);
		debug(t);
		System.err.println();
		return t;
	}

	public static Text test(String descr, File file) {
		String fileText = null;
		try {
			fileText = FileUtils.readFile(file.getPath(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Text t = test(descr, fileText);
		// SwingUtilities.invokeLater(new Viewer(t));
		return t;
	}

	public static Text solve(String... strings) {
		if (pipe == null) init();
		String stringText = StringUtils.join(strings, "\n");
		Text t = pipe.getText(stringText);
		new MentionFinder().findMentions(t);
		new Ruler().resolve(t);
		return t;
	}

	public static void debug(Text t) {
		for (Sentence s : t.getSentences()) {
			System.err.println(s.getTextString());
			System.err.println(" " + s);
			for (Mention m : s.getOrderedMentions()) {
				System.err.println(" @" + m.getMentionChain().getID() + " " + m); // +
																					// "\t\t"
																					// +
																					// m.toParamString());
			}
		}
	}

}
