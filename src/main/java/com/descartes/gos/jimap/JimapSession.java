package com.descartes.gos.jimap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.james.imap.api.ImapSessionState;
import org.apache.james.imap.api.process.ImapLineHandler;
import org.apache.james.imap.api.process.ImapSession;
import org.apache.james.imap.api.process.SelectedMailbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dummy session that allows the default processor chain to function
 * (log, storage of mailbox, storage of session-state and storage of attributes).
 * @author fwiers
 *
 */
public class JimapSession implements ImapSession {

	private static AtomicLong sessionId = new AtomicLong();;

	private ConcurrentHashMap<String, Object> attr = new ConcurrentHashMap<String, Object>();
	private ImapSessionState state = ImapSessionState.NON_AUTHENTICATED;
	private SelectedMailbox mailbox;
	
	private Logger log = LoggerFactory.getLogger("ImapSession." + sessionId.incrementAndGet());
	
	public Logger getLog() {
		return log;
	}

	public void logout() {
		closeMailbox();
		state = ImapSessionState.LOGOUT;
	}

	public ImapSessionState getState() {
		return state;
	}

	public void authenticated() {
		state = ImapSessionState.AUTHENTICATED;
	}

	public void selected(SelectedMailbox mailbox) {
		state = ImapSessionState.SELECTED; 
		closeMailbox();
		this.mailbox = mailbox;
	}

	public void deselect() {
		state = ImapSessionState.AUTHENTICATED; 
		closeMailbox();
	}

	public SelectedMailbox getSelected() {
		return mailbox;
	}
	
    private void closeMailbox() {
        
    	if (mailbox != null) {
            mailbox.deselect();
            mailbox = null;
        }
    }

	public Object getAttribute(String key) {
		return (key == null ? null : attr.get(key));
	}

	public void setAttribute(String key, Object value) {
		
		if (log.isDebugEnabled()) {
			log.debug("Session attribute [" + (key == null ? "<null>" : key) + "]=[" + (value == null ? "<null>" : value.toString()) + "]");
		}
		if (key == null) {
			log.error("Cannot set value for null-key.", new NullPointerException());
		} else if (value == null) {
			if (log.isDebugEnabled()) {
				log.debug("Removing value for key [" + key + "]");
			}
			attr.remove(key);
		} else {
			attr.put(key, value);
		}
	}

	public boolean startTLS() {
		return false;
	}

	public boolean isTLSActive() {
		return false;
	}

	public boolean supportStartTLS() {
		return false;
	}

	public boolean isCompressionActive() {
		return false;
	}

	public boolean isCompressionSupported() {
		return false;
	}

	public boolean startCompression() {
		return false;
	}

	public void pushLineHandler(ImapLineHandler lineHandler) {
		// NO-OP: there is no "get lineHandler"/
	}

	public void popLineHandler() {
		// NO-OP: there is no "get lineHandler"/
	}

	public boolean supportMultipleNamespaces() {
		return false;
	}

	public boolean isPlainAuthDisallowed() {
		return false;
	}

}
