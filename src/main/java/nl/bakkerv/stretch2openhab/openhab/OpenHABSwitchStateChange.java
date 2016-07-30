package nl.bakkerv.stretch2openhab.openhab;

import java.util.Objects;

public class OpenHABSwitchStateChange {

	public static enum SwitchState {
		ON,
		OFF
	};

	private final String switchName;
	private final SwitchState switchState;

	public OpenHABSwitchStateChange(final String switchName, final SwitchState switchState) {
		this.switchName = switchName;
		this.switchState = switchState;
	}

	public static OpenHABSwitchStateChange create(final String switchName, final SwitchState switchState) {
		return new OpenHABSwitchStateChange(switchName, switchState);
	}

	public String getSwitchName() {
		return this.switchName;
	}

	public SwitchState getSwitchState() {
		return this.switchState;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.switchName, this.switchState);
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (this.getClass() != other.getClass()) {
			return false;
		}
		OpenHABSwitchStateChange o = (OpenHABSwitchStateChange) other;
		return Objects.equals(this.switchName, o.switchName) &&
				Objects.equals(this.switchState, o.switchState);
	}

	@Override
	public String toString() {
		return String.format("%s -> %s", this.switchName, this.switchState);
	}

}
