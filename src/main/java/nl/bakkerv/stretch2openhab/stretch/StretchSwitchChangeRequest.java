package nl.bakkerv.stretch2openhab.stretch;

public class StretchSwitchChangeRequest {

	private final String name;
	private final SwitchState switchState;

	public StretchSwitchChangeRequest(final String name, final SwitchState newState) {
		this.name = name;
		this.switchState = newState;
	}

	public static StretchSwitchChangeRequest create(final String name, final SwitchState newState) {
		return new StretchSwitchChangeRequest(name, newState);
	}

	public String getName() {
		return this.name;
	}

	public SwitchState getSwitchState() {
		return this.switchState;
	}

}
