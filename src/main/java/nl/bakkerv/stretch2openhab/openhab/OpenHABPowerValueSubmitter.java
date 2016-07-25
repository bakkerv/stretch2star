package nl.bakkerv.stretch2openhab.openhab;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabModule;

public class OpenHABPowerValueSubmitter implements Runnable {

	private Client client;
	private String openHABURL;
	private String name;
	private String value;

	private static final Logger logger = LoggerFactory.getLogger(OpenHABPowerValueSubmitter.class);

	@Inject
	public OpenHABPowerValueSubmitter(
			final Client client,
			@Named(StretchToOpenhabModule.OPENHAB_CONFIG) final OpenHABConfig openHABConfig,
			@Assisted("name") final String name, @Assisted("value") final String value) {
		this.client = client;
		this.openHABURL = openHABConfig.getOpenHABRESTURL();
		this.name = name;
		this.value = value;
	}

	@Override
	public void run() {
		logger.debug("Submitting {} -> {} @ {}/{}/state", this.name, this.value, this.openHABURL, this.name);
		final Response cr = this.client.target(this.openHABURL).path(this.name).path("state")
				.request()// .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
				.put(Entity.entity(this.value, MediaType.TEXT_PLAIN));
		logger.debug("Done: {}", cr.getStatusInfo());
	}

}
