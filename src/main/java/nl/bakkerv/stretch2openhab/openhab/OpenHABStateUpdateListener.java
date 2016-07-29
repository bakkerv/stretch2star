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
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;
import nl.bakkerv.stretch2openhab.openhab.OpenHABSwitchStateChangeListener.OpenHABSwitchState;
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
	private OpenHABSwitchStateChangeListener stateChangeListener;

	@Inject
	public OpenHABStateUpdateListener(@SuppressWarnings("rawtypes") final Client wsClient,
			final javax.ws.rs.client.Client webClient,
			final OpenHABConfig openhabConfig,
			@Assisted final OpenHABSwitchStateChangeListener stateChangeListener) {
		this.wsClient = wsClient;
		this.webClient = webClient;
		this.openhabConfig = openhabConfig;
		this.stateChangeListener = stateChangeListener;
	}

	@Override
	public void run() {

		Map<String, String> itemsInOpenHAB = requestItems(this.openhabConfig.getPlugwiseSwitchPageURL());
		log.info("{}", itemsInOpenHAB);
		for (Entry<String, String> e : itemsInOpenHAB.entrySet()) {
			subscribeToStateUpdate(e.getKey(), e.getValue());
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

	private void subscribeToStateUpdate(final String name, final String baseURL) {
		final RequestBuilder<?> requestBuilder = this.wsClient.newRequestBuilder()
				.method(METHOD.GET)
				.uri(baseURL + "/state")
				.transport(TRANSPORT.WEBSOCKET);
		try {
			Socket socket = this.wsClient.create();
			this.websocketConnections.put(name, socket);
			log.info("Connnecting {}", name);
			socket.on(new Function<String>() {
				@Override
				public void on(final String r) {
					log.debug("{} -> {}", name, r);
					OpenHABStateUpdateListener.this.stateChangeListener.switchChanged(name, OpenHABSwitchState.valueOf(r));
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
			this.websocketConnections.remove(name);
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
			System.err.println("Cafe con Leche is malformed today. How embarrassing!");
		} catch (IOException ex) {
			System.err.println("Could not connect to Cafe con Leche. The site may be down.");
		}
		return returnValue;
	}

}
