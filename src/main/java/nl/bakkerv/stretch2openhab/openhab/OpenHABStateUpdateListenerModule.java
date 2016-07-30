package nl.bakkerv.stretch2openhab.openhab;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;

public class OpenHABStateUpdateListenerModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Client.class).toInstance(ClientFactory.getDefault().newClient());
		requireBinding(EventBus.class);
	}

}
