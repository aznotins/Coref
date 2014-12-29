package tmp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
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

public class CopyOfVisual implements Runnable, ActionListener {

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
		if (!mentionMarkerMap.containsKey(m)) mentionMarkerMap.put(m, new ArrayList<Marker>());
		mentionMarkerMap.get(m).add(l);
	}
	
	public Marker getTokenWord(Token t) {
		return tokenMarkerMap.get(t);
	}
	
	public Marker getMentionLabel(Token t) {
		return tokenMarkerMap.get(t);
	}
	
	class Marker extends JLabel {
		
		//public enum Type {};
		
		private static final long serialVersionUID = -4474129677160482060L;
		//public enum Type {} type;
		private Font font;
		
//		private final FontRenderContext fontRenderContext1;
//		private final FontRenderContext fontRenderContext2;

		public void highlight() {
			setBackground(Color.RED);
			setOpaque(true);
		}
		
		public void unhighlight() {
			setBackground(Color.GREEN);
			setOpaque(false);
		}
		
		public void setBold(boolean bold) {
			int style = bold ? Font.BOLD : ~Font.BOLD;
			Font font = getFont().deriveFont(getFont().getStyle() & style);
			setFont(font);
		}
		
		public Marker(final String text) {

			super(text);
			

			setFocusable(true);
			setBackground(new Color(0,0,0,0));
			setOpaque(true);
			
			Font font = getFont().deriveFont(getFont().getStyle() & ~Font.BOLD);
			setFont(font);
			
			//font1 = new Font("Serif", Font.BOLD, 12);
			
//			fontRenderContext1 = getFontMetrics(font1).getFontRenderContext();
//			fontRenderContext2 = getFontMetrics(font2).getFontRenderContext();
			
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
					//Marker.this.setOpaque(true);
					//setFont(font1);
					//setFont(font2);
//					Rectangle bounds = getBounds();
//					Rectangle2D stringBounds = font2.getStringBounds(getText(),
//							fontRenderContext2);
//					bounds.width = (int) stringBounds.getWidth();
//					bounds.height = (int) stringBounds.getHeight();
//					setBounds(bounds);
					//repaint();
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
								//mm.setBackground(new Color(0,0,0,0));
								mm.unhighlight();
							}
						}
					}
					//Marker.this.setOpaque(false);
					//setFont(font1);
//					setFont(font1);
//					Rectangle bounds = getBounds();
//					Rectangle2D stringBounds = font1.getStringBounds(getText(),
//							fontRenderContext1);
//					bounds.width = (int) stringBounds.getWidth();
//					bounds.height = (int) stringBounds.getHeight();
//					setBounds(bounds);
//					repaint();
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
		//tableaux.setLayout(new FlowLayout());
		tableaux.setLayout(new BoxLayout(tableaux, BoxLayout.PAGE_AXIS));
		//tableaux.setAlignmentX(Container.LEFT_ALIGNMENT);
		//tableaux.setLayout(new ScrollPaneLayout());
//		tableaux.setLayout(new GridBagLayout());
		tableaux.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		//tableaux.setSize(new Dimension(WIDTH, HEIGHT));
		addRandom();
		// for (int i = 1500; --i >= 0;) {
		// addRandom();
		// }
		JScrollPane scrollFrame = new JScrollPane(tableaux);
		//scrollFrame.setAlignmentX(Container.LEFT_ALIGNMENT);
		//scrollFrame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		//scrollFrame.setSize(new Dimension(WIDTH, HEIGHT));
		//tableaux.setA(true);
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

//				JLabel l = new JLabel("_");
//				l.setLayout(new FlowLayout(FlowLayout.LEFT));
//
//				l.add(w);
//				l.setBackground(Color.BLUE);
//				w.setVisible(true);

				sentencePanel.add(w);
				
				for (Mention m : t.getEndMentions()) {
					Marker mentionLabel = new Marker("]");
					sentencePanel.add(mentionLabel);
					addMentioLabelPair(m, mentionLabel);
				}
			}
			tableaux.add(sentencePanel);
			sentencePanel.setAlignmentX(Container.LEFT_ALIGNMENT);
			
			//break;
		}
		//tableaux.add(Box.createVerticalGlue());

		// String[] arr = "Jānis_ devās_ uz_ savām_ mājām_".split(" ");
		// JPanel ff = new JPanel();
		// Mentions =
		// for (String w : arr) {
		// Word x = new Word(w);
		// Word y = new Word(w);
		// tableaux.add(new JLabel(" ["));
		// tableaux.add(x);
		// tableaux.add(y);
		// tableaux.add(new JLabel("] "));
		//
		// // Group g = new Group("tes");
		// // g.add(x);
		// // g.add(y);
		// //
		// // tableaux.add(g);
		// //x.setBounds(0, 0, 100, 16);
		//
		// // ff.add(x);
		// // frame.add(x);
		// // frame.getContentPane().add(x);
		// //letter.setBounds(random.nextInt(WIDTH), random.nextInt(HEIGHT), 16,
		// 16);
		//
		//
		// }
		// tableaux.add(ff);
		// Word letter = new Word(Character.toString((char) ('a' + random
		// .nextInt(26))));
		// letter.setBounds(random.nextInt(WIDTH), random.nextInt(HEIGHT), 16,
		// 16);
		// tableaux.add(letter);
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new CopyOfVisual());
	}
	
}
