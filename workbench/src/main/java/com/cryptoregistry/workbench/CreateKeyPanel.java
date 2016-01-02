/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2015 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import javax.swing.JPasswordField;
import javax.swing.SwingWorker;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;

import javax.swing.JComboBox;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.Buttermilk;
import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.c2.key.C2KeyMetadata;
import com.cryptoregistry.c2.key.Curve25519KeyContents;
import com.cryptoregistry.c2.key.Curve25519KeyForPublication;
import com.cryptoregistry.ec.ECKeyContents;
import com.cryptoregistry.ec.ECKeyForPublication;
import com.cryptoregistry.ec.ECKeyMetadata;
import com.cryptoregistry.formats.JSONFormatter;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEAlg;
import com.cryptoregistry.rsa.RSAKeyContents;
import com.cryptoregistry.rsa.RSAKeyForPublication;
import com.cryptoregistry.rsa.RSAKeyMetadata;
import com.cryptoregistry.util.Check10K;
import com.cryptoregistry.util.entropy.TresBiEntropy;
import com.cryptoregistry.util.entropy.TresBiEntropy.Result;

import javax.swing.JCheckBox;

public class CreateKeyPanel extends JPanel implements PasswordListener, RegHandleListener {

	private static final long serialVersionUID = 1L;
	JComboBox<KeyGenerationAlgorithm> keyalgComboBox;
	JComboBox<PBEAlg> pbeAlgComboBox;
	private final JPasswordField password0;
	private final JPasswordField password1;
	JLabel passwordEqualityMsg;
	JLabel againLbl;
	JLabel lblEntropy;
	JButton btnCreate;
	JCheckBox chckbxOverrideDefaultPassword;
	
	private Check10K tenK;
	
	@SuppressWarnings("unused")
	private KeyGenerationAlgorithm keyAlg;
	private CryptoKey secureKey;
	private CryptoKey keyForPublication;
	private String keyText;
	private String regHandle;
	private String adminEmail = "";
	
	private Password defaultPassword;
	
	private List<CreateKeyListener> listeners = new ArrayList<CreateKeyListener>();
	private JLabel lblRegistrationHandle;
	private JLabel label;
	
