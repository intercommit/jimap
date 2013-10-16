package com.descartes.gos.jimap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.james.imap.api.process.ImapProcessor;
import org.apache.james.imap.decode.main.DefaultImapDecoder;
import org.apache.james.imap.decode.main.ImapRequestStreamHandler;
import org.apache.james.imap.encode.VanishedResponseEncoder;
import org.apache.james.imap.encode.main.DefaultImapEncoderFactory;
import org.apache.james.imap.encode.main.DefaultLocalizer;
import org.apache.james.imap.main.DefaultImapDecoderFactory;
import org.apache.james.imap.processor.main.DefaultImapProcessorFactory;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.acl.SimpleGroupMembershipResolver;
import org.apache.james.mailbox.acl.UnionMailboxACLResolver;
import org.apache.james.mailbox.inmemory.InMemoryMailboxSessionMapperFactory;
import org.apache.james.mailbox.store.Authenticator;
import org.apache.james.mailbox.store.StoreMailboxManager;
import org.apache.james.mailbox.store.StoreSubscriptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs some standard IMAP client commands using the default encoder- and processing-chain.
 * Expected (log) output is shown in a comment at the end of this class.
 */
public class TestCommands {

	private static Logger log = LoggerFactory.getLogger(TestCommands.class);

	public static Charset CS = StandardCharsets.US_ASCII;

	public static void main(String... args) {
		
		final String[] commands = new String[] {
			"A001 LOGIN mrc secret",
			"A002 SELECT \"INBOX\"",
			"A153 SEARCH NOT Deleted",
			"A164 EXPUNGE",
			"A003 NOOP",
			"A004 LOGOUT"
		};
		TestCommands tc = new TestCommands();
		try {
			tc.runClientCommands(commands);
		} catch (Exception e) {
			log.error("TC failed", e);
		}
	}
	
	/**
	 * Prepares an IMAP processing chain that handles the given IMAP client commands.
	 * All effects of the client commands are non-functional: 
	 * nothing is actually done with any (email) messages by the processor chain.
	 */
	private void runClientCommands(String[] commands) throws Exception {
		
		DefaultLocalizer localizer = new DefaultLocalizer();
		// Set imap encoder chain.
		VanishedResponseEncoder imapEncoder = (VanishedResponseEncoder) DefaultImapEncoderFactory.createDefaultEncoder(localizer, false);
		
		// Setup imap decoder.
		DefaultImapDecoder imapDecoder = (DefaultImapDecoder) DefaultImapDecoderFactory.createDecoder();
		
		// Setup an in-memory mailbox, required to setup a processor chain.
        InMemoryMailboxSessionMapperFactory sessionMapper = new InMemoryMailboxSessionMapperFactory();
        Authenticator authenticator = new JimapAuthenticator();
        MailboxACLResolver aclResolver = new UnionMailboxACLResolver();
        GroupMembershipResolver groupMembershipResolver = new SimpleGroupMembershipResolver();
        
        StoreMailboxManager<Long> mailboxManager = new StoreMailboxManager<Long>(sessionMapper, authenticator, aclResolver, groupMembershipResolver);
        mailboxManager.init();
        
        // Create the processor chain
        StoreSubscriptionManager subscriptionManager = new StoreSubscriptionManager(sessionMapper);
        ImapProcessor processor = DefaultImapProcessorFactory.createDefaultProcessor(mailboxManager, subscriptionManager);
		
        // Create handler that captures server response for client commands.
        ImapRequestStreamHandler irs = new ImapRequestStreamHandler(imapDecoder, processor, imapEncoder);
        
        JimapSession session = new JimapSession();
        
        for (String c : commands) {
            handleLine(c, irs, session);
        }
	}
	
