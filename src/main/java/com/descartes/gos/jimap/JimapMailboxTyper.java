package com.descartes.gos.jimap;

import org.apache.james.imap.api.process.ImapSession;
import org.apache.james.imap.api.process.MailboxType;
import org.apache.james.imap.api.process.MailboxTyper;
import org.apache.james.mailbox.model.MailboxPath;

/**
 * Always return INBOX mailbox-type.
 * @author fwiers
 *
 */
public class JimapMailboxTyper implements MailboxTyper {

	public MailboxType getMailboxType(ImapSession session, MailboxPath path) {
		return MailboxType.INBOX;
	}

}
