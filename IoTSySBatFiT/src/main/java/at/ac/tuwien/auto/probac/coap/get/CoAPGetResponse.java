package at.ac.tuwien.auto.probac.coap.get;

import ch.ethz.inf.vs.californium.coap.Response;

public class CoAPGetResponse {

	private boolean isSuccessfulResponse;
	
	private Response response;

	public CoAPGetResponse(boolean isSuccessfulResponse, Response response) {
		this.isSuccessfulResponse = isSuccessfulResponse;
		this.response = response;
	}
	
	public boolean isSuccessfulResponse() {
		return isSuccessfulResponse;
	}

	public Response getResponse() {
		return response;
	}
}
