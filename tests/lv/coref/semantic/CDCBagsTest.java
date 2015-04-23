package lv.coref.semantic;

import static org.junit.Assert.*;

import org.json.simple.JSONObject;
import org.junit.Test;

public class CDCBagsTest {

	@Test
	public void testGetJson() {
		CDCBags bagsJanis = new CDCBags();
		bagsJanis.nameBag.add(new String[] { "Jānis", "Jānis Bērziņš", "vadītājs" });
		bagsJanis.mentionBag.add(new String[] { "SIA Cirvis" });
		bagsJanis.contextBag.add(new String[] { "vadīt" });
		
		JSONObject json = bagsJanis.getJson();
		assertTrue(json.containsKey("mentionbag"));
		JSONObject mentions = (JSONObject) json.get("mentionbag");
		assertTrue(mentions.containsKey("SIA Cirvis"));
		assertTrue(json.containsKey("namebag"));
		JSONObject names = (JSONObject) json.get("namebag");
		assertTrue(names.containsKey("Jānis"));
		assertTrue(json.containsKey("contextbag"));
		JSONObject contexts = (JSONObject) json.get("contextbag");
		assertTrue(contexts.containsKey("vadīt"));
		
		assertEquals(bagsJanis, new CDCBags(json.toJSONString()));
	}

}
