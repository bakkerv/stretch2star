package nl.bakkerv.stretch2openhab.openhab;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Request.METHOD;
import org.atmosphere.wasync.Request.TRANSPORT;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;

public class OpenHABStateUpdateListener implements Runnable {

	@Inject
	Client client;

	@Inject
	OpenHABConfig openhabConfig;

	private final static Logger logger = LoggerFactory.getLogger(OpenHABStateUpdateListener.class);

	@Override
	public void run() {
		@SuppressWarnings("rawtypes")
		final RequestBuilder requestBuilder = this.client.newRequestBuilder()
				.method(METHOD.GET)
				.uri(this.openhabConfig.getPlugwiseSwitchPageURL())
				.decoder(new Decoder<String, Reader>() {

					@Override
					public Reader decode(final Event type, final String s) {
						return new StringReader(s);
					}

				})
				.transport(TRANSPORT.WEBSOCKET)
				.transport(TRANSPORT.LONG_POLLING);
		while (true) {
			try {
				Socket socket = this.client.create();
				socket.on(new Function<Reader>() {
					@Override
					public void on(final Reader r) {
						// Read the response
						logger.info("{}", r);
					}
				}).on(new Function<Throwable>() {

					@Override
					public void on(final Throwable t) {
						logger.error("{}", t);
					}

				}).open(requestBuilder.build());
			} catch (IOException ioe) {
				logger.error("Something went wrong: {}", ioe);

			}
		}

	}

}
