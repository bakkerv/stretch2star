package nl.bakkerv.stretch2openhab.openhab;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request.METHOD;
import org.atmosphere.wasync.Request.TRANSPORT;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;
import nl.bakkerv.stretch2openhab.openhab.OpenHABSwitchStateChange.SwitchState;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;

public class OpenHABStateUpdateListener implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(OpenHABStateUpdateListener.class);
	@SuppressWarnings("rawtypes")
	Client wsClient;
	javax.ws.rs.client.Client webClient;

	OpenHABConfig openhabConfig;

	Map<String, Socket> websocketConnections = Maps.newHashMap();
	private EventBus eventBus;

	@Inject
	public OpenHABStateUpdateListener(@SuppressWarnings("rawtypes") final Client wsClient,
			final javax.ws.rs.client.Client webClient,
			final OpenHABConfig openhabConfig,
			final EventBus eventBus) {
		this.wsClient = wsClient;
		this.webClient = webClient;
		this.openhabConfig = openhabConfig;
		this.eventBus = eventBus;
	}

	@Override
	public void run() {

		Map<String, String> itemsInOpenHAB = requestItems(this.openhabConfig.getPlugwiseSwitchPageURL());
		log.info("{}", itemsInOpenHAB);
		for (Entry<String, String> e : itemsInOpenHAB.entrySet()) {
			new Thread(new DeviceStateUpdateListener(e.getKey(), e.getValue(), this.wsClient, this.eventBus)).start();
		}
		synchronized (this) {
			while (true) {
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}
		}

	}

	public static class DeviceStateUpdateListener implements Runnable {

		private String baseURL;
		private String name;
		@SuppressWarnings("rawtypes")
		private Client wsClient;
		private EventBus eventBus;
		private Socket socket;

		public DeviceStateUpdateListener(final String name, final String baseURL,
				@SuppressWarnings("rawtypes") final Client wsClient, final EventBus eventBus) {
			this.name = name;
			this.baseURL = baseURL;
			this.wsClient = wsClient;
			this.eventBus = eventBus;
		}

		@Override
		public void run() {
			log.info("Subscribing to events for {} at {}", this.name, this.baseURL);
			this.eventBus.register(this);
			final RequestBuilder<?> requestBuilder = this.wsClient.newRequestBuilder()
					.method(METHOD.GET)
					.uri(this.baseURL + "/state")
					.transport(TRANSPORT.WEBSOCKET);
			try {
				this.socket = this.wsClient.create();
				this.socket.on(new Function<String>() {
					@Override
					public void on(final String r) {
						log.debug("{} -> {}", DeviceStateUpdateListener.this.name, r);
						DeviceStateUpdateListener.this.eventBus
								.post(OpenHABSwitchStateChange.create(DeviceStateUpdateListener.this.name, SwitchState.valueOf(r)));
					}
				})
						.on(new Function<Throwable>() {

							@Override
							public void on(final Throwable t) {
								log.error("{}", t);
							}

						}).open(requestBuilder.build());
			} catch (IOException ioe) {
				log.error("Something went wrong: {}", ioe);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				new Thread(this).start();
			} finally {
				this.eventBus.unregister(this);
			}

		}

	}

	private Map<String, String> requestItems(final String plugwiseSwitchPageURL) {
		Map<String, String> returnValue = Maps.newHashMap();
		try {
			Builder parser = new Builder();
			Document doc = parser.build(plugwiseSwitchPageURL);
			Nodes query = doc.query("//members");
			for (int i = 0; i < query.size(); i++) {
				String name = query.get(i).query("./name").get(0).getValue();
				String link = query.get(i).query("./link").get(0).getValue();
				returnValue.put(name, link);
			}
		} catch (ParsingException ex) {
			System.err.println("Response is invalid");
		} catch (IOException ex) {
			System.err.println("Cannot reach openHAB website");
		}
		return returnValue;
	}

}
