package nl.bakkerv.stretch2openhab.stretch;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.junit.Before;
import org.junit.Test;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import nl.bakkerv.stretch2openhab.config.StretchToOpenhabConfiguration.StretchConfig;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

public class StretchPowerValuesProviderTest {

	private StretchValuesProvider providerUnderTest;
	private Client clientMock;
	private StretchConfig configMock;
	private Lock lock;

	@Before
	public void setUp() {
		this.clientMock = mock(Client.class);
		this.configMock = mock(StretchConfig.class);

		when(this.configMock.getStretchURL()).thenReturn("http://localhost");

		final WebTarget webTargetMock = mock(WebTarget.class);
		when(this.clientMock.target(anyString())).thenReturn(webTargetMock);
		when(webTargetMock.path(anyString())).thenReturn(webTargetMock);
		this.lock = mock(Lock.class);
		this.providerUnderTest = new StretchValuesProvider(this.clientMock, this.configMock, this.lock);
	}

	protected String readTestXML() throws IOException {
		final InputStream input = this.getClass().getResourceAsStream("/modules.xml");
		BufferedReader br = new BufferedReader(new InputStreamReader(input, Charsets.UTF_8));
		StringBuffer sb = new StringBuffer();
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line).append('\n');
		}
		return sb.toString();
	}

	@Test
	public void testParseModulesXML() throws ValidityException, ParsingException, IOException {
		final Map<String, PlugValue> actual = this.providerUnderTest.parseModulesXML(readTestXML());
		final Map<String, PlugValue> expected = ImmutableMap.of(
				"000D6F0003B9C40B", PlugValue.create(new BigDecimal("0.85"), SwitchState.on),
				"000D6F00035628E4", PlugValue.create(new BigDecimal("1.64"), SwitchState.off));
		org.assertj.core.api.Assertions.assertThat(actual).isEqualTo(expected);
	}

}
