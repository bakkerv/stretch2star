package nl.bakkerv.stretch2openhab.openhab;

public interface OpenHABStateUpdateListenerFactory {

	public OpenHABStateUpdateListener create(final OpenHABSwitchStateChangeListener stateChangeListener);

}
