package lv.coref.score;

import java.util.ArrayList;
import java.util.List;

import lv.coref.data.Text;
import lv.coref.io.ConllReaderWriter;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;

public class Corpus {
	List<Text> texts = new ArrayList<>();

	public void add(List<Text> texts) {
		this.texts.addAll(texts);
	}

	public void add(String refFile, String hypFile) {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text text = rw.getText(hypFile);
		Text goldText = rw.getText(refFile);
		text.setPairedText(goldText);
		goldText.setPairedText(text);
		goldText.setId(text.getId());
		texts.add(text);
	}

	public void evaluate() {
		List<Text> cumulativeTexts = new ArrayList<>(texts.size());
		for (Text t : texts) {
			
//			System.err.println(t);
//			System.err.println(t.getPairedText());
			
			cumulativeTexts.add(t);
//			SummaryScorer scorer = new SummaryScorer();
//			scorer.add(t);
//			System.err.println("=== SCORE " + t.getId() + " ===");
//			System.err.println(scorer);
		}
		SummaryScorer cumulativeScorer = new SummaryScorer();
		cumulativeScorer.add(cumulativeTexts);
		System.err.println("=== TOTAL_SCORE ===");
		System.err.println(cumulativeScorer);
	}

	public void resolve() {
		MentionFinder mf = new MentionFinder();
		Ruler r = new Ruler();
		for (Text t : texts) {
			mf.findMentions(t);
			r.resolve(t);
			t.finalizeMentionChains();
		}
	}

	public static void main(String args[]) {
		Corpus c = new Corpus();
//		c.add("data/corpus/corefconll/anotation_40.corefconll","data/corpus/conll/anotation_40.conll");
//		c.add("data/corpus/corefconll/anotation_41.corefconll","data/corpus/conll/anotation_41.conll");
//		c.add("data/corpus/corefconll/anotation_42.corefconll","data/corpus/conll/anotation_42.conll");
//		c.add("data/corpus/corefconll/anotation_43.corefconll","data/corpus/conll/anotation_43.conll");
//		c.add("data/corpus/corefconll/anotation_44.corefconll","data/corpus/conll/anotation_44.conll");
		c.add("data/corpus/corefconll/interview_16.corefconll","data/corpus/conll/interview_16.conll");
		c.add("data/corpus/corefconll/interview_23.corefconll","data/corpus/conll/interview_23.conll");
		c.add("data/corpus/corefconll/interview_27.corefconll","data/corpus/conll/interview_27.conll");
		c.add("data/corpus/corefconll/interview_38.corefconll","data/corpus/conll/interview_38.conll");
		c.add("data/corpus/corefconll/interview_43.corefconll","data/corpus/conll/interview_43.conll");
		c.add("data/corpus/corefconll/interview_46.corefconll","data/corpus/conll/interview_46.conll");
//		c.add("data/corpus/corefconll/lvvest_40.corefconll","data/corpus/conll/lvvest_40.conll");
//		c.add("data/corpus/corefconll/lvvest_41.corefconll","data/corpus/conll/lvvest_41.conll");
//		c.add("data/corpus/corefconll/lvvest_42.corefconll","data/corpus/conll/lvvest_42.conll");
//		c.add("data/corpus/corefconll/lvvest_88.corefconll","data/corpus/conll/lvvest_88.conll");
//		c.add("data/corpus/corefconll/lvvest_89.corefconll","data/corpus/conll/lvvest_89.conll");
//		c.add("data/corpus/corefconll/news_60.corefconll","data/corpus/conll/news_60.conll");
//		c.add("data/corpus/corefconll/news_61.corefconll","data/corpus/conll/news_61.conll");
//		c.add("data/corpus/corefconll/news_62.corefconll","data/corpus/conll/news_62.conll");
//		c.add("data/corpus/corefconll/news_63.corefconll","data/corpus/conll/news_63.conll");

		c.resolve();
		c.evaluate();
	}

}
