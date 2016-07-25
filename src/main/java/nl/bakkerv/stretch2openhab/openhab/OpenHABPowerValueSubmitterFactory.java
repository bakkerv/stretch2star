package nl.bakkerv.stretch2openhab.openhab;

import com.google.inject.assistedinject.Assisted;

public interface OpenHABPowerValueSubmitterFactory {

	public OpenHABPowerValueSubmitter create(@Assisted("name") final String name,
			@Assisted("value") final String value);
}
