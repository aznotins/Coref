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
package lv.coref.semantic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.io.Config;
import lv.coref.lv.Constants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.restlet.engine.connector.ConnectionState;

public class KNB {

	private final static Logger log = Logger.getLogger(KNB.class.getName());

	public static final int DEFAULT_FETCH_SIZE = 50;

	public static boolean VERBOSE = false;

	private Connection connection = null;

	private static KNB knb;

	public static KNB getInstance() {
		if (knb == null) {
			knb = new KNB();
			if (knb.connection == null)
				knb.init(Config.getInstance().filter(Config.PREFIX_KNB));
		}
		return knb;
	}

	public void init(Properties prop) {
		String url = prop.getProperty(Config.PROP_KNB_URL);
		String user = prop.getProperty(Config.PROP_KNB_USER);
		String password = prop.getProperty(Config.PROP_KNB_PASSWORD);
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Unable to connect to database", e);
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void debug(String query) {
		Level level = Level.SEVERE;
		System.err.println("=====");
		log.log(level, query);
		try (Statement stmt = connection.createStatement()) {
			stmt.setFetchSize(DEFAULT_FETCH_SIZE);
			try (ResultSet rs = stmt.executeQuery(query)) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				for (int i = 1; i <= columnCount; i++) {
					if (i > 1)
						System.err.print("\t");
					System.err.printf("%s(%s)", rsmd.getColumnName(i), rsmd.getColumnTypeName(i));
				}
				System.err.println("\n----");
				int counter = 0;
				while (rs.next()) {
					if (++counter % 20 == 0)
						log.log(level, "..Fetched %d records", counter);
					for (int i = 1; i <= columnCount; i++) {
						if (i > 1)
							System.err.print("\t");
						String columnValue = rs.getString(i);
						System.err.print(columnValue);
					}
					System.err.println();
				}
				log.log(level, "Fetched %d records in total", counter);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed query", e);
		}
	}

	public Map<String, EntityData> getEntityDataByName(String name, int limit) {
		Map<String, EntityData> r = new HashMap<>();
		int counter = 0;
		List<String> ids = getEntityIdsByName(name);
		log.info(String.format("Found %d matched entities", ids.size()));
		for (String id : ids) {
			if (counter >= limit && limit >= 0) {
				break;
			}
			EntityData ed = getEntityData(id, true);
			r.put(id, ed);
		}
		return r;
	}

	public Map<String, EntityData> getEntityData(List<String> ids) {
		Map<String, EntityData> r = new HashMap<>();
		for (String id : ids) {
			EntityData ed = getEntityData(id, false);
			r.put(id, ed);
		}
		return r;
	}

	public List<String> getEntityIdsByName(String name) {
		name = name.toLowerCase();
		String query = String
				.format("select distinct e.entityid from entityothernames n join entities e on n.entityid = e.entityid where lower(n.name) = '%s' and e.deleted is false and n.deleted is false",
						name);
		List<String> ids = new ArrayList<>();
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				while (rs.next()) {
					ids.add(rs.getString(1));
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving entity ids", e);
		}
		// System.err.printf("IDS for %s: %s\n", name, ids);
		return ids;
	}

	public CDCBags getCDCBags(String entityId) {
		String query = String.format("select wordbags from cdc_wordbags where entityid = %s", entityId);
		CDCBags bags = null;
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				while (rs.next()) {
					String jsonString = rs.getString(1);
					bags = new CDCBags(jsonString);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving CDCWordBags", e);
		}
		// System.err.println(bag);
		return bags;
	}

	public static class EntityData {
		public String name;
		public String id;
		public int category;
		public String inflections;
		public List<String> aliases = new ArrayList<>();
		public String outerId;

		public String toString() {
			return String.format("{id=%s cat=%s name='%s' outerid=%s aliases=%s inflections=%s]}", id, category, name,
					outerId, aliases, inflections);
		}
	}

	public EntityData getEntityData(String entityId, boolean allData) {
		String query = null;
		if (!allData)
			query = String.format(
					"select entityid, name, category from entities where deleted is false and entityid = %s", entityId);
		else
			query = String
					.format("select e.entityid, e.name, e.category, e.nameinflections, array_agg(n.name) aliases, min(i.outerid) "
							+ "ids from entities e left outer join (select * from entityothernames where deleted is false) n on "
							+ "e.entityid = n.entityid left outer join entityouterids i on e.entityid = i.entityid where "
							+ "e.entityid = %s and e.deleted is false group by e.entityid, e.name, e.category, e.nameinflections",
							entityId);
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				EntityData en = new EntityData();
				rs.next();
				en.id = rs.getString("entityid");
				en.name = rs.getString("name");
				en.category = rs.getInt("category");
				if (allData) {
					en.inflections = rs.getString("nameinflections");
					en.aliases.addAll(Arrays.asList((String[]) rs.getArray("aliases").getArray()));
					en.outerId = rs.getString(1);
				}
				return en;
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving entity data", e);
		}
		return null;
	}

