package lv.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lv.label.Labels.LabelList;
import lv.label.Labels.LabelText;

public class SimpleTypeSafeMap implements TypeSafeMap, Serializable {

	private static final long serialVersionUID = 1L;
	
	private Map<Class<?>, Object> delegate = new HashMap<Class<?>, Object>();
	
	@Override
	public <VALUE, KEY extends Key<VALUE>> boolean has(Class<KEY> key) {
		return delegate.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <VALUE, KEY extends Key<VALUE>> VALUE get(Class<KEY> key) {
		return (VALUE) delegate.get(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <VALUE, KEY extends Key<VALUE>> VALUE get(Class<KEY> key, VALUE defaultValue) {
		if (delegate.containsKey(key))
			return (VALUE) delegate.get(key);
		else
			return defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	public <VALUE, KEY extends Key<VALUE>> VALUE get(String key) {
		VALUE value = null;
		try {
			value = (VALUE) delegate.get((Class<KEY>) Class.forName(key));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (VALUE) value;
	}

	@Override
	public <VALUE, KEY extends Key<VALUE>> VALUE set(Class<KEY> key, VALUE value) {
		delegate.put(key, value);
		return null;
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <VALUE, KEY extends Key<VALUE>> VALUE remove(Class<KEY> key) {
		return (VALUE) delegate.remove(key);
	}

	@Override
	public Set<Map.Entry<Class<?>, Object>> entrySet() {		
		return delegate.entrySet();
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("[");
		boolean first = true;
		for (Class<?> x : delegate.keySet()) {
			if (first) first = false;
			else s.append(", ");
			s.append(x.getSimpleName());
			s.append(" = ");
			s.append(delegate.get(x));			
		}
		s.append(']');
		return s.toString();
	}

	public static void main(String[] args) {
		TypeSafeMap m = new SimpleTypeSafeMap();

		m.set(LabelList.class, Arrays.asList("te", "tur"));
		List<String> listLables = m.get(LabelList.class);
		System.err.println(listLables);
		System.err.println(m.get(LabelList.class));

		m.set(LabelText.class, "text");
		String text = m.get(LabelText.class);
		System.err.println(text);
		
		System.err.println(m.entrySet());

		// m.set(Integer.class, new Integer(1));
		// Integer integer = m.get(Integer.class);
		// System.err.println(integer);

		// String integerString = m.get(Integer.class);
		// System.err.println(integerString);

		// ArrayCoreMap a = new ArrayCoreMap();
		// a.set(TextAnnotation.class, "jans texts");
		// System.err.println(a.get(TextAnnotation.class));
		//
		// a.set(LVGazAnnotation.class, new HashSet<String>(Arrays.asList("g1",
		// "g2")));
		// System.err.println(a.get(LVGazAnnotation.class));

	}

}
