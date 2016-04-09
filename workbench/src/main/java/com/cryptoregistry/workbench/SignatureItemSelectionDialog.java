/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.cryptoregistry.signature.CryptoSignature;

public class SignatureItemSelectionDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private SignatureItemSelectionPanel panel;

	public SignatureItemSelectionDialog(JFrame frame, String tabText, Set<KeyWrapper> keys) {
		// modal and use once only
		super(frame, "Signature Item Selection Dialog", true);
		panel = new SignatureItemSelectionPanel(this, tabText);
		panel.setKeys(keys);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public boolean isOK() {
		return panel.isOK();
	}

	public CryptoSignature getSig() {
		return panel.getSig();
	}
	
	
	
}
