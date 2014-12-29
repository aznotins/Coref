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
