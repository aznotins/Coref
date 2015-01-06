package lv.coref.score;

import java.util.Arrays;
import java.util.List;

import lv.coref.data.Text;

public class SummaryScorer {
	MentionScorer mentionScorer = new MentionScorer();
	ConllEvalScorer conllScorer = new ConllEvalScorer(false);
	ConllEvalScorer headConllScorer = new ConllEvalScorer(true);
	
	public void add(Text text) {
		add(Arrays.asList(text));
	}
	
	public void add(List<Text> texts) {		
		mentionScorer.add(texts);
		conllScorer.add(texts);
		headConllScorer.add(texts);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mentionScorer);
		sb.append("\n").append(conllScorer);
		sb.append("\n").append(headConllScorer);
		
//		sb.append("\n").append(conllScorer.getSummary());
		
		return sb.toString();
	}
}
