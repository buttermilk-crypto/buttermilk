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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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

import net.iharder.Base64;

import com.cryptoregistry.KeyMaterials;
import com.cryptoregistry.MapData;
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
    JButton btnCreateSession;
	
	JFileChooser fc;
	
	final JLabel lblStatusLabel;
	
	public AddKeyPanel(Properties props) {
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
				 int returnVal = fc.showOpenDialog(AddKeyPanel.this);
				 
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
		
		lblStatusLabel = new JLabel("...");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(scroll, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(textField, GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewButton))
						.addComponent(lblLoadKeyTo)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblStatusLabel, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, 152, Short.MAX_VALUE)
							.addComponent(btnCreateSession)))
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
						.addComponent(btnCreateSession)
						.addComponent(lblStatusLabel))
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
				// error detection 
				JSONReader reader = new JSONReader(new StringReader(res));
				KeyMaterials km = reader.parse();
				List<MapData> localData = km.mapData();
				if(localData.size()>0 &&localData.get(0).data.containsKey("Error")){
					// error data present
					sessionTok = null;
				}else{
					// OK, process sessionToken Encryption
					sessionTok = unencrypt(localData.get(0), client.getEphemeralKey());
					System.err.println("Got session token, length = "+sessionTok.length+" bytes");
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
					btnCreateSession.setEnabled(true);
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			 
			 /*  on the other side...
			  * 
			  * 	data.put("EncryptedSessionToken", tokenString);
			  *	    data.put("Encoding", "Base64url");
			  *		data.put("EncryptedWith", clientKey.getMetadata().getHandle());
			  */
			private byte [] unencrypt(MapData data, RSAKeyContents ephemeralKey){
				String tokenString = data.data.get("EncryptedSessionToken");
			//	String encoding = data.data.get("Encoding");
				String encryptedWith = data.data.get("EncryptedWith");
				if(!ephemeralKey.getMetadata().getHandle().equals(encryptedWith)){
					// sanity checking, these should match
					throw new RuntimeException("These should match: "
							+encryptedWith
							+", "
							+ephemeralKey.getMetadata().getHandle()
					);
				}
				RSAEngineFactory.Padding pad = RSAEngineFactory.Padding.OAEPWITHSHA256ANDMGF1PADDING;
				byte[] bytes = null;
				try {
					bytes = CryptoFactory.INSTANCE.decrypt(
							ephemeralKey, 
							pad, 
							Base64.decode(tokenString, Base64.URL_SAFE)
					);
					System.err.println("Completed decryption: "+bytes.length+" bytes");
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return bytes;
				
			}
			
		};
		worker.execute();
	}
}
