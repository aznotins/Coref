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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KNBUtils {

	private final static Logger log = Logger.getLogger(KNBUtils.class.getName());

	public static String[] frameTypes = { "Being_born", "People_by_age", "Death", "Personal_relationship",
			"Being_named", "Residence", "Education_teaching", "People_by_vocation", "People_by_origin",
			"Being_employed", "Hiring", "Employment_end", "Membership", "Change_of_leadership", "Giving",
			"Intentionally_create", "Participation", "Earnings_and_losses", "Possession", "Lending", "Trial", "Attack",
			"Win_prize", "Statement", "Public_procurement", "Product_line", "Unstructured" };

	public static String getFrameName(int code) {
		if (code >= 0 && code < frameTypes.length)
			return frameTypes[code];
		else {
			log.log(Level.WARNING, "Bad frame code: {0}", code);
			return "";
		}
	}

	public static String[][] frameElements = {
			{ "Child", "Time", "Place", "Relatives" },
			{ "Person", "Age" },
			{ "Protagonist", "Time", "Place", "Manner", "Cause" },
			{ "Partner_1", "Partner_2", "Partners", "Relationship", "Time" },
			{ "Name", "Entity", "Type" },
			{ "Resident", "Location", "Frequency", "Time" },
			{ "Student", "Institution", "Subject", "Qualification", "Time", "Place" },
			{ "Person", "Vocation", "Time", "Descriptor" },
			{ "Origin", "Person", "Ethnicity" },
			{ "Employee", "Employer", "Position", "Compensation", "Place_of_employment", "Time", "Manner",
					"Employment_start", "Employment_end" },
			{ "Employee", "Employer", "Position", "Appointer", "Manner", "Place", "Time", "Previous_employee" },
			{ "Employee", "Employer", "Position", "Appointer", "Manner", "Place", "Time", "Future_employee" },
			{ "Member", "Group", "Time", "Standing" },
			{ "Candidate", "Body", "Role", "New_leader", "Result", "Time", "Place" },
			{ "Donor", "Recipient", "Theme", "Time" },
			{ "Created_entity", "Creator", "Manner", "Industry", "Time", "Place" },
			{ "Participant_1", "Event", "Time", "Place", "Manner", "Institution" },
			{ "Earner", "Earnings", "Goods", "Profit", "Time", "Unit", "Growth" },
			{ "Owner", "Possession", "Time", "Share" },
			{ "Borrower", "Lender", "Theme", "Collateral", "Time", "Units" },
			{ "Defendant", "Charges", "Court", "Prosecutor", "Lawyer", "Judge", "Place", "Time" },
			{ "Victim", "Assailant", "Result", "Circumstances", "Depictive", "Reason", "Weapon", "Manner", "Place",
					"Time" },
			{ "Competitor", "Prize", "Competition", "Result", "Rank", "Time", "Place", "Organizer", "Opponent" },
			{ "Medium", "Speaker", "Message", "Time" },
			{ "Institution", "Theme", "Expected_amount", "Candidates", "Winner", "Result", "Time" },
			{ "Brand", "Institution", "Products" }, { "Entity", "Property", "Category" } };

	public static String getElementName(int frameCode, int elementCode) {
		String name = "";
		try {
			// -1, jo freimu lomas numurējas no 1 (0-tā - target)
			name = frameElements[frameCode][elementCode - 1];
		} catch (ArrayIndexOutOfBoundsException e) {
			log.log(Level.WARNING, "No such element: frameCode={0}, elementCode={1}", new Object[] { frameCode,
					elementCode });
		}
		return name;
	}

	public static String[] entityTypes = { null, "location", "organization", "person", "profession", "sum", "time",
			"relationship", "qualification", "descriptor", "relatives", "prize", "media", "product", "event",
			"industry" };
	
	public static Map<String, Integer>entityTypeCodes = new HashMap<>();
	static {
		entityTypeCodes.put("location", 1);
		entityTypeCodes.put("organization", 2);
		entityTypeCodes.put("person", 3);
		entityTypeCodes.put("profession", 4);
		entityTypeCodes.put("sum", 5);
		entityTypeCodes.put("time", 6);
		entityTypeCodes.put("relationship", 7);
		entityTypeCodes.put("qualification", 8);
		entityTypeCodes.put("descriptor", 9);
		entityTypeCodes.put("relatives", 10);
		entityTypeCodes.put("prize", 11);
		entityTypeCodes.put("media", 12);
		entityTypeCodes.put("product", 13);
		entityTypeCodes.put("event", 14);
		entityTypeCodes.put("industry", 15);		
	}
	
	public static int getEntityTypeCode(String name) {
		if (name == null)
			return 0;
		if (name == "organizations")
			name = "organization"; // TODO - salabo reālu situāciju datos, taču nav skaidrs kurā brīdī tādi bugaini dati tika izveidoti.
		Integer code = entityTypeCodes.get(name);
		if (code == null) {
			log.log(Level.WARNING, "No such EntityCategory: {0}", name);
			return 0;
		} else {
			return code;
		}
	}

	public static String getEntityTypeName(int code) {
		for (String name : entityTypeCodes.keySet()) {
			if (entityTypeCodes.get(name) == code)
				return name;
		}
		log.log(Level.WARNING, "No such EntityTypeCode: {0}", code);
		return "";
	}

}
