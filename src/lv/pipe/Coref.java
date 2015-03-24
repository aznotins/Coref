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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.label.Annotation;

public class Coref implements PipeTool {

	public static String PROP_RUN_NEL = "coref.runNEL";

	private static Coref instance;

	public static Coref getInstance() {
		if (instance == null)
			instance = new Coref();
		return instance;
	}

	private boolean runNEL = true;

	@Override
	public void init(Properties prop) {
		if (Boolean.parseBoolean(prop.getProperty(PROP_RUN_NEL, "false"))) {
			this.runNEL = true;
		} else {
			this.runNEL = false;
		}
	}

	@Override
	public Annotation process(Annotation doc) {
		Text text = Annotation.makeText(doc);
		CorefPipe.getInstance().process(text, runNEL);
		Annotation.makeAnnotationFromText(doc, text);
		return doc;
	}

	@Override
	public Annotation processParagraph(Annotation paragraph) {
		return null;
	}

	@Override
	public Annotation processSentence(Annotation sentence) {
		return null;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		Config.logInit();
		Tokenizer tok = Tokenizer.getInstance();
		Annotation doc = tok
				.process("Uzņēmuma SIA \"Cirvis\" prezidents Jānis Bērziņš. Viņš uzņēmumu vada no 2015. gada.");

		MorphoTagger morpho = MorphoTagger.getInstance();
		Properties morphoProp = new Properties();
		morphoProp.setProperty("morpho.classifierPath", "models/lv-morpho-model.ser.gz");
		morphoProp.setProperty("malt.workingDir", "./models");
		morphoProp.setProperty("malt.extraParams", "-m parse -lfi parser.log");
		morpho.init(morphoProp);
		morpho.process(doc);

		NerTagger ner = NerTagger.getInstance();
		Properties nerProp = new Properties();
		nerProp.load(new FileReader("lv-ner-tagger.prop"));
		ner.init(nerProp);
		ner.process(doc);

		MaltParser malt = MaltParser.getInstance();
		Properties maltParserProp = new Properties();
		maltParserProp.setProperty("malt.modelName", "langModel-pos-corpus");
		maltParserProp.setProperty("malt.workingDir", "./models");
		maltParserProp.setProperty("malt.extraParams", "-m parse -lfi parser.log");
		malt.init(maltParserProp);
		malt.process(doc);

		MateTools mate = MateTools.getInstance();
		mate.init(new Properties());
		mate.process(doc);
		is2.parser.Pipe.executerService.shutdown();

		Coref coref = Coref.getInstance();
		coref.process(doc);

		System.out.println(doc.toStringPretty());

		System.exit(0);
	}

}
