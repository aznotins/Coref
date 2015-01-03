package lv.coref.data;

public class MentionCategory {

	public final static String UNKNOWN = "null";

	public static enum Category {
		unknown, person, location, organization, profession, media, product, event, time, sum;

		public String toString() {
			if (this == unknown) {
				return UNKNOWN;
			} else {
				return name();
			}
		}
	}

	private Category category = Category.unknown;

	public String get() {
		return category.toString();
	}

	public void set(String category) {
		category = category.toLowerCase();
		if (category.equals(UNKNOWN)) {
			this.category = Category.unknown;
			return;
		}
		try {
			this.category = Category.valueOf(category);
		} catch (Exception e) {
			this.category = Category.unknown;
		}
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof Category) {
			return category == (Category) obj;
		} else	if (obj instanceof MentionCategory) {
			MentionCategory otherMention = (MentionCategory) obj;
			if (category != otherMention.category) return false;
		} else {
			return false;
		}
		return true;
	}
	
	public boolean weakEquals(Object obj) {
		if (obj == null)
			return false;
		if (this.category == Category.unknown) return true;
		if (obj instanceof Category) {
			Category oCategory = (Category) obj;
			if (oCategory == Category.unknown) return true;
			return category == oCategory;
		} else	if (obj instanceof MentionCategory) {
			MentionCategory otherMention = (MentionCategory) obj;
			if (otherMention.category == Category.unknown) return true;
			if (category != otherMention.category) return false;
		} else {
			return false;
		}
		return true;
	}

	public String toString() {
		return category.toString();
	}
	
	public static void main(String args[]) {
		MentionCategory mc = new MentionCategory();
		mc.set("profession");
		MentionCategory mc2 = new MentionCategory();
		mc2.set("profession");
		System.err.println(mc.equals(MentionCategory.Category.profession));
		System.err.println(mc.equals(mc2));
	}
}
