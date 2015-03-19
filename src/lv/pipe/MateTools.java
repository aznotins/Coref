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

import is2.data.SentenceData09;
import is2.parser.Options;
import is2.parser.Parser;
import is2.parser.Pipe;
import is2.util.OptionsSuper;

import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import lv.label.Annotation;
import lv.label.Labels.LabelIndex;
import lv.label.Labels.LabelParagraphs;
import lv.label.Labels.LabelSDP;
import lv.label.Labels.LabelSDPLabel;
import lv.label.Labels.LabelSDPTarget;
import lv.label.Labels.LabelSentences;
import lv.label.Labels.LabelPosTagSimple;
import lv.label.Labels.LabelTokens;

public class MateTools implements PipeTool {
	private Parser mateToolsParser;
	private OptionsSuper options;

	private static MateTools instance;

	public static MateTools getInstance() {
		if (instance == null)
			instance = new MateTools();
		return instance;
	}
	
	public static void close() {
		if (instance != null)
			instance = null;
	}

	@Override
	public void init(Properties prop) {
		PrintStream original = System.out;
		System.setOut(System.err); // temporary redirect verbose output to
									// System.err verbose output
		String[] arguments = new String[] { "-model", "models/leta-20150315.model", "-test", "taube.conll", "-out",
				"taube.parsed.conll" };
		options = new Options(arguments);
		mateToolsParser = new Parser(options);
		System.setOut(original);
		System.err.println("mate tools loaded.");
	}

	@Override
	public Annotation process(Annotation doc) {
		if (doc.has(LabelParagraphs.class)) {
			List<Annotation> pLabels = doc.get(LabelParagraphs.class);
			for (Annotation pLabel : pLabels) {
				processParagraph(pLabel);
			}
		}
		return doc;
	}

	@Override
	public Annotation processParagraph(Annotation paragraph) {
		if (paragraph.has(LabelSentences.class)) {
			List<Annotation> sLabels = paragraph.get(LabelSentences.class);
			for (Annotation sLabel : sLabels) {
				processSentence(sLabel);
			}
		}
		return paragraph;
	}

	@Override
	public Annotation processSentence(Annotation sentence) {
		if (sentence.has(LabelTokens.class)) {
			List<Annotation> tLabels = sentence.get(LabelTokens.class);
			SentenceData09 it = new SentenceData09();

			it.id = new String[tLabels.size() + 1];
			it.forms = new String[tLabels.size() + 1];
			it.plemmas = new String[tLabels.size() + 1];
			it.lemmas = new String[tLabels.size() + 1];
			it.gpos = new String[tLabels.size() + 1];
			it.ppos = new String[tLabels.size() + 1];
			it.labels = new String[tLabels.size() + 1];
			it.plabels = new String[tLabels.size() + 1];
			it.heads = new int[tLabels.size() + 1];
			it.pheads = new int[tLabels.size() + 1];
			it.ofeats = new String[tLabels.size() + 1];

			it.id[0] = "0";
			it.forms[0] = is2.io.CONLLReader09.ROOT;
			it.plemmas[0] = is2.io.CONLLReader09.ROOT_LEMMA;
			// it.fillp[0] = "N";
			it.lemmas[0] = is2.io.CONLLReader09.ROOT_LEMMA;
			it.gpos[0] = is2.io.CONLLReader09.ROOT_POS;
			it.ppos[0] = is2.io.CONLLReader09.ROOT_POS;
			it.labels[0] = is2.io.CONLLReader09.NO_TYPE;
			it.heads[0] = -1;
			it.plabels[0] = is2.io.CONLLReader09.NO_TYPE;
			it.pheads[0] = -1;
			it.ofeats[0] = is2.io.CONLLReader09.NO_TYPE;

			int counter = 1;
			for (Annotation tLabel : tLabels) {
				it.id[counter] = tLabel.get(LabelIndex.class).toString();
				it.forms[counter] = tLabel.getText();
				it.lemmas[counter] = tLabel.getLemma();
				it.plemmas[counter] = tLabel.getLemma();
				it.gpos[counter] = tLabel.get(LabelPosTagSimple.class);
				// i09.ofeats[counter] =
				// s.append("\t").append(tLabel.get(LabelMorphoFeatures.class));
				counter += 1;
			}

			SentenceData09 i09 = mateToolsParser.parse(it, mateToolsParser.params, options.label, options);
			addParserColumns(i09, sentence);
		}
		return sentence;
	}

