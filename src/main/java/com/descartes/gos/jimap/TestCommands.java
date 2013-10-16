package com.descartes.gos.jimap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.james.imap.api.message.response.StatusResponseFactory;
import org.apache.james.imap.decode.main.DefaultImapDecoder;
import org.apache.james.imap.decode.main.ImapRequestStreamHandler;
import org.apache.james.imap.encode.VanishedResponseEncoder;
import org.apache.james.imap.encode.main.DefaultImapEncoderFactory;
import org.apache.james.imap.main.DefaultImapDecoderFactory;
import org.apache.james.imap.message.response.UnpooledStatusResponseFactory;
import org.apache.james.imap.processor.DefaultProcessorChain;
import org.apache.james.imap.processor.EnableProcessor;
import org.apache.james.imap.processor.base.UnknownRequestProcessor;
import org.apache.james.mailbox.acl.GroupMembershipResolver;
import org.apache.james.mailbox.acl.MailboxACLResolver;
import org.apache.james.mailbox.acl.SimpleGroupMembershipResolver;
import org.apache.james.mailbox.acl.UnionMailboxACLResolver;
import org.apache.james.mailbox.inmemory.InMemoryMailboxSessionMapperFactory;
import org.apache.james.mailbox.store.StoreMailboxManager;
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
		
		// Set imap encoder chain.
		VanishedResponseEncoder imapEncoder = (VanishedResponseEncoder) DefaultImapEncoderFactory.createDefaultEncoder(new JimapLocalizer(), false);
		
		// Setup imap decoder.
		DefaultImapDecoder imapDecoder = (DefaultImapDecoder)DefaultImapDecoderFactory.createDecoder();
		// StatusResponseFactory srf is the same as the one used in imapDecoder 
		// (but that field is private, so create a new one)
		StatusResponseFactory srf = new UnpooledStatusResponseFactory();
		
		// Setup an in-memory mailbox, required to setup a processor chain.
        InMemoryMailboxSessionMapperFactory sessionMapper = new InMemoryMailboxSessionMapperFactory();
        MailboxACLResolver aclResolver = new UnionMailboxACLResolver();
        GroupMembershipResolver groupMembershipResolver = new SimpleGroupMembershipResolver();
        StoreMailboxManager<Long> mailboxManager = new StoreMailboxManager<Long>(sessionMapper, new JimapAuthenticator(), aclResolver, groupMembershipResolver);
        mailboxManager.init();

        // Create the processor chain
        EnableProcessor processor = (EnableProcessor) DefaultProcessorChain.createDefaultChain(new UnknownRequestProcessor(srf), mailboxManager, 
				new JimapSubscriptionManager(), srf, new JimapMailboxTyper(), 5000L, TimeUnit.MILLISECONDS, new HashSet<String>());
		
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
		
        log.info("C:\n" + line);
        line += "\n"; // Line should end with CRLF, but just LF also works.
		ByteArrayInputStream bin = new ByteArrayInputStream(line.getBytes(CS));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        irs.handleRequest(bin, bout, session);
        String s = new String(bout.toByteArray(), CS);
        s = s.substring(0,  s.lastIndexOf('\r')); // Server response always ends with CRLF, but logging already adds a LF.
		log.info("S:\n" + s);
	}
}
/*
Expected output:

09:47:50.003 [main] INFO  com.descartes.gos.jimap.TestCommands - C:
A001 LOGIN mrc secret
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <tag>: A001
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <command>: LOGIN
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [INVALID_COMMAND_COUNT]=[0]
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [org.apache.james.api.imap.MAILBOX_SESSION_ATTRIBUTE_SESSION_KEY]=[MailboxSession ( sessionId = 3869451373015414172 open = true  )]
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - INBOX does not exist. Creating it.
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - createMailbox #private:mrc:INBOX
09:47:50.019 [main] INFO  com.descartes.gos.jimap.TestCommands - S:
A001 OK LOGIN completed.
09:47:50.019 [main] INFO  com.descartes.gos.jimap.TestCommands - C:
A002 SELECT "INBOX"
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <tag>: A002
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <command>: SELECT
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [INVALID_COMMAND_COUNT]=[0]
09:47:50.019 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.034 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.034 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [SEARCHRES_SAVED_SET]=[<null>]
09:47:50.034 [main] DEBUG com.descartes.gos.jimap.JimapSession - Removing value for key [SEARCHRES_SAVED_SET]
09:47:50.034 [main] INFO  com.descartes.gos.jimap.TestCommands - S:
* FLAGS (\Answered \Deleted \Draft \Flagged \Seen)
* 0 EXISTS
* 0 RECENT
* OK [UIDVALIDITY 136448498] UIDs valid
* OK [PERMANENTFLAGS (\Answered \Deleted \Draft \Flagged \Seen)] Limited
* OK [HIGHESTMODSEQ 0] Highest
* OK [UIDNEXT 1] Predicted next UID
A002 OK [READ-WRITE] SELECT completed.
09:47:50.034 [main] INFO  com.descartes.gos.jimap.TestCommands - C:
A153 SEARCH NOT Deleted
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <tag>: A153
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <command>: SEARCH
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [INVALID_COMMAND_COUNT]=[0]
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [SEARCH_MODSEQ]=[<null>]
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Removing value for key [SEARCH_MODSEQ]
09:47:50.050 [main] INFO  com.descartes.gos.jimap.TestCommands - S:
* SEARCH
A153 OK SEARCH completed.
09:47:50.050 [main] INFO  com.descartes.gos.jimap.TestCommands - C:
A164 EXPUNGE
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <tag>: A164
09:47:50.050 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <command>: EXPUNGE
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [INVALID_COMMAND_COUNT]=[0]
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [ENABLED_CAPABILITIES]=[[]]
09:47:50.066 [main] INFO  com.descartes.gos.jimap.TestCommands - S:
A164 OK EXPUNGE completed.
09:47:50.066 [main] INFO  com.descartes.gos.jimap.TestCommands - C:
A003 NOOP
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <tag>: A003
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <command>: NOOP
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [INVALID_COMMAND_COUNT]=[0]
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Loaded mailbox #private:mrc:INBOX
09:47:50.066 [main] INFO  com.descartes.gos.jimap.TestCommands - S:
A003 OK NOOP completed.
09:47:50.066 [main] INFO  com.descartes.gos.jimap.TestCommands - C:
A004 LOGOUT
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <tag>: A004
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Got <command>: LOGOUT
09:47:50.066 [main] DEBUG com.descartes.gos.jimap.JimapSession - Session attribute [INVALID_COMMAND_COUNT]=[0]
09:47:50.066 [main] INFO  com.descartes.gos.jimap.TestCommands - S:
* BYE IMAP4rev1 Server logging out
A004 OK LOGOUT completed.
*/

