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
package lv.coref.transform;

import lv.coref.data.Node;
import lv.coref.data.Paragraph;
import lv.coref.data.Sentence;
import lv.coref.data.Text;
import lv.coref.io.ConllReaderWriter;
import lv.coref.io.ReaderWriter;

public class Transformer {
	public static void main(String[] args) throws Exception {
		ReaderWriter rw = new ConllReaderWriter();
		Text t = rw.read("news_63.conll");
		
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
