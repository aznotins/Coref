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
package lv.coref.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import lv.coref.data.Text;
import lv.label.Annotation;
import lv.util.FileUtils;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

public class PipeClient {
	private final static Logger log = Logger.getLogger(PipeClient.class.getName());

	public static String SERVICE = "http://localhost:8183/pipe";

	private Client client;
	private String service;

	public PipeClient() {
		this(SERVICE);
	}

	private static PipeClient pipe;

	public static PipeClient getInstance() {
		if (pipe == null) {
			pipe = new PipeClient();
		}
		return pipe;
	}

	public PipeClient(String service) {
		log.log(Level.FINE, "Init pipe");
		this.service = service;
		client = new Client(Protocol.HTTP);
		try {
			client.start();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to start client", e);
		}
		// FIXME restlet traucē logošanai, jāpārlādē konfigurācija
		Config.logInit();
		log.log(Level.INFO, "Init pipe");
	}

	public void close() {
		try {
			client.stop();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to start client", e);
		}
	}

	public Text getText(String text) {
		Text t = null;
		Request r = new Request();
		r.setResourceRef(service);
		r.setMethod(Method.POST);
		r.setEntity(text, MediaType.TEXT_PLAIN);
		try {

			String result = client.handle(r).getEntityAsText();
//			System.err.println(result);
			if (result == null) {
				throw new Exception("Null in response");
			}
			StringReader sr = new StringReader(result);
			t = new ConllReaderWriter().read(new BufferedReader(sr));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to get response (please run PipeServer)", e);
		}

		return t;
	}
	
	public Annotation getAnnotation(String text) {
		Annotation a = null;
		Request r = new Request();
		r.setResourceRef(service);
		r.setMethod(Method.POST);
		r.setEntity(text, MediaType.TEXT_PLAIN);
		try {
			String result = client.handle(r).getEntityAsText();
			if (result == null) {
				throw new Exception("Null in response");
			}
			StringReader sr = new StringReader(result);
			Text t = new ConllReaderWriter().read(new BufferedReader(sr));
			a = Annotation.makeAnnotationFromText(new Annotation(), t);
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to get POST response", e);
		}
		return a;
	}
	
	public Text getTextGet(String text) {
		Text t = null;
		Request r = new Request();
		r.setResourceRef(service + "/" + text);
		r.setMethod(Method.GET);
		try {
			String result = client.handle(r).getEntityAsText();
			// System.err.println(result);
			StringReader sr = new StringReader(result);
			t = new ConllReaderWriter().read(new BufferedReader(sr));
		} catch (Exception e) {
			log.log(Level.SEVERE, "Unable to get GET response", e);
		}
		return t;
	}

	public Text read(String filename) throws IOException {
		String textString = FileUtils.readFile(filename);
		Text text = getText(textString);
		return text;
	}

	public static void main(String[] args) {
		Config.logInit();
		PipeClient p = new PipeClient();
		System.out.println(p.getText("Jānis Kalniņš devās mājup.\n\nAlta vista."));
		System.out.println(p.getTextGet("Jānis Kalniņš devās mājup.\n\n\nAsta vista."));
		log.severe("Test logging");
		p.close();
	}
}
