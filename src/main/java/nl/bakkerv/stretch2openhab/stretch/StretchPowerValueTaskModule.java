package nl.bakkerv.stretch2openhab.stretch;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class StretchPowerValueTaskModule extends AbstractModule {

	@Override
	public void configure() {

		install(new FactoryModuleBuilder()
				.implement(StretchPowerValueTask.class, StretchPowerValueTask.class)
				.build(StretchPowerValueTaskFactory.class));
	}

}
