package at.ac.tuwien.auto.probac.coap.obs;

import java.net.URI;

import org.apache.log4j.Logger;

import ch.ethz.inf.vs.californium.CoapClient;
import ch.ethz.inf.vs.californium.CoapHandler;
import ch.ethz.inf.vs.californium.CoapObserveRelation;
import ch.ethz.inf.vs.californium.CoapResponse;
import ch.ethz.inf.vs.californium.network.EndpointManager;

public class CoAPObserve {

	private static Logger log = Logger.getLogger(CoAPObserve.class);

	private CoAPObserveThread coAPObserveThread;

	public CoAPObserve(URI uri) {
		coAPObserveThread = new CoAPObserveThread(uri);
	}

	public void observe() {
		coAPObserveThread.start();
	}

	public void cancel() {
		coAPObserveThread.stopObserving();
	}

	private static class CoAPObserveThread extends Thread implements
			CoapHandler {

		private CoapClient coapClient;

		private boolean subscribed = false;
		
		private boolean run = true;

		private CoapObserveRelation coapObserveRelation;

		public CoAPObserveThread(URI uri) {
			coapClient = new CoapClient(uri);
		}

		@Override
		public void run() {
			coapObserveRelation = coapClient.observe(this);
			while (run) {
				try {
					sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (!run) {
					break;
				}
			}
		}

		public void failed() {
			stopObserving();
		}

		public void responded(CoapResponse coapResponse) {
			String responseText = coapResponse.getResponseText();
			if (responseText != null && responseText.toLowerCase().startsWith("added")) {
				subscribed = true;
			}
			log.info(responseText);
		}

		public void stopObserving() {
			run = false;
			if (subscribed) {
				cancelSubscription();
				stopEndpoint();
				log.info("Sending observation cancellation");
			}
		}

		private void cancelSubscription() {
			coapObserveRelation.cancel();
		}

		private void stopEndpoint() {
			EndpointManager.getEndpointManager().getDefaultEndpoint().stop();
		}
	}
}