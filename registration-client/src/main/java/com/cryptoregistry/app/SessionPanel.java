/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JTextPane;

import com.cryptoregistry.formats.JSONReader;

import asia.redact.bracket.properties.Properties;

public class SessionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Properties props;
	private JTextField textField;
	private JTextPane textPane;
    JButton btnCreateSession;
	
	JFileChooser fc;
	
	public SessionPanel(Properties props) {
		super();
		this.props = props;
		 fc = new JFileChooser();
		 Path currentRelativePath = Paths.get("");
		 String cd = currentRelativePath.toAbsolutePath().toString();
		 fc.setCurrentDirectory(new File(cd));
		 fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		JLabel lblLoadKeyTo = new JLabel("Enter path to Signing Key, this is what you generated at registration");
		
		textField = new JTextField();
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("File Dialog");
		btnNewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				 int returnVal = fc.showOpenDialog(SessionPanel.this);
				 
		            if (returnVal == JFileChooser.APPROVE_OPTION) {
		                File file = fc.getSelectedFile();
		                try {
							textField.setText(file.getCanonicalPath());
							btnCreateSession.setEnabled(true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
				
			}
			
		});
		
		btnCreateSession = new JButton("Create Session");
		btnCreateSession.setEnabled(false);
		btnCreateSession.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				btnCreateSession.setEnabled(false);
				createSession();
			}
			
		});
		
		textPane = new JTextPane();
		JScrollPane scroll = new JScrollPane(textPane);
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(scroll, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(textField, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton))
						.addComponent(btnCreateSession, Alignment.TRAILING)
						.addComponent(lblLoadKeyTo))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblLoadKeyTo)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnNewButton))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(scroll, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnCreateSession)
					.addContainerGap())
		);
		setLayout(groupLayout);
	}
	
	/**
	 * Run in a swing worker
	 * 
	 */
	private void createSession() {
		
		textPane.setText("Working...");

		SwingWorker<String,String> worker = new SwingWorker<String,String>() {
			
			private byte[]sessionTok;
			protected String doInBackground() throws Exception {
				// send message with ephemeral RSA key, response has our encrypted token
				SessionClient client = new SessionClient(props, textField.getText());
				String res = client.request();
				return res;
			}
			 @Override
			public void done() {
				try {
					String res = get();
					textPane.setText(res);
					if(sessionTok!= null) {
						SwingRegistrationWizardGUI.km.setSessionToken(sessionTok);
					}
					btnCreateSession.setEnabled(true);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		};
		worker.execute();
	}
}
