/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.cryptoregistry.MapData;

public class EditAttributeDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private EditAttributePanel panel;

	public EditAttributeDialog(JFrame frame) {
		super(frame, "Edit Attribute Dialog", true);
		panel = new EditAttributePanel(frame);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
	//	setVisible(true);
	}
	
	public void open() {
		this.panel.setOwner(this);
		this.panel.init();
		this.setVisible(true);
	}

	public MapData toMapData() {
		return panel.toMapData();
	}
	
}
