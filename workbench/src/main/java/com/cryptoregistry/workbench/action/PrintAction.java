/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench.action;


import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.cryptoregistry.workbench.UUIDTextPane;


public class PrintAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private JFileChooser fc;
	private JTabbedPane tabs;
	@SuppressWarnings("unused")
	private boolean saveTo;
	
	public PrintAction(JTabbedPane tabs, JFileChooser fc) {
		super();
		this.fc = fc;
		this.tabs = tabs;
		this.putValue(Action.NAME, "Print...");
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int index = tabs.getSelectedIndex();
		if(index == -1) return;
		
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(index)).getViewport().getView();
		try {
			pane.print();
		} catch (PrinterException e) {
			e.printStackTrace();
		}
		
	}

}