	/**
	 * Executes the client IMAP request and logs the server IMAP response.
	 */
	private void handleLine(String line, ImapRequestStreamHandler irs, JimapSession session) throws Exception {
		
        session.getLog().info("C:\n" + line);
        line += "\n"; // Line should end with CRLF, but just LF also works.
		ByteArrayInputStream bin = new ByteArrayInputStream(line.getBytes(CS));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        irs.handleRequest(bin, bout, session);
        String s = new String(bout.toByteArray(), CS);
        s = s.substring(0,  s.lastIndexOf('\r')); // Server response always ends with CRLF, but logging already adds a LF.
        session.getLog().info("S:\n" + s);
	}
}
/*
Expected output:

12:37:19.659 [main] INFO  ImapSession.1 - C:
A001 LOGIN mrc secret
12:37:19.668 [main] DEBUG ImapSession.1 - Got <tag>: A001
12:37:19.668 [main] DEBUG ImapSession.1 - Got <command>: LOGIN
12:37:19.669 [main] DEBUG ImapSession.1 - Session attribute [INVALID_COMMAND_COUNT]=[0]
12:37:19.672 [main] DEBUG ImapSession.1 - Session attribute [org.apache.james.api.imap.MAILBOX_SESSION_ATTRIBUTE_SESSION_KEY]=[MailboxSession ( sessionId = -5906446043418841451 open = true  )]
12:37:19.673 [main] DEBUG ImapSession.1 - INBOX does not exist. Creating it.
12:37:19.673 [main] DEBUG ImapSession.1 - createMailbox #private:mrc:INBOX
12:37:19.683 [main] INFO  ImapSession.1 - S:
A001 OK LOGIN completed.
12:37:19.684 [main] INFO  ImapSession.1 - C:
A002 SELECT "INBOX"
12:37:19.684 [main] DEBUG ImapSession.1 - Got <tag>: A002
12:37:19.684 [main] DEBUG ImapSession.1 - Got <command>: SELECT
12:37:19.685 [main] DEBUG ImapSession.1 - Session attribute [INVALID_COMMAND_COUNT]=[0]
12:37:19.685 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.696 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.704 [main] DEBUG ImapSession.1 - Session attribute [SEARCHRES_SAVED_SET]=[<null>]
12:37:19.704 [main] DEBUG ImapSession.1 - Removing value for key [SEARCHRES_SAVED_SET]
12:37:19.705 [main] INFO  ImapSession.1 - S:
* FLAGS (\Answered \Deleted \Draft \Flagged \Seen)
* 0 EXISTS
* 0 RECENT
* OK [UIDVALIDITY 1940001560] UIDs valid
* OK [PERMANENTFLAGS (\Answered \Deleted \Draft \Flagged \Seen)] Limited
* OK [HIGHESTMODSEQ 0] Highest
* OK [UIDNEXT 1] Predicted next UID
A002 OK [READ-WRITE] SELECT completed.
12:37:19.705 [main] INFO  ImapSession.1 - C:
A153 SEARCH NOT Deleted
12:37:19.705 [main] DEBUG ImapSession.1 - Got <tag>: A153
12:37:19.705 [main] DEBUG ImapSession.1 - Got <command>: SEARCH
12:37:19.707 [main] DEBUG ImapSession.1 - Session attribute [INVALID_COMMAND_COUNT]=[0]
12:37:19.707 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.720 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.720 [main] DEBUG ImapSession.1 - Session attribute [SEARCH_MODSEQ]=[<null>]
12:37:19.720 [main] DEBUG ImapSession.1 - Removing value for key [SEARCH_MODSEQ]
12:37:19.720 [main] INFO  ImapSession.1 - S:
* SEARCH
A153 OK SEARCH completed.
12:37:19.720 [main] INFO  ImapSession.1 - C:
A164 EXPUNGE
12:37:19.720 [main] DEBUG ImapSession.1 - Got <tag>: A164
12:37:19.721 [main] DEBUG ImapSession.1 - Got <command>: EXPUNGE
12:37:19.721 [main] DEBUG ImapSession.1 - Session attribute [INVALID_COMMAND_COUNT]=[0]
12:37:19.721 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.721 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.721 [main] DEBUG ImapSession.1 - Session attribute [ENABLED_CAPABILITIES]=[[]]
12:37:19.722 [main] INFO  ImapSession.1 - S:
A164 OK EXPUNGE completed.
12:37:19.722 [main] INFO  ImapSession.1 - C:
A003 NOOP
12:37:19.722 [main] DEBUG ImapSession.1 - Got <tag>: A003
12:37:19.722 [main] DEBUG ImapSession.1 - Got <command>: NOOP
12:37:19.722 [main] DEBUG ImapSession.1 - Session attribute [INVALID_COMMAND_COUNT]=[0]
12:37:19.722 [main] DEBUG ImapSession.1 - Loaded mailbox #private:mrc:INBOX
12:37:19.722 [main] INFO  ImapSession.1 - S:
A003 OK NOOP completed.
12:37:19.722 [main] INFO  ImapSession.1 - C:
A004 LOGOUT
12:37:19.722 [main] DEBUG ImapSession.1 - Got <tag>: A004
12:37:19.722 [main] DEBUG ImapSession.1 - Got <command>: LOGOUT
12:37:19.722 [main] DEBUG ImapSession.1 - Session attribute [INVALID_COMMAND_COUNT]=[0]
12:37:19.722 [main] INFO  ImapSession.1 - S:
* BYE IMAP4rev1 Server logging out
A004 OK LOGOUT completed.

*/

