package lv.coref.lv;


public class Constants {
	public static enum PosTag {
		N, V, P, ADJ, ADV, PRE, CONJ, NUM, INTERJ, PART, PUNC, X, UNKNOWN
	}

	public static enum Category {
		unknown, person, location, organization, profession, media, product, event, time, sum;
		private final static String UNKNOWN = "unk";

		public boolean isUnkown() {
			return this.equals(unknown);
		}
		
		public boolean compatible(Object o) {
			if (this.equals(unknown)) return true;
			if (o.equals(unknown)) return true;
			if ((this.equals(person) || this.equals(profession)) && (o.equals(person) || o.equals(profession))) return true;
			return this.equals(o);
		}
		
		public boolean strictEquals(Object o) {
			if (this.equals(unknown))
				return false;
			if (o.equals(unknown))
				return false;
			return this.equals(o);
		}

		public boolean weakEquals(Object o) {
			if (this.equals(unknown))
				return true;
			if (o.equals(unknown))
				return true;
			return this.equals(o);
		}

		public String toString() {
			if (this == unknown) return UNKNOWN;
			return name();
		}

		public static Category get(String value) {
			try {
				return valueOf(value);
			} catch (Exception e) {
//				System.err.println("Category not found: " + value);
				// e.printStackTrace();
			}
			return unknown;
		}
	}

	public static enum Gender {
		M, F, N, UNKNOWN;
		public boolean strictEquals(Object o) {
			if (this.equals(UNKNOWN))
				return false;
			if (o.equals(UNKNOWN))
				return false;
			return this.equals((Object) o);
		}

		public boolean weakEquals(Object o) {
			if (this.equals(Gender.UNKNOWN))
				return true;
			if (o.equals(Gender.UNKNOWN))
				return true;
			return this.equals(o);
		}
		
		public String toString() {
			if (this == UNKNOWN) return "UNK";
			return name();
		}
	}

	public static enum Number {
		SG, PL, UNKNOWN;
		public boolean strictEquals(Object o) {
			if (this.equals(UNKNOWN))
				return false;
			if (o.equals(UNKNOWN))
				return false;
			return this.equals(o);
		}

		public boolean weakEquals(Object o) {
			if (this.equals(Number.UNKNOWN))
				return true;
			if (o.equals(Number.UNKNOWN))
				return true;
			return this.equals(o);
		}
		
		public String toString() {
			if (this == UNKNOWN) return "UNK";
			return name();
		}
	}

	public static enum Case {
		NOM, GEN, DAT, ACC, INST, LOC, VOC, UNKNOWN;
		public boolean strictEquals(Case o) {
			if (this.equals(UNKNOWN))
				return false;
			if (o.equals(UNKNOWN))
				return false;
			return this.equals((Object) o);
		}

		public boolean weakEquals(Case o) {
			if (this.equals(Case.UNKNOWN))
				return true;
			if (o.equals(Case.UNKNOWN))
				return true;
			return this.equals(o);
		}
		
		public String toString() {
			if (this == UNKNOWN) return "UNK";
			return name();
		}
	}

	public static enum Person {
		FIRST, SECOND, THIRD, UNKNOWN;
		
		public String toString() {
			if (this == UNKNOWN) return "UNK";
			return name();
		}
	}

	public static enum Type {
		NP, NE, PRON, CONJ, UNKNOWN;
		
		public String toString() {
			if (this == UNKNOWN) return "UNK";
			return name();
		}
	}

	public static enum PronType {
		PERSONAL, REFLEXIVE, POSSESIVE, DEMONSTRATIVE, INDEFINITE, INTERROGATIVE, RELATIVE, DEFINITE, UNKNOWN
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.err.println(Gender.F.strictEquals(Gender.F));
		Category c = Category.get("testx");
		System.err.println(c);
		System.err.println(Category.get("test"));
		System.err.println(Category.get("unknown"));
		System.err.println(Category.get("person"));

	}

}
