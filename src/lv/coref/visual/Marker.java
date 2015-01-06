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