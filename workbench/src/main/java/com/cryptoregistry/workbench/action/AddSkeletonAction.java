/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.EventObject;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.CryptoContact;
import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.workbench.CryptoKeySelectionEvent;
import com.cryptoregistry.workbench.CryptoKeySelectionListener;
import com.cryptoregistry.workbench.RegHandleEvent;
import com.cryptoregistry.workbench.RegHandleListener;
import com.cryptoregistry.workbench.UUIDTextPane;

public class AddSkeletonAction extends AbstractAction implements RegHandleListener, CryptoKeySelectionListener {

	private static final long serialVersionUID = 1L;
	
	JTabbedPane tabs;
	String skeletonText;
	String regHandle;
	String adminEmail;
	
	CryptoKey currentKey;
	RegistrationType type;

	public AddSkeletonAction(JTabbedPane tabs, String regHandle, String adminEmail, RegistrationType type) {
		
		switch(type){
			case BASIC: this.putValue(Action.NAME, "Basic"); break;
			case INDIVIDUAL: this.putValue(Action.NAME, "Individual"); break;
			case BUSINESS: this.putValue(Action.NAME, "Business"); break;
			case WEBSITE: this.putValue(Action.NAME, "Website"); break;
		}
		
		this.type = type;
		this.tabs = tabs;
		this.regHandle = regHandle;
		this.adminEmail = adminEmail;
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("skeleton.txt");
		 final char[] buffer = new char[64];
		    final StringBuilder out = new StringBuilder();
		    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
		        for (;;) {
		            int rsz = reader.read(buffer, 0, buffer.length);
		            if (rsz < 0)
		                break;
		            out.append(buffer, 0, rsz);
		        }
		        skeletonText = out.toString();
		    
		    } catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int currentIndex = tabs.getSelectedIndex();
		if(currentIndex == -1){
			JOptionPane.showMessageDialog((Component)evt.getSource(),
				    "Please create a tab for this operation to write into and then try again.",
				    "Request",
				    JOptionPane.WARNING_MESSAGE);
			return;
		}
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(currentIndex)).getViewport().getView();
		if(regHandle == null) regHandle = "";
		if(adminEmail == null) adminEmail = "";
		switch(type) {
			case BASIC: {
				String s0 = skeletonText.replace("$regHandle$", regHandle);
				String s1 = s0.replace("$adminEmail$", adminEmail);
				pane.setText(s1);
				break;
			}
			case INDIVIDUAL: {
				MapData iContact = genMapData("template-contacts-individual.properties");
				JSONFormatter format = new JSONFormatter(regHandle,adminEmail);
				format.setIncludeEmpty(true);
				format.add(new CryptoContact(iContact));
				if(currentKey != null) {
					format.add(currentKey);
				}
				StringWriter writer = new StringWriter();
				format.format(writer);
				pane.setText(writer.toString());
				break;
			}
			case BUSINESS: {
				MapData busContact = genMapData("template-contacts-business.properties");
				JSONFormatter format = new JSONFormatter(regHandle,adminEmail);
				format.setIncludeEmpty(true);
				format.add(new CryptoContact(busContact));
				if(currentKey != null) {
					format.add(currentKey);
				}
				StringWriter writer = new StringWriter();
				format.format(writer);
				pane.setText(writer.toString());
				break;
			}
			case WEBSITE: {
				MapData wc0 = genMapData("template-contacts-website-registrant.properties");
				MapData wc1 = genMapData("template-contacts-website-admin.properties");
				MapData wc2 = genMapData("template-contacts-website-technical.properties");
				JSONFormatter format = new JSONFormatter(regHandle,adminEmail);
				format.setIncludeEmpty(true);
				format.add(new CryptoContact(wc0));
				format.add(new CryptoContact(wc1));
				format.add(new CryptoContact(wc2));
				if(currentKey != null) {
					format.add(currentKey);
				}
				StringWriter writer = new StringWriter();
				format.format(writer);
				pane.setText(writer.toString());
				break;
			}
			default: return;
		}
	}

	@Override
	public void registrationHandleChanged(EventObject evt) {
		RegHandleEvent event = (RegHandleEvent)evt;
		regHandle = event.getRegHandle();
		
	}

	@Override
	public void currentKeyChanged(EventObject evt) {
		CryptoKeySelectionEvent e = (CryptoKeySelectionEvent) evt;
		currentKey = e.getKey().keyForPublication();
	}
	
	private MapData genMapData(String propName) {
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(propName);
		Properties props = Properties.Factory.getInstance(in);
		return new MapData(UUID.randomUUID().toString(),props.getFlattenedMap());
	}

}
