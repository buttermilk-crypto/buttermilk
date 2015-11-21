package com.cryptoregistry.app;

import java.io.InputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;

import asia.redact.bracket.properties.Properties;

/** 
 * create a session and cache the token
 * 
 */
public class SessionDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public SessionDialog(JFrame parent, String title, Properties props) {
		super(parent, title);
		

		getContentPane().add(new SessionPanel(props));

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public static void main(String[] a) {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	InputStream in = Thread.currentThread()
	            			.getContextClassLoader().getResourceAsStream("regwizard.properties");
	            	Properties props = Properties.Factory.getInstance(in);
	            	new SessionDialog(new JFrame(), "Create Session", props);
	            }
	        });
		
	}
}
