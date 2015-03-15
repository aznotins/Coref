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

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import lv.label.Annotation;
import lv.label.Labels.LabelDependency;
import lv.label.Labels.LabelIndex;
import lv.label.Labels.LabelMorphoFeatures;
import lv.label.Labels.LabelParagraphs;
import lv.label.Labels.LabelParent;
import lv.label.Labels.LabelPosTag;
import lv.label.Labels.LabelSentences;
import lv.label.Labels.LabelPosTagSimple;
import lv.label.Labels.LabelTokens;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.symbol.SymbolTable;
import org.maltparser.core.syntaxgraph.DependencyStructure;
import org.maltparser.core.syntaxgraph.edge.Edge;
import org.maltparser.core.syntaxgraph.node.DependencyNode;

public class MaltParser implements PipeTool {
	private MaltParserService maltServ;

	private static MaltParser instance;

	public static MaltParser getInstance() {
		if (instance == null)
			instance = new MaltParser();
		return instance;
	}

	@Override
	public void init(Properties prop) {
		try {
			maltServ = new MaltParserService();
			StringBuilder params = new StringBuilder();
			params.append("-c ").append(prop.getProperty("malt.modelName"));
			params.append(" -w ").append(prop.getProperty("malt.workingDir", "."));
			params.append(" ").append(prop.getProperty("malt.extraParams", ""));
			System.err.println("MaltParser " + params);
			
			maltServ.initializeParserModel(params.toString().trim());
			System.err.println("MaltParser loaded.");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			String[] conllRows = new String[tLabels.size()];
			int counter = 0;
			for (Annotation tLabel : tLabels) {
				StringBuilder s = new StringBuilder();
				s.append(tLabel.get(LabelIndex.class));
				s.append("\t").append(tLabel.getText());
				s.append("\t").append(tLabel.getLemma());
				s.append("\t").append(tLabel.get(LabelPosTag.class));
				s.append("\t").append(tLabel.get(LabelPosTagSimple.class));
				s.append("\t").append(tLabel.get(LabelMorphoFeatures.class));
				s.append("\t");
				conllRows[counter++] = s.toString();
			}
			try {
				DependencyStructure graph = maltServ.parse(conllRows);
				addParserColumns(graph, sentence);
			} catch (MaltChainedException e) {
				e.printStackTrace();
			}
		}
		return sentence;
	}

	private Annotation addParserColumns(DependencyStructure graph, Annotation sentence) throws MaltChainedException {
		if (!sentence.has(LabelTokens.class)) {
			return sentence;
		}
		List<Annotation> tLabels = sentence.get(LabelTokens.class);
		for (int i = 1; i <= graph.getHighestDependencyNodeIndex(); i++) {
			DependencyNode node = graph.getDependencyNode(i);
			// for (SymbolTable table : node.getLabelTypes()) {
			// System.err.println(node.getLabelSymbol(table));
			// }

			if (node == null) {
				System.err.println("NULL node");
				continue;
			}

			if (!node.hasHead()) {
				System.err.println("Node has no head");
				continue;
			}

			if (i - 1 >= tLabels.size()) {
				System.err.println("Mismatched alignment with MaltParser");
				continue;
			}

			Edge e = node.getHeadEdge();
			// System.er.println(e.getSource().getIndex());
			tLabels.get(i - 1).set(LabelParent.class, e.getSource().getIndex());

			StringBuilder labelBuilder = new StringBuilder();
			if (e.isLabeled()) {
				for (SymbolTable table : e.getLabelTypes()) {
					labelBuilder.append(e.getLabelSymbol(table));
				}
			} else {
				for (SymbolTable table : graph.getDefaultRootEdgeLabels().keySet()) {
					labelBuilder.append(graph.getDefaultRootEdgeLabelSymbol(table));
				}
			}
			// System.err.println(label);
			String label = labelBuilder.toString().replaceAll("#false#", "_");
			tLabels.get(i - 1).set(LabelDependency.class, label);
		}
		return sentence;
	}

	public static void main(String[] args) throws MaltChainedException, IOException {
		Tokenizer tok = Tokenizer.getInstance();
		Annotation doc = tok.process("Uzņēmuma SIA \"Cirvis\" prezidents Jānis Bērziņš. Viņš uzņēmumu vada no 2015. gada.");
		
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
		
		System.out.println(doc.toStringPretty());

	}
}