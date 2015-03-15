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
package lv.coref.visual;

import java.awt.Color;

import javax.swing.JLabel;

class Marker extends JLabel {
	private static final long serialVersionUID = -4247350154185131551L;
	protected TextMapping textMapping;

	public Marker(String text, TextMapping textMapping) {
		super(text);
		this.textMapping = textMapping;
	}

	public void highlight(boolean highlight) {
		if (highlight) {
			setBackground(Color.RED);
			setOpaque(true);
		} else {
			setBackground(Color.GREEN);
			setOpaque(false);
		}
	}
}