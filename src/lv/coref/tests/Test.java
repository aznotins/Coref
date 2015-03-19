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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import lv.coref.data.Text;
import lv.coref.io.Config.FORMAT;
import lv.coref.io.ConllReaderWriter;
import lv.coref.io.CorefPipe;
import lv.coref.io.JsonReaderWriter;
import lv.coref.io.ReaderWriter;
import lv.coref.mf.MentionFinder;
import lv.coref.rules.Ruler;

public class Test {

	public static void pipeTest() throws Exception {
		CorefPipe p = new CorefPipe(FORMAT.CONLL, FORMAT.JSON);

		OutputStream out = new FileOutputStream(new File("data/problem/merged.out"));

		Vector<InputStream> stream = new Vector<>();
		stream.add(new FileInputStream(new File("data/problem/07577F6B-5DE8-42D1-8CA2-9A5387F54A3C.stage2.conll")));
		stream.add(new ByteArrayInputStream("\n\n\n\n".getBytes(StandardCharsets.UTF_8)));
		stream.add(new FileInputStream(new File("data/problem/1324D392-A6F7-4969-ABA2-B6FE861BA2BD.stage2.conll")));
		SequenceInputStream in = new SequenceInputStream(stream.elements());

		p.setInputStream(in);
		p.setOutputStream(out);
		p.run();
	}

	public static void singleTest() throws Exception {
		ReaderWriter rw = new ConllReaderWriter(ConllReaderWriter.TYPE.LETA);
		Text t = rw.read("data/problem/07577F6B-5DE8-42D1-8CA2-9A5387F54A3C.stage2.conll");
		new MentionFinder().findMentions(t);
		new Ruler().resolve(t);
		t.removeCommonUnknownSingletons();
		rw.write("data/problem/07577F6B-5DE8-42D1-8CA2-9A5387F54A3C.stage2.coref_conll", t);
		new JsonReaderWriter().write("data/problem/07577F6B-5DE8-42D1-8CA2-9A5387F54A3C.stage2.coref_json", t);
		System.out.println(t);
	}

	public static void main(String[] args) throws Exception {
//		singleTest();
		pipeTest();
	}
}
