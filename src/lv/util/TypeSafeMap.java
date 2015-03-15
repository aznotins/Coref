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
