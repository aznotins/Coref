package lv.coref.lv;

import lv.coref.lv.Constants.*;
import lv.coref.lv.Constants.Number;

public class MorphoUtils {

	public static PosTag getPosTag(String tag) {
		if (tag.charAt(0) == 'n') return PosTag.N;
		else if (tag.charAt(0) == 'v') return PosTag.V;
		else if (tag.charAt(0) == 'a') return PosTag.ADJ;
		else if (tag.charAt(0) == 'p') return PosTag.P;
		else if (tag.charAt(0) == 'r') return PosTag.ADV;
		else if (tag.charAt(0) == 's') return PosTag.PRE;
		else if (tag.charAt(0) == 'c') return PosTag.CONJ;
		else if (tag.charAt(0) == 'm') return PosTag.NUM;
		else if (tag.charAt(0) == 'i') return PosTag.INTERJ;
		else if (tag.charAt(0) == 'q') return PosTag.PART;
		else if (tag.charAt(0) == 'z') return PosTag.PUNC;
		else if (tag.charAt(0) == 'x') return PosTag.X;
		else return PosTag.UNKNOWN;
	}
	
	public static Number getNumber(String tag) {
		if (getPosTag(tag) == PosTag.N) {
			if (tag.charAt(3) == 's') return Number.SG;
			else if (tag.charAt(3) == 'p') return Number.PL;
			else return Number.UNKNOWN;
		}
		if (getPosTag(tag) == PosTag.P) {
			if (tag.charAt(4) == 's') return Number.SG;
			else if (tag.charAt(4) == 'p') return Number.PL;
			else return Number.UNKNOWN;
		}
		return Number.UNKNOWN;
	}
	
	public static Gender getGender(String tag) {
		if (getPosTag(tag) == PosTag.N) {
			if (tag.charAt(2) == 'm') return Gender.M;
			else if (tag.charAt(2) == 'f') return Gender.F;
			else return Gender.UNKNOWN;
		}
		if (getPosTag(tag) == PosTag.P) {
			if (tag.charAt(3) == 'm') return Gender.M;
			else if (tag.charAt(3) == 'f') return Gender.F;
			else return Gender.UNKNOWN;
		}
		return Gender.UNKNOWN;
	}
	
	public static Case getCase(String tag) {
		if (getPosTag(tag) == PosTag.N) {
			switch (tag.charAt(4)) {
				case 'n': return Case.NOM;
				case 'g': return Case.GEN;
				case 'd': return Case.DAT;
				case 'a': return Case.ACC; 
				case 'l': return Case.LOC;
				case 'v': return Case.VOC; 
				//case 's': return Case.NOM; //�enet�venis
				default: return Case.UNKNOWN;
			}
		}
		if (getPosTag(tag) == PosTag.P) {
			switch (tag.charAt(5)) {
				case 'n': return Case.NOM;
				case 'g': return Case.GEN;
				case 'd': return Case.DAT;
				case 'a': return Case.ACC; 
				case 'l': return Case.LOC;
				case 'v': return Case.VOC; 
				//case 's': return Case.NOM; //�enet�venis
				default: return Case.UNKNOWN;
			}
		}
		return Case.UNKNOWN;
	}
	
	public static Person getPerson(String tag) {
		if (getPosTag(tag) == PosTag.P) {
			if (tag.charAt(2) == '1') return Person.FIRST;
			else if (tag.charAt(2) == '2') return Person.SECOND;
			else if (tag.charAt(2) == '3') return Person.THIRD;
		}
		return Person.UNKNOWN;
	}
	
	public static PronType getPronounType(String tag) {
		if (getPosTag(tag) == PosTag.P) {
			switch (tag.charAt(1)) {
				case 'p': return PronType.PERSONAL;
				case 'x': return PronType.REFLEXIVE;
				case 's': return PronType.POSSESIVE;
				case 'd': return PronType.DEMONSTRATIVE;
				case 'i': return PronType.INDEFINITE;
				case 'q': return PronType.INTERROGATIVE;
				case 'r': return PronType.RELATIVE;
				case 'g': return PronType.DEFINITE;
				default: return PronType.UNKNOWN;
			}
		}
		return PronType.UNKNOWN;
	}
	
	public static void main(String[] args) {

	}

}
