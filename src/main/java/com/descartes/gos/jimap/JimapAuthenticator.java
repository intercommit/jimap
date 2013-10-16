package com.descartes.gos.jimap;

import org.apache.james.mailbox.store.Authenticator;

/**
 * Always allows access.
 * @author fwiers
 *
 */
public class JimapAuthenticator implements Authenticator {

	public boolean isAuthentic(String userid, CharSequence passwd) {
		return true;
	}

}
