package nl.bakkerv.stretch2openhab.stretch;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.StretchConfig;

public class StretchRelaySwitcher {

	private WebTarget target;
	private EventBus eventBus;

	@Inject
	public StretchRelaySwitcher(final Client client,
			final StretchConfig stretchConfig,
			final EventBus eventBus) {
		this.eventBus = eventBus;
		this.target = client.target(stretchConfig.getStretchURL()).path("core").path("appliances");
		this.eventBus.register(this);
	}

	@Subscribe
	public void onSwitchRequest(final StretchSwitchChangeRequest request) {
		this.target.path(request.getName()).path("relay").request().put(
				Entity.entity(String.format("<relay><state>%s</state></relay>", request.getSwitchState()),
						MediaType.APPLICATION_FORM_URLENCODED));
	}

}
