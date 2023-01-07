package net.spacedvoid.beatblocks.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LimitedMap<K, V> implements Map<K, V> {
	private final Map<K, V> map;
	private final int maxSize;
	
	public LimitedMap(int maxSize) {
		this.map = new HashMap<>(maxSize + 1, 1);
		this.maxSize = maxSize;
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	
	@Override
	public V get(Object key) {
		return map.get(key);
	}
	
	/**
	 * @return The previous value associated with the key, or null if the map has reached the max size and so could not put a new entry
	 */
	@Nullable
	@Override
	public V put(K key, V value) {
		if(map.size() < maxSize) return map.put(key, value);
		else return null;
	}
	
	@Override
	public V remove(Object key) {
		return map.remove(key);
	}
	
	/**
	 * Silently fails if putting <code>m</code> exceeds the maxSize. Does not partially add entries of <code>m</code>.
	 */
	@Override
	public void putAll(@NotNull Map<? extends K, ? extends V> m) {
		if(this.size() + m.size() <= this.maxSize) map.putAll(m);
	}
	
	@Override
	public void clear() {
		map.clear();
	}
	
	@NotNull
	@Override
	public Set<K> keySet() {
		return map.keySet();
	}
	
	@NotNull
	@Override
	public Collection<V> values() {
		return map.values();
	}
	
	@NotNull
	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}
}
