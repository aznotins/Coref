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
package lv.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileUtils {

	public static List<String> getFiles(String baseDir, int limit, int skip,
			String endsWith) {
		List<String> res = new ArrayList<>();
		File dir = new File(baseDir);
		int res_counter = 0;
		int counter = 0;
		for (File f : dir.listFiles()) {
			if (counter++ < skip)
				continue;
			if (f.isFile() && f.getName().endsWith(endsWith)) {
				res.add(f.getAbsolutePath());
				if (limit > 0 && ++res_counter >= limit)
					break;
			}
		}
		return res;
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	public static String readFile(String path) throws IOException {
		return readFile(path, Charset.forName("UTF-8"));
	}

	public static Set<String> getSetFromColumn(String filename, int column,
			String seperator) {
		Set<String> result = new HashSet<>();
		try {
			result = getSetFromColumn(new File(filename).toURI().toURL(),
					column, seperator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Set<String> getSetFromColumn(URL file, int column,
			String seperator) {
		Set<String> result = new HashSet<String>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					file.openStream()));
			String line;
			while (true) {
				line = reader.readLine();
				if (line == null)
					break;
				if (line.length() == 0)
					continue;
				String[] bits = line.split(seperator);
				if (column < bits.length) {
					result.add(bits[column]);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

}
