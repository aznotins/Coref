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

import java.io.OutputStream;

public class StringOutputStream extends OutputStream {

	StringBuilder sb = new StringBuilder();

	public StringOutputStream() {
	}

	synchronized public void clear() {
		sb.setLength(0);
	}

	@Override
	synchronized public void write(int i) {
		sb.append((char) i);
	}

	@Override
	synchronized public String toString() {
		return sb.toString();
	}

}
