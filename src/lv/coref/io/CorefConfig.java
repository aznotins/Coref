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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class CorefConfig {
	private final static Logger log = Logger.getLogger(CorefConfig.class.getName());

	private Properties props = new Properties();

	public void load(String[] args) {
		// props = StringUtils.argsToProperties(args);
		for (int i = 0; i < args.length; i++) {
			String key = args[i];
			if (key.length() > 0 && key.charAt(0) == '-') {
				if (key.charAt(1) == '-')
					key = key.substring(2);
				else
					key = key.substring(1);
				if (i < args.length) {
					String value = args[i + 1];
					if (key.equalsIgnoreCase(PROP)) {
						try {
							props.load(new FileInputStream(value));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					props.setProperty(key, value);
				}
			}
		}
	}

	public static void logConfig(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			LogManager.getLogManager().readConfiguration(fis);
			fis.close();			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void set(String key, String value) {
		props.setProperty(key, value);
	}

	public String get(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public String get(String key) {
		return props.getProperty(key);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Field field : this.getClass().getDeclaredFields()) {
			Property descr = null;
			for (Annotation a : field.getAnnotations()) {
				if (Property.class.isAssignableFrom(a.getClass())) {
					descr = (Property) a;
					break;
				}
			}
			if (!field.getName().startsWith("PROP"))
				continue;
			try {
				sb.append(String.format("-%-20s\t%-20s\t%s\n", field.get(this), descr != null ? descr.def() : "null",
						descr != null ? descr.descr() : "null"));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	@Property(descr = "Property file")
	private static final String PROP = "prop";

	public static enum FORMAT {
		CONLL, JSON, MMAX
	};

	@Property(descr = "Input type [conll, json, mmax]", def = "conll", type = "lv.coref.io.FORMAT")
	public static final String PROP_INPUT = "input";

	public FORMAT getINPUT() {
		String value = props.getProperty(PROP_INPUT, "conll");
		return FORMAT.valueOf(value.toUpperCase());
	}

	@Property(descr = "Output type [conll, json, mmax]", def = "conll", type = "lv.coref.io.FORMAT")
	public static final String PROP_OUTPUT = "output";

	public FORMAT getOUTPUT() {
		String value = props.getProperty(PROP_OUTPUT, "conll");
		return FORMAT.valueOf(value.toUpperCase());
	}

	@Property(descr = "Do coreference resolution [true, false]", def = "true", type = "Boolean")
	public static final String PROP_SOLVE = "solve";

	public boolean getSOLVE() {
		String value = props.getProperty(PROP_SOLVE, "true");
		return value.equalsIgnoreCase("yes");
	}

	@Property(descr = "Knowledge base driver", def = "org.postgresql.Driver", type = "String")
	private static String PROP_KNB_DRIVER = "knb.driver";

	public String getKNB_DRIVER() {
		return props.getProperty(PROP_KNB_DRIVER, "org.postgresql.Driver");
	}

	@Property(descr = "Knowledge base url", def = "jdbc:postgresql://localhost:5432/knb", type = "String")
	private static String PROP_KNB_URL = "knb.url";

	public String getKNB_URL() {
		return props.getProperty(PROP_KNB_URL, "jdbc:postgresql://localhost:5432/knb");
	}

	@Property(descr = "Knowledge base user", def = "user", type = "String")
	private static String PROP_KNB_USER = "knb.user";

	public String getKNB_USER() {
		return props.getProperty(PROP_KNB_USER, "postgres");
	}

	@Property(descr = "Knowledge base user password", def = "password", type = "String")
	private static String PROP_KNB_PASSWORD = "knb.password";

	public String getKNB_PASSWORD() {
		return props.getProperty(PROP_KNB_PASSWORD, "password");
	}
	
	@Property(descr = "Preprocessing pipeline webservice (produces conll with ner and syntax from text)", def = "http://localhost:8182/nertagger")
	private static String PROP_PREPROCESS_WEBSERVICE = "web.pipe";

	public String getPREPROCESS_WEBSERVICE() {
		return props.getProperty(PROP_PREPROCESS_WEBSERVICE, "http://localhost:8182/nertagger");
	}
	
	public static void main(String[] args) {
		CorefConfig cc = new CorefConfig();
		System.err.println(cc);

	}
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Property {
	String descr() default "N/A";

	String def() default "N/A";

	String type() default "String";
}