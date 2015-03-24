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
package lv.pipe;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.label.Annotation;
import lv.label.Labels.LabelDocumentDate;
import lv.label.Labels.LabelDocumentId;
import lv.util.FileUtils;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Pipe {

	private final static Logger log = Logger.getLogger(Pipe.class.getName());

	private static Pipe pipe = null;

	private InputStream inStream = System.in;
	private OutputStream outStream = System.out;

	public static String PROP_TOKENIZER = "tokenizer";
	public static String PROP_TAGGER = "tagger";
	public static String PROP_NER = "ner";
	public static String PROP_PARSER = "parser";
	public static String PROP_SPD = "spd";
	public static String PROP_COREF = "coref";
	public static String PROP_NEL = "nel";

	private boolean runAll = false;
	private Set<String> tools = null;

	public void setTools(String toolString) {
		tools = new HashSet<>();
		tools.addAll(Arrays.asList(toolString.trim().split("\\s+|,")));
	}

	public void setRunAll(boolean all) {
		runAll = all;
	}

	public boolean toRun(String toolName) {
		if (runAll)
			return true;
		if (tools.contains(toolName))
			return true;
		return false;
	}

	public static Pipe getInstance() {
		if (pipe == null) {
			System.err.println(Config.getInstance().toString());
			pipe = new Pipe();
			pipe.init();
		}
		return pipe;
	}

	public void init() {
		String toolString = Config.getInstance().get(Config.PROP_PIPE_TOOLS, "");
		if (toolString.length() == 0)
			setRunAll(true);
		else {
			setTools(toolString);
		}

		if (toRun(PROP_TOKENIZER)) {
			Tokenizer tok = Tokenizer.getInstance();
			tok.init(new Properties());
		}

		if (toRun(PROP_TAGGER)) {
			MorphoTagger morpho = MorphoTagger.getInstance();
			Properties morphoProp = new Properties();
			morphoProp.setProperty("morpho.classifierPath", "models/lv-morpho-model.ser.gz");
			morpho.init(morphoProp);
		}

		if (toRun(PROP_NER)) {
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
		}

		if (toRun(PROP_PARSER)) {
			MaltParser malt = MaltParser.getInstance();
			Properties maltParserProp = new Properties();
			maltParserProp.setProperty("malt.modelName", "langModel-pos-corpus");
			maltParserProp.setProperty("malt.workingDir", "./models");
			maltParserProp.setProperty("malt.extraParams", "-m parse -lfi parser.log");
			malt.init(maltParserProp);
		}
		
		if (toRun(PROP_SPD)) {
			MateTools mate = MateTools.getInstance();
			mate.init(new Properties());
		}

		if (toRun(PROP_COREF)) {
			Properties corefProperties = new Properties();
			corefProperties.setProperty(Coref.PROP_RUN_NEL, Boolean.toString(toRun(PROP_NEL)));
		}
	}

	public void setInputStream(InputStream inStream) {
		this.inStream = inStream;
	}

	public void setOutputStream(OutputStream outStream) {
		this.outStream = outStream;
	}

	public Annotation process(String text) {
		Annotation doc = new Annotation(text);
		return process(doc);
	}

	public Text processText(Annotation doc) {
		if (toRun(PROP_TOKENIZER))
			Tokenizer.getInstance().process(doc);
		if (toRun(PROP_TAGGER))
			MorphoTagger.getInstance().process(doc);
		if (toRun(PROP_NER))
			NerTagger.getInstance().process(doc);
		if (toRun(PROP_PARSER))
			MaltParser.getInstance().process(doc);
		if (toRun(PROP_SPD))
			MateTools.getInstance().process(doc);
		if (toRun(PROP_COREF)) {
			Text text = Annotation.makeText(doc);
			text.setId(doc.get(LabelDocumentId.class));
			text.setDate(doc.get(LabelDocumentDate.class));
			CorefPipe.getInstance().process(text, toRun(PROP_NEL));
			return text;
		} else {
			return Annotation.makeText(doc);
		}
	}

	public Annotation process(Annotation doc) {
		if (toRun(PROP_TOKENIZER))
			Tokenizer.getInstance().process(doc);
		if (toRun(PROP_TAGGER))
			MorphoTagger.getInstance().process(doc);
		if (toRun(PROP_NER))
			NerTagger.getInstance().process(doc);
		if (toRun(PROP_PARSER))
			MaltParser.getInstance().process(doc);
		if (toRun(PROP_SPD))
			MateTools.getInstance().process(doc);
		if (toRun(PROP_COREF))
			Coref.getInstance().process(doc);
		return doc;
	}

	public Annotation read(String filename) throws IOException {
		String textString = FileUtils.readFile(filename);
		Annotation doc = process(textString);
		return doc;
	}

	public Annotation readJson(BufferedReader in) {
		StringBuilder builder = new StringBuilder();
		try {
			for (String line = null; (line = in.readLine()) != null;) {
				if (line.trim().length() == 0)
					break;
				builder.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		JSONObject json = (JSONObject) JSONValue.parse(builder.toString());
		if (json == null)
			return null;
		String docid = (String) json.get("document");
		String date = (String) json.get("date");
		String txt = (String) json.get("text");

		Annotation doc = new Annotation(txt);
		if (docid != null)
			doc.set(LabelDocumentId.class, docid);
		if (date != null)
			doc.set(LabelDocumentDate.class, date);
		return doc;
	}

	// public void write(Text text, OutputStream out) {
	// ReaderWriter rw = null;
	// if (Config.getInstance().getOUTPUT().equals(Config.FORMAT.JSON))
	// rw = new JsonReaderWriter();
	// else if (Config.getInstance().getOUTPUT().equals(Config.FORMAT.CONLL))
	// rw = new ConllReaderWriter(TYPE.LETA);
	// try {
	// rw.write(out, text, false);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public void write(Annotation doc, OutputStream out) {
		try {
			doc.printJson(new PrintStream(out, true, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.log(Level.SEVERE, "Unable to write json: " + doc.get(LabelDocumentId.class), e);
		}
	}

	public void run() {
		BufferedReader in = null;
		OutputStream out = outStream;
		try {
			in = new BufferedReader(new InputStreamReader(inStream, "UTF8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		while (true) {
			Annotation doc = readJson(in);
			if (doc == null) {
				break;
			}
			// System.err.println("ID " + doc.get(LabelDocumentId.class));
			pipe.process(doc);
			write(doc, out);
		}
		log.log(Level.SEVERE, "Pipe has ended");
	}

	public static void close() {
		MateTools.close();
		if (pipe != null) {
			// Mate tools paliek parsera threadi karājamies, kurus viņš nesatīra
			is2.parser.Pipe.executerService.shutdown();
		}
	}

	public static void main(String[] args) {
		CorefPipe.getInstance().init(args);
		Config.logInit();
		Pipe.getInstance().run();

		// Annotation a =
		// Pipe.getInstance().process("Finanšu ministrs Andris Vilks devās bekot.");
		// Annotation a = Pipe.getInstance().process(
		// "Uzņēmuma SIA \"Cirvis\" prezidents Jānis Bērziņš. Viņš uzņēmumu vada no 2015. gada.");
		// Annotation a = Pipe.getInstance().read("test_taube.txt")
		// System.out.println(a.toStringPretty());
		// a.printJson(System.out);

		Pipe.close();
		System.exit(0);
	}
}
