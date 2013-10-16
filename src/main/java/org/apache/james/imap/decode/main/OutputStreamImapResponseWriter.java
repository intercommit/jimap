package org.apache.james.imap.decode.main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.james.imap.encode.ImapResponseWriter;
import org.apache.james.imap.message.response.Literal;

/**
 * Class providing methods to send response messages from the server to the
 * client.
 * <br>Copied from test sources.
 */
public class OutputStreamImapResponseWriter implements ImapResponseWriter {

	private final OutputStream output;

	public OutputStreamImapResponseWriter(OutputStream output) {
		this.output = output;
	}

	public void flush() throws IOException {
		output.flush();
	}



	/**
	 * @see
	 * org.apache.james.imap.encode.ImapResponseWriter#write(org.apache.james.imap.message.response.Literal)
	 */
	public void write(Literal literal) throws IOException {
		InputStream in = null;
		try {
			in = literal.getInputStream();

			byte[] buffer = new byte[1024];
			for (int len; (len = in.read(buffer)) != -1;) {
				output.write(buffer, 0, len);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}

	}

	/**
	 * @see org.apache.james.imap.encode.ImapResponseWriter#write(byte[])
	 */
	public void write(byte[] buffer) throws IOException {
		output.write(buffer);
	}

}
