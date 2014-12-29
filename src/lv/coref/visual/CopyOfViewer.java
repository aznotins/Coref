package lv.coref.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import lv.coref.data.Mention;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;

public class CopyOfViewer implements Runnable, ActionListener {

	Map<Token, Marker> tokenMarkerMap = new HashMap<>();
	Map<Marker, Token> markerTokenMap = new HashMap<>();
	Map<Mention, List<Marker>> mentionMarkerMap = new HashMap<>();
	Map<Marker, Mention> markerMentionMap = new HashMap<>();

	public void addTokenWordPair(Token t, Marker w) {
		tokenMarkerMap.put(t, w);
		markerTokenMap.put(w, t);
	}

	public void addMentioLabelPair(Mention m, Marker l) {
		markerMentionMap.put(l, m);
		if (!mentionMarkerMap.containsKey(m))
			mentionMarkerMap.put(m, new ArrayList<Marker>());
		mentionMarkerMap.get(m).add(l);
	}

	public Marker getTokenWord(Token t) {
		return tokenMarkerMap.get(t);
	}

	public Marker getMentionLabel(Token t) {
		return tokenMarkerMap.get(t);
	}

	class Marker extends JLabel {

		//public enum MarkerType { TOKEN, MENTION, OTHER };

		private static final long serialVersionUID = -4474129677160482060L;
		//public MarkerType type = MarkerType.OTHER;
		private Font font;

		public void highlight() {
			setBackground(Color.RED);
			setOpaque(true);
			setBold(true);
		}

		public void unhighlight() {
			setBackground(Color.GREEN);
			setOpaque(false);
			setBold(false);
		}

		public void setBold(boolean bold) {
			int style = bold ? (getFont().getStyle() | Font.BOLD) : (getFont()
					.getStyle() & ~Font.BOLD);
			Font font = getFont().deriveFont(style);
			setFont(font);
		}

		public Marker(final String text) {

			super(text);

			setFocusable(true);
			setBackground(new Color(0, 0, 0, 0));
			setOpaque(true);

//			Font font = getFont().deriveFont(getFont().getStyle() & ~Font.BOLD);
//			setFont(font);
			setBold(false);

			MouseInputAdapter mouseHandler = new MouseInputAdapter() {

				@Override
				public void mouseEntered(final MouseEvent e) {
					Marker.this.highlight();
					if (Marker.this.getText().equals("[")) {
						Mention m = markerMentionMap.get(Marker.this);
						System.err.println("MOUSE ON " + m);
						if (m != null) {
							for (Token t : m.getTokens()) {
								System.err.println("\t " + t);
								Marker mm = tokenMarkerMap.get(t);
								mm.highlight();
							}
						}
					}
				}

				@Override
				public void mouseExited(final MouseEvent e) {
					Marker.this.unhighlight();
					if (Marker.this.getText().equals("[")) {
						Mention m = markerMentionMap.get(Marker.this);
						System.err.println("MOUSE ON " + m);
						if (m != null) {
							for (Token t : m.getTokens()) {
								System.err.println("\t " + t);
								Marker mm = tokenMarkerMap.get(t);
								// mm.setBackground(new Color(0,0,0,0));
								mm.unhighlight();
							}
						}
					}
				}
			};
			addMouseListener(mouseHandler);
		}
	}

	private static final int WIDTH = 400;
	private static final int HEIGHT = 800;
	private Random random = new Random();
	private JFrame frame = new JFrame("SimplePaintSurface");
	private JPanel tableaux;

	@Override
	public void run() {
		tableaux = new JPanel();
		tableaux.setLayout(new BoxLayout(tableaux, BoxLayout.PAGE_AXIS));
		tableaux.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		addRandom();
		JScrollPane scrollFrame = new JScrollPane(tableaux);
		frame.add(scrollFrame, BorderLayout.CENTER);

		JButton add = new JButton("Add");
		add.addActionListener(this);
		frame.add(add, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		tableaux.requestFocusInWindow();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		addRandom();
		tableaux.repaint();
	}

	void initCoreferences() {

	}

	void addRandom() {

		ConllReaderWriter rw = new ConllReaderWriter();
		Text text = rw.getText("news_63.conll");
		MentionFinder mf = new MentionFinder();
		mf.findMentions(text);
		Ruler r = new Ruler();
		r.resolve(text);
		text.finalizeMentionChains();

		List<Sentence> sentences = text.getSentences();
		for (Sentence s : sentences) {
			JPanel sentencePanel = new JPanel();
			sentencePanel.setLayout(new WrapLayout(FlowLayout.LEFT));
			sentencePanel.setSize(new Dimension(WIDTH, 1));

			for (Token t : s) {
				t.getMentions();

				for (Mention m : t.getOrderedStartMentions()) {
					Marker mentionLabel = new Marker("[");
					sentencePanel.add(mentionLabel);
					addMentioLabelPair(m, mentionLabel);
				}

				Marker w = new Marker(t.getWord() + " ");
				addTokenWordPair(t, w);

				sentencePanel.add(w);

				for (Mention m : t.getEndMentions()) {
					Marker mentionLabel = new Marker("]");
					sentencePanel.add(mentionLabel);
					addMentioLabelPair(m, mentionLabel);
				}
			}
			tableaux.add(sentencePanel);
			sentencePanel.setAlignmentX(Container.LEFT_ALIGNMENT);

			// break;
		}
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new CopyOfViewer());
	}

}
