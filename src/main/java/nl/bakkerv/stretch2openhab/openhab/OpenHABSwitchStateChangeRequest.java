package nl.bakkerv.stretch2openhab.openhab;

public class OpenHABSwitchStateChangeRequest extends OpenHABSwitchStateChange {

	public OpenHABSwitchStateChangeRequest(final String switchName, final SwitchState switchState) {
		super(switchName, switchState);
	}

	public static OpenHABSwitchStateChangeRequest create(final String switchName, final SwitchState switchState) {
		return new OpenHABSwitchStateChangeRequest(switchName, switchState);
	}

}
