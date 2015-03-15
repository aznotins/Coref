package lv.pipe;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import lv.label.Annotation;
import lv.label.Labels.LabelParagraphs;
import lv.label.Labels.LabelSentences;
import lv.label.Labels.LabelText;
import lv.label.Labels.LabelTokens;
import lv.semti.morphology.analyzer.Analyzer;
import lv.semti.morphology.analyzer.Splitting;
import lv.semti.morphology.analyzer.Word;
import edu.stanford.nlp.sequences.LVMorphologyReaderAndWriter;

public class Tokenizer implements PipeTool {

	private static int SENTENCE_LENGTH_CAP = Splitting.DEFAULT_SENTENCE_LENGTH_CAP;

	private Analyzer analyzer;
	
	private static Tokenizer instance = null;
	
	public static Tokenizer getInstance() {
		if (instance == null)
			instance = new Tokenizer();
		if (instance.analyzer == null) {
			LVMorphologyReaderAndWriter.initAnalyzer();
			instance.analyzer = LVMorphologyReaderAndWriter.getAnalyzer();
		}
		return instance;
	}
	
	@Override
	public void init(Properties prop) {
		// TODO Auto-generated method stub
		
	}

	public Annotation process(String text) {
		Annotation doc = new Annotation();
		doc.set(LabelText.class, text);
		return process(doc);
	}

	@Override
	public Annotation process(Annotation doc) {
		if (!doc.has(LabelText.class))
			return doc;
		String text = doc.getText();
		String[] paragraphs = text.split("(\\r?\\n)");
		List<Annotation> pLabels = new ArrayList<>(paragraphs.length);
		for (String paragraph : paragraphs) {
			paragraph = paragraph.trim();
			if (paragraph.length() > 0) {
				Annotation pLabel = new Annotation();
				pLabel.setText(paragraph);
				pLabels.add(pLabel);
				processParagraph(pLabel);
			}
		}
		doc.set(LabelParagraphs.class, pLabels);
		return doc;
	}

	@Override
	public Annotation processParagraph(Annotation paragraph) {
		if (!paragraph.has(LabelText.class))
			return paragraph;
		String text = paragraph.getText();
		LinkedList<LinkedList<Word>> sentences = Splitting.tokenizeSentences(analyzer, text, SENTENCE_LENGTH_CAP);
		List<Annotation> sLabels = new ArrayList<>();
		for (LinkedList<Word> sentence : sentences) {
			List<Annotation> tLabels = new ArrayList<>();
			for (Word w : sentence) {
				Annotation tLabel = new Annotation();
				tLabel.set(LabelText.class, w.getToken());
				tLabels.add(tLabel);
			}
			Annotation sLabel = new Annotation();
			sLabel.set(LabelTokens.class, tLabels);
			sLabels.add(sLabel);
		}
		paragraph.set(LabelSentences.class, sLabels);
		return paragraph;
	}
	
	@Override
	public Annotation processSentence(Annotation sentence) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		Tokenizer tok = Tokenizer.getInstance();
		Annotation doc = tok.process("Uzņēmuma SIA \"Cirvis\" prezidents Jānis Bērziņš. Viņš uzņēmumu vada no 2015. gada.");
		System.out.println(doc.toStringPretty());
	}
	
}
