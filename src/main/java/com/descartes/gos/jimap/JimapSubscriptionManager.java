package com.descartes.gos.jimap;

import java.util.Collection;
import java.util.HashSet;

import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.SubscriptionManager;
import org.apache.james.mailbox.exception.SubscriptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy subscription manager for the default processing chain (have not seen it being used though).
 * All methods log a debug statement and do nothing further.
 * @author fwiers
 *
 */
public class JimapSubscriptionManager implements SubscriptionManager {

	private static Logger log = LoggerFactory.getLogger(JimapSubscriptionManager.class);

	public void startProcessingRequest(MailboxSession session) {
		log.debug("Starting processing request.");
	}

	public void endProcessingRequest(MailboxSession session) {
		log.debug("Ending processing request.");
	}

	public void subscribe(MailboxSession session, String mailbox) throws SubscriptionException {
		log.debug("Subscribing to mailbox " + mailbox);
	}

	public Collection<String> subscriptions(MailboxSession session)	throws SubscriptionException {
		log.debug("Returning no subscriptions.");
		return new HashSet<String>();
	}

	public void unsubscribe(MailboxSession session, String mailbox)	throws SubscriptionException {
		log.debug("Unsubscribing from mailbox " + mailbox);
	}

}
