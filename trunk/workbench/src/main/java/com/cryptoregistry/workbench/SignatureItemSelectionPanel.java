package com.cryptoregistry.workbench;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;

import java.awt.BorderLayout;
import java.util.EventObject;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JButton;

import com.cryptoregistry.CryptoKey;

public class SignatureItemSelectionPanel extends JPanel implements CreateKeyListener, UnlockKeyListener {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JComboBox<KeyWrapper> comboBox;
	private DefaultComboBoxModel<KeyWrapper> comboBoxModel;
	JLabel uuidLabel;
	JTabbedPane tabs; // set this when dialog opened

	public SignatureItemSelectionPanel() {
		
		JLabel lblSignatureUuid = new JLabel("Signature UUID:");
		
		uuidLabel = new JLabel("...");
		
		JPanel panel = new JPanel();
		
		comboBox = new JComboBox<KeyWrapper>();
		
		JLabel lblSigner = new JLabel("Signer:");
		
		JButton btnSign = new JButton("Sign");
		JButton btnCancel = new JButton("Cancel");
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblSignatureUuid)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(uuidLabel, GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblSigner)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBox, 0, 392, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
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
						.addComponent(lblSigner)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnSign)
						.addComponent(btnCancel))
					.addContainerGap(16, Short.MAX_VALUE))
		);
		panel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setModel(new SignatureItemTableModel());
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		scrollPane.setViewportView(table);
		setLayout(groupLayout);
		
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
	}

	@Override
	public void keyCreated(CreateKeyEvent evt) {
		KeyWrapper wrapper = new KeyWrapper(evt.getKey());
		this.comboBoxModel.addElement(wrapper);
	}

	public void setTabs(JTabbedPane tabs) {
		this.tabs = tabs;
	}
}
