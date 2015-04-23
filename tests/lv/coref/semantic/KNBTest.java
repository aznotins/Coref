package lv.coref.semantic;

import static org.junit.Assert.*;

import java.util.List;

import lv.coref.semantic.KNB.EntityData;
import lv.coref.semantic.KNB.EntityMentionData;
import lv.coref.semantic.KNB.FrameData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KNBTest {

	KNB knb = KNB.getInstance();
	
	/**
	 * Existing entity with already blessed summary frames
	 */
	String EXISTING_ENTITY_NAME = "Andris Bērziņš";
	Integer EXISTING_ENTITY_ID = null;
	
	String TEST_ENTITY_NAME = "_TEST_ENTITY_";
	int TEST_ENTITY_ID_1 = -999999;
	int TEST_ENTITY_ID_2 = -999998;
	int TEST_ENTITY_ID_3 = -999997;
	
	@Before
	public void setUp() {
		EXISTING_ENTITY_ID = KNB.getInstance().getEntityIdsByName(EXISTING_ENTITY_NAME).get(0);
	}
	
	@After
	public void tearDown() {
		// delete test data from database
		List<Integer> testEntities = knb.getEntityIdsByName(TEST_ENTITY_NAME);
		for (int id : testEntities) {
			knb.deleteEntity(id, true);
		}
		knb.deleteEntity(TEST_ENTITY_ID_1, true);
		knb.deleteEntity(TEST_ENTITY_ID_2, true);
		knb.deleteEntity(TEST_ENTITY_ID_3, true);
	}
	
	@Test
	public void setUpTest() {
		assertNotNull(EXISTING_ENTITY_ID);		
	}

	@Test
	public void testGetCDCBags() {
		CDCBags bags = KNB.getInstance().getCDCBags(EXISTING_ENTITY_ID);
		assertNotNull(bags);
	}

	@Test
	public void testPutDeleteCDCBags() {
		CDCBags bagsJanis = new CDCBags();
		bagsJanis.nameBag.add(new String[] { "Jānis", "Jānis Jānis", "biedētājs" });
		bagsJanis.mentionBag.add(new String[] { "SIA Cirvis" });
		bagsJanis.contextBag.add(new String[] { "vadīt" });
		
		Integer entityId = TEST_ENTITY_ID_2;
		
		knb.putCDCBags(bagsJanis, entityId);
		CDCBags bags = KNB.getInstance().getCDCBags(entityId);		
		assertEquals(bagsJanis, bags);

		knb.deleteCDCBags(entityId);
		assertNull(KNB.getInstance().getCDCBags(entityId));
	}
	
	@Test
	public void testPutDeleteEntityMention() {
		EntityMentionData em = new EntityMentionData(-999, "_TEST_DOCUMENT_");
		knb.putEntityMention(em);
		List<EntityMentionData> mentions = knb.getEntityMentions(em.entityId, em.documentId);
		System.err.println(mentions);
		assertTrue(mentions.size() == 1);
		knb.deleteEntityMentions(em.entityId, em.documentId);
		mentions = knb.getEntityMentions(em.entityId, em.documentId);
		System.err.println(mentions);
		assertTrue(mentions.size() == 0);
	}

	@Test
	public void testGetEntity() {
		EntityData ed = knb.getEntityData(EXISTING_ENTITY_ID, true);
		assertNotNull(ed);
		assertNotNull(ed.name);
		assertTrue(ed.category > 0);
	}
	
	@Test
	public void testPutDeleteEntity() {
		EntityData ed = new EntityData();
		ed.name = TEST_ENTITY_NAME;
		ed.aliases.add("_TEST_ENTITY_ALIAS_");
		ed.outerIds.add("_TEST_ENTITY_OUTER_ID_");
		ed.source = "_TEST_";
		ed.category = 0;
		
		int newId = knb.putEntity(ed, true);
		System.err.printf("Inserted new entity: %d", newId);
		assertTrue(newId > 0);
		
		EntityData x = knb.getEntityData(newId, true);
		assertNotNull(x);
		assertEquals(ed.name, x.name);
		assertEquals(ed.aliases.get(0), x.aliases.get(0));
		assertEquals(ed.outerIds.get(0), x.outerId);
		assertEquals(ed.category, x.category);
		
		knb.deleteEntity(newId, true);
		
		x = knb.getEntityData(newId, true);
		assertNull(x);
	}

	@Test
	public void testGetSummaryFrameDataById() {
		List<FrameData> fds = knb.getSummaryFrameDataById(EXISTING_ENTITY_ID);
		assertTrue(fds.size() > 0);
	}

}
