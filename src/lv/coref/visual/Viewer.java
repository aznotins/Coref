package lv.coref.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
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
import lv.coref.data.MentionChain;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;

public class Viewer implements Runnable, ActionListener {

	Text text;
	TextMapping textMapping = new TextMapping();

	private static final int WIDTH = 500;
	private static final int HEIGHT = 800;
	private Random random = new Random();
	private JFrame frame = new JFrame("LVCoref");
	private JPanel textPanel;
	private JPanel corefPanel;

	Viewer(Text text) {
		this.text = text;
	}
	
	@Override
	public void run() {
		textPanel = new JPanel();
		
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
		//textPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setText(text);		
		JScrollPane scrollFrame = new JScrollPane(textPanel);
		frame.add(scrollFrame, BorderLayout.CENTER);
		textPanel.requestFocusInWindow();
		
		corefPanel = new JPanel();
		corefPanel.setLayout(new BoxLayout(corefPanel, BoxLayout.PAGE_AXIS));
		JScrollPane corefScrollFrame = new JScrollPane(corefPanel);
		corefScrollFrame.setPreferredSize(new Dimension(300, -1));
		frame.add(corefScrollFrame, BorderLayout.EAST);
		setCorefPanel(text);
		

//		JButton add = new JButton("Add");
//		add.addActionListener(this);
//		corefPanel.add(add);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(WIDTH, HEIGHT);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		
		
		
		//textPanel.repaint();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		//setText();
	}
	
	void setCorefPanel(Text text) {
		for (MentionChain mc : text.getMentionChains()) {
			if (mc.size() < 2) continue;
			corefPanel.add(new JLabel("=== " + mc.getID() + " === " + mc.getRepresentative()));
			for (Mention m : mc) {
				StringBuilder sb = new StringBuilder("<html>" + m.toString() + "</html>");
				if (sb.length() > 30) sb.insert(30, "\n\t");
				
				MentionMarker mentionMarker = new MentionMarker(sb.toString(), textMapping, m);
				corefPanel.add(mentionMarker);
			}
		}
	}

	void setText(Text text) {

		List<Sentence> sentences = text.getSentences();
		for (Sentence s : sentences) {
			JPanel sentencePanel = new JPanel();
			sentencePanel.setLayout(new WrapLayout(FlowLayout.LEFT));
			sentencePanel.setSize(new Dimension(WIDTH, 1));

			for (Token t : s) {
				t.getMentions();

				for (Mention m : t.getOrderedStartMentions()) {
					MentionMarker mentionMarker = new MentionMarker(
							"<html><sub>[</sub></html>", textMapping, m);
					textMapping.addMentionMarkerPair(m, mentionMarker);
					sentencePanel.add(mentionMarker);
				}

				TokenMarker tokenMarker = new TokenMarker(t.getWord() + " ",
						textMapping, t);
				textMapping.addTokenMarkerPair(t, tokenMarker);
				sentencePanel.add(tokenMarker);

				for (Mention m : t.getOrderedEndMentions()) {
					String endText = "<html><sub>]</sub></html>";
					if (m.getMentionChain() != null
							&& m.getMentionChain().size() > 1)
						endText = "<html><sub>" + m.getMentionChain().getID()
								+ "]</sub></html>";

					MentionMarker mentionMarker = new MentionMarker(endText,
							textMapping, m);
					// textMapping.addMentionMarkerPair(m, mentionMarker);
					sentencePanel.add(mentionMarker);
				}
			}
			textPanel.add(sentencePanel);
			sentencePanel.setAlignmentX(Container.LEFT_ALIGNMENT);
			
			// break;
		}
	}

	public static void main(final String[] args) {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text text = rw.getText("data/test.conll");
		MentionFinder mf = new MentionFinder();
		mf.findMentions(text);
		Ruler r = new Ruler();
		r.resolve(text);

		text.finalizeMentionChains();
		
//		Text gold = new ConllReaderWriter().getText("news_63_gold.conll");
//		text.setPairedText(gold);
//		gold.setPairedText(text);		
		SwingUtilities.invokeLater(new Viewer(text));
	}

}
