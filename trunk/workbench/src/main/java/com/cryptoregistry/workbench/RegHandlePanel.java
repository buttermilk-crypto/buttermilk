/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.SwingWorker;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.handle.CryptoHandle;
import com.cryptoregistry.handle.Handle;

public class RegHandlePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JTextField regHandleTextField;
	private final RegHandleChecker checker;
	
	private final ExceptionHolder exception;
	private JTextField existingHandleTextField;
	
	private final RegHandleSearchDialog parent;
	
	private List<RegHandleListener> listeners = new ArrayList<RegHandleListener>();
	
	public RegHandlePanel(RegHandleSearchDialog par, Properties props){
		super();
		
		this.parent = par;
		exception = new ExceptionHolder();
		checker = new RegHandleChecker(props);
	
		JLabel lblRegistrationHandle = new JLabel("Search for an available registration handle (requires Internet connection)");
		final JLabel validationLabel = new JLabel("...");
		final JLabel lblAvailable = new JLabel("...");
		
		regHandleTextField = new JTextField("");
		regHandleTextField.setColumns(10);
		
		JButton btnCheckAvailability = new JButton("Check Availability");
		btnCheckAvailability.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String regHandle = regHandleTextField.getText();
				if(regHandle == null || regHandle.trim().equals("")){
					// do nothing
					return;
				}
				
				lblAvailable.setText("Checking...");
				lblAvailable.setEnabled(false);
				
				SwingWorker<Boolean,String> worker = new SwingWorker<Boolean,String>() {
					
					@Override
					protected Boolean doInBackground() throws Exception {
						try {
						return checker.check(regHandle);
						}catch(RuntimeException x){
							exception.ex = x;
							return false;
						}
					}
					
					 @Override
					public void done() {
						 try {
							 lblAvailable.setEnabled(true);
								if(get()) {
									lblAvailable.setText("Available!");
								}else{
									if(exception.hasException()){
										lblAvailable.setText(exception.ex.getMessage());
										exception.ex.printStackTrace();
									}else{
										lblAvailable.setText("Not Available, Sorry.");
									}
								}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				};
				worker.execute();
			}
		});
		
		
		regHandleTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				lblAvailable.setText("...");
				String text = regHandleTextField.getText();
				Handle h = CryptoHandle.parseHandle(text);
				if(h.validate()){
					validationLabel.setText("Valid Syntax: type "+h.getClass().getSimpleName());
					validationLabel.setForeground(Color.BLACK);
				}else{
					validationLabel.setText("Formatting error.");
					validationLabel.setForeground(Color.RED);
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
		});
		
		JButton btnCreate = new JButton("OK");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireRegHandleChangedEvent(regHandleTextField.getText().trim());	
				parent.setVisible(false);
				parent.dispose();
			} 
		});
		parent.getRootPane().setDefaultButton(btnCreate);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.setVisible(false);
				parent.dispose();
			}
		});
		
		JLabel lblIAlReady = new JLabel("I already have a registration handle in mind, or pre-existing:");
		
		existingHandleTextField = new JTextField();
		existingHandleTextField.setColumns(10);
		if(props.containsKey("registration.handle")){
			existingHandleTextField.setText(props.get("registration.handle"));
		}
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				fireRegHandleChangedEvent(existingHandleTextField.getText().trim());	
				parent.setVisible(false);
				parent.dispose();
			}
		});
		
		JButton btnCancel_1 = new JButton("Cancel");
		btnCancel_1.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.setVisible(false);
				parent.dispose();
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(existingHandleTextField, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addGap(10)
							.addComponent(lblAvailable, GroupLayout.DEFAULT_SIZE, 89, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(validationLabel, GroupLayout.PREFERRED_SIZE, 167, GroupLayout.PREFERRED_SIZE)
							.addGap(43)
							.addComponent(btnCheckAvailability))
						.addComponent(regHandleTextField, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(btnCancel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCreate))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(btnCancel_1)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnOk))
						.addComponent(lblRegistrationHandle)
						.addComponent(lblIAlReady))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblRegistrationHandle)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(regHandleTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(validationLabel)
								.addComponent(lblAvailable)))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnCheckAvailability)))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCreate)
						.addComponent(btnCancel))
					.addGap(28)
					.addComponent(lblIAlReady)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(existingHandleTextField, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOk)
						.addComponent(btnCancel_1))
					.addContainerGap(22, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}


	public JTextField getRegHandleTextField() {
		return regHandleTextField;
	}
	
	public void addRegHandleListener(RegHandleListener rhl){
		listeners.add(rhl);
	}
	
	private void fireRegHandleChangedEvent(String handle){
		for(RegHandleListener rhl: listeners){
			RegHandleEvent evt = new RegHandleEvent(this);
			evt.setRegHandle(handle);
			rhl.registrationHandleChanged(evt);
		}
	}
	
}
