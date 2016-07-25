package nl.bakkerv.stretch2openhab.openhab;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class OpenHABPowerValueSubmitterModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder()
				.implement(OpenHABPowerValueSubmitter.class, OpenHABPowerValueSubmitter.class)
				.build(OpenHABPowerValueSubmitterFactory.class));

	}

}
