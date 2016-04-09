/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench.action;


import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import asia.redact.bracket.properties.OutputAdapter;
import asia.redact.bracket.properties.PlainOutputFormat;
import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.workbench.ButtonTabComponent;
import com.cryptoregistry.workbench.UUIDTextPane;
import com.cryptoregistry.workbench.WorkbenchGUI;

public class ShowPropertiesAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabs;
	private Properties props;

	public ShowPropertiesAction(JTabbedPane tabs, Properties props) {
		this.tabs = tabs;
		this.props = props;
		this.putValue(Action.NAME, "Show Effective Properties");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {

			UUIDTextPane pane = new UUIDTextPane();
			pane.setFont(WorkbenchGUI.plainTextFont);
			StringWriter writer = new StringWriter();
			OutputAdapter adapter = new OutputAdapter(props);
			try {
				adapter.writeTo(writer, new PlainOutputFormat());
			} catch (IOException e) {}
			pane.setText(writer.toString());
			JScrollPane scroll = new JScrollPane(pane);
			tabs.add("Properties",scroll);
        	tabs.setTabComponentAt(tabs.indexOfComponent(scroll), new ButtonTabComponent(tabs));
			pane.requestFocusInWindow();
	}

}
