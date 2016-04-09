/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2016 David R. Smith. All Rights Reserved.
 *
 */
package com.cryptoregistry.workbench;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.SwingWorker;

import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.MapData;
import com.cryptoregistry.ec.ECKeyContents;
import com.cryptoregistry.formats.JSONGenericReader;
import com.cryptoregistry.rsa.RSAKeyContents;
import com.cryptoregistry.signature.CryptoSignature;
import com.cryptoregistry.signature.builder.C2SignatureCollector;
import com.cryptoregistry.signature.builder.ECDSASignatureBuilder;
import com.cryptoregistry.signature.builder.RSASignatureBuilder;
import com.cryptoregistry.workbench.SignatureItemTableModel.SigElement;
import com.cryptoregistry.c2.key.Curve25519KeyContents;

public class SignatureItemSelectionPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JComboBox<KeyWrapper> comboBox;
//	private DefaultComboBoxModel<KeyWrapper> comboBoxModel;
	JLabel uuidLabel;
	SignatureItemTableModel dataModel;
	
	JDialog dialog;
	boolean OK = false;
	CryptoSignature sig;
	
	String regHandle;
	
	/**
	 * Input text should be valid Key Materials formatted
	 * @param text
	 */
	public SignatureItemSelectionPanel(final JDialog dialog, String text){
		this();
		this.dialog = dialog;
		JSONGenericReader reader = new JSONGenericReader(new StringReader(text));
		regHandle = reader.regHandle();
		List<MapData> list = reader.allData();
		for(MapData md: list){
			String uuid = md.uuid;
			Iterator<String> iter =  md.data.keySet().iterator();
			while(iter.hasNext()){
				String key = iter.next();
				String value = md.data.get(key);
				dataModel.add(new SigElement(Boolean.TRUE,uuid+":"+key, value));
			}
		}
	}

	public SignatureItemSelectionPanel() {
		
		JLabel lblSignatureUuid = new JLabel("Signature UUID:");
		
		uuidLabel = new JLabel("...");
		
		JPanel panel = new JPanel();
		
		comboBox = new JComboBox<KeyWrapper>();
		
		JLabel lblSigner = new JLabel("Signer:");
		
		final JButton btnSign = new JButton("Sign");
		btnSign.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				btnSign.setText("Signing...");
				btnSign.setEnabled(false);
				
				SwingWorker<Boolean,String> worker = new SwingWorker<Boolean,String>() {
					
					@Override
					protected Boolean doInBackground() throws Exception {
						KeyWrapper key = (KeyWrapper)comboBox.getSelectedItem();
						switch(key.key.getMetadata().getKeyAlgorithm()) {
							case Curve25519:
								C2SignatureCollector builder = new C2SignatureCollector(regHandle,(Curve25519KeyContents)key.key);
								for(SigElement item: dataModel.getList()){
									if(item.selected) {
										builder.collect(item.key, item.value);
									}
								}
								sig = builder.build();
								break;
							case DSA:
								System.err.println("DSA Not Implemented Yet");
								return Boolean.FALSE;
							case EC:
								ECDSASignatureBuilder ecbuilder = new ECDSASignatureBuilder(regHandle,(ECKeyContents)key.key);
								for(SigElement item: dataModel.getList()){
									if(item.selected) {
										ecbuilder.update(item.key, item.value);
									}
								}
								sig = ecbuilder.build();
								break;
							  case JNEO:
								System.err.println("No signature available, sig algorithm withdrawn");
								return Boolean.FALSE;
							case RSA:	
								RSASignatureBuilder rsabuilder = new RSASignatureBuilder(regHandle,(RSAKeyContents)key.key);
								for(SigElement item: dataModel.getList()){
									if(item.selected) {
										rsabuilder.update(item.key, item.value);
									}
								}
								sig = rsabuilder.build();
								break;
							case Symmetric:
								System.err.println("Not a key which can be used for digital signature.");
								return Boolean.FALSE;
							default:
								System.err.println("Unknown key type: "+key.key);
								return Boolean.FALSE;
						}
						return Boolean.TRUE;
					}
					
					 @Override
					public void done() {
						 try {
							 btnSign.setEnabled(true);
							 btnSign.setText("Sign");
							if(get()) {
								dialog.setVisible(false);
								dialog.dispose();
								OK = true;	
							}else{
								dialog.setVisible(false);
								dialog.dispose();
								OK = false;	
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				};
				worker.execute();
			}
		});
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
				OK = false;
			}
			
		});
		
		JButton btnAddElements = new JButton("Add Elements...");
		btnAddElements.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("TODO - not done yet");
			}
			
		});
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblSignatureUuid)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(uuidLabel, GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnAddElements)
							.addPreferredGap(ComponentPlacement.RELATED, 162, Short.MAX_VALUE)
							.addComponent(lblSigner)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 391, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnCancel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSign)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSignatureUuid)
						.addComponent(uuidLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblSigner)
						.addComponent(btnAddElements))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSign)
						.addComponent(btnCancel))
					.addContainerGap(33, Short.MAX_VALUE))
		);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		dataModel = new SignatureItemTableModel();
		table.setModel(dataModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setPreferredWidth(350);
		table.getColumnModel().getColumn(2).setPreferredWidth(200);
		
		scrollPane.setViewportView(table);
		setLayout(groupLayout);
		this.setPreferredSize(new Dimension(700,330));
	//	this.getRootPane().setDefaultButton(btnSign);
		
	}

	public boolean isOK() {
		return OK;
	}
	
	public CryptoSignature getSig() {
		return sig;
	}

	public void setKeys(Set<KeyWrapper> keys){
		if(keys == null) return;
		DefaultComboBoxModel<KeyWrapper> model = (DefaultComboBoxModel<KeyWrapper>) this.comboBox.getModel();
		for(KeyWrapper key : keys){
			if(KeyGenerationAlgorithm.isUsableForSignature(key.key.getMetadata().getKeyAlgorithm())){
				model.addElement(key);
			}
		}
	}

	public static final void main(String [] str){
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
					JFrame frame = new JFrame();
					frame.getContentPane().setLayout(new BorderLayout());
					SignatureItemSelectionPanel panel = new SignatureItemSelectionPanel();
					panel.dataModel.add(new SigElement(Boolean.FALSE,"Key0", "item0"));
					panel.dataModel.add(new SigElement(Boolean.TRUE,"Key1", "item1"));
					frame.getContentPane().add(panel, BorderLayout.CENTER);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.pack();
					frame.setVisible(true);
	            }
	        });
		
	}
}
