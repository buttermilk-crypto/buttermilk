package com.cryptoregistry.workbench;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class EnterPasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private EnterPasswordPanel panel;

	public EnterPasswordDialog(JFrame frame, String keyDescriptor) {
		// modal and use once only
		super(frame, "Enter Password Dialog", true);
		panel = new EnterPasswordPanel(this, keyDescriptor);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public char[] getPassword() {
		return panel.getPasswordField().getPassword();
	}

	public boolean isOK() {
		return panel.isOK();
	}
	
	
}
