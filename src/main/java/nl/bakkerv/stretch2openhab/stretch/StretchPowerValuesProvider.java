package nl.bakkerv.stretch2openhab.stretch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import jersey.repackaged.com.google.common.collect.Maps;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.StretchConfig;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabModule;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class StretchPowerValuesProvider {

	private WebTarget target;

	final static Logger logger = LoggerFactory.getLogger(StretchPowerValuesProvider.class);

	@Inject
	public StretchPowerValuesProvider(final Client client, @Named(StretchToOpenhabModule.STRETCH_CONFIG) final StretchConfig stretchConfig) {
		this.target = client.target(stretchConfig.getStretchURL()).path("core").path("modules");
	}

	public Map<String, BigDecimal> fetchPowerValues() {
		try {
			final String req = this.target.request()
					.accept(MediaType.TEXT_XML)
					.acceptEncoding("*")
					.get(String.class);
			return parseModulesXML(req);
		} catch (Exception ie) {
			logger.error("Could not fetch values from Stretch: {}", ie.getMessage());
			return ImmutableMap.of();
		}
	}

	protected Map<String, BigDecimal> parseModulesXML(final String req) throws ParsingException, ValidityException, IOException {
		Builder parser = new Builder();
		final Document parsedXML = parser.build(req, null);
		Map<String, BigDecimal> returnValue = Maps.newHashMap();
		final Nodes modules = parsedXML.query("//module");
		for (int i = 0; i < modules.size(); i++) {
			final Node node = modules.get(i);
			// final Elements test = node.getChildElements("mac_address");
			String mac = node.query(".//mac_address").get(0).getValue();
			final Nodes test2 = node.query(".//electricity_point_meter/measurement");
			BigDecimal bd = new BigDecimal(0);
			for (int k = 0; k < test2.size(); k++) {
				final Element element1 = (Element) test2.get(k);
				if (element1.getAttribute("directionality").getValue().equals("consumed")) {
					bd = bd.add(new BigDecimal(element1.getValue()));
				}
				if (element1.getAttribute("directionality").getValue().equals("produced")) {
					bd = bd.subtract(new BigDecimal(element1.getValue()));
				}
			}
			returnValue.put(mac, bd);

		}
		return returnValue;
	}

}
