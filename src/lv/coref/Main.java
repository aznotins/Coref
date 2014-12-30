package lv.coref;

import java.io.IOException;

import lv.coref.data.*;
import lv.coref.io.ConllReaderWriter;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;

public class Main {

	public static void main(String[] args) throws IOException {
		ConllReaderWriter rw = new ConllReaderWriter();
//		Text t = rw.getText("sankcijas.conll");
//		Text t = rw.getText("news_63.conll");	
		Text t = rw.getText("data/test.conll");	
		
		Text gold = new ConllReaderWriter().getText("data/test.corefconll");
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
		
		t.finalizeMentionChains();
		
		System.out.println(t);
		rw.write("test.out", t);
		
		for (MentionChain mc : t.getMentionChains()) {
			if (mc.size() > 1) System.out.println(mc);
		}
		
	}

}
