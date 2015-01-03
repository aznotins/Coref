package lv.coref.io;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import lv.coref.data.Text;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

public class Pipe {
	public static String SERVICE = "http://85.254.250.53:9999/nertagger/";
	//public static String SERVICE = "localhost/nertagger/";
	
	private String service;
	
	public Pipe() {
		this.service = SERVICE;
	}
	
	public Pipe(String service) {
		this.service = service;
	}
	
	public Text getText(String text) {
//		try {
//			text = URLEncoder.encode(text, "UTF8");
//		} catch (UnsupportedEncodingException e1) {
//			e1.printStackTrace();
//		}
		Text t = null;
		Client client = new Client(Protocol.HTTP);
	    Request r = new Request();
	    r.setResourceRef(service + text);
	    r.setMethod(Method.GET);
	    try {
			String result = client.handle(r).getEntityAsText();
			//System.err.println(result);
			StringReader sr = new StringReader(result);
			t = new ConllReaderWriter().getText(new BufferedReader(sr));
		} catch (Exception e) {
			e.printStackTrace();
		}
	    return t;
	}
	
	public static void main(String[] args) {
		Pipe p = new Pipe();
		System.out.println(p.getText("Jānis Kalniņš devās mājup.\n\n\nAsta vista."));
	}
}
