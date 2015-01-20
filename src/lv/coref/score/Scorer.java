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
package lv.coref.score;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Scorer {
	double tp = 0;
	double fp = 0;
	double fn = 0;

	double p = 0;
	double r = 0;
	double f1 = 0;

	public double getTP() {
		return tp;
	}

	public void setTP(double count) {
		tp = count;
	}
	
	public void addTP(double count) {
		tp += count;
	}

	public void addTP() {
		addTP(1);
	}
	
	public void setFP(double count) {
		fp = count;
	}

	public double getFP() {
		return fp;
	}

	public void addFP(double count) {
		fp += count;
	}

	public void addFP() {
		addFP(1);
	}
	
	public void setFN(double count) {
		fn = count;
	}

	public double getFN() {
		return fn;
	}

	public void addFN(double count) {
		fn += count;
	}

	public void addFN() {
		addFN(1);
	}

	public double getPrecision() {
		return p;
	}
	
	public void setPrecision(double p) {
		this.p = p;
	}
	
	public double getRecall() {
		return r;
	}
	
	public void setRecall(double r) {
		this.r = r;
	}
	
	public double getF1() {
		return f1;
	}
	
	public void setF1(double f1) {
		this.f1 = f1;
	}
	
	public void calculate() {
		p = (tp + fp) > 0 ? 1.0 * tp / (tp + fp) : 0;
		r = (tp + fn) > 0 ? 1.0 * tp / (tp + fn) : 0;
		f1 = (p + r > 0) ? 2 * p * r / (p + r) : 0;
	}
	
	public double getCalculatedPrecision() {
		return (tp + fp) > 0 ? 1.0 * tp / (tp + fp) : 0;
	}

	public double getCalculatedRecall() {
		return (tp + fn) > 0 ? 1.0 * tp / (tp + fn) : 0;
	}

	public double getcalculatedF1() {
		double p = getCalculatedPrecision();
		double r = getCalculatedRecall();
		return (p + r > 0) ? 2 * p * r / (p + r) : 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		NumberFormat nf = new DecimalFormat("0.00");
		NumberFormat nfsimple = new DecimalFormat("0");

		sb.append(nf.format(getF1() * 100));
		sb.append("\t").append(nf.format(getPrecision() * 100));
		sb.append("\t").append(nf.format(getRecall() * 100));
		sb.append("\t").append(nfsimple.format(getTP()));
		sb.append("\t").append(nfsimple.format(getFP()));
		sb.append("\t").append(nfsimple.format(getFN()));

		return sb.toString();

	}
}
