package com.cryptoregistry.workbench;

import java.util.EventObject;

import javax.swing.JDialog;
import javax.swing.JFrame;


public class EnterPasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private EnterPasswordPanel panel;

	public EnterPasswordDialog(JFrame parent, String title) {
		super(parent,title);
		panel = new EnterPasswordPanel(this);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		//setVisible(true);
	}
	
	public void open() {
		setVisible(true);
	}
	
	public void addPasswordChangedListener(PasswordListener pw) {
		panel.addPasswordChangedListener(pw);
	}
	
	public static void main(String[] a) {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	EnterPasswordDialog pd =new EnterPasswordDialog(new JFrame(), "Enter a Password");
	            	pd.addPasswordChangedListener(new PasswordListener(){
						@Override
						public void passwordChanged(EventObject evt) {
							char [] pass = ((PasswordEvent)evt).getPasswordValue();
							System.err.println(new String(pass));
						}
	            	});
	            	pd.open();
	            }
	        });
		
	}

}
