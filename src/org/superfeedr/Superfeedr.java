/**
 * Copyright (c) 2009 julien
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.superfeedr;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.IQ.Type;
import org.superfeedr.extension.notification.SuperfeedrEventExtension;
import org.superfeedr.extension.subscription.SubUnSubExtension;
import org.superfeedr.extension.subscription.SubscriptionListExtension;
import org.superfeedr.packet.SuperfeedrIQ;

/**
 * The SuperFeedr Class is the entry point to access the superFeedr XMPP
 * functionnalities. To use this Class, you should already have a superfeedr
 * account.
 * 
 * @author thomas RICARD
 * 
 */
public class Superfeedr {

	private static final String FIREHOSER = "firehoser.superfeedr.com";

  public static final String DEFAULT_RESOURCE = "superfeedr-java";	
	
	// The pseudo ISO 8601 date formatter that misses the timezone (thanks to
	// java)
	private static DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	/**
	 * This class converts a ISO 8601 date represented as a string to a java
	 * Date Object
	 * 
	 * @param date
	 * @return
	 */
	public static Date convertDate(final String date) {
		try {
			return m_ISO8601Local.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return new Date();
		}
	}

	// The current connection to the XMPP server
	private XMPPConnection connection;

	// The JID used by this superfeedr instance
	private String jid;

	// The Listeners of event on this superfeedr instance
	private ArrayList<OnNotificationHandler> onNotificationHandlers = new ArrayList<OnNotificationHandler>();

	private Map<String, OnResponseHandler> pendingOnReponseHandlers = new HashMap<String, OnResponseHandler>();

	// The server this instance is connected to
	private String server;

	/**
	 * Constructor of a superfeedr instance that will connect to the specified
	 * server using the jid and password as credentials, using the provided resource
	 * 
	 * @param jid
	 *            the jid you want to connect with
	 * @param password
	 *            the password to use
	 * @param server
	 *            the server you connect to
	 * @param resource
	 *            the resource you want to connect with
	 */
	public Superfeedr(final String jid, final String password, final String server, final String resource) throws XMPPException {
		if (jid == null || password == null || server == null || resource == null)
			throw new IllegalArgumentException("arguments cannot be null");
	
		this.jid = jid;
		this.server = server;

		// Creating the connection to the XMPP server
		connection = new XMPPConnection(this.server);

		// Now, time to connect and auth
		try {
			connection.connect();
			connection.login(this.jid, password, resource);

			connection.addPacketListener(new SuperFeedrPacketListener(), new OrFilter(new PacketTypeFilter(Message.class), new PacketTypeFilter(IQ.class)));
		} catch (XMPPException e) {
			if (connection != null && connection.isConnected()) {
				connection.disconnect();
			}
			// Let's the caller deals with the exception
			throw e;
		}
	}
	
  /**
   * Constructor of a superfeedr instance that will connect to the specified
   * server using the jid and password as credentials, using the default resource
   * 
   * @param jid
   *            the jid you want to connect with
   * @param password
   *            the password to use
   * @param server
   *            the server you connect to
   */
	public Superfeedr(final String jid, final String password, final String server) throws XMPPException {
	  this(jid, password, server, DEFAULT_RESOURCE);
	}

	/**
	 * This method is used to close the connection to the Superfeer Server.
	 * 
	 * @throws XMPPException
	 */
	public void close() throws XMPPException {
		if (connection != null && connection.isConnected()) {
			connection.disconnect();
		}
	}

	/**
	 * Add handler as a notification handler on this superfeedr. When a
	 * notification is received, a SuperfeedrEventExtension object is created
	 * and passed to each listener
	 * 
	 * @param handler
	 *            the handler to be added
	 */
	public void addOnNotificationHandler(final OnNotificationHandler handler) {
		if (handler != null && !onNotificationHandlers.contains(handler)) {
			onNotificationHandlers.add(handler);
		}
	}

	/**
	 * 
	 * @param event
	 */
	private void fireOnNotificationHandlers(final SuperfeedrEventExtension event) {
		for (OnNotificationHandler handler : onNotificationHandlers) {
			handler.onNotification(event);
		}
	}

	/**
	 * Try to remove the specified handler from the list of notification handler
	 * 
	 * @param handler
	 *            the notification handler to be removed from the list
	 */
	public void removeOnNotificationHandler(final OnNotificationHandler handler) {
		onNotificationHandlers.remove(handler);
	}

	private void subUnsubscribe(final SubUnSubExtension subUnsubscription, OnResponseHandler handler) {
		SuperfeedrIQ iq = new SuperfeedrIQ(subUnsubscription.toXML());
		iq.setTo(FIREHOSER);
		iq.setType(Type.SET);
		connection.sendPacket(iq);
		pendingOnReponseHandlers.put(iq.getPacketID(), handler);
	}

	/**
	 * Call this method to add subscription to your superfeedr account. The
	 * passed URL must be well formatted and must represent something that can
	 * be used as source by superfeedr. See the Superfeedr website for
	 * information about that.
	 * 
	 * @param feedUrls
	 *            the list of feeds you want to add to your superfeedr account
	 * @param handler
	 *            the callback
	 */
	public void subscribe(List<URL> feedUrls, OnResponseHandler handler) {
		subUnsubscribe(new SubUnSubExtension(feedUrls, jid + "@" + server, SubUnSubExtension.TYPE_SUBSCRIPTION), handler);
	}

	/**
	 * Call this method to remove subscription from your superfeedr account.
	 * 
	 * @param feedUrls
	 *            the list of feeds you want to add to your superfeedr account
	 * @param handler
	 *            the callback
	 */
	public void unsubscribe(List<URL> feedUrls, OnResponseHandler handler) {
		subUnsubscribe(new SubUnSubExtension(feedUrls, jid + "@" + server, SubUnSubExtension.TYPE_UNSUBSCRIPTION), handler);
	}

	/**
	 * This method is used to retreive the feeds URL you subscribed to.
	 * 
	 * @return a list of your feed url
	 */
	public List<URL> getSubscriptionList(final OnResponseHandler handler) {

		SubscriptionListExtension list = new SubscriptionListExtension(null, jid + "@" + server, 1);
		System.err.println(list.toXML());
		SuperfeedrIQ iq = new SuperfeedrIQ(list.toXML());
		iq.setTo(FIREHOSER);
		iq.setType(Type.GET);
		connection.sendPacket(iq);
		pendingOnReponseHandlers.put(iq.getPacketID(), handler);
		return null;
	}

	private class SuperFeedrPacketListener implements PacketListener {
		public void processPacket(Packet packet) {
			
			if (packet instanceof Message) {
				fireOnNotificationHandlers((SuperfeedrEventExtension) ((Message) packet).getExtension(SuperfeedrEventExtension.NAMESPACE));
			} else {
				String packetID = packet.getPacketID();

				OnResponseHandler handler = pendingOnReponseHandlers.get(packetID);

				if (handler != null) {
					pendingOnReponseHandlers.remove(packetID);

					XMPPError error = packet.getError();

					if (error != null) {
						StringBuilder builder = new StringBuilder(error.getCondition());
						builder.append("Type = \n");
						builder.append(error.getType().name());
						builder.append("\n");
						List<PacketExtension> extensions = error.getExtensions();
						for (PacketExtension packetExtension : extensions) {
							builder.append(packetExtension.getElementName());
							builder.append("\n");
						}
						handler.onError(builder.toString());
					} else {
						
						handler.onSuccess(null);

					}
				}
			}
		}
	}
}
