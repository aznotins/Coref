package tmp;

import java.io.File;
import java.util.UUID;

import lv.coref.data.Mention;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.ConllReaderWriter;
import lv.coref.io.Pipe;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;
import lv.coref.score.SummaryScorer;

public class Test {
	
	public static void resolveAndScore(String refFile, String hypFile) throws Exception {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text text = rw.read(hypFile);
		MentionFinder mf = new MentionFinder();
		mf.findMentions(text);
		Ruler r = new Ruler();
		r.resolve(text);
		
		//text.removeSingletons();
		
		System.out.println(text);
		rw.write("tmp/" + new File(text.getId()).getName() + "_" + UUID.randomUUID() + ".conll", text);
		
		Text goldText = new ConllReaderWriter().read(refFile);
		text.setPairedText(goldText);
		goldText.setPairedText(text);
		
		SummaryScorer summaryScorer = new SummaryScorer();
		summaryScorer.add(text);
		System.err.println(summaryScorer);
	}

	public static Text resolveFile(String filename) throws Exception {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text t = rw.read(filename);
		return resolve(t);
	}
	
	public static Text resolve(Text t) {
		MentionFinder mf = new MentionFinder();
		mf.findMentions(t);
		Ruler r = new Ruler();
		r.resolve(t);
		return t;
	}
	
	public static Text resolve(String text) {
		Text t = new Pipe().getText(text);
		return resolve(t);
	}
	
	public static void test() throws Exception {
		ConllReaderWriter rw = new ConllReaderWriter();
//		Text t = rw.getText("sankcijas.conll");
//		Text t = rw.getText("news_63.conll");	
		Text t = rw.read("data/test.conll");	
		
		Text gold = new ConllReaderWriter().read("data/test.corefconll");
		t.setPairedText(gold);
		gold.setPairedText(t);
		
		MentionFinder mf = new MentionFinder();
		mf.findMentions(t);
		//System.out.println(t);
		
		main:
		for (Paragraph p : t) {
			for (Sentence s : p) {
				//System.out.println(s.getRootNode().getNestedNodeString(true, 0));
				for (Mention m : s.getMentions()) {
//					System.out.println(m);
//					System.out.println("\t" + m.getMention(false));
					//System.out.println("ANTECEDETNS: " + m.getPotentialAntecedents(-1, -1, 2));
				}
			}
		}
		
		Ruler r = new Ruler();
		r.resolve(t);
		
		System.out.println(t);
		//rw.write("test.out", t);
		//new ConllReaderWriter().write("tmp/test.out", t);
		
//		for (MentionChain mc : t.getMentionChains()) {
//			if (mc.size() > 1) System.out.println(mc);
//		}
		
		SummaryScorer summaryScorer = new SummaryScorer();
		summaryScorer.add(t);
		System.err.println(summaryScorer);
		
//		SwingUtilities.invokeLater(new Viewer(t));
//		SwingUtilities.invokeLater(new Viewer(gold));
		
	}
	

	
	public static void main(String[] args) throws Exception {
//		test();
//		resolveAndScore("data/corpus/corefconll/interview_23.corefconll","data/corpus/conll/interview_23.conll");
		
//		Text text = resolveFile("data/test/sankcijas.conll");
//		new ConllReaderWriter().write("data/test/out/" + new File(text.getId()).getName()+ "_out", text);
//		for (Sentence s : text.getSentences()) {
//			System.err.println(s);
//			for (Mention m : s.getOrderedMentions()) {
//				System.err.println(" - " + m  + " \t\t" + m.toParamString());
//			}
//		}
		
		Text t = resolve("Jānis Kalniņš devās mājup.");
		System.out.println(t);
		
	}

}
