package nl.bakkerv.stretch2openhab.stretch;

import java.math.BigDecimal;
import java.util.Map;

public interface StretchResultNotifier {

	public void processStretchResults(Map<String, BigDecimal> fetchedValues);

}
