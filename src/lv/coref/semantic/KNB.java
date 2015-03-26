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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import lv.coref.io.Config;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.postgresql.util.PGobject;

public class KNB {

	private final static Logger log = Logger.getLogger(KNB.class.getName());

	public static final int DEFAULT_FETCH_SIZE = 50;
	
	public static enum CODE { OK, FAILED };
	
	private Connection connection = null;
	private int dataSet = 0;

	private static KNB knb;

	public static KNB getInstance() {
		if (knb == null) {
			knb = new KNB();
			if (knb.connection == null) {
				knb.init(Config.getInstance().filter(Config.PREFIX_KNB));
			}				
		}
		return knb;
	}

	public void init(Properties prop) {
		String url = prop.getProperty(Config.PROP_KNB_URL);
		String user = prop.getProperty(Config.PROP_KNB_USER);
		String password = prop.getProperty(Config.PROP_KNB_PASSWORD);
		dataSet = Integer.parseInt(prop.getProperty(Config.PROP_KNB_DATASET, "0"));
		try {
			connection = DriverManager.getConnection(url, user, password);
			connection.setAutoCommit(false);
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
	
	public int getDataSet() {
		return dataSet;
	}
	
	public void setDataSet(int dataSet) {
		this.dataSet = dataSet;
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
	
	/**
	 * Executes insert/update query without committing
	 * @param sql
	 */
	public void update(String sql) {
		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed insert/update query", e);
		}
	}

	public Map<Integer, EntityData> getEntityDataByName(String name, int limit) {
		Map<Integer, EntityData> r = new HashMap<>();
		int counter = 0;
		List<Integer> ids = getEntityIdsByName(name);
		log.info(String.format("Found %d matched entities", ids.size()));
		for (int id : ids) {
			if (counter >= limit && limit >= 0) {
				break;
			}
			EntityData ed = getEntityData(id, true);
			r.put(id, ed);
		}
		return r;
	}

	public Map<Integer, EntityData> getEntityData(List<Integer> ids) {
		Map<Integer, EntityData> r = new HashMap<>();
		for (Integer id : ids) {
			EntityData ed = getEntityData(id, false);
			r.put(id, ed);
		}
		return r;
	}

	public List<Integer> getEntityIdsByName(String name) {
		String query = "select distinct e.entityid from entityothernames n join entities e on n.entityid = e.entityid where lower(n.name) = ? and e.deleted is false and n.deleted is false";
		name = name.toLowerCase();
		List<Integer> ids = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, name);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					ids.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving entity ids", e);
		}
		// System.err.printf("IDS for %s: %s\n", name, ids);
		return ids;
	}

