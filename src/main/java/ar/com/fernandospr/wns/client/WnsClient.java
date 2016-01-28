package ar.com.fernandospr.wns.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;

import ar.com.fernandospr.wns.WnsProxyProperties;
import ar.com.fernandospr.wns.exceptions.WnsException;
import ar.com.fernandospr.wns.model.WnsAbstractNotification;
import ar.com.fernandospr.wns.model.WnsNotificationRequestOptional;
import ar.com.fernandospr.wns.model.WnsNotificationResponse;
import ar.com.fernandospr.wns.model.WnsOAuthToken;
import ar.com.fernandospr.wns.model.types.WnsNotificationType;

public class WnsClient {
	private static final String SCOPE = "notify.windows.com";
	private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
	private static final String AUTHENTICATION_URI = "https://login.live.com/accesstoken.srf";

	private String sid;
	private String clientSecret;
	private WnsOAuthToken token;
	private Client client;

	public WnsClient(String sid, String clientSecret, boolean logging) {
		this(sid, clientSecret, null, logging);
	}

	public WnsClient(String sid, String clientSecret, WnsProxyProperties proxyProps, boolean logging) {
		this.sid = sid;
		this.clientSecret = clientSecret;
		this.client = createClient(logging, proxyProps);
	}

	private static Client createClient(boolean logging, WnsProxyProperties proxyProps) {
		Client client = null;

		if (proxyProps == null) {
			client = ResteasyClientBuilder.newClient();
		} else {
			String scheme = proxyProps.getProtocol();
			String hostname = proxyProps.getHost();
			int port = proxyProps.getPort();

			String user = proxyProps.getUser();
			String pass = proxyProps.getPass();

			client = new ResteasyClientBuilder().defaultProxy(hostname, port, scheme).build();
		}

		client.register(JacksonJaxbXMLProvider.class);
		client.register(JacksonJsonProvider.class);

		if (logging) {
			client = client.register(new LoggingFilter());
		}

		return client;
	}

	/**
	 * Based on <a href=
	 * "http://msdn.microsoft.com/en-us/library/windows/apps/hh465407.aspx">http
	 * ://msdn.microsoft.com/en-us/library/windows/apps/hh465407.aspx</a>
	 *
	 * @throws WnsException
	 *             when authentication fails
	 */
	public void refreshAccessToken() throws WnsException {
		WebTarget target = client.target(AUTHENTICATION_URI);

		MultivaluedMap<String, String> formData = new MultivaluedHashMap<String, String>();
		formData.add("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS);
		formData.add("client_id", this.sid);
		formData.add("client_secret", this.clientSecret);
		formData.add("scope", SCOPE);
		Response response = target.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).post(Entity.form(formData));

		if (response.getStatus() != 200) {
			throw new WnsException("Authentication failed. HTTP error code: " + response.getStatus());
		}

		this.token = response.readEntity(WnsOAuthToken.class);
	}

	/**
	 * @param channelUri
	 * @param resourceBuilder
	 * @param notification
	 * @param retriesLeft
	 *            to push the notification if the token expires
	 * @return WnsNotificationResponse please see response headers from <a href=
	 *         "http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx#send_notification_response">
	 *         http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.
	 *         aspx#send_notification_response</a>
	 * @throws WnsException
	 *             when authentication fails
	 */
	public WnsNotificationResponse push(WnsResourceBuilder resourceBuilder, String channelUri, WnsAbstractNotification notification, int retriesLeft,
			WnsNotificationRequestOptional optional) throws WnsException {
		WebTarget target = client.target(channelUri);
		Invocation.Builder webResourceBuilder = resourceBuilder.build(target, notification, getToken().access_token, optional);
		String type = notification.getType().equals(WnsNotificationType.RAW) ? MediaType.APPLICATION_OCTET_STREAM : MediaType.TEXT_XML;

		Response response = webResourceBuilder.buildPost(Entity.entity(resourceBuilder.getEntityToSendWithNotification(notification), type)).invoke();

		WnsNotificationResponse notificationResponse = new WnsNotificationResponse(channelUri, response.getStatus(), response.getStringHeaders());
		if (notificationResponse.code == 200) {
			return notificationResponse;
		}

		if (notificationResponse.code == 401 && retriesLeft > 0) {
			retriesLeft--;
			// Access token may have expired
			refreshAccessToken();
			// Retry
			return this.push(resourceBuilder, channelUri, notification, retriesLeft, optional);
		}

		// Assuming push failed
		return notificationResponse;
	}

	private WnsOAuthToken getToken() throws WnsException {
		if (this.token == null) {
			refreshAccessToken();
		}
		return this.token;
	}

	/**
	 * @param channelUris
	 * @param resourceBuilder
	 * @param notification
	 * @param retriesLeft
	 *            to push the notification if the token expires
	 * @return list of WnsNotificationResponse for each channelUri, please see
	 *         response headers from <a href=
	 *         "http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.aspx#send_notification_response">
	 *         http://msdn.microsoft.com/en-us/library/windows/apps/hh465435.
	 *         aspx#send_notification_response</a>
	 * @throws WnsException
	 *             when authentication fails
	 */
	public List<WnsNotificationResponse> push(WnsResourceBuilder resourceBuilder, List<String> channelUris, WnsAbstractNotification notification, int retriesLeft,
			WnsNotificationRequestOptional optional) throws WnsException {
		List<WnsNotificationResponse> responses = new ArrayList<WnsNotificationResponse>();
		for (String channelUri : channelUris) {
			WnsNotificationResponse response = push(resourceBuilder, channelUri, notification, retriesLeft, optional);
			responses.add(response);
		}
		return responses;
	}
}
