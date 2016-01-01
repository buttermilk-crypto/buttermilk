package com.cryptoregistry.workbench.action;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyWrapper;
import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.workbench.EnterPasswordDialog;
import com.cryptoregistry.workbench.UnlockKeyEvent;
import com.cryptoregistry.workbench.PasswordEvent;
import com.cryptoregistry.workbench.PasswordListener;
import com.cryptoregistry.workbench.UUIDTextPane;
import com.cryptoregistry.workbench.UnlockKeyListener;


public class UnlockKeysAction extends AbstractAction implements PasswordListener {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private JFrame frame;
	
	private JLabel statusPane;
	
	private char [] defaultPassword;
	
	private List<UnlockKeyListener> listeners = new ArrayList<UnlockKeyListener>();
	
	public UnlockKeysAction(JFrame frame, JTabbedPane tabs, JLabel statusPane) {
		super();
		this.frame = frame;
		this.tabs = tabs;
		this.statusPane = statusPane;
		this.putValue(Action.NAME, "Unlock Key(s) in CurrentTab");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int index = tabs.getSelectedIndex();
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(index)).getViewport().getView();
		String text = pane.getText();
		if(text.length()==0)return;
		JSONReader reader = new JSONReader(new StringReader(text));
		KeyMaterials km = reader.parse();
		for(CryptoKeyWrapper wrapper : km.keys()) {
			if(wrapper.isSecure()) {
				if(defaultPassword != null){
					boolean ok = wrapper.unlock(new Password(defaultPassword));
					if(ok) {
						CryptoKey key = wrapper.getKeyContents();
						fireSecureKeyUnlocked(key);
						statusPane.setText("Unlocked "+wrapper.distingushedHandle());
					}else{
						// password failed, ask for password and try again
						JOptionPane.showMessageDialog((Component)evt.getSource(),
							    "Default password value failed to unlock the key. Please enter a password or press Cancel.",
							    "Request",
							    JOptionPane.WARNING_MESSAGE);
						EnterPasswordDialog dialog = new EnterPasswordDialog(frame,wrapper.distingushedHandle());
						if(dialog.isOK()){
							ok = wrapper.unlock(new Password(dialog.getPassword()));
							if(ok) {
								CryptoKey key = wrapper.getKeyContents();
								fireSecureKeyUnlocked(key);
								statusPane.setText("Unlocked "+wrapper.distingushedHandle());
							}else{
								JOptionPane.showMessageDialog((Component)evt.getSource(),
									    "Sorry, that failed.",
									    "Result",
									    JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				}else{
					// default password is null, ask for password and try again
					EnterPasswordDialog dialog = new EnterPasswordDialog(frame,wrapper.distingushedHandle());
					if(dialog.isOK()){
						boolean ok = wrapper.unlock(new Password(dialog.getPassword()));
						if(ok) {
							CryptoKey key = wrapper.getKeyContents();
							fireSecureKeyUnlocked(key);
						}else{
							JOptionPane.showMessageDialog((Component)evt.getSource(),
								    "Sorry, that failed.",
								    "Result",
								    JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		}
	}

	@Override
	public void passwordChanged(EventObject evt) {
		PasswordEvent pevt = (PasswordEvent)evt;
		defaultPassword = pevt.getPasswordValue();
	}
	
	private void fireSecureKeyUnlocked(CryptoKey key){
		for(UnlockKeyListener l: listeners){
			UnlockKeyEvent evt = new UnlockKeyEvent(this);
			evt.setKey(key);
			l.keyUnlocked(evt);
		}
	}
	
	public void addUnlockKeyListener(UnlockKeyListener listener){
		this.listeners.add(listener);
	}

}
