package nl.bakkerv.stretch2openhab.openhab;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.ClientBuilder;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;

public class OpenHABStateUpdateListenerTest {

	@Inject
	private OpenHABStateUpdateListenerFactory updateStateListenerFactory;

	@Before
	public void setUp() {
		Guice.createInjector(new AbstractModule() {

			@Override
			public void configure() {
				OpenHABConfig config = mock(OpenHABConfig.class);
				when(config.getPlugwiseSwitchPageURL()).thenReturn("http://192.168.60.50:8080/rest/items/PlugwiseSwitches");
				bind(Client.class).toInstance(ClientFactory.getDefault().newClient(AtmosphereClient.class));
				bind(OpenHABConfig.class).toInstance(config);
				bind(javax.ws.rs.client.Client.class).toInstance(ClientBuilder.newClient());
				install(new OpenHABStateUpdateListenerModule());
			}

		}).injectMembers(this);
	}

	@Test
	public void test() {
		this.updateStateListenerFactory.create(new OpenHABSwitchStateChangeListener() {

			@Override
			public void switchChanged(final String name, final OpenHABSwitchState newState) {
				System.out.println(name + " --> " + newState);
			}
		}).run();
	}

}
