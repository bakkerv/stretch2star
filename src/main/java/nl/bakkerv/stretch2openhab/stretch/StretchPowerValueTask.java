package nl.bakkerv.stretch2openhab.stretch;

import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class StretchPowerValueTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(StretchPowerValueTask.class);

	private StretchPowerValuesProvider fetcher;
	private StretchResultNotifier[] notifiers;

	@Inject
	public StretchPowerValueTask(final StretchPowerValuesProvider fetcher, @Assisted final StretchResultNotifier... notifiers) {
		this.fetcher = fetcher;
		this.notifiers = notifiers;
	}

	@Override
	public void run() {
		logger.debug("Fetching values");
		final Map<String, BigDecimal> fetchedPowerValues = this.fetcher.fetchPowerValues();
		for (StretchResultNotifier n : this.notifiers) {
			n.processStretchResults(fetchedPowerValues);
		}
	}

}
