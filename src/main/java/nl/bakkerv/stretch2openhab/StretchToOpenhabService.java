package nl.bakkerv.stretch2openhab;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import ch.qos.logback.classic.Level;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabModule;
import nl.bakkerv.stretch2openhab.openhab.OpenHABPowerValueSubmitterFactory;
import nl.bakkerv.stretch2openhab.openhab.OpenHABPowerValueSubmitterModule;
import nl.bakkerv.stretch2openhab.stretch.StretchPowerValueTaskFactory;
import nl.bakkerv.stretch2openhab.stretch.StretchPowerValueTaskModule;
import nl.bakkerv.stretch2openhab.stretch.StretchResultNotifier;

public class StretchToOpenhabService implements StretchResultNotifier {

	private ExecutorService postThreadPools;
	@Inject
	private OpenHABPowerValueSubmitterFactory openHABValueSubmitterFactory;
	@Inject
	private StretchToOpenhabConfiguration configuration;
	@Inject
	private BiMap<String, String> deviceMapping;

	private final static Logger logger = LoggerFactory.getLogger(StretchToOpenhabService.class);

	public static void main(final String[] args) throws InterruptedException {
		if (args.length != 1) {
			System.err.println("Usage: <<configFile>>");
			System.exit(1);
		}
		new StretchToOpenhabService(args[0]);
	}

	public StretchToOpenhabService(final String configFile) throws InterruptedException {
		Injector i = Guice.createInjector(new StretchToOpenhabModule(configFile),
				new StretchPowerValueTaskModule(),
				new OpenHABPowerValueSubmitterModule());
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		this.postThreadPools = Executors.newFixedThreadPool(3);
		i.injectMembers(this);
		scheduler.scheduleAtFixedRate(i.getInstance(StretchPowerValueTaskFactory.class).create(this), 0, 5, TimeUnit.SECONDS);
		setupLogging();
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

	@Override
	public void processStretchResults(final Map<String, BigDecimal> fetchedValues) {
		for (Entry<String, BigDecimal> entry : fetchedValues.entrySet()) {
			if (!this.deviceMapping.containsKey(entry.getKey())) {
				logger.debug("Skipping {}", entry.getKey());
				continue;
			}
			final String name = this.deviceMapping.get(entry.getKey());
			this.postThreadPools.submit(this.openHABValueSubmitterFactory.create(name, entry.getValue().toPlainString()));
		}
		this.postThreadPools.submit(this.openHABValueSubmitterFactory.create("Plugwise_last_updated", Instant.now().toString()));
	}

}
