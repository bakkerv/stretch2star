package nl.bakkerv.stretch2openhab.stretch;

import java.util.Map;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;

import jersey.repackaged.com.google.common.base.MoreObjects;
import jersey.repackaged.com.google.common.collect.Maps;

public class StretchValues extends ForwardingMap<String, PlugValue> {

	private final Map<String, PlugValue> fetchedValues;

	protected StretchValues() {
		this(Maps.newHashMap());
	}

	protected StretchValues(final Map<String, PlugValue> fetchValues) {
		this.fetchedValues = fetchValues;
	}

	public static StretchValues create(final Map<String, PlugValue> fetchValues) {
		return new StretchValues(fetchValues);
	}

	public static StretchValues create() {
		return new StretchValues();
	}

	public static StretchValues empty() {
		return new StretchValues(ImmutableMap.of());
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("fetchedValues", this.fetchedValues).toString();
	}

	public Map<String, PlugValue> getFetchedValues() {
		return this.fetchedValues;
	}

	@Override
	protected Map<String, PlugValue> delegate() {
		return this.fetchedValues;
	}

}
