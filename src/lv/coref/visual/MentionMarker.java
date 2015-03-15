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
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.event.MouseInputAdapter;

import lv.coref.data.Mention;
import lv.coref.data.Token;

public class MentionMarker extends Marker {

	private static final long serialVersionUID = -304118293347178743L;
	Mention mention;
	boolean clicked = false;

	public MentionMarker(String text, TextMapping textMapping, Mention mention) {
		super(text, textMapping);
		this.mention = mention;

		setForeground(ViewerUtils.getMentionClusterColor(mention));
		if (mention.getSentence().getPairedSentence() != null) {
			if (mention.getMention(true) == null) {
				setForeground(Color.RED);
			}
			if (mention.getMention(false) == null) {
				setForeground(Color.MAGENTA);
			}
		}

		addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseEntered(final MouseEvent e) {
				highlight(true);
				// showCoreferences(true);

				System.err.println(" @ " + MentionMarker.this.mention);
				System.err.println("   " + MentionMarker.this.mention.toParamString());

			}

			@Override
			public void mouseExited(final MouseEvent e) {
				if (!clicked)
					highlight(false);
			}

			@Override
			public void mouseClicked(final MouseEvent e) {
				System.err.println(MentionMarker.this.mention.getSentence());
				clicked = !clicked;
				highlight(clicked);
			}
		});
	}

	public void highlight(boolean highlight) {
		if (highlight) {
			for (Mention m : mention.getMentionChain()) {
				for (Token t : m.getTokens()) {
					TokenMarker tokenMarker = (TokenMarker) textMapping.getTokenMarker(t);
					if (tokenMarker == null)
						continue;
					// tokenMarker.highlight(highlight);
					tokenMarker.setBackground(Color.LIGHT_GRAY);
					tokenMarker.setOpaque(true);
				}
				setBackground(Color.LIGHT_GRAY);
				setOpaque(true);
			}
		} else {
			for (Mention m : mention.getMentionChain()) {
				for (Token t : m.getTokens()) {
					TokenMarker tokenMarker = (TokenMarker) textMapping.getTokenMarker(t);
					if (tokenMarker == null)
						continue;
					// tokenMarker.highlight(highlight);
					tokenMarker.setBackground(Color.RED);
					tokenMarker.setOpaque(false);
				}
				setBackground(Color.RED);
				setOpaque(false);
			}
		}
	}

	public void paint(Graphics g) {
		super.paint(g);
	}

	public void showCoreferences(boolean showCoref) {
		if (showCoref) {
			Graphics g = getGraphics();
			List<Mention> mentions = mention.getMentionChain().getOrderedMentions();
			MentionMarker prev = null;
			for (Mention m : mentions) {
				g.drawOval(100, 100, 200, 300);
				if (prev != null) {
					g.drawLine(this.getX(), this.getY(), prev.getX(), prev.getY());
				}
				prev = (MentionMarker) textMapping.getMentionMarker(m);
			}
		}
	}

}
