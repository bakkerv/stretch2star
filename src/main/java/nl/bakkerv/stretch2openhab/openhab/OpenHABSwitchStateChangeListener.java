package nl.bakkerv.stretch2openhab.openhab;

public interface OpenHABSwitchStateChangeListener {

	public enum OpenHABSwitchState {
		ON,
		OFF
	};

	public void switchChanged(final String itemName, OpenHABSwitchState newState);

}
