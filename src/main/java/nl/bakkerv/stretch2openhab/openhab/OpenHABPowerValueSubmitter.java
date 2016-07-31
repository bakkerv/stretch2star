package nl.bakkerv.stretch2openhab.openhab;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.OpenHABConfig;

public class OpenHABPowerValueSubmitter implements Runnable {

	private final Client client;
	private final String openHABURL;
	private final String name;
	private final String value;
	private final SubmitMode submitMode;

	public enum SubmitMode {
		PUT,
		POST;
	}

	private static final Logger logger = LoggerFactory.getLogger(OpenHABPowerValueSubmitter.class);

	@Inject
	public OpenHABPowerValueSubmitter(
			final Client client,
			final OpenHABConfig openHABConfig,
			@Assisted("name") final String name, @Assisted("value") final String value,
			@Assisted final SubmitMode submitMode) {
		this.client = client;
		this.submitMode = submitMode;
		this.openHABURL = openHABConfig.getOpenHABRESTURL();
		this.name = name;
		this.value = value;
	}

	@Override
	public void run() {
		logger.debug("Submitting {} -> {} @ {}/{}/state", this.name, this.value, this.openHABURL, this.name);
		Builder request = this.client.target(this.openHABURL).path(this.name).path("state")
				.request();
		Response cr = null;
		switch (this.submitMode) {
		case POST:
			cr = request.post(Entity.entity(this.value, MediaType.TEXT_PLAIN));
			break;
		case PUT:
			cr = request.put(Entity.entity(this.value, MediaType.TEXT_PLAIN));
			break;
		}
		logger.debug("Done: {}", cr.getStatusInfo());
	}

}
