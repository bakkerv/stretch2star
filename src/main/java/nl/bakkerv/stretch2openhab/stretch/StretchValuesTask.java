package nl.bakkerv.stretch2openhab.stretch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

public class StretchValuesTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(StretchValuesTask.class);

	private StretchValuesProvider fetcher;

	private EventBus eventBus;

	@Inject
	public StretchValuesTask(final StretchValuesProvider fetcher, final EventBus eventBus) {
		this.fetcher = fetcher;
		this.eventBus = eventBus;
	}

	@Override
	public void run() {
		logger.debug("Fetching values");
		StretchValues fetchedPowerValues = this.fetcher.fetchValues();
		this.eventBus.post(fetchedPowerValues);
	}

}
