package nl.bakkerv.stretch2openhab.stretch;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class StretchPowerValueTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(StretchPowerValueTask.class);

	private StretchPowerValuesProvider fetcher;

	private EventBus eventBus;

	@Inject
	public StretchPowerValueTask(final StretchPowerValuesProvider fetcher, final EventBus eventBus) {
		this.fetcher = fetcher;
		this.eventBus = eventBus;
	}

	@Override
	public void run() {
		logger.debug("Fetching values");
		final Map<String, BigDecimal> fetchedPowerValues = this.fetcher.fetchPowerValues();
		this.eventBus.post(fetchedPowerValues);
	}

}
