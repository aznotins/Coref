package lv.coref.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;

import org.junit.Test;

public class IOTest {

	@Test
	public void conllReaderWriterTest() throws IOException {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text text = rw.getText("news_63.conll");
		int parCount = text.size();
		int sentCount = 0;
		int tokCount = 0;
		for (Paragraph par : text) {
			assertTrue("Non empty paragraph", par.size() > 0);
			for (Sentence sent : par) {
				sentCount++;
				assertTrue("Non empty sentence", sent.size() > 0);
				for (Token tok : sent) {
					tokCount++;
				}
			}
		}
		assertTrue("Paragraph total count=" + parCount, parCount == 1);
		assertTrue("Sentence total count=" + sentCount, sentCount == 15);
		assertTrue("Token total count=" + tokCount, tokCount == 284);		
	}
	
	

}
