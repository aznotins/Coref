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
package lv.coref.util;

/**
 * Reads the output of a process started by Process.exec()
 *
 * Adapted from:
 *
 * http://www.velocityreviews.com/forums/t130884-process-runtimeexec-causes-subprocess-hang.html
 *
 * @author pado
 *
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

public class StreamGobbler extends Thread {

	InputStream is;
	Writer outputFileHandle;

	public StreamGobbler(InputStream is, Writer outputFileHandle) {
		this.is = is;
		this.outputFileHandle = outputFileHandle;
	}

	public void run() {

		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			for (String s; (s = br.readLine()) != null;) {
				outputFileHandle.write(s);
				outputFileHandle.write("\n");
			}

			isr.close();
			br.close();
			outputFileHandle.flush();
		} catch (Exception ex) {
			System.out.println("Problem reading stream :"
					+ is.getClass().getCanonicalName() + " " + ex);
			ex.printStackTrace();
		}

	}
}
