package nl.bakkerv.stretch2openhab.stretch;

import java.math.BigDecimal;
import java.util.Map;

import jersey.repackaged.com.google.common.base.MoreObjects;

public class StretchResults {

	public enum SwitchState {
		ON,
		OFF
	}

	private final Map<String, BigDecimal> fetchedPowerValues;
	private final Map<String, SwitchState> switchStates;

	public StretchResults(final Map<String, BigDecimal> fetchedPowerValues, final Map<String, SwitchState> switchStates) {
		this.fetchedPowerValues = fetchedPowerValues;
		this.switchStates = switchStates;
	}

	public static StretchResults create(final Map<String, BigDecimal> fetchedPowerValues, final Map<String, SwitchState> switchState) {
		return new StretchResults(fetchedPowerValues, switchState);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("powervalues", this.fetchedPowerValues).add("switchStates", this.switchStates).toString();
	}

	public Map<String, BigDecimal> getFetchedPowerValues() {
		return this.fetchedPowerValues;
	}

	public Map<String, SwitchState> getSwitchStates() {
		return this.switchStates;
	}

}
