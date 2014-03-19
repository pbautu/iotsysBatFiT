package at.ac.tuwien.auto.probac.coap.get;

import java.net.URI;

import org.apache.log4j.Logger;

import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;

public class CoAPGet {

	private static Logger log = Logger.getLogger(CoAPGet.class);

	private boolean contikiIotsysServer;

	private Request request;

	private long responseTimeoutSec;

	public CoAPGet(URI uri, boolean contikiIotsysServer,
			long responseTimeoutSec) {
		this.contikiIotsysServer = contikiIotsysServer;
		request = Request.newGet();
		request.setURI(uri);
		this.responseTimeoutSec = responseTimeoutSec * 1000;
	}

	public CoAPGetResponse doCoAPGet() {

		CoAPGetResponse coAPGetResponse = null;
		
		if (contikiIotsysServer) {
			request.getOptions().setBlock2(7, true, 0);
		}
		request.send();

		try {
			Response response = request.waitForResponse(responseTimeoutSec);
			
			if (response != null) {
				coAPGetResponse = new CoAPGetResponse(true, response);
				log.info(String.format("Response: %s",
						response.getPayloadString()));
			} else {
				coAPGetResponse = new CoAPGetResponse(false, null);
				request.cancel();
				log.info("Response timeout"); //
			}
		} catch (InterruptedException e) {
			log.error(String.format("Receiving of response interrupted: %s",
					e.getMessage()));
		}
		return coAPGetResponse;
	}
}
