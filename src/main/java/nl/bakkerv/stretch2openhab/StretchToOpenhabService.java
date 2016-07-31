package nl.bakkerv.stretch2openhab;

import java.time.Instant;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import ch.qos.logback.classic.Level;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabModule;
import nl.bakkerv.stretch2openhab.openhab.OpenHABPowerValueSubmitter.SubmitMode;
import nl.bakkerv.stretch2openhab.openhab.OpenHABPowerValueSubmitterFactory;
import nl.bakkerv.stretch2openhab.openhab.OpenHABPowerValueSubmitterModule;
import nl.bakkerv.stretch2openhab.openhab.OpenHABStateUpdateListener;
import nl.bakkerv.stretch2openhab.openhab.OpenHABStateUpdateListenerModule;
import nl.bakkerv.stretch2openhab.openhab.OpenHABSwitchStateChange;
import nl.bakkerv.stretch2openhab.stretch.PlugValue;
import nl.bakkerv.stretch2openhab.stretch.StretchRelaySwitcher;
import nl.bakkerv.stretch2openhab.stretch.StretchSwitchChangeRequest;
import nl.bakkerv.stretch2openhab.stretch.StretchValues;
import nl.bakkerv.stretch2openhab.stretch.StretchValuesTask;
import nl.bakkerv.stretch2openhab.stretch.SwitchState;

public class StretchToOpenhabService {

	private ExecutorService postThreadPools;
	@Inject
	private OpenHABPowerValueSubmitterFactory openHABValueSubmitterFactory;
	@Inject
	private StretchToOpenhabConfiguration configuration;
	@Inject
	@Named(StretchToOpenhabModule.DEVICE_MAPPING)
	private BiMap<String, String> deviceMapping;
	@Inject
	@Named(StretchToOpenhabModule.SWITCH_MAPPING)
	private BiMap<String, String> switchMapping;
	@Inject
	private EventBus eventBus;
	@Inject
	private StretchRelaySwitcher relaySwitcher;
	@Inject
	private Provider<OpenHABStateUpdateListener> openHABUpdateListenerProvider;
	private OpenHABStateUpdateListener openHABUpdateListener;

	private final static Logger log = LoggerFactory.getLogger(StretchToOpenhabService.class);

	public static void main(final String[] args) throws InterruptedException {
		if (args.length != 1) {
			System.err.println("Usage: <<configFile>>");
			System.exit(1);
		}
		new StretchToOpenhabService(args[0]);
	}

	public StretchToOpenhabService(final String configFile) throws InterruptedException {
		Injector i = Guice.createInjector(new StretchToOpenhabModule(configFile),
				new OpenHABPowerValueSubmitterModule(),
				new OpenHABStateUpdateListenerModule());
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		this.postThreadPools = Executors.newFixedThreadPool(3);
		i.injectMembers(this);
		scheduler.scheduleAtFixedRate(i.getInstance(StretchValuesTask.class), 0, 5, TimeUnit.SECONDS);
		setupLogging();
		this.eventBus.register(this);
		synchronized (this) {
			while (true) {
				this.wait();
			}
		}

	}

	private void setupLogging() {
		Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (root instanceof ch.qos.logback.classic.Logger) {
			final ch.qos.logback.classic.Logger l = (ch.qos.logback.classic.Logger) root;
			l.setLevel(this.configuration.isDebugEnabled() ? Level.DEBUG : Level.INFO);
		}

	}

	@Subscribe
	public void onStretchResults(final StretchValues results) {
		log.info("Received values from Stretch: {}", results);
		if (results == null || results.getFetchedValues() == null) {
			return;
		}
		for (Entry<String, PlugValue> entry : results.getFetchedValues().entrySet()) {
			if (!this.deviceMapping.containsKey(entry.getKey())) {
				log.debug("Skipping {}", entry.getKey());
				continue;
			}
			final String name = this.deviceMapping.get(entry.getKey());
			this.postThreadPools.submit(this.openHABValueSubmitterFactory.create(
					name,
					entry.getValue().getPowerValue().toPlainString(),
					SubmitMode.PUT));
		}
		this.postThreadPools.submit(this.openHABValueSubmitterFactory.create(
				"Plugwise_last_updated",
				Instant.now().toString(),
				SubmitMode.PUT));
		if (this.openHABUpdateListener == null) {
			for (Entry<String, PlugValue> entry : results.getFetchedValues().entrySet()) {
				if (!this.deviceMapping.containsKey(entry.getKey())) {
					log.debug("Skipping {}", entry.getKey());
					continue;
				}
				String name = this.deviceMapping.get(entry.getKey());
				name = name.replace("_Power", "_Switch");
				if (!this.switchMapping.containsKey(name)) {
					log.debug("Skipping {}", name);
					continue;
				}
				this.postThreadPools.submit(this.openHABValueSubmitterFactory.create(
						name,
						entry.getValue().getSwitchState().toString().toUpperCase(),
						SubmitMode.PUT));
			}
			this.openHABUpdateListener = this.openHABUpdateListenerProvider.get();
			new Thread(this.openHABUpdateListener).start();
		}
	}

	@Subscribe
	public void onOpenHABSwitchStateChange(final OpenHABSwitchStateChange change) {
		if (this.switchMapping.containsKey(change.getSwitchName())) {
			String relayID = this.switchMapping.get(change.getSwitchName());
			this.eventBus.post(StretchSwitchChangeRequest.create(relayID, SwitchState.valueOf(change.getSwitchState().toString().toLowerCase())));
		}
	}
}
