package nl.bakkerv.stretch2openhab.openhab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;
import nl.bakkerv.stretch2openhab.openhab.OpenHABSwitchStateChange.SwitchState;

public class OpenHABStateUpdateListenerTest {

	@Inject
	EventBus eventBus;
	@Inject
	OpenHABStateUpdateListener updateListener;
	@Inject
	javax.ws.rs.client.Client wsClient;

	int updatesReceived = 0;

	private static final Logger log = LoggerFactory.getLogger(OpenHABStateUpdateListenerTest.class);

	@Before
	public void setUp() {
		Guice.createInjector(new AbstractModule() {

			@Override
			public void configure() {
				OpenHABConfig config = mock(OpenHABConfig.class);
				when(config.getPlugwiseSwitchPageURL()).thenReturn("http://192.168.60.50:8080/rest/items/PlugwiseSwitches");
				bind(OpenHABConfig.class).toInstance(config);
				bind(javax.ws.rs.client.Client.class).toInstance(ClientBuilder.newClient());
				bind(EventBus.class).in(Singleton.class);
				install(new OpenHABStateUpdateListenerModule());
			}

		}).injectMembers(this);
		this.eventBus.register(this);
	}

	@Test
	public void testStateChangeGetsNotified() throws InterruptedException {
		Thread thread = new Thread(this.updateListener);
		thread.start();
		Thread.sleep(100);
		this.eventBus.post(OpenHABSwitchStateChange.create("GF_Samsung_decoder_Switch", SwitchState.ON));
		this.wsClient.target("http://192.168.60.50:8080/rest/items/GF_Samsung_decoder_Switch/state").request()
				.put(Entity.<String> entity("OFF", javax.ws.rs.core.MediaType.TEXT_PLAIN));
		this.wsClient.target("http://192.168.60.50:8080/rest/items/GF_Samsung_decoder_Switch/state").request()
				.put(Entity.<String> entity("ON", javax.ws.rs.core.MediaType.TEXT_PLAIN));
		Thread.sleep(250);
		assertThat(this.updatesReceived).isGreaterThan(1);
	}

	@Subscribe
	public void onOpenHABStateChange(final OpenHABSwitchStateChange change) {
		log.info("change {}", change);
		this.updatesReceived++;
	}

}
