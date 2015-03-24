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
package lv.pipe;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.JsonReaderWriter;
import lv.label.Annotation;
import lv.util.FileUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class PipeTest {
	Pipe pipe;
	String file1 = "resource/sample/test_taube.txt";
	String file2 = "resource/sample/test_taube.txt";
	String jsonStr1;
	String jsonStr2;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		Config.logInit();
		pipe = Pipe.getInstance();
		JSONObject json1 = new JSONObject();
		String txt1 = FileUtils.readFile(file1);
		json1.put("text", txt1);
		json1.put("document", "id1");
		json1.put("date", "01/01/2015");
		jsonStr1 = json1.toJSONString();

		JSONObject json2 = new JSONObject();
		String txt2 = FileUtils.readFile(file2);
		json2.put("text", txt2);
		json2.put("document", "id2");
		json2.put("date", "02/02/2015");
		jsonStr2 = json2.toJSONString();
	}

	@AfterClass
	public static void tearDown() {
		Pipe.close();
		System.exit(0);
	}

	@Test
	public void testSingleFile() throws Exception {
		pipe.setInputStream(new ByteArrayInputStream(jsonStr1.getBytes(StandardCharsets.UTF_8)));
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pipe.setOutputStream(bos);
		pipe.run();
		String output = null;
		try {
			output = bos.toString("utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.err.println(output);
		JSONObject json = (JSONObject) JSONValue.parse(output);
		Text t = new JsonReaderWriter().read(json);
		assertEquals("01/01/2015", t.getDate());
		assertEquals("id1", t.getId());
	}

	@Test
	public void testMultipleFiles() throws Exception {
		Vector<InputStream> stream = new Vector<>();
		stream.add(new ByteArrayInputStream(jsonStr1.getBytes(StandardCharsets.UTF_8)));
		stream.add(new ByteArrayInputStream("\n\n".getBytes(StandardCharsets.UTF_8)));
		stream.add(new ByteArrayInputStream(jsonStr2.getBytes(StandardCharsets.UTF_8)));
		SequenceInputStream in = new SequenceInputStream(stream.elements());

		pipe.setInputStream(in);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		pipe.setOutputStream(bos);
		pipe.run();
		String output = null;
		try {
			output = bos.toString("utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String[] outputs = output.trim().split("(\\r?\\n)+");
		assertEquals(outputs.length, 2);

		JSONObject json = (JSONObject) JSONValue.parse(outputs[0]);
		Text t = new JsonReaderWriter().read(json);
		assertEquals(t.getDate(), "01/01/2015");
		assertEquals(t.getId(), "id1");

		JSONObject json2 = (JSONObject) JSONValue.parse(outputs[1]);
		Text t2 = new JsonReaderWriter().read(json2);
		assertEquals(t2.getDate(), "02/02/2015");
		assertEquals(t2.getId(), "id2");
	}

	@Test
	public void makeReadJSON() throws IOException {
		String txtString = "Jānis Bērziņš";
		File temp = File.createTempFile("coreftmp", ".tmp");
		Annotation doc = Pipe.getInstance().process(txtString);
		doc.printJson(new PrintStream(temp));
		String jsonStr = FileUtils.readFile(temp.getAbsolutePath());
		JSONObject json = (JSONObject) JSONValue.parse(jsonStr);
		JSONArray sents = (JSONArray) json.get("sentences");
		JSONObject sent = (JSONObject) sents.get(0);
		String jsonText = (String) sent.get("text");
		assertEquals(txtString, jsonText);
	}

	@Test
	public void testJsonMentions() throws IOException {
		String txtString = "Jānis Bērziņš. Pēteris Kalniņš.";
		File temp = File.createTempFile("coreftmp", ".tmp");
		Annotation doc = Pipe.getInstance().process(txtString);
		// System.err.println(doc.toStringPretty());
		doc.printJson(new PrintStream(temp));
		String jsonStr = FileUtils.readFile(temp.getAbsolutePath());
		JSONObject json = (JSONObject) JSONValue.parse(jsonStr);
		
		Set<Integer> mentionIds = new HashSet<>();
		Set<Integer> entityIds = new HashSet<>();
		
		JSONObject nes = (JSONObject) json.get("namedEntities");
		for (Object key : nes.keySet()) {
			int keyId = Integer.parseInt((String)key);
			int id = ((Long)((JSONObject)nes.get((String)key)).get("id")).intValue();
			assertEquals(keyId, id);
			entityIds.add(id);
		}
		assertEquals(true, entityIds.size() > 0);
		
		JSONArray sents = (JSONArray) json.get("sentences");
		for (int iSent = 0; iSent < sents.size(); iSent++) {
			JSONObject sent = (JSONObject) sents.get(iSent);
			JSONArray tokens = (JSONArray) sent.get("tokens");
			for (int iTok = 0; iTok < tokens.size(); iTok++) {
				JSONObject t = (JSONObject) tokens.get(iTok);
				if (t.containsKey("mentions")) {
					JSONArray mentions = (JSONArray) t.get("mentions");
					for (int iMent = 0; iMent < mentions.size(); iMent++) {
						JSONObject mention = (JSONObject) mentions.get(iMent);
						mentionIds.add(((Long) mention.get("id")).intValue());
					}
				}
			}
		}
		assertEquals(true, mentionIds.size() > 0);
		
		for (int mid : mentionIds) {
			assertEquals(true, entityIds.contains(mid));
		}
		for (int eid : entityIds) {
			assertEquals(true, mentionIds.contains(eid));
		}
	}

	@Test
	public void AnnotationTest() {
		Annotation doc = Pipe.getInstance().process(
				"Uzņēmuma SIA \"Cirvis\" prezidents Jānis Bērziņš. Viņš uzņēmumu vada no 2015. gada.");
		System.err.println(doc.toStringPretty());
		System.err.println(Annotation.getConllString(doc));
	}

}