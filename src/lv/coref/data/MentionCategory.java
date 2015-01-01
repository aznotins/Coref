package lv.coref.data;

public class MentionCategory {

	public final static String UNKNOWN = "null";

	public static enum Category {
		unknown, person, location, organization, profession, media, product, event;

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
		if (getClass() != obj.getClass())
			return false;
		MentionCategory other = (MentionCategory) obj;

		return category == other.category;
	}

	public String toString() {
		return category.toString();
	}
}
