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
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
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

import net.iharder.Base64;

import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.formats.JSONGenericReader;
import com.cryptoregistry.formats.JSONReader;
import com.cryptoregistry.rsa.CryptoFactory;
import com.cryptoregistry.rsa.RSAEngineFactory;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.rsa.RSAKeyContents;

public class AddKeyPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private Properties props;
	private JTextField textField;
	private JTextPane textPane;
    JButton btnSendKeys;
	
	JFileChooser fc;
	
	final JLabel lblStatusLabel;
	
	public AddKeyPanel(Properties props) {
		super();
		this.props = props;
		 fc = new JFileChooser();
		 Path currentRelativePath = Paths.get("");
		 String cd = currentRelativePath.toAbsolutePath().toString();
		 fc.setCurrentDirectory(new File(cd));
		 fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		JLabel lblLoadKeyTo = new JLabel("Enter path to file containing contacts, for-publication key data, signatures, etc.");
		
		textField = new JTextField();
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("File Dialog");
		btnNewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				 int returnVal = fc.showOpenDialog(AddKeyPanel.this);
				 
		            if (returnVal == JFileChooser.APPROVE_OPTION) {
		                File file = fc.getSelectedFile();
		                try {
							textField.setText(file.getCanonicalPath());
							btnSendKeys.setEnabled(true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		            }
			}
			
		});
		
		btnSendKeys = new JButton("Send To Server");
		btnSendKeys.setEnabled(false);
		btnSendKeys.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(SwingRegistrationWizardGUI.km == null) {
					JOptionPane.showMessageDialog(btnSendKeys, "Session key not set. Please create a session first.");
					return;
				}
				btnSendKeys.setEnabled(false);
				sendData();
			}
			
		});
		
		textPane = new JTextPane();
		JScrollPane scroll = new JScrollPane(textPane);
		
		lblStatusLabel = new JLabel("...");
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
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(lblStatusLabel, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
							.addComponent(btnSendKeys))
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
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSendKeys)
						.addComponent(lblStatusLabel))
					.addContainerGap())
		);
		setLayout(groupLayout);
	}
	
	/**
	 * Run in a swing worker
	 * 
	 */
	private void sendData() {
		
		textPane.setText("Working...");

		SwingWorker<String,String> worker = new SwingWorker<String,String>() {
			
			private byte[]sessionTok;
			
			protected String doInBackground() throws Exception {
				
				if(SwingRegistrationWizardGUI.km != null) {
					sessionTok = SwingRegistrationWizardGUI.km.getSessionToken();
				}else{
					throw new RuntimeException("Missing session token");
				}
				
				// first get the contents and make an hmac
				File keyFile = new File(textField.getText());
				JSONGenericReader reader = new JSONGenericReader(keyFile);
				reader.clearExistingMacs();
				reader.embedHMac(sessionTok);
				StringWriter writer = new StringWriter();
				reader.reformat(writer);
				
				// send message with embedded mac created using the session token
				AddKeyClient client = new AddKeyClient(props, writer.toString());
				String res = client.request();
				// error detection 
				JSONReader result = new JSONReader(new StringReader(res));
				KeyMaterials km = result.parse();
				List<MapData> localData = km.mapData();
				if(localData.size()>0 &&localData.get(0).data.containsKey("Error")){
					// error data present
					sessionTok = null;
				}else{
					// OK, success response
					System.err.println("All good on add request...");
				}
				return res;
			}
			 @Override
			public void done() {
				try {
					String res = get();
					textPane.setText(res);
					if(sessionTok!= null && SwingRegistrationWizardGUI.km != null) {
						SwingRegistrationWizardGUI.km.setSessionToken(sessionTok);
						lblStatusLabel.setText("Got it");
					}
					btnSendKeys.setEnabled(true);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			
		};
		worker.execute();
	}
}
