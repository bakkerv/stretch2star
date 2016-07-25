package nl.bakkerv.stretch2openhab.stretch;

public interface StretchPowerValueTaskFactory {

	public StretchPowerValueTask create(StretchResultNotifier... notifiers);

}
