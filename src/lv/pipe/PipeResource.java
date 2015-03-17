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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import lv.label.Annotation;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class PipeResource extends ServerResource {

	private static Pipe pipe = Pipe.getInstance();

//	static {
//		pipe.init();
//	}

	@Get
	public String retrieve() {
		String query = (String) getRequest().getAttributes().get("query");
		// System.err.println("GET: " + query);
		try {
			query = URLDecoder.decode(query, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return process(query);
	}

	@Post
	public String accept(String input) {
		String resp = process(input);
		// System.err.println("Response: " + resp);
		return resp;
	}

	@Options
	public void doOptions(Representation entity) {
		Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Form();
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		responseHeaders.add("Access-Control-Allow-Origin", "*");
		responseHeaders.add("Access-Control-Allow-Methods", "POST,GET,OPTIONS");
		responseHeaders.add("Access-Control-Allow-Headers", "Content-Type");
		responseHeaders.add("Access-Control-Allow-Credentials", "false");
		responseHeaders.add("Access-Control-Max-Age", "60");
	}

	public static String process(String text) {
		Annotation doc = pipe.process(text);
		return Annotation.getConllString(doc);
	}

}
