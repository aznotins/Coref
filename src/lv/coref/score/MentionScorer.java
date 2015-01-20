package lv.coref.score;

import java.util.List;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.data.Text;

public class MentionScorer {

	private Scorer boundaryScorer = new Scorer();
	private Scorer headScorer = new Scorer();

	public void add(List<Text> texts) {
		for (Text t : texts) {
			add(t);
		}
	}
	
	public void add(Text text) {
		Text pairedText = text.getPairedText();
		if (pairedText == null)
			return;

		for (Sentence s : text.getSentences()) {
			for (Mention m : s.getMentions()) {
				if (m.getMention(true) != null) {
					boundaryScorer.addTP();
				} else {
					boundaryScorer.addFP();
					//System.err.println("SCORE " + m);
				}
				if (m.getMention(false) != null) {
					headScorer.addTP();
				} else {
					headScorer.addFP();
				}
			}
		}

		for (Sentence s : pairedText.getSentences()) {
			for (Mention m : s.getMentions()) {
				if (m.getMention(true) == null) {
					boundaryScorer.addFN();
				}
				if (m.getMention(false) != null) {
					headScorer.addFN();
				}
			}
		}
		boundaryScorer.calculate();
		headScorer.calculate();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("MentionBoundaryScorer: \t")
				.append(boundaryScorer.toString());
		sb.append("\n");
		sb.append("MentionHeadScorer: \t").append(headScorer.toString());
		return sb.toString();
	}
}
