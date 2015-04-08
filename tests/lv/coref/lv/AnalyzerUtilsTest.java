package lv.coref.lv;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import lv.coref.lv.Constants.Category;

import org.junit.Test;

public class AnalyzerUtilsTest {

	@Test
	public void testNormalizeAndraVilka() {
		assertEquals("Andris Vilks", AnalyzerUtils.normalize("Andra Vilka", "person"));
	}

	@Test
	public void testNormalizeAndriVilku() {
		assertEquals("Andris Vilks", AnalyzerUtils.normalize("Andri Vilku", "person"));
	}

	@Test
	public void testNormalizeAndraAmbaina() {
		assertEquals("Andris Ambainis", AnalyzerUtils.normalize("Andra Ambaiņa", "person"));
	}

	@Test
	public void testInflectAndrisAmbainis() {
		Map<String, String> inflections = AnalyzerUtils.inflect("Andris Ambainis", "person");
		assertEquals("Andra Ambaiņa", inflections.get("Ģenitīvs"));
		assertEquals("Andri Ambaini", inflections.get("Akuzatīvs"));
	}
	
	@Test
	public void testNormalize5() {
		assertEquals("Nacionālais veselības dienests", AnalyzerUtils.normalize("Nacionālā veselības dienesta", "organization"));
		assertEquals("Nacionālais veselības dienests", AnalyzerUtils.normalize("Nacionālo veselības dienestu", "org"));
	}
	@Test
	public void testNormalize6() {
		assertEquals("Labklājības ministrija", AnalyzerUtils.normalize("Labklājības ministrijas", "organization"));
	}
}
