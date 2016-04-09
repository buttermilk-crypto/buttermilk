/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.handle.CryptoHandle;
import com.cryptoregistry.handle.Handle;
import com.cryptoregistry.workbench.ExceptionHolder;
import com.cryptoregistry.workbench.RegistrationSender;
import com.cryptoregistry.workbench.UUIDTextPane;

public class RegisterAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private JTabbedPane tabs;
	private Properties props;
	private JLabel statusLabel;

	private RegistrationSender sender;
	private ExceptionHolder exception;

	public RegisterAction(JTabbedPane tabs, Properties props, JLabel statusLabel) {
		this.tabs = tabs;
		this.putValue(Action.NAME, "Register");
		this.props = props;
		this.statusLabel = statusLabel;
		exception = new ExceptionHolder();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		final Component comp = (Component) evt.getSource();
		int index = tabs.getSelectedIndex();
		if (index == -1)
			return; // fail because no tabs found
		final UUIDTextPane pane = (UUIDTextPane) ((JScrollPane) tabs
				.getComponentAt(index)).getViewport().getView();
		final String regJSON = pane.getText();
		
		JSONReader reader = new JSONReader(new StringReader(regJSON));
		KeyMaterials km = reader.parse();
		ErrorMsg msg = validateBasics(km);
		if(msg != null) {
			// problem
			JOptionPane.showMessageDialog(comp, "Problem: "
					+ msg.detail, msg.error,
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		statusLabel.setText("Using "
				+ props.get("registration.services.hostname") + "...");
		sender = new RegistrationSender(props);

		SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {

			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					sender.request(regJSON);
					return sender.isSuccess();
				} catch (RuntimeException x) {
					exception.ex = x;
					return false;
				}
			}

			@Override
			public void done() {
				try {
					statusLabel.setText("Sent.");

					if (get()) {
						JOptionPane.showMessageDialog(comp, "Success!",
								"Registration Results",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					} else {
						if (exception.hasException()) {
							String msg = exception.ex.getMessage();
							JOptionPane.showMessageDialog(comp, "Problem: "
									+ msg, "Registration Results",
									JOptionPane.ERROR_MESSAGE);

						} else {
							JOptionPane.showMessageDialog(
									comp,
									"Sorry, Registration Pre-check Failed: "
											+ sender.getResponseBody(),
									"Registration Results",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}
	
	private ErrorMsg validateBasics(KeyMaterials km) {

		String regHandle = km.regHandle();
		Handle handle = CryptoHandle.parseHandle(regHandle);
		if (handle == null) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Invalid registration handle: null");
		}

		if (!handle.validate()) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Invalid registration handle format");
		}

		String privateEmail = km.email();
		if (privateEmail == null || privateEmail.trim().length() == 0) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Admin email field is empty or blank");
		}
		List<MapData> keys = km.keyMaps();
		if (keys == null || keys.size() == 0) {
			return new ErrorMsg("Registration Pre-check Failed",
					"No keys found, expecting one");
		}
		// check that the keys are all for publication
		// if not, then fail.
		for (MapData k : keys) {
			if (!k.uuid.endsWith("-P")) {
				return new ErrorMsg("Registration Pre-check Failed",
						"Key found which is not apparently for publication: " + k.uuid);
			}
		}

		List<MapData> contacts = km.contactMaps();
		if (contacts == null || contacts.size() == 0) {
			return new ErrorMsg("Registration Pre-check Failed",
					"No contact records parsed. At least one contact record is expected.");
		}

		List<MapData> sigs = km.signatureMaps();
		if (sigs == null || sigs.size() == 0) {
			return new ErrorMsg("Registration Pre-check Failed",
					"No signatures found. A self-signed signature is expected.");
		}

		// basic size safeguards

		if (sigs.size() > 2) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Too many signature records, max of 2 allowed");
		}

		if (contacts.size() > 20) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Too many contact records, max of 20 allowed");
		}

		if (keys.size() > 3) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Too many keys, max of 3 allowed");
		}

		if (km.mapData().size() > 3) {
			return new ErrorMsg("Registration Pre-check Failed",
					"Too many local data entries, max of 3 allowed");
		}
		
		boolean foundCopyright = false;
		boolean foundAgreement = false;
		boolean affirmation = false;
		
		for(MapData md : km.mapData()){
			System.err.println(md);
			
			if(md.data.containsKey("Copyright")) {
				foundCopyright = true;
			}
			if(md.data.containsKey("TermsOfServiceAgreement")){
				foundAgreement = true;
			}
			if(md.data.containsKey("InfoAffirmation")){
				affirmation = true;
			}
		}
		
		if (!foundCopyright) {
			return new ErrorMsg("Registration Pre-check Failed",
					"No 'Copyright' field found in local data");
		}
		
		if (!foundAgreement) {
			return new ErrorMsg("Registration Pre-check Failed",
					"No 'TermsOfServiceAgreement' field found in local data, this is required");
		}
		
		if (!affirmation) {
			return new ErrorMsg("Registration Pre-check Failed",
					"No 'InfoAffirmation' field found in local data, this is required");
		}

		// OK

		return null;
	}

	class ErrorMsg  {

		public String error;
		public String detail = "";

		public ErrorMsg(String error) {
			super();
			this.error = error;
		}

		public ErrorMsg(String error, String detail) {
			super();
			this.error = error;
			this.detail = detail;
		}
	}

}
