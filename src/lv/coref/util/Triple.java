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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class Triple<T1, T2, T3> implements Serializable {

	private static final long serialVersionUID = -4182871682751645440L;
	public T1 first;
	public T2 second;
	public T3 third;

	public Triple(T1 first, T2 second, T3 third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public T1 first() {
		return first;
	}

	public T2 second() {
		return second;
	}

	public T3 third() {
		return third;
	}

	public void setFirst(T1 o) {
		first = o;
	}

	public void setSecond(T2 o) {
		second = o;
	}

	public void setThird(T3 o) {
		third = o;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (!(o instanceof Triple)) {
			return false;
		}

		final Triple triple = (Triple) o;

		if (first != null ? !first.equals(triple.first) : triple.first != null) {
			return false;
		}
		if (second != null ? !second.equals(triple.second) : triple.second != null) {
			return false;
		}
		if (third != null ? !third.equals(triple.third) : triple.third != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = (first != null ? first.hashCode() : 0);
		result = 29 * result + (second != null ? second.hashCode() : 0);
		result = 29 * result + (third != null ? third.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "(" + first + "," + second + "," + third + ")";
	}

	public List<Object> asList() {
		return Arrays.asList(first, second, third);
	}

	/**
	 * Returns a Triple constructed from X, Y, and Z. Convenience method; the
	 * compiler will disambiguate the classes used for you so that you don't
	 * have to write out potentially long class names.
	 */
	public static <X, Y, Z> Triple<X, Y, Z> makeTriple(X x, Y y, Z z) {
		return new Triple<X, Y, Z>(x, y, z);
	}

}