	/**
	 * Atgriež entītijas crossdocumentcoreference wordbagus pēc padotā entītijas
	 * ID
	 */
	public CDCBags getCDCBags(int entityId) {
		String query = "select wordbags from cdc_wordbags where entityid = ?";
		CDCBags bags = null;
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, entityId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					String jsonString = rs.getString(1);
					bags = new CDCBags(jsonString);
				} else {
					log.log(Level.WARNING, "Did not find CDCWordBags for {0}", entityId);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving CDCWordBags", e);
		}
		// System.err.println(bag);
		return bags;
	}
	
	public void deleteCDCBags(Integer entityId) {
		String deleteQuery = "delete from cdc_wordbags where entityId = ?";
		try (PreparedStatement delStmt = connection.prepareStatement(deleteQuery)) {			
			delStmt.setInt(1, entityId);
			delStmt.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			log.log(Level.SEVERE, String.format("Failed to delete CDCBags for %d", entityId), e);
		}
	}

	public void putCDCBags(CDCBags bags, Integer entityId) {
		String deleteQuery = "delete from cdc_wordbags where entityid = ?";
		String insertQuery = "insert into cdc_wordbags values (?, ?)";
		try (PreparedStatement delStmt = connection.prepareStatement(deleteQuery);
				PreparedStatement inStmt = connection.prepareStatement(insertQuery)) {
			
			delStmt.setInt(1, entityId);
			delStmt.executeUpdate();
			
			inStmt.setInt(1, entityId);			
			PGobject json = new PGobject();
			json.setType("json");
			json.setValue(bags.getJson().toJSONString());
			inStmt.setObject(2, json);
			inStmt.executeUpdate();
			
			connection.commit();
		} catch (SQLException e) {
			log.log(Level.SEVERE, String.format("Failed to insert CDCBags for %d : %s", entityId, bags), e);
		}
	}
	
	public static class EntityMentionData {
		public int entityId;
		public String documentId;
		public Boolean chosen;
		public Double cos_similarity;
		public Boolean blessed;
		public Boolean unclear;
		public String locations;
		public EntityMentionData() {}
		public EntityMentionData(int entityId, String documentId) {
			this.entityId = entityId;
			this.documentId = documentId;
		}
		public String toString() {
			return String.format("{%d in %s: blessed=%s, sim=%f, loc:%s}", entityId, documentId, blessed, cos_similarity, locations);
		}
	}
	
	public List<EntityMentionData> getEntityMentions(int entityId, String documentId) {
		String query = "select entityid, documentid, chosen, cos_similarity, blessed, unclear, locations from entitymentions where entityid = ? and documentid = ?";
		List<EntityMentionData> res = new LinkedList<>();
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, entityId);
			stmt.setString(2, documentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					EntityMentionData em = new EntityMentionData(entityId, documentId);
					em.chosen = rs.getBoolean("chosen");
					em.cos_similarity = rs.getDouble("cos_similarity");
					if (rs.wasNull()) em.cos_similarity = null;
					em.blessed = rs.getBoolean("blessed");
					em.unclear = rs.getBoolean("unclear");
					em.locations = rs.getString("locations");
					res.add(em);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving CDCWordBags", e);
		}
		return res;
	}
	
	public void deleteEntityMentions(int entityId, String documentId) {
		String deleteQuery = "delete from entitymentions where entityid = ? and documentid = ?";
		try (PreparedStatement delStmt = connection.prepareStatement(deleteQuery)) {
			delStmt.setInt(1, entityId);
			delStmt.setString(2, documentId);
			delStmt.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to delete EntityMention for {0} in {1}: {2}", new Object[] {entityId, documentId, e});
		}
	}
	
	public void putEntityMention(EntityMentionData em) {
		
		// int entityId, String documentId, Boolean chosen, Double cos_similarity, Boolean blessed, Boolean unclear, String locations
		String deleteQuery = "delete from entitymentions where entityid = ? and documentid = ?";
		String insertQuery = "insert into entitymentions (entityid, documentid, chosen, cos_similarity, blessed, unclear, locations) values (?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement delStmt = connection.prepareStatement(deleteQuery);
				PreparedStatement inStmt = connection.prepareStatement(insertQuery)) {
			delStmt.setInt(1, em.entityId);
			delStmt.setString(2, em.documentId);
			delStmt.executeUpdate();
			
			inStmt.setInt(1, em.entityId);
			inStmt.setString(2, em.documentId);
			if (em.chosen == null) inStmt.setNull(3, Types.BOOLEAN);
			else inStmt.setBoolean(3, em.chosen);
			if (em.cos_similarity == null) inStmt.setNull(4, Types.DOUBLE);
			else inStmt.setDouble(4, em.cos_similarity);
			if (em.blessed == null) inStmt.setNull(5, Types.BOOLEAN);
			else inStmt.setBoolean(5, em.blessed);
			if (em.unclear == null) inStmt.setNull(6, Types.BOOLEAN);
			else inStmt.setBoolean(6, em.unclear);
			inStmt.setString(7, em.locations);
			if (em.chosen == null) inStmt.setNull(7, Types.BOOLEAN);
			else inStmt.setBoolean(7, em.chosen);
			inStmt.executeUpdate();

			connection.commit();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to insert EntityMention", e);
		}
	}

	public static class EntityData {
		public String name;
		public Integer entityId;
		public int category;
		public String inflections;
		public List<String> aliases = new ArrayList<>();
		public List<String> outerIds = new ArrayList<>(1);
		public String outerId;
		public boolean hidden = false;
		public int cv_status = 0;
		public String source;
		
		public String toString() {
			return String.format("{id=%d cat=%s name='%s' outerid=%s aliases=%s inflections=%s]}", entityId, category, name,
					outerId, aliases, inflections);
		}
	}

	public EntityData getEntityData(int entityId, boolean allData) {
		String query = null;
		if (!allData)
			query = "select entityid, name, category from entities where deleted is false and entityid = ?";
		else
			query = "select e.entityid, e.name, e.category, e.nameinflections, array_agg(n.name) aliases, min(i.outerid) ids from entities e left outer join (select * from entityothernames where deleted is false) n on  e.entityid = n.entityid left outer join entityouterids i on e.entityid = i.entityid where e.entityid = ? and e.deleted is false group by e.entityid, e.name, e.category, e.nameinflections";
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, entityId);
			try (ResultSet rs = stmt.executeQuery()) {
				EntityData en = new EntityData();
				if (rs.next()) {
					en.entityId = rs.getInt("entityid");
					en.name = rs.getString("name");
					en.category = rs.getInt("category");
					if (allData) {
						en.inflections = rs.getString("nameinflections");
						en.aliases.addAll(Arrays.asList((String[]) rs.getArray("aliases").getArray()));
						en.outerId = rs.getString("ids");
					}
					return en;
				} else {
					throw new SQLException("Returned zero rows");
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving entity data for " + entityId, e);
		}
		return null;
	}
	
	/**
	 * Ievieto jaunu entītiju, atgriež jaunās entītijas id
	 * @param ed
	 */
	public Integer putEntity(EntityData ed, boolean commit) {
		int id = -1;
		String query = "INSERT INTO Entities(Name, OtherNames, OuterID, category, DataSet, NameInflections, Hidden, cv_status, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, ed.name);
			stmt.setBoolean(2, ed.aliases.size() > 0);
			stmt.setBoolean(3, ed.outerIds.size() > 0);
			stmt.setInt(4, ed.category);
			stmt.setInt(5, dataSet);
			stmt.setString(6, ed.inflections);
			stmt.setBoolean(7, ed.hidden);
			stmt.setInt(8, ed.cv_status);
			stmt.setString(9, ed.source);
			int affectedRows = stmt.executeUpdate();
			connection.commit();
			if (affectedRows == 0) {
	            throw new SQLException("Insert entity failed, no rows affected.");
	        }			
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) id = generatedKeys.getInt(1);
	            else throw new SQLException("Insert entity failed, no ID obtained.");
	        }
			
			putEntityOtherNames(id, ed.aliases, false, false);
			
			putEntityOuterIds(id, ed.outerIds, false);
			
			if (commit)
				connection.commit();		
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to insert Entity", e);
		}
		return id;
	}
	
	public void putEntityOtherNames(int entityId, List<String> aliases, boolean isAuthorative, boolean commit) {
		String query = "INSERT INTO EntityOtherNames(EntityID, Name, isAuthorative) VALUES (?, ?, ?)";
		if (aliases == null || aliases.size() == 0) return;
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			for (String alias : aliases) {
				stmt.setInt(1, entityId);
				stmt.setString(2,alias);
				stmt.setBoolean(3, isAuthorative);
				stmt.addBatch();
			}
			stmt.executeBatch();
			if (commit) connection.commit();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to insert EntityOtherNames for {0}: {1}", new Object[] {entityId, e});
		}
	}
	
	public void putEntityOuterIds(int entityId, List<String> outerIds, boolean commit) {
		String query = "INSERT INTO EntityOuterIDs(EntityID, OuterID) VALUES (?, ?)";
		if (outerIds == null || outerIds.size() == 0) return;
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			for (String outerId : outerIds) {
				stmt.setInt(1, entityId);
				stmt.setString(2, outerId);
				stmt.addBatch();
			}
			stmt.executeBatch();
			if (commit) connection.commit();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to insert EntityOuterIds for {0}: {1}", new Object[] {entityId, e});
		}
	}
	
	public void deleteEntity(int entityId, boolean fullDelete) {
		if (fullDelete) {
			update(String.format("DELETE FROM FrameData where EntityID = %d", entityId));
			update(String.format("DELETE FROM SummaryFrameRoleData where EntityID = %d", entityId));
			update(String.format("DELETE FROM EntityMentions where EntityID = %d", entityId));
			update(String.format("DELETE FROM EntityOtherNames where EntityID = %d", entityId));
			update(String.format("DELETE FROM EntityOuterIDs where EntityID = %d", entityId));			
			update(String.format("DELETE FROM Entities where EntityID = %d", entityId));
		} else {
			update(String.format("UPDATE Entities set Deleted = True where EntityID = %d", entityId));
		}
		try {
			connection.commit();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to delete Entity", e);
		}
	}

	public static class FrameData {
		public int frameId;
		public Map<String, Integer> elements = new HashMap<>(); // { roleId =>
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

	public List<FrameData> getSummaryFrameDataById(Integer entityId) {
		String query = "select f.frameid, blessed, sourceid, frametypeid, summaryinfo, framecnt, targetword, json_agg(r) as elements from SummaryFrames f "
				+ "join (select frameid, roleid, entityid from SummaryFrameRoleData) r on r.frameid = f.frameid "
				+ "where f.frameid in (select frameid from SummaryFrameRoleData where entityid = ?) "
				+ "group by f.frameid, blessed, sourceid, frametypeid, summaryinfo, framecnt, targetword";
		List<FrameData> fds = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, entityId);
			try (ResultSet rs = stmt.executeQuery()) {
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
						fd.elements.put(role, ((Long) jsonFr.get("entityid")).intValue());
					}
					fds.add(fd);
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving summary frame data", e);
		}
		return fds;
	}

	public List<String> getEntityTextFacts(Integer entityId) {
		String query = "select id, text from entitytextfacts where entityid = ?";
		List<String> res = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, entityId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					res.add(rs.getString("text"));
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed retrieving CDCWordBags", e);
		}
		return res;
	}
	
	public Set<Integer> getBlessedEntityMentions(String documentId) {
		String query = "select entityid from entitymentions where blessed is true and documentid = ?";
		Set<Integer> res = new HashSet<>();
		try (PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setString(1, documentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					res.add(rs.getInt(1));
				}
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "Failed to retrieve blessed entity mentions for " + documentId, e);
		}
		return res;
	}

	public static void dbgEntity(KNB knb, String name) {
		int limit = 10;
		int counter = 0;
		List<Integer> ids = knb.getEntityIdsByName(name);
		System.err.printf("Found %d matched entities for '%s'\n", ids.size(), name);
		for (int id : ids) {
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
		
		// knb.debug("select A.entityid, B.name, A.category from Entities as A inner join entityothernames as b on A.entityid = B.entityid limit 10");
		// knb.debug("select * from entities limit 10");
//		System.err.println(knb.getEntityIdsByName("Imants Ziedonis"));
//		System.err.println(knb.getCDCBags(2203874));
//		System.err.println(knb.getCDCBags(2203873));
//		System.err.println(knb.getEntityData(2203874, false));
//		System.err.println(knb.getEntityData(2203874, true));
//
//		System.err.println(knb.getEntityDataByName("SIA \"BLC\"", 10));
//
//		System.err.println(knb.getSummaryFrameDataById(2203874));
//		System.err.println(knb.getEntityTextFacts(2203874));
//
//		System.err.println(NEL.makeGlobalEntityBags(2203874));

		// System.err.println(knb.getCDCBags("2203874").nameBag.values().iterator().next().getClass());

		KNB.getInstance().close();
	}
}
