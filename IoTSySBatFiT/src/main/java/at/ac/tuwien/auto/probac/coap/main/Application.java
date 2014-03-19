package at.ac.tuwien.auto.probac.coap.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.log4j.Logger;

import at.ac.tuwien.auto.probac.coap.get.CoAPGet;
import at.ac.tuwien.auto.probac.coap.get.CoAPGetResponse;
import at.ac.tuwien.auto.probac.coap.obs.CoAPObserve;

public class Application {

	private final static Logger log = Logger.getLogger(Application.class);

	public static void main(String[] args) {

		String propertiesFileName = "configuration.properties";
		Properties applicationProperties = new Properties();
		InputStream inputStream = Application.class.getClassLoader()
				.getResourceAsStream(propertiesFileName);

		try {
			applicationProperties.load(inputStream);
		} catch (IOException e) {
			System.err.println(String.format(
					"Could not load properties file [%s]; error message: %s",
					propertiesFileName, e.getMessage()));
			System.exit(-1);
		}

		URI temperatureUri = null;
		URI batteryUri = null;

		try {
			temperatureUri = new URI(
					(String) applicationProperties.get("appl.uri.coap.observe"));
			batteryUri = new URI(
					(String) applicationProperties.get("appl.uri.coap.get"));
		} catch (URISyntaxException e) {
			Application.log.error(String.format("Invalid URI: %s",
					e.getMessage()));
			System.exit(-1);
		}

		long coapGetPollingIntervalSec = Long.valueOf((String) applicationProperties
				.get("appl.coap.request.interval.sec"));
		long coapGetResponseTimeoutSec = Long.valueOf((String) applicationProperties
				.get("appl.coap.response.timeout.sec"));
		long coAPGetResponseUnsuccessfulCountdown = Long.valueOf((String) applicationProperties
				.get("appl.coap.request.tries"));

		CoAPObserve coAPObserve = new CoAPObserve(temperatureUri);
		coAPObserve.observe();

		CoAPGet coAPGet = new CoAPGet(batteryUri, true,
				coapGetResponseTimeoutSec);
		CoAPGetResponse coAPGetResponse;

		log.info("Starting to send send CoAP GET requests");

		while (0 < coAPGetResponseUnsuccessfulCountdown) {
			coAPGetResponse = coAPGet.doCoAPGet();
			if (coAPGetResponse.isSuccessfulResponse()) {
				// do something
				
				// reset the tries counter
				coAPGetResponseUnsuccessfulCountdown = Long.valueOf((String) applicationProperties
						.get("appl.coap.request.tries"));
			} else {
				coAPGetResponseUnsuccessfulCountdown--;
			}

			if (0 < coAPGetResponseUnsuccessfulCountdown) {
				try {
					Thread.sleep(coapGetPollingIntervalSec * 1000);
				} catch (InterruptedException e) {
					log.error(String.format(
							"Exception in polling break; message: %s",
							e.getMessage()));
				}
			}
		}
		coAPObserve.cancel();
		log.info("Exiting");
		System.exit(0);
	}

}
