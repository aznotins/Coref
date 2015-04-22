/*******************************************************************************
 * Copyright 2014,2015 Institute of Mathematics and Computer Science, University of Latvia
 * Author: ArtÅ«rs ZnotiÅ†Å�
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Config {
	private final static Logger log = Logger.getLogger(Config.class.getName());

	public static final String DEFAULT_CONFIG_FILE = "coref.prop";
	
	public static String PROP_CONFIG_FILE = "config.file";
	
	private static Config config = null;
	
	public static Config getInstance() {
		if (config == null) {
			config = new Config();
			config.load(DEFAULT_CONFIG_FILE);
		}
		return config;
	}
	
	public static void init(String[] args) {
		config = new Config();
		config.load(args);
	}

	private Properties props = new Properties();
	
	public void load(String filename) {
		try {
			props.load(new FileInputStream(filename));
			props.setProperty(PROP_CONFIG_FILE, filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
						load(value);
					}
					props.setProperty(key, value);
				}
			}
		}
	}
	
	public static void logInit() {
		String logConfigFile = DEFAULT_CONFIG_FILE;
		if (getInstance().props.contains(PROP_CONFIG_FILE)) {
			logConfigFile = getInstance().props.getProperty(PROP_CONFIG_FILE);
		}
		LogManager.getLogManager().reset();
		try {			
			FileInputStream fis = new FileInputStream(logConfigFile);
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

	public Properties filter(String prefix) {
		return filter(props, prefix);
	}

	public static Properties filter(Properties prop, String prefix) {
		Properties res = new Properties();
		for (Entry<Object, Object> entry : prop.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (key.startsWith(prefix)) {
				res.setProperty(key, value);
			}
		}
		return res;
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
	
	public boolean containsKey(String key) {
		return props.containsKey(key);
	}
	
	public boolean isTrue(String key) {
		String value = props.getProperty(key);
		if (value != null && value.equalsIgnoreCase("true"))
			return true;
		return false;
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
				sb.append(String.format("-%-20s\t%-20s\t%-20s\t%s\n", field.get(this), props.getProperty(field.get(this).toString()), descr != null ? descr.def() : "null",
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

	public static final String PREFIX_PIPE = "pipe.";

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
	
	@Property(descr = "Runned pipe tools [tokenizer, tagger, ner, parser, spd, coref, nel]", def = "tokenizer tagger ner parser spd coref nel")
	public static final String PROP_PIPE_TOOLS = "pipe.tools";
	
	@Property(descr = "Pipe input format [text, json_meta]", def = "")
	public static final String PROP_PIPE_INPUT = "pipe.input";
	
	@Property(descr = "Pipe output format [json, json_array]", def = "tokenizer tagger ner parser spd coref nel")
	public static final String PROP_PIPE_OUTPUT = "pipe.output";

	@Property(descr = "Remove singletons during postprocessing", def = "false")
	public static final String PROP_COREF_REMOVE_SINGLETONS = "coref.remSingletons";
	
	@Property(descr = "Remove common unknown category singletons during postprocessing", def = "false")
	public static final String PROP_COREF_REMOVE_COMMON_UKNOWN_SINGLETONS = "coref.remCommonUnknownSingletons";
	
	@Property(descr = "Remove descriptor mentions for professions", def = "false")
	public static final String PROP_COREF_REMOVE_DESCRIPTOR_MENTIONS = "coref.remDescriptors";
	
	@Property(descr = "Print decisions for head or exact match mentionsm separate with: |", def = "")
	public static final String PROP_COREF_DEBUG_MENTION_STRINGS = "coref.debugMentionStrings";
	
	public static final String PREFIX_KNB = "knb.";

	@Property(descr = "Knowledge base url", def = "jdbc:postgresql://localhost:5432/knb", type = "String")
	public static String PROP_KNB_URL = "knb.url";

	@Property(descr = "Knowledge base user", def = "user", type = "String")
	public static String PROP_KNB_USER = "knb.user";

	@Property(descr = "Knowledge base user password", def = "password", type = "String")
	public static String PROP_KNB_PASSWORD = "knb.password";
	
	@Property(descr = "Use database for NEL [true,false]", def = "true")
	public static String PROP_KNB_ENABLE = "knb.enable";
	
	@Property(descr = "KNB dataset [int]", def = "0")
	public static String PROP_KNB_DATASET = "knb.dataset";

	@Property(descr = "Preprocessing pipeline webservice (produces conll with ner and syntax from text)", def = "http://localhost:8182/nertagger")
	public static String PROP_PREPROCESS_WEBSERVICE = "web.pipe";
	
	public static final String PREFIX_NEL = "nel.";
	
	@Property(descr = "Prints NEL disambiguation information to std.err [true,false]", def = "false")
	public static String PROP_NEL_SHOW_DISAMBIGUATION = "nel.showDisambiguation";
	
	@Property(descr = "Print inserted entities [true,false]", def = "false")
	public static String PROP_NEL_SHOW_INSERTS = "nel.showInserts";
	
	@Property(descr = "Print linked entities [true,false]", def = "false")
	public static String PROP_NEL_SHOW_ENTITIES = "nel.showEntities";
	
	@Property(descr = "Allow NEL to upload entities to database [true,false]", def = "false")
	public static String PROP_NEL_UPLOAD = "nel.upload";
	
	@Property(descr = "Print all verbose NEL decisions [true,false]", def = "false")
	public static String PROP_NEL_VERBOSE = "nel.verbose";
	
	public static void main(String[] args) {
		Config cc = new Config();
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