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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lv.label.Annotation;
import lv.label.Labels.LabelIndex;
import lv.label.Labels.LabelMorphoFeatures;
import lv.label.Labels.LabelParagraphs;
import lv.label.Labels.LabelPosTag;
import lv.label.Labels.LabelSentences;
import lv.label.Labels.LabelPosTagSimple;
import lv.label.Labels.LabelText;
import lv.label.Labels.LabelTokens;
import lv.lumii.morphotagger.Dictionary;
import lv.semti.morphology.analyzer.Word;
import lv.semti.morphology.analyzer.Wordform;
import lv.semti.morphology.attributes.AttributeNames;
import edu.stanford.nlp.ie.ner.CMMClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LVMorphologyAnalysis;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

@SuppressWarnings("unchecked")
public class MorphoTagger implements PipeTool {

	private static boolean MINI_TAG = false;
	private static boolean FEATURES = false;
	private static boolean LETA_FEATURES = true;

	private static CMMClassifier<CoreLabel> morphoClassifier;

	private static MorphoTagger instance;

	public static MorphoTagger getInstance() {
		if (instance == null)
			instance = new MorphoTagger();
		return instance;
	}

	public void init(Properties prop) {
		try {
			morphoClassifier = CMMClassifier.getClassifier(prop.getProperty("morpho.classifierPath", ""));
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Annotation process(Annotation doc) {
		if (doc.has(LabelParagraphs.class)) {
			List<Annotation> pLabels = doc.get(LabelParagraphs.class);
			for (Annotation pLabel : pLabels) {
				processParagraph(pLabel);
			}
		}
		return doc;
	}

	public Annotation processParagraph(Annotation paragraph) {
		if (paragraph.has(LabelSentences.class)) {
			List<Annotation> sLabels = paragraph.get(LabelSentences.class);
			for (Annotation sLabel : sLabels) {
				processSentence(sLabel);
			}
		}
		return paragraph;
	}

	public Annotation processSentence(Annotation sentence) {
		if (sentence.has(LabelTokens.class)) {
			List<Annotation> tokens = sentence.get(LabelTokens.class);
			// This is not working returns all "xf":
			// List<Word> sent = new ArrayList<Word>(tokens.size());
			// for (Annotation token : tokens) {
			// String word = token.get(TextLabel.class);
			// sent.add(new Word(word));
			// }
			// List<CoreLabel> coreLabels =
			// LVMorphologyReaderAndWriter.analyzeSentence2(sent);
			List<CoreLabel> sent = new ArrayList<CoreLabel>(tokens.size());
			for (Annotation token : tokens) {
				String word = token.get(LabelText.class);
				CoreLabel wi = new CoreLabel();
				wi.setWord(word);
				sent.add(wi);
			}
			CoreLabel sEnd = new CoreLabel();
			sEnd.setWord("<s>");
			sent.add(sEnd);
			List<CoreLabel> coreLabels = LVMorphologyReaderAndWriter.analyzeLabels(sent);
			
			morphoClassifier.classify(coreLabels);
			sentence.remove(LabelTokens.class);
			List<Annotation> tLabels = new ArrayList<Annotation>(coreLabels.size());
			int counter = 1;
			for (CoreLabel w : coreLabels) {
				Annotation tLabel = new Annotation();
				String token = w.getString(TextAnnotation.class);
				// token = token.replace(' ', '_');
				if (token.contains("<s>"))
					continue;
				tLabel.setText(token);
				tLabel.set(LabelIndex.class, counter++);

				Word analysis = w.get(LVMorphologyAnalysis.class);
				Wordform mainwf = analysis.getMatchingWordform(w.getString(AnswerAnnotation.class), false);

				if (mainwf != null) {
					String lemma = mainwf.getValue(AttributeNames.i_Lemma);
					// lemma = lemma.replace(' ', '_');
					String answer = w.getString(AnswerAnnotation.class);
					if (answer.length() == 0)
						answer = "_"; // no empty tag
					tLabel.setLemma(lemma);
					tLabel.set(LabelPosTagSimple.class, answer);
					tLabel.set(LabelPosTag.class, mainwf.getTag());

					// Feature atribūtu filtri
					if (MINI_TAG)
						mainwf.removeNonlexicalAttributes();
					if (LETA_FEATURES) {
						addLETAfeatures(mainwf);
						// mainwf.removeAttribute(AttributeNames.i_SourceLemma);
						// FIXME - atvasinātiem vārdiem šis var būt svarīgs,
						// atpriedekļotas lemmas..
						mainwf.removeTechnicalAttributes();
					}

					// vārda fīčas
					StringBuilder s = mainwf.pipeDelimitedEntries();
					if (FEATURES) {
						// visas fīčas, ko lietoja trenējot
						Datum<String, String> d = morphoClassifier.makeDatum(coreLabels, counter,
								morphoClassifier.featureFactory);

						for (String feature : d.asFeatures()) {
							// noņemam trailing |C, kas tām fīčām tur ir
							s.append(feature.substring(0, feature.length() - 2).replace(' ', '_'));
							s.append('|');
						}
					}
					// noņemam peedeejo | separatoru, kas ir lieks
					s.deleteCharAt(s.length() - 1);
					s.append('\t');
					String morphoFeatures = s.toString();
					tLabel.set(LabelMorphoFeatures.class, morphoFeatures);

				}
				tLabels.add(tLabel);
			}
			sentence.set(LabelTokens.class, tLabels);
		}
		return sentence;
	}

	private static void addLETAfeatures(Wordform wf) {
		String lemma = wf.getValue(AttributeNames.i_Lemma);

		if (wf.isMatchingStrong(AttributeNames.i_PartOfSpeech, AttributeNames.i_Number)) {
			// uzskatam ka nav atšķirības starp skaitļiem ja ciparu skaits
			// vienāds
			String numbercode = lemma.replaceAll("\\d", "0");
			wf.addAttribute("LETA_lemma", numbercode);
		} else if (wf.isMatchingStrong(AttributeNames.i_CapitalLetters, AttributeNames.v_FirstUpper)
				&& Dictionary.dict("surnames").contains(lemma))
			wf.addAttribute("LETA_lemma", "_surname_");
		else if (Dictionary.dict("vocations").contains(lemma))
			wf.addAttribute("LETA_lemma", "_vocation_");
		else if (Dictionary.dict("relations").contains(lemma))
			wf.addAttribute("LETA_lemma", "_relationship_");
		else if (Dictionary.dict("partijas").contains(lemma))
			wf.addAttribute("LETA_lemma", "_party_");
		/*
		 * TODO - nočekot kā visā procesā sanāk ar case-sensitivity, te tas ir
		 * svarīgi
		 */
		else if (Dictionary.dict("months").contains(lemma))
			/*
			 * TODO - te būtu jāčeko, lai personvārdi Marts un Jūlijs te
			 * neapēdas, ja ir ar lielo burtu ne teikuma sākumā
			 */
			wf.addAttribute("LETA_lemma", "_month_");
		else if (Dictionary.dict("common_lemmas").contains(lemma))
			wf.addAttribute("LETA_lemma", lemma);
		else
			wf.addAttribute("LETA_lemma", "_rare_");
	}

	public static void main(String[] args) {
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

		System.err.println(doc.toStringPretty());
	}
}
