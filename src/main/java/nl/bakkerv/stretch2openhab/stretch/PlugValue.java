package nl.bakkerv.stretch2openhab.stretch;

import java.math.BigDecimal;
import java.util.Objects;

import jersey.repackaged.com.google.common.base.MoreObjects;

public class PlugValue {
	private final BigDecimal powerValue;
	private final SwitchState switchState;

	protected PlugValue(final BigDecimal powerValue, final SwitchState switchState) {
		this.powerValue = powerValue;
		this.switchState = switchState;
	}

	public static PlugValue create(final BigDecimal powerValue, final SwitchState switchState) {
		return new PlugValue(powerValue, switchState);
	}

	public BigDecimal getPowerValue() {
		return this.powerValue;
	}

	public SwitchState getSwitchState() {
		return this.switchState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.powerValue, this.switchState);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		PlugValue other = (PlugValue) obj;
		return Objects.equals(this.powerValue, other.powerValue) &&
				Objects.equals(this.switchState, other.switchState);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("powerValue", this.powerValue).add("switchState", this.switchState).toString();
	}

}