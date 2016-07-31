package nl.bakkerv.stretch2openhab.stretch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.StretchConfig;
import nl.bakkerv.stretch2openhab.config.StretchToOpenhabModule;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class StretchValuesProvider {

	private WebTarget target;

	final static Logger logger = LoggerFactory.getLogger(StretchValuesProvider.class);

	private final Lock stretchLock;

	@Inject
	public StretchValuesProvider(final Client client,
			final StretchConfig stretchConfig,
			@Named(StretchToOpenhabModule.STRETCH_ACCESS_LOCK) final Lock stretchLock) {
		this.stretchLock = stretchLock;
		this.target = client.target(stretchConfig.getStretchURL()).path("core").path("modules");
	}

	public StretchValues fetchValues() {
		try {
			this.stretchLock.lock();
			final String req = this.target.request()
					.accept(MediaType.TEXT_XML)
					.acceptEncoding("*")
					.get(String.class);
			return parseModulesXML(req);
		} catch (Exception ie) {
			logger.error("Could not fetch values from Stretch: {}", ie.getMessage(), ie);
			return StretchValues.empty();
		} finally {
			this.stretchLock.unlock();
		}
	}

	protected StretchValues parseModulesXML(final String req) throws ParsingException, ValidityException, IOException {
		Builder parser = new Builder();
		final Document parsedXML = parser.build(req, null);
		StretchValues returnValue = StretchValues.create();
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
			final Nodes relay = node.query(".//relay/measurement");
			SwitchState st = SwitchState.unknown;
			if (relay.size() != 0) {
				st = SwitchState.valueOf(relay.get(0).getValue());
			}
			returnValue.put(mac, PlugValue.create(bd, st));
		}
		return returnValue;
	}

}
