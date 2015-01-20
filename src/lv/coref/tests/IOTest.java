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
package lv.coref.tests;

import static org.junit.Assert.assertTrue;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.data.Token;
import lv.coref.io.ConllReaderWriter;

import org.junit.Test;

public class IOTest {

	@Test
	public void conllReaderWriterTest() throws Exception {
		ConllReaderWriter rw = new ConllReaderWriter();
		Text text = rw.read("data/test.conll");
		
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
		assertTrue("Sentence total count=" + sentCount, sentCount == 130);
		assertTrue("Token total count=" + tokCount, tokCount == 2391);		
	}
	
	

}