	public CreateKeyPanel(final JDialog dialog, Properties props){
		super();
		if(props.containsKey("registration.handle")){
			regHandle = props.get("registration.handle");
		}
		if(props.containsKey("registration.email")){
			adminEmail = props.get("registration.email");
		}
		tenK = new Check10K();
		
		JLabel lblPassword = new JLabel("Password:");
		JLabel lblAgain = new JLabel("Again:");
		
		password0 = new JPasswordField("");
		password0.setEnabled(true);
		
		password0.setColumns(10);
		password0.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkPassword();
				checkEntropy();
			}
		});
		
		password1 = new JPasswordField("");
		password1.setEnabled(true);
		password1.setColumns(10);
		password1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkPassword();
				checkEntropy();
			}
		});
		
		lblEntropy = new JLabel("Entropy: 0 bits");
		
		final CreateKeyPanel instance = this;
		btnCreate = new JButton("Create");
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int count = password0.getPassword().length;
				if(count == 0) {
					JOptionPane.showMessageDialog((JButton)e.getSource(),
						    "Non-empty password required here to proceed.",
						    "Request",
						    JOptionPane.WARNING_MESSAGE);
					chckbxOverrideDefaultPassword.setSelected(true);
					password0.requestFocusInWindow();
					return;
				}
					
				btnCreate.setText("Working...");
				btnCreate.setEnabled(false);
				SwingWorker<String,String> worker = new SwingWorker<String,String>() {
					protected String doInBackground() throws Exception {
						createKey();
						formatKey();
						return keyText;
					}
					 @Override
					public void done() {
					  try {
							get();
							instance.fireKeyCreated(); //broadcast new key
							btnCreate.setText("Create");
							btnCreate.setEnabled(true);
							dialog.setVisible(false);
							dialog.dispose();
					  } catch (Exception e) {
							e.printStackTrace();
					  } 
					}
				};
				worker.execute();
			}
		});
		
		dialog.getRootPane().setDefaultButton(btnCreate);
		
		KeyGenerationAlgorithm [] e = KeyGenerationAlgorithm.usableForSignature();
		keyalgComboBox = new JComboBox<KeyGenerationAlgorithm>();
		DefaultComboBoxModel<KeyGenerationAlgorithm> model = new DefaultComboBoxModel<KeyGenerationAlgorithm>(e);
		keyalgComboBox.setModel(model);
		
		PBEAlg [] pbes = PBEAlg.values();
		DefaultComboBoxModel<PBEAlg> pbemodel = new DefaultComboBoxModel<PBEAlg>(pbes);
		pbeAlgComboBox = new JComboBox<PBEAlg>();
		pbeAlgComboBox.setModel(pbemodel);
		
		JLabel lblKeyAlg = new JLabel("Asymmetric Key Algorithm:");
		
		againLbl = new JLabel("");
		
		JLabel lblPasswordBasedEncryption = new JLabel("Password Based (PBE) Encryption Algorithm:");
		
		passwordEqualityMsg = new JLabel("...");
		
		chckbxOverrideDefaultPassword = new JCheckBox("Override Default Password");
		chckbxOverrideDefaultPassword.setSelected(false);
	
		
		chckbxOverrideDefaultPassword.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				  if (e.getStateChange() == ItemEvent.SELECTED) {
					  // when checked, enable password fields for editing.
					  password0.setEnabled(true);
					  password0.setText("");
					  password1.setEnabled(true);
					  password1.setText("");
					  password0.requestFocusInWindow();
				  }else if (e.getStateChange() == ItemEvent.DESELECTED) {
					  // when unchecked, disable password fields for editing; set to default password value
					  password0.setEnabled(false);
					 if(defaultPassword != null) password0.setText(new String(defaultPassword.getPassword()));
					  password1.setEnabled(false);
					if(defaultPassword != null)  password1.setText(new String(defaultPassword.getPassword()));
				  }
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				 dialog.setVisible(false);
				 dialog.dispose();
			}
		});
		
		lblRegistrationHandle = new JLabel("Registration Handle: ");
		
		label = new JLabel("...");
		if(regHandle != null){
			label.setText(regHandle);
		}
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblKeyAlg)
						.addComponent(againLbl)
						.addComponent(lblPasswordBasedEncryption))
					.addGap(31)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(keyalgComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(pbeAlgComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(112, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
					.addContainerGap(270, Short.MAX_VALUE)
					.addComponent(btnCancel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(btnCreate, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED, 4, GroupLayout.PREFERRED_SIZE)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblPassword)
								.addComponent(lblAgain))
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGap(10)
									.addComponent(passwordEqualityMsg)
									.addGap(108)
									.addComponent(lblEntropy))
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
									.addComponent(password1)
									.addComponent(password0, GroupLayout.PREFERRED_SIZE, 331, GroupLayout.PREFERRED_SIZE))))
						.addComponent(chckbxOverrideDefaultPassword)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblRegistrationHandle)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(label)))
					.addContainerGap(41, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblKeyAlg)
						.addComponent(keyalgComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(againLbl)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblPasswordBasedEncryption)
							.addComponent(pbeAlgComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRegistrationHandle)
						.addComponent(label))
					.addGap(18)
					.addComponent(chckbxOverrideDefaultPassword)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPassword)
						.addComponent(password0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAgain)
						.addComponent(password1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblEntropy)
						.addComponent(passwordEqualityMsg))
					.addPreferredGap(ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnCreate, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnCancel))
					.addContainerGap())
		);
		
		setLayout(groupLayout);
		checkPassword();
		checkEntropy();
	}
	
	//
	public void reinitialize() {
		this.secureKey = null;
		this.keyForPublication = null;
		this.keyText = null;
	}

	public JPasswordField getPassword0() {
		return password0;
	}
	
	private void checkPassword() {
		char[]pass0 = password0.getPassword();
		char[]pass1 = password1.getPassword();
		
		if(pass0 == null || pass0.length==0){
			this.passwordEqualityMsg.setForeground(Color.RED);
			this.passwordEqualityMsg.setText("Empty");
			return;
		}
		
		boolean ok = Arrays.equals(pass0, pass1);
		if(ok) {
			this.passwordEqualityMsg.setForeground(Color.BLACK);
			this.passwordEqualityMsg.setText("Match");
			this.btnCreate.setEnabled(true);
			if(tenK.contains(new String(pass0))){
				this.passwordEqualityMsg.setForeground(Color.RED);
				this.passwordEqualityMsg.setText("In 10K List!");
				this.btnCreate.setEnabled(false);
			}
		}else{
			this.passwordEqualityMsg.setForeground(Color.RED);
			this.passwordEqualityMsg.setText("Input not equal");
			this.btnCreate.setEnabled(false);
		}
	}
	
	private void checkEntropy() {
		char[]pass0 = password0.getPassword();
		if(pass0 == null || pass0.length ==0) {
			this.lblEntropy.setText("Entropy: 0 bits");
			return;
		}
		byte [] bytes = toBytes(pass0);
		TresBiEntropy bi = new TresBiEntropy(bytes);
		Result res = bi.calc();
		int entropy = (int) res.bitsOfEntropy;
		this.lblEntropy.setText("Entropy: "+entropy+" bits");
	}
	
	private byte[] toBytes(char[] chars) {
	    CharBuffer charBuffer = CharBuffer.wrap(chars);
	    ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
	    byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
	            byteBuffer.position(), byteBuffer.limit());
	    Arrays.fill(charBuffer.array(), '\u0000'); 
	    Arrays.fill(byteBuffer.array(), (byte) 0);
	    return bytes;
	}

	private void formatKey() {
		StringWriter writer = new StringWriter();
		JSONFormatter builder = new JSONFormatter(regHandle, adminEmail);
		builder.add(this.keyForPublication);
		builder.add(this.secureKey);
		builder.format(writer);
		keyText = writer.toString();
	}

	private void createKey(){
		
		if(regHandle == null || regHandle.trim().length() == 0) {
			System.err.println("registration handle not defined, please do that first.");
			JOptionPane.showMessageDialog(this,
				    "Proposed registration handle not defined, please do that first.",
				    "Notice",
				    JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// gather the required values
		KeyGenerationAlgorithm keyAlg = (KeyGenerationAlgorithm) keyalgComboBox.getSelectedItem();
		PBEAlg pbeAlg = (PBEAlg) pbeAlgComboBox.getSelectedItem();
		char [] password = password0.getPassword();
		
		// 1.0 -- create Key of desired type
		switch(keyAlg){
			case Curve25519: {
				
				C2KeyMetadata meta = null;
				if(pbeAlg == PBEAlg.PBKDF2) {
					meta = C2KeyMetadata.createSecurePBKDF2(password);
				}else if(pbeAlg == PBEAlg.SCRYPT){
					meta = C2KeyMetadata.createSecureScrypt(password);
				}
				Curve25519KeyContents contents = Buttermilk.INSTANCE.generateC2Keys(meta);
				Curve25519KeyForPublication pub = contents.copyForPublication();
				this.keyAlg = keyAlg;
				this.keyForPublication = pub;
				this.secureKey = contents;
				break;
			}
			case EC: {
				ECKeyMetadata meta = null;
				if(pbeAlg == PBEAlg.PBKDF2) {
					meta = ECKeyMetadata.createSecurePBKDF2(password);
				}else if(pbeAlg == PBEAlg.SCRYPT){
					meta = ECKeyMetadata.createSecureScrypt(password);
				}
				ECKeyContents contents = Buttermilk.INSTANCE.generateECKeys(meta, "P-256");
				ECKeyForPublication pub = contents.cloneForPublication();
				this.keyAlg = keyAlg;
				this.keyForPublication = pub;
				this.secureKey = contents;
				break;
			}
			case RSA: {
				RSAKeyMetadata meta = null;
				if(pbeAlg == PBEAlg.PBKDF2) {
					meta = RSAKeyMetadata.createSecurePBKDF2(password);
				}else if(pbeAlg == PBEAlg.SCRYPT){
					meta = RSAKeyMetadata.createSecureScrypt(password);
				}
				RSAKeyContents contents = Buttermilk.INSTANCE.generateRSAKeys(meta);
				RSAKeyForPublication pub = contents.cloneForPublication();
				this.keyAlg = keyAlg;
				this.keyForPublication = pub;
				this.secureKey = contents;
				break;
			}
			
			default: {}
		}
	}
	
	private void fireKeyCreated(){
		for(CreateKeyListener c: listeners){
			CreateKeyEvent evt = new CreateKeyEvent(this);
			evt.setKey(secureKey);
			evt.setKeyForPublication(keyForPublication);
			evt.setTextualRepresentation(keyText);
			c.keyCreated(evt);
		}
	}

	public boolean addCreateKeyListener(CreateKeyListener e) {
		return listeners.add(e);
	}

	@Override
	public void passwordChanged(EventObject evt) {
		defaultPassword = new Password(((PasswordEvent)evt).getPasswordValue());
		if(this.chckbxOverrideDefaultPassword.isSelected()){
			// do nothing
		}else{
			password0.setText(new String(defaultPassword.getPassword()));
			password1.setText(new String(defaultPassword.getPassword()));
			checkPassword();
			checkEntropy();
		}
	}

	@Override
	public void registrationHandleChanged(EventObject evt) {
		regHandle = ((RegHandleEvent)evt).getRegHandle();
		label.setText(regHandle);
	}
	
}
