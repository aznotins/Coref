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
