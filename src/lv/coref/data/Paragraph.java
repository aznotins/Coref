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
package lv.coref.data;

import java.util.ArrayList;
import java.util.List;

public class Paragraph extends ArrayList<Sentence> {

	private static final long serialVersionUID = -4746893959959169033L;

	private Text text;
	private int position;

	public Paragraph() {
	}

	public Paragraph(int position) {
		this.position = position;
	}

	public boolean add(Sentence s) {
		s.setPosition(this.size());
		s.setParagraph(this);
		return super.add(s);
	}

	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public List<Mention> getMentions() {
		List<Mention> r = new ArrayList<>();
		for (Sentence s : this) {
			r.addAll(s.getMentions());
		}
		return r;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Sentence sentence : this)
			sb.append(sentence.toString()).append("\n");
		return sb.toString();
	}

}
