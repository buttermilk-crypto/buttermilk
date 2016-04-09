/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JFileChooser;

public class InitialSetupDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextField rootDirTextField;
	private JFileChooser fc;
	private boolean succeeded;
	private JTextField adminEmailTextField;
	private final EmailFormatValidator validator = new EmailFormatValidator();

	public InitialSetupDialog(Frame owner, String title) {
		super(owner, title, true);
		
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.WEST);
		
		JLabel lblPleaseChooseThe = new JLabel("Please choose the directory where by default your key materials will be stored:");
		
		rootDirTextField = new JTextField();
		rootDirTextField.setColumns(10);
		
		JButton btnSelect = new JButton("Select");
		btnSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Component comp = (Component) evt.getSource();
			    int returnVal = fc.showOpenDialog(comp);

		        if (returnVal == JFileChooser.APPROVE_OPTION) {
		            File file = fc.getSelectedFile();
		            try {
						rootDirTextField.setText(file.getCanonicalPath());
						succeeded = true;
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
		        }else{
		        	succeeded = false;
		        }
			}
		});
		
		final InitialSetupDialog isd = this;
		final JButton btnOk = new JButton("OK");
		btnOk.setEnabled(false);
		btnOk.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				isd.setVisible(false);
				isd.dispose();
			}
		});
		
		final JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				isd.setVisible(false);
				isd.dispose();
			}
		});
		
		JLabel lblEnterAnAdministrative = new JLabel("Enter an administrative, default contact email address:");
		
		adminEmailTextField = new JTextField();
		adminEmailTextField.setColumns(50);
		adminEmailTextField.addKeyListener(new KeyAdapter(){
			@Override
			public void keyReleased(KeyEvent e) {
				String email = adminEmailTextField.getText();
				if(email == null || email.length() <10) {
					btnOk.setEnabled(false);
					return;
				}
				if(validator.validate(email)){
					btnOk.setEnabled(true);
				}else{
					btnOk.setEnabled(false);
				}
			}
		});
		
		
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
						.addComponent(lblPleaseChooseThe, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 442, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
							.addComponent(rootDirTextField, GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSelect))
						.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
							.addComponent(btnCancel)
							.addGap(5)
							.addComponent(btnOk))
						.addComponent(lblEnterAnAdministrative)
						.addComponent(adminEmailTextField, GroupLayout.PREFERRED_SIZE, 295, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPleaseChooseThe)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSelect)
						.addComponent(rootDirTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(11)
					.addComponent(lblEnterAnAdministrative)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(adminEmailTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 56, Short.MAX_VALUE)
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCancel)
						.addComponent(btnOk))
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		panel.setPreferredSize(new Dimension(462,200));
		pack();
		setVisible(true);
	}

	public JTextField getRootDirTextField() {
		return rootDirTextField;
	}

	public boolean isSucceeded() {
		return succeeded;
	}

	public JTextField getAdminEmailTextField() {
		return adminEmailTextField;
	}
}
