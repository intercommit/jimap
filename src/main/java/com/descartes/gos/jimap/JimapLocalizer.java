package com.descartes.gos.jimap;

import org.apache.james.imap.api.display.HumanReadableText;
import org.apache.james.imap.api.display.Locales;
import org.apache.james.imap.api.display.Localizer;

/**
 * Does not localize anything.
 * @author fwiers
 *
 */
public class JimapLocalizer implements Localizer {

	public String localize(HumanReadableText text, Locales locales) {
		return text.getDefaultValue();
	}

}
