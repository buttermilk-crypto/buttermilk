package com.cryptoregistry.workbench;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class SignatureItemSelectionDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private SignatureItemSelectionPanel panel;

	public SignatureItemSelectionDialog(JFrame frame, String tabText) {
		// modal and use once only
		super(frame, "Signature Item Selection Dialog", true);
		panel = new SignatureItemSelectionPanel(this, tabText);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public boolean isOK() {
		return panel.isOK();
	}
	
	
	
}