	private Annotation addParserColumns(SentenceData09 graph, Annotation sentence) {
		if (!sentence.has(LabelTokens.class)) {
			return sentence;
		}

		// setting up the initial arrays of outgoing edges
		List<List<Annotation>> edges = new ArrayList<List<Annotation>>(graph.length());
		for (int i = 0; i < graph.length(); i++) {
			edges.add(new LinkedList<Annotation>());
		}

		// filling direct edges
		for (int i = 0; i < graph.length(); i++) {
			String label = graph.plabels[i];
			int head = graph.pheads[i];
			if ("_null_".equalsIgnoreCase(label))
				continue;
			if (label.contains(";"))
				label = label.split(";")[0]; // Peking system - if indirect
												// graph (non-tree) edges are
												// encoded in the label, we will
												// process that part later
			if (!label.endsWith("~R")) { // normal edge
				List<Annotation> edge = edges.get(i);
				Annotation a = new Annotation();
				a.set(LabelSDPLabel.class, label);
				a.set(LabelSDPTarget.class, head);
				edge.add(a);
			} else { // Peking system - reversed labels
				List<Annotation> edge = edges.get(head - 1);
				Annotation a = new Annotation();
				a.set(LabelSDPLabel.class, label.substring(0, label.length() - 2));
				a.set(LabelSDPTarget.class, i + 1);
				edge.add(a);
			}
		}

		// filling indirect edges
		for (int node = 0; node < graph.length(); node++) {
			String label = graph.plabels[node];
			int head = graph.pheads[node];

			if (!label.contains(";"))
				continue; // if only direct edges, then nothing to do here
			String[] labels = label.split(";");
			for (int i = 1; i < labels.length; i++) {
				// Assuming label format like "[2]aux_ARG2"
				// NB! Will break if somehow graph contains links to 10th level
				// grand-grand...parents
				int parentlevel = Integer.parseInt(labels[i].substring(1, 2));
				int ancestor = node;

				while (parentlevel > 0) {
					try {
						Annotation direct_edge = edges.get(ancestor).get(0);
						int potential_ancestor = direct_edge.get(LabelSDPTarget.class) - 1;
						if (potential_ancestor>=0) ancestor = potential_ancestor;
					} catch (IndexOutOfBoundsException e) {
						break; // nav tāda ancestora
					}
					parentlevel -= 1;
				}
				try {
					List<Annotation> e = edges.get(ancestor);
				} catch (IndexOutOfBoundsException e) {
					ancestor = node; // ja ancestors slikts
				}				
				
				String newlabel = labels[i].substring(3);
				if (!newlabel.endsWith("~R")) { // normal order
					List<Annotation> edge = edges.get(node);
					Annotation a = new Annotation();
					a.set(LabelSDPLabel.class, newlabel);
					a.set(LabelSDPTarget.class, ancestor + 1);
					edge.add(a);
				} else { // reversed
					List<Annotation> edge = edges.get(ancestor);
					Annotation a = new Annotation();
					a.set(LabelSDPLabel.class, newlabel.substring(0, newlabel.length() - 2));
					a.set(LabelSDPTarget.class, node + 1);
					edge.add(a);
				}
			}
		}

		List<Annotation> tLabels = sentence.get(LabelTokens.class);
		for (int i = 0; i < graph.length(); i++) {
			tLabels.get(i).set(LabelSDP.class, edges.get(i));
		}
		return sentence;
	}

	public static void main(String[] args) throws Exception {
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

		MateTools mate = MateTools.getInstance();
		mate.init(new Properties());
		mate.process(doc);

		Pipe.executerService.shutdown();
		System.out.println(doc.toStringPretty());

		System.exit(0);
	}

}