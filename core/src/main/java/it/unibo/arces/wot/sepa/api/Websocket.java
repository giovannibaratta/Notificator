/* This class is part of the SPARQL 1.1 SE Protocol (an extension of the W3C SPARQL 1.1 Protocol) API
 * 
 * Author: Luca Roffia (luca.roffia@unibo.it)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package it.unibo.arces.wot.sepa.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import it.unibo.arces.wot.sepa.commons.protocol.SSLSecurityManager;
import it.unibo.arces.wot.sepa.commons.response.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class Websocket {
	protected Logger logger = LogManager.getLogger("WebsocketClientEndpoint");

	private String wsUrl;
	private boolean ssl;
	private INotificationHandler handler;
	private WebSocketClient socket;
	private SSLSecurityManager sm;

	private Watchdog watchDog = null;

	public static long SUBSCRIBE_TIMEOUT = 15000;
	private Response response;

	public Websocket(String wsUrl, boolean ssl, INotificationHandler handler, String jksFile)
			throws IllegalArgumentException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {

		if (wsUrl == null)
			throw new IllegalArgumentException("URL is null");

		this.handler = handler;
		this.wsUrl = wsUrl;
		this.ssl = ssl;
		if(jksFile == null && ssl)
			throw new IllegalArgumentException("Manca il file jks");
		if(jksFile != null && ssl)
			this.sm = new SSLSecurityManager("TLS", jksFile, "sepa2017", "sepa2017");
	}

	private void createWebsocket(String url, boolean ssl)
			throws URISyntaxException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, InterruptedException {

		if (socket != null)
			return;

		socket = new WebSocketClient(new URI(wsUrl)) {

			@Override
			public void onOpen(ServerHandshake handshakedata) {
				logger.debug("@onOpen");
			}

			@Override
			public void onMessage(String message) {
				logger.debug("@onMessage " + message);

				if (handler == null) {
					logger.error("Notification handler is NULL");
					return;
				}

				synchronized (handler) {
					JsonObject notify = new JsonParser().parse(message).getAsJsonObject();

					// Ping
					if (notify.get("ping") != null) {
						handler.onPing();

						watchDog.ping();
						return;
					}

					// Subscribe confirmed
					if (notify.get("subscribed") != null) {
						response = new SubscribeResponse(notify.get("subscribed").getAsString());

						synchronized (socket) {
							socket.notify();
						}

						watchDog.subscribed();
						return;
					}

					// Unsubscribe confirmed
					if (notify.get("unsubscribed") != null) {
						response = new UnsubscribeResponse(notify.get("unsubscribed").getAsString());

						synchronized (socket) {
							socket.notify();
						}
	
						watchDog.unsubscribed();
						return;
					}

					// Notification
					if (notify.get("results") != null) {
						handler.onSemanticEvent(new Notification(notify));
						return;
					}

					// Error
					if (notify.get("code") != null) {
						handler.onError(new ErrorResponse(notify.get("code").getAsInt()));

						synchronized (socket) {
							socket.notify();
						}

						return;
					}
				}

			}

			@Override
			public void onClose(int code, String reason, boolean remote) {
				socket = null;
			}

			@Override
			public void onError(Exception ex) {
				logger.debug("@onError " + ex.getMessage());
			}

		};

		if (ssl)
			socket.setSocket(sm.createSSLSocket());

		socket.connectBlocking();
	}

	public Response subscribe(String sparql, String alias, String jwt) throws IllegalArgumentException {

		logger.debug("@subscribe");

		if (sparql == null)
			throw new IllegalArgumentException("SPARQL query is null");

		try {
			createWebsocket(wsUrl, ssl);
		} catch (UnrecoverableKeyException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException
				| CertificateException | URISyntaxException | IOException | InterruptedException e) {
			return new ErrorResponse(500, e.getMessage());
		}

		if (socket == null)
			return new ErrorResponse(500, "Socket is null");

		if (watchDog == null) {
			watchDog = new Watchdog(handler, this, sparql, alias, jwt);

			// watchDog.start();
		}

		// Create SPARQL 1.1 Subscribe request
		JsonObject request = new JsonObject();
		request.add("subscribe", new JsonPrimitive(sparql));

		if (alias != null)
			request.add("alias", new JsonPrimitive(alias));
		else
			logger.debug("Alias is null");

		if (jwt != null)
			request.add("authorization", new JsonPrimitive("Bearer " + jwt));
		else
			logger.debug("Authorization is null");
		logger.debug(request.toString());

		// Send request
		socket.send(request.toString());

		// Wait response
		response = new ErrorResponse(408, "Timeout");
		synchronized (socket) {
			try {
				socket.wait(SUBSCRIBE_TIMEOUT);
			} catch (InterruptedException e) {

			}
		}
		return response;

		// Send fragmented request (test)
		// byte[] req = request.toString().getBytes("UTF-8");
		// ByteBuffer buffer = ByteBuffer.allocate(1);
		// buffer.limit(1);
		// for (int i=0; i < req.length; i++) {
		// buffer.rewind();
		// buffer.put(req[i]);
		// buffer.rewind();
		// socket.sendFragmentedFrame(Opcode.TEXT, buffer, (i == req.length-1));
		// }
	}

	public Response unsubscribe(String spuid, String jwt) throws IllegalArgumentException, IOException,
			URISyntaxException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, InterruptedException {
		logger.debug("@unsubscribe spuid:" + spuid + " jwt: " + jwt);

		if (spuid == null)
			throw new IllegalArgumentException("SPUID is null");

		createWebsocket(wsUrl, ssl);

		if (socket == null)
			throw new IOException("Websocket is null");

		// Create SPARQL 1.1 Unsubscribe request
		JsonObject request = new JsonObject();
		if (spuid != null)
			request.add("unsubscribe", new JsonPrimitive(spuid));

		if (jwt != null)
			request.add("authorization", new JsonPrimitive("Bearer " + jwt));

		socket.send(request.toString());

		// Wait response
		response = new ErrorResponse(408, "Timeout");
		synchronized (socket) {
			try {
				socket.wait(SUBSCRIBE_TIMEOUT);
			} catch (InterruptedException e) {

			}
		}
		return response;
	}

	public void setNotificationHandler(INotificationHandler handler) {
		if (handler == null) {
			logger.fatal("Notification handler is null. Client cannot be initialized");
			throw new IllegalArgumentException("Notificaton handler is null");
		}

		this.handler = handler;

	}
}
