/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.util.EventObject;

import javax.swing.JDialog;
import javax.swing.JFrame;


public class SetDefaultPasswordDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private SetDefaultPasswordPanel panel;

	public SetDefaultPasswordDialog(JFrame parent, String title) {
		super(parent,title);
		panel = new SetDefaultPasswordPanel(this);
		System.err.println(panel.getPreferredSize());
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
	            	SetDefaultPasswordDialog pd =new SetDefaultPasswordDialog(new JFrame(), "Enter a Password");
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
