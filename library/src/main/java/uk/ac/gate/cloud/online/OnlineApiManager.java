package uk.ac.gate.cloud.online;

import uk.ac.gate.cloud.client.RestClient;

/**
 * Main entry point for the online processing API.
 *
 * @author Ian Roberts
 */
public class OnlineApiManager {
  private RestClient client;

  /**
   * Construct a <code>JobManager</code> using the given
   * {@link RestClient} to communicate with the API.
   * 
   * @param client the client object used for communication
   */
  public OnlineApiManager(RestClient client) {
    this.client = client;
  }

  /**
   * Construct a <code>JobManager</code> accessing the GATE Cloud public
   * API with the given credentials.
   * 
   * @param apiKeyId API key ID for authentication
   * @param apiPassword corresponding password
   */
  public OnlineApiManager(String apiKeyId, String apiPassword) {
    this(new RestClient(apiKeyId, apiPassword));
  }

  /**
   * Create an endpoint object that can call the given REST API URL.
   * 
   * @param endpointUrl the endpoint URL
   */
  public ApiEndpoint getEndpoint(String endpointUrl) {
    ApiEndpoint ep = new ApiEndpoint();
    ep.setClient(client);
    ep.endpointUrl = endpointUrl;
    return ep;
  }

}
