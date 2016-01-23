package com.cryptoregistry.workbench;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JTextPane;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.KeyGenerationAlgorithm;
import com.cryptoregistry.ec.ECKeyForPublication;
import com.cryptoregistry.rsa.RSAKeyForPublication;

import javax.swing.JButton;


public class UnlockedKeyPanel extends JPanel implements CreateKeyListener, UnlockKeyListener {

	private static final long serialVersionUID = 1L;
	
	private JComboBox<KeyWrapper> comboBox;
	private DefaultComboBoxModel<KeyWrapper> comboBoxModel;
	private JTextPane textPane;
	private JScrollPane scroll;
	
	private CryptoKey currentKey;
	private JButton btnDone;
	private JButton btnCopyToClipboard;
	
	private List<CryptoKeySelectionListener> listeners = new ArrayList<CryptoKeySelectionListener>();

	public UnlockedKeyPanel(final UnlockedKeyDialog dialog) {
		super();
		JLabel lblSelectAnUnlocked = new JLabel("Select an unlocked key to make it the Current Key:");
	
		comboBox = new JComboBox<KeyWrapper>();
		comboBoxModel = (DefaultComboBoxModel<KeyWrapper>) comboBox.getModel();
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				KeyWrapper key = (KeyWrapper) comboBox.getSelectedItem();
				textPane.setText(describe(key.key));
				currentKey = key.key;
				fireCryptoKeySelectionEvent(currentKey);
			}
		});
		
		JLabel lblCurrentKeyInfo = new JLabel("Current Key Info:");
		
		textPane = new JTextPane();
		scroll = new JScrollPane(textPane);
		
		btnDone = new JButton("Done");
		btnDone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}
		});
		dialog.getRootPane().setDefaultButton(btnDone);
		
		btnCopyToClipboard = new JButton("Copy To Clipboard");
		btnCopyToClipboard.setEnabled(false);
		btnCopyToClipboard.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Component comp = (Component) evt.getSource();
				if(currentKey == null){
					JOptionPane.showMessageDialog(comp,
						    "No key available",
						    "Problem, no key available",
						    JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				StringSelection stringSelection = new StringSelection(currentKey.formatJSON());
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
				
			}
		});
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBox, 0, 430, Short.MAX_VALUE)
						.addComponent(lblSelectAnUnlocked)
						.addComponent(lblCurrentKeyInfo)
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
							.addComponent(btnCopyToClipboard)
							.addPreferredGap(ComponentPlacement.RELATED, 284, Short.MAX_VALUE)
							.addComponent(btnDone))
						.addComponent(scroll, GroupLayout.PREFERRED_SIZE, 337, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblSelectAnUnlocked)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblCurrentKeyInfo)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scroll, GroupLayout.PREFERRED_SIZE, 168, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnDone)
						.addComponent(btnCopyToClipboard))
					.addContainerGap(26, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}

	public CryptoKey getCurrentKey() {
		return currentKey;
	}

	@Override
	public void keyCreated(CreateKeyEvent evt) {
		KeyWrapper wrapper = new KeyWrapper(evt.getKey());
		this.comboBoxModel.addElement(wrapper);
		btnCopyToClipboard.setEnabled(true);
	}
	
	public String describe(CryptoKey key){
		StringBuffer buf = new StringBuffer();
		buf.append(key.getMetadata().getDistinguishedHandle());
		buf.append("\n");
		buf.append(key.getMetadata().getKeyAlgorithm());
		buf.append("\n");
		buf.append("CreatedOn: ");
		buf.append(key.getMetadata().getCreatedOn());
		buf.append("\n");
		// special case, more info
		if(key.getMetadata().getKeyAlgorithm() == KeyGenerationAlgorithm.EC){
			buf.append("CurveName:");
			buf.append(((ECKeyForPublication)key).curveName);
			buf.append("\n");
		}else if(key.getMetadata().getKeyAlgorithm() == KeyGenerationAlgorithm.RSA){
			buf.append("Strength:");
			buf.append(((RSAKeyForPublication)key).metadata.strength);
			buf.append("\n");
		}
		return buf.toString();
	}

	@Override
	public void keyUnlocked(EventObject evt) {
		UnlockKeyEvent uevt = (UnlockKeyEvent)evt;
		CryptoKey key = uevt.getKey();
		String handle = key.getMetadata().getHandle();
		int size = comboBoxModel.getSize();
		for(int i = 0;i<size;i++){
			KeyWrapper wrapper = comboBoxModel.getElementAt(i);
			if(handle.equals(wrapper.key.getMetadata().getHandle())) {
				return;
			}
		}
		// ok, we don't have this key already
		this.comboBoxModel.addElement(new KeyWrapper(key));
		btnCopyToClipboard.setEnabled(true);
	}
	
	private void fireCryptoKeySelectionEvent(CryptoKey key) {
		for(CryptoKeySelectionListener l: listeners){
			CryptoKeySelectionEvent evt = new CryptoKeySelectionEvent(this);
			evt.setKey(key);
			l.currentKeyChanged(evt);
		}
	}
	
	public void addCryptoKeySelectionListener(CryptoKeySelectionListener listener){
		listeners.add(listener);
	}
}
