package nl.bakkerv.stretch2openhab.openhab;

import com.google.inject.assistedinject.Assisted;

import nl.bakkerv.stretch2openhab.openhab.OpenHABPowerValueSubmitter.SubmitMode;

public interface OpenHABPowerValueSubmitterFactory {

	public OpenHABPowerValueSubmitter create(@Assisted("name") final String name,
			@Assisted("value") final String value,
			final SubmitMode submitMode);
}
