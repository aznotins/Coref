package lv.pipe;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import lv.label.Annotation;

public class Pipe {
	private static Pipe instance = null;

	public static Pipe getInstance() {
		if (instance == null)
			instance = new Pipe();
		return instance;
	}

	public void init(Properties props) {
		Tokenizer tok = Tokenizer.getInstance();
		tok.init(new Properties());

		MorphoTagger morpho = MorphoTagger.getInstance();
		Properties morphoProp = new Properties();
		morphoProp.setProperty("morpho.classifierPath", "models/lv-morpho-model.ser.gz");
		morphoProp.setProperty("malt.workingDir", "./models");
		morphoProp.setProperty("malt.extraParams", "-m parse -lfi parser.log");
		morpho.init(morphoProp);

		NerTagger ner = NerTagger.getInstance();
		Properties nerProp = new Properties();
		try {
			nerProp.load(new FileReader("lv-ner-tagger.prop"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ner.init(nerProp);

		MaltParser malt = MaltParser.getInstance();
		Properties maltParserProp = new Properties();
		maltParserProp.setProperty("malt.modelName", "langModel-pos-corpus");
		maltParserProp.setProperty("malt.workingDir", "./models");
		maltParserProp.setProperty("malt.extraParams", "-m parse -lfi parser.log");
		malt.init(maltParserProp);
	}

	public Annotation process(String text) {
		// System.err.println("Pipe process text: " + text.replaceAll("\n",
		// "<\n>"));
		Annotation doc = new Annotation(text);
		Tokenizer.getInstance().process(doc);
		MorphoTagger.getInstance().process(doc);
		NerTagger.getInstance().process(doc);
		MaltParser.getInstance().process(doc);
		// System.err.println("Processed annotation: " + doc.toStringPretty());
		return doc;
	}

	public static void main(String[] args) {
		Pipe pipe = Pipe.getInstance();
		pipe.init(new Properties());

		Annotation doc = pipe
				.process("Uzņēmuma SIA \"Cirvis\" prezidents Jānis Bērziņš. Viņš uzņēmumu vada no 2015. gada.");
		System.err.println(doc.toStringPretty());
		System.err.println(Annotation.getConllString(doc));
	}
}
