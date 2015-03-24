package integration;

import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.coref.io.PipeClient;
import lv.label.Annotation;
import lv.pipe.Coref;
import lv.pipe.Pipe;
import lv.util.StringUtils;

public class MentionTest {
	
	public static boolean USE_PIPE_CLIENT = true;

	public static void main(String[] args) {
		Config.logInit();
		Config.getInstance().set(Config.PROP_NEL_SHOW_DISAMBIGUATION, "false");

		testMention("Šodien biroja vadītājs Pēteris Kalniņš.", "biroja vadītājs", "profession");
		testMention("Šodien Ķīnas sekretāre Inga Liepiņa.", "sekretāre", "profession");
		testMention("Šodien Ķīnas preses sekretāre Inga Liepiņa.", "preses sekretāre", "profession");
		testMention("Šodien preses sekretāre sociālajos jautājumos Inga Liepiņa.", "preses sekretāre", "profession");

		testMention("Šodien veselības ministrs Jānis Bērziņš.", "veselības ministrs", "profession");

		testMention("Šodien Gudrinieku biroja vadītājs Pēteris Kalniņš.", "vadītājs", "profession");
		testMention("Šodien biroja vadītājs Pēteris Kalniņš.", "biroja vadītājs", "profession");

		testMentions("Rīgas reliģiskās draudzes vadītājs Jānis Bērziņš.",
				new MP("Rīgas reliģiskās draudzes", "organization"),
				new MP("vadītājs", "profession"));
		
		testMentions("Šodien reliģiskās draudzes vadītājs Jānis Bērziņš.",
				new MP("reliģiskās draudzes", "organization"),
				new MP("vadītājs", "profession"));
		
		testMentions("Draudzes vadītājs Jānis Bērziņš.",
				new MP("Draudzes vadītājs", "profession"));
		
		testMentions("Rīgas domes Satiksmes departamenta vadītājs Jānis Bērziņš.",
				new MP("Rīgas domes", "organization"),
				new MP("Satiksmes departamenta vadītājs", "profession"),
				new MP("Jānis Bērziņš", "person"));

		testMentions("Vides aizsardzības un reģionālās attīstības ministre Līga Liepiņa.",
				new MP("Vides aizsardzības un reģionālās attīstības ministre", "profession"));

		testMentions("Tieslietu ministrijas Sabiedrisko attiecību nodaļas vadītāja Jana Saulīte",
				new MP("Tieslietu ministrijas", "organization"),
				new MP("Sabiedrisko attiecību nodaļas vadītāja", "profession"));
		
		testMentions("Informēja Valsts kancelejas Preses departamenta vadītājs Aivis Freidenfelds.",
				new MP("Valsts kancelejas", "organization"),
				new MP("Preses departamenta vadītājs", "profession"));
		
		testMentions("Eiropas integrācijas biroja vadītāju Edvardu Kušneru .",
				new MP("Eiropas integrācijas biroja", "organization"),
				new MP("vadītāju", "profession"));
		
		testMentions("Stradiņa slimnīcas Invazīvās un neatliekamās kardioloģijas nodaļas vadītājs Andrejs Ērglis",
				new MP("Stradiņa slimnīcas", "organization"),
				new MP("Invazīvās un neatliekamās kardioloģijas nodaļas vadītājs", "profession"));
		
		testMentions("Šodien biroja vadītājs Edvards Kušners.",
				new MP("biroja vadītājs", "profession"));
		
		Pipe.close();
		System.exit(0);
	}

	public static boolean testMention(String text, String mentionString, String type) {
		return testMentions(text, new MP(mentionString, type));
	}

	public static boolean testMention(Annotation doc, MP mp) {
		Annotation a = doc.getMention(mp.mentionString, mp.type, mp.par, mp.sent, mp.tok);
		if (a != null) {
			System.err.printf("+ \"%s\" (%s)\n", mp.mentionString, mp.type);
			return true;
		} else {
			System.err.printf("@ \"%s\" (%s)\n", mp.mentionString, mp.type);
			return false;
		}
	}

	public static boolean testMentions(String text, MP... mentionPlaces) {
		Annotation doc = getAnnotation(text);
		return testMentions(doc, mentionPlaces);
	}

	public static boolean testMentions(Annotation doc, MP... mentionPlaces) {
		System.err.printf("\n==== %s \n", doc.getText().trim());
		System.err.printf("==== %s \n", getFormattedTextString(doc));

		boolean ok = true;
		for (MP mp : mentionPlaces) {
			ok &= testMention(doc, mp);
		}
		return ok;
	}

	public static String getFormattedTextString(Annotation doc) {
		Text text = Annotation.makeText(doc);
		CorefPipe.getInstance().process(text);
		String str = text.toString();
		if (str != null) {
			str = str.trim().replaceAll("\\r?\\n", " <NEWLINE> ");
		}
		return str;
	}

	public static Annotation getAnnotation(String... strings) {
		String stringText = StringUtils.join(strings, "\n");
		Annotation doc = null;
		if (USE_PIPE_CLIENT) {
			doc = PipeClient.getInstance().getAnnotation(stringText);
			Coref.getInstance().process(doc);
		} else {
			doc = Pipe.getInstance().process(stringText);
		}
		return doc;
	}
}

class MP {
	public String type;
	public String mentionString;
	public int par = -1;
	public int sent = -1;
	public int tok = -1;

	MP(String mentionString, String type, int par, int sent, int tok) {
		this.mentionString = mentionString;
		this.type = type;
		this.par = par;
		this.sent = sent;
		this.tok = tok;
	}

	MP(String mentionString, String type) {
		this.mentionString = mentionString;
		this.type = type;
	}
}
