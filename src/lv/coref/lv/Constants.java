package lv.coref.lv;

public class Constants { 
	public static enum PosTag {
		N, V, P, ADJ, ADV, PRE, CONJ, NUM, INTERJ, PART, PUNC, X, UNKNOWN
	}

	public static enum Gender { M, F, N, UNKNOWN }
	
	public static enum Number { SG, PL, UNKNOWN }
	
	public static enum Case { NOM, GEN, DAT, ACC, INST, LOC, VOC, UNKNOWN }
	
	public static enum Person { FIRST, SECOND, THIRD, UNKNOWN }
	
	public static enum Type { NP, NE, PRON, CONJ, UNKNOWN}
	
	public static enum PronType {
		PERSONAL, REFLEXIVE, POSSESIVE, DEMONSTRATIVE, INDEFINITE, 
		INTERROGATIVE, RELATIVE, DEFINITE, UNKNOWN
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
