package lv.coref.transform;

import lv.coref.data.Node;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.ConllReaderWriter;

public class Transformer {
	public static void main(String[] args) {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text t;
		t = rw.getText("news_63.conll");
		
		for (Paragraph p : t) {
			for (Sentence s : p) {
				System.out.println(s);
				for (Node n : s.getNodes(false)) {
					if (n.getLabel().endsWith("pred")) {
						//System.out.println(n.getNestedNodeString(true));
						
						System.out.println(n.getHeads());
						for (Node child : n.getChildren()) {
							System.out.println("\t" + child.getLabel() + " " + child.getTokens());
						}
						
					}
				}
			}
		}
	}
}