	public static class FrameData {
		public int frameId;
		public Map<String, String> elements = new HashMap<>(); // { roleId =>
																// entityId }
		public int frameType;
		public boolean blessed;
		public String sourceId;
		public String targetWord;
		public String summaryInfo;
		public int frameCnt;

		public String toString() {
			return String.format("{frameId=%d type=%d elements=%s blessed=%s source=%s target=%s summary=%s cnt=%d]}",
					frameId, frameType, elements, blessed, sourceId, targetWord, summaryInfo, frameCnt);
		}
	}

	public List<FrameData> getSummaryFrameDataById(String entityId) {
		String query = String
				.format("select f.frameid, blessed, sourceid, frametypeid, summaryinfo, framecnt, targetword, json_agg(r) as elements from SummaryFrames f "
						+ "join (select frameid, roleid, entityid from SummaryFrameRoleData) r on r.frameid = f.frameid "
						+ "where f.frameid in (select frameid from SummaryFrameRoleData where entityid = %s) "
						+ "group by f.frameid, blessed, sourceid, frametypeid, summaryinfo, framecnt, targetword",
						entityId);
		List<FrameData> fds = new ArrayList<>();
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				while (rs.next()) {
					FrameData fd = new FrameData();
					fd.frameId = rs.getInt("frameid");
					fd.frameType = rs.getInt("frametypeid");
					fd.blessed = rs.getBoolean("blessed");
					fd.sourceId = rs.getString("sourceid");
					fd.targetWord = rs.getString("targetword");
					fd.summaryInfo = rs.getString("summaryinfo");
					fd.frameCnt = rs.getInt("framecnt");
					// System.err.println(rs.getObject("elements"));
					JSONArray jsonElements = (JSONArray) JSONValue.parse(rs.getString("elements"));
					for (int i = 0; i < jsonElements.size(); i++) {
						JSONObject jsonFr = (JSONObject) jsonElements.get(i);
						// System.err.println(jsonFr.get("roleid").getClass());
						// System.err.println(Integer.valueOf(((Long)jsonFr.get("roleid")).intValue()));
						// System.err.println((String) jsonFr.get("entityid"));
						String role = KNBUtils.getElementName(fd.frameType,
								Integer.valueOf(((Long) jsonFr.get("roleid")).intValue()));
						fd.elements.put(role, (String) Long.toString((Long) jsonFr.get("entityid")));
					}
					fds.add(fd);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving summary frame data", e);
		}
		return fds;
	}

	public List<String> getEntityTextFacts(String entityId) {
		String query = String.format("select id, text from entitytextfacts where entityid = %s", entityId);
		List<String> res = new ArrayList<>();
		try (Statement stmt = connection.createStatement()) {
			try (ResultSet rs = stmt.executeQuery(query)) {
				while (rs.next()) {
					while (rs.next()) {
						res.add(rs.getString("text"));
					}
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving CDCWordBags", e);
		}
		return res;
	}
	
	public static void dbgEntity(KNB knb, String name) {
		int limit = 10;
		int counter = 0;
		List<String> ids = knb.getEntityIdsByName(name);
		System.err.printf("Found %d matched entities for '%s'\n", ids.size(), name);
		for (String id : ids) {
			if (counter >= limit && limit >= 0) {
				break;
			}
			EntityData ed = knb.getEntityData(id, true);
			CDCBags bag = knb.getCDCBags(id);
			System.err.println(ed);
			System.err.println(bag);
		}
	}

	public static void main(String[] args) {
		Config.logInit();
		KNB knb = KNB.getInstance();
		knb.debug("select A.entityid, B.name, A.category from Entities as A inner join entityothernames as b on A.entityid = B.entityid limit 10");
		knb.debug("select * from entities limit 10");
		System.err.println(knb.getEntityIdsByName("Imants Ziedonis"));
		System.err.println(knb.getCDCBags("2203874"));
		System.err.println(knb.getCDCBags("2203873"));
		System.err.println(knb.getEntityData("2203874", false));
		System.err.println(knb.getEntityData("2203874", true));

		System.err.println(knb.getEntityDataByName("SIA \"BLC\"", 10));

		System.err.println(knb.getSummaryFrameDataById("2203874"));
		System.err.println(knb.getEntityTextFacts("2203874"));

		System.err.println(NEL.makeGlobalEntityBags("2203874"));
		
//		System.err.println(knb.getCDCBags("2203874").nameBag.values().iterator().next().getClass());

		KNB.getInstance().close();
	}
}
