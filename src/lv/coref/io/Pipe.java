package lv.coref.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import lv.coref.data.Text;
import lv.coref.util.FileUtils;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

public class Pipe {
	public static String SERVICE = "http://85.254.250.53:9999/nertagger";
//	public static String SERVICE = "http://localhost:8182/nertagger";
	Client client;
	private String service;
	
	public Pipe() {
		this(SERVICE);
	}
	
	public Pipe(String service) {
		this.service = service;
		client = new Client(Protocol.HTTP);
		try {
			client.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Text getText(String text) {
//		try {
//			text = URLEncoder.encode(text, "UTF8");
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		}
		Text t = null;
	    Request r = new Request();
	    r.setResourceRef(service);
	    r.setMethod(Method.POST);
	    r.setEntity(text, MediaType.TEXT_PLAIN);
	    try {
			String result = client.handle(r).getEntityAsText();
			//System.err.println(result);
			StringReader sr = new StringReader(result);
			t = new ConllReaderWriter().read(new BufferedReader(sr));
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    return t;
	}
	
	public Text getTextGet(String text) {
//		try {
//			text = URLEncoder.encode(text, "UTF8");
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		}
		Text t = null;
	    Request r = new Request();
	    r.setResourceRef(service + "/" + text);
	    r.setMethod(Method.GET);
	    try {
			String result = client.handle(r).getEntityAsText();
			//System.err.println(result);
			StringReader sr = new StringReader(result);
			t = new ConllReaderWriter().read(new BufferedReader(sr));
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return t;
	}
	
	public Text read(String filename) throws IOException {
		String textString = FileUtils.readFile(filename);
		Text text = getText(textString);
		return text;
	}
	
	public static void main(String[] args) {
		Pipe p = new Pipe();
		System.out.println(p.getText("Jānis Kalniņš devās mājup.\n\n\nAsta vista."));
		//System.out.println(p.getText("Jānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\nJānis Kalniņš devās mājup. Asta vista.\n\n"));
		//System.out.println(p.getText("Jānis Kalniņš devās mājup.\n\n\nAsta vista."));
	}
}
