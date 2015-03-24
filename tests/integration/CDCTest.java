package integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import lv.coref.data.Mention;
import lv.coref.data.MentionChain;
import lv.coref.data.Text;
import lv.coref.io.Config;
import lv.coref.io.CorefPipe;
import lv.coref.io.PipeClient;
import lv.coref.score.Scorer;
import lv.coref.semantic.CDCBags;
import lv.coref.semantic.Entity;
import lv.coref.semantic.NEL;
import lv.label.Annotation;
import lv.pipe.Pipe;
import lv.util.FileUtils;

public class CDCTest {

	public static String ENTITY_KEY = "entity#";
	
	public static boolean USE_PIPE_CLIENT = true;

	public static Text resolve(String filename) {
		String txt = "";
		try {
			txt = FileUtils.readFile(filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Text text = null;
		if (!USE_PIPE_CLIENT) {
			Pipe.getInstance();
		}
		Config.getInstance().set(Config.PROP_KNB_ENABLE, "false");
		if (USE_PIPE_CLIENT) {
			text = PipeClient.getInstance().getText(txt);
			CorefPipe.getInstance().process(text);
		} else {
			String fileContents;
			try {
				fileContents = FileUtils.readFile(filename);
				text = Pipe.getInstance().processText(new Annotation(fileContents));
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		return text;
	}

	public static MentionChain getCluster(String name, Text text) {
		MentionChain res = null;
		for (MentionChain mc : text.getMentionChains()) {
			Mention m = mc.getRepresentative();
			String title = m.getString();
			if (title.equalsIgnoreCase(name)) {
				res = mc;
				break;
			}
		}
		return res;
	}

	public static void runTest(Properties prop) {
		File dir = new File(prop.getProperty("directory"));
		List<String> names = new ArrayList<>();
		String namesString = prop.getProperty("name");
		String[] nameStrings = namesString.split("\\s*\\|\\s*");
		for (String name : nameStrings) {
			names.add(name.trim());
		}
		int limitFiles = Integer.parseInt(prop.getProperty("limitFiles", "100000"));
		Map<Integer, String> entities = new HashMap<>();
		for (Object oKey : prop.keySet()) {
			String key = (String) oKey;
			String value = dir.getPath() + "/" + prop.getProperty(key);
			if (key.startsWith(ENTITY_KEY)) {
				Integer id = Integer.parseInt(key.substring(key.indexOf(ENTITY_KEY) + ENTITY_KEY.length()));
				entities.put(id, value);
			}
		}
		// System.err.println("Entities: " + entities);
		Map<Integer, CDCBags> globalBags = new HashMap<>(); // cache candidates {
															// id => cdcBags }
		Map<Integer, Scorer> scorers = new HashMap<>();
		Scorer scorer = new Scorer();
		for (Integer eid : entities.keySet()) {
			System.err.println("--- Entity: " + eid + " in: " + entities.get(eid));
			File entityFolder = new File(entities.get(eid));
			scorers.put(eid, new Scorer());
			int counter = 1;
			for (File txt : entityFolder.listFiles()) {
				System.err.println(txt);
				if (counter++ > limitFiles)
					break;
				if (!txt.getName().endsWith(".txt"))
					continue;
				Text text = resolve(txt.getAbsolutePath());
				if (text == null) {
					System.err.println("Couldn't resolve document, returned null");
					continue;
				}
				MentionChain mc = null;
				for (String name : names) {
					mc = getCluster(name, text);
					if (mc != null)
						break; // already found main entity cluster
				}
				if (mc == null) {
					System.err.printf("Couldn't find entity cluster %s in %s\n", names, txt.getPath());
					continue;
				}
				Entity e = Entity.makeEntity(mc);
				Set<Integer> candidatesId = NEL.getInstance().getGlobalIdCandidates(e);

				CDCBags eBags = new CDCBags();
				eBags.nameBag = CDCBags.makeNameBag(e);
				eBags.mentionBag = CDCBags.makeMentionBagFromMentions(text.getMentionChains());
				eBags.contextBag = CDCBags.makeContextBag(e);

				double maxSim = 0.0;
				Integer maxId = null;
				Map<Integer, Double> cosineSim = new HashMap<>();
				for (Integer candidateId : candidatesId) {
					CDCBags bags = globalBags.get(candidateId);
					if (bags == null) {
						bags = NEL.makeGlobalEntityBags(candidateId);
						globalBags.put(candidateId, bags);
					}
//					bags = NEL.makeGlobalEntityBags(candidateId);
					double sim = CDCBags.cosineSimilarity(bags, eBags);
					cosineSim.put(candidateId, sim);
					// System.err.printf("Candidate: %s %s\n", candidateId,
					// bags);
					// System.err.printf("Cosine similarity: %.4f\n", sim);
					if (sim > maxSim) {
						maxSim = sim;
						maxId = candidateId;
					}
				}
				System.err.println("Cosine similarities: " + cosineSim);
				if (maxId == null) {
					System.err.println("maxId = NULL in " + entityFolder.getPath());
					System.err.println(mc);
					scorer.addFN(1);
					scorers.get(eid).addFN(1);
				} else if (maxSim == 0) {
					System.err.println("maxSim = NULL in " + entityFolder.getPath());
					System.err.println(mc);
					scorer.addFN(1);
					scorers.get(eid).addFN(1);
				} else if (maxId.equals(eid)) {
					scorer.addTP(1);
					scorers.get(eid).addTP(1);
				} else {
					System.err.printf("NOT EQUAL: %d %d", eid, maxId);
					scorer.addFN(1);
					scorers.get(eid).addFN(1);
					scorer.addFP(1);
					scorers.get(eid).addFP(1);
				}
				scorer.calculate();
				System.err.println("Results so far: " + scorer);
			}
		}
		scorer.calculate();
		System.err.println("---- RESULTS ----");
		System.err.println(scorer);
		for (Integer eid : scorers.keySet()) {
			scorers.get(eid).calculate();
			System.err.println(eid + " : " + scorers.get(eid));
		}
	}

	public static void runTest(String propFile) {
		Properties prop = new Properties();
		try {

			prop.load(new InputStreamReader(new FileInputStream(propFile), "UTF8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		runTest(prop);
	}

	public static void testBuildCDC() {
		System.err.println(NEL.makeGlobalEntityBags(2204831));
		System.err.println(NEL.makeGlobalEntityBags(2204832));
		System.err.println(NEL.makeGlobalEntityBags(2204833));
		System.err.println(NEL.makeGlobalEntityBags(2204834));
	}

	public static void main(String[] args) {
		Config.logInit();
		testBuildCDC();
		runTest("resource/testdata/Vilki/vilki_test.prop");
		Pipe.close();
		System.exit(0);
	}

}
