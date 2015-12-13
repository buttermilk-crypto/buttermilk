package com.cryptoregistry.app.workbench;

import java.io.InputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;

import asia.redact.bracket.properties.Properties;

public class PasswordInputDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private PasswordInputPanel panel;

	public PasswordInputDialog(JFrame parent, String title, Properties props) {
		// make model
		super(parent, title, true);
		panel = new PasswordInputPanel(this, props, false);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}
	
	public char [] getPassword() {
		return panel.getPassword();
	}
	
	public static void main(String[] a) {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	InputStream in = Thread.currentThread()
	            			.getContextClassLoader().getResourceAsStream("regwizard.properties");
	            	Properties props = Properties.Factory.getInstance(in);
	            	PasswordInputDialog dialog = new PasswordInputDialog(null, "Enter Password", props);
	            	char [] pass = dialog.getPassword();
	            	if(pass == null) {
	            		System.err.println("password was null");
	            	}else{
	            		System.err.println(new String(pass));
	            	}
	            }
	        });
		
	}


}
