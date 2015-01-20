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
