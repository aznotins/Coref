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
import java.util.Properties;
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

	public static Pipe getInstance() {
		if (pipe == null) {
			System.err.println(Config.getInstance().toString());
			pipe = new Pipe();
			pipe.init();
		}
		return pipe;
	}

	public void init() {
		Tokenizer tok = Tokenizer.getInstance();
		tok.init(new Properties());

		MorphoTagger morpho = MorphoTagger.getInstance();
		Properties morphoProp = new Properties();
		morphoProp.setProperty("morpho.classifierPath", "models/lv-morpho-model.ser.gz");
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

		MateTools mate = MateTools.getInstance();
		mate.init(new Properties());
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
		Tokenizer.getInstance().process(doc);
		MorphoTagger.getInstance().process(doc);
		NerTagger.getInstance().process(doc);
		MaltParser.getInstance().process(doc);
		MateTools.getInstance().process(doc);
		// TODO Add coreference annotation
		Text text = Annotation.makeText(doc);
		text.setId(doc.get(LabelDocumentId.class));
		text.setDate(doc.get(LabelDocumentDate.class));
		CorefPipe.getInstance().process(text);
		return text;
	}

	public Annotation process(Annotation doc) {
		Tokenizer.getInstance().process(doc);
		MorphoTagger.getInstance().process(doc);
		NerTagger.getInstance().process(doc);
		MaltParser.getInstance().process(doc);
		MateTools.getInstance().process(doc);
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
