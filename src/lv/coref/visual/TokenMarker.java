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
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.MouseInputAdapter;

import lv.coref.data.Token;

public class TokenMarker extends Marker {

	private static final long serialVersionUID = -1329173569198855170L;
	Token token;

	public TokenMarker(String text, TextMapping textMapping, Token token) {
		super(text, textMapping);
		this.token = token;
		
		if (token != null && token.getMentions().size() > 0) {
			
			Map<TextAttribute, Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
			fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			
			
			//setFont(getFont().deriveFont(fontAttributes));
			setFont(getFont().deriveFont(getFont().getStyle() | Font.BOLD));
			setFont(getFont().deriveFont(getFont().getStyle() | Font.ITALIC));
			//setForeground(Color.BLUE);
		} else {
			setFont(getFont().deriveFont(Font.PLAIN));
		}

		addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseEntered(final MouseEvent e) {
				// highlight(true);
			}

			@Override
			public void mouseExited(final MouseEvent e) {
				// highlight(false);
			}
		});
	}

	public void highlight(boolean highlight, Color color) {
		if (highlight) {
			setBackground(color);
			setOpaque(true);
		} else {
			setBackground(Color.GREEN);
			setOpaque(false);
		}
	}

}
