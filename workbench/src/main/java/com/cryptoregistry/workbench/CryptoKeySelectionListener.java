package com.cryptoregistry.workbench;

import java.util.EventObject;

/**
 * Get a notification that a key was selected and made the "current key." 
 * 
 * @author Dave
 *
 */
public interface CryptoKeySelectionListener {
	void currentKeyChanged(EventObject evt);
}
