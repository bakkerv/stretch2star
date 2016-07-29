package nl.bakkerv.stretch2openhab.openhab;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class OpenHABStateUpdateListenerModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new FactoryModuleBuilder()
				.implement(OpenHABStateUpdateListener.class, OpenHABStateUpdateListener.class)
				.build(OpenHABStateUpdateListenerFactory.class));

	}

}
