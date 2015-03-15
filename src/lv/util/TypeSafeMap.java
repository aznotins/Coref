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

import java.util.Map;
import java.util.Set;

public interface TypeSafeMap {

	public interface Key<VALUE> {
	}

	public <VALUE, KEY extends Key<VALUE>> boolean has(Class<KEY> key);

	public <VALUE, KEY extends Key<VALUE>> VALUE get(Class<KEY> key);
	
	public <VALUE, KEY extends Key<VALUE>> VALUE get(Class<KEY> key, VALUE defaultValue);

	public <VALUE, KEY extends Key<VALUE>> VALUE set(Class<KEY> key, VALUE value);

	public <VALUE, KEY extends Key<VALUE>> VALUE remove(Class<KEY> key);

	public Set<Map.Entry<Class<?>, Object>> entrySet();

	public int size();

}
