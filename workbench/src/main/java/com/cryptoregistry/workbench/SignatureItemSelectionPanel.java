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
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JButton;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.MapData;
import com.cryptoregistry.formats.JSONGenericReader;
import com.cryptoregistry.workbench.SignatureItemTableModel.SigElement;

public class SignatureItemSelectionPanel extends JPanel implements CreateKeyListener, UnlockKeyListener {

	private static final long serialVersionUID = 1L;
	private JTable table;
	private JComboBox<KeyWrapper> comboBox;
	private DefaultComboBoxModel<KeyWrapper> comboBoxModel;
	JLabel uuidLabel;
	SignatureItemTableModel dataModel;
	
	JDialog dialog;
	boolean OK = false;
	
	/**
	 * Input text should be valid Key Materials formatted
	 * @param text
	 */
	public SignatureItemSelectionPanel(final JDialog dialog, String text){
		this();
		this.dialog = dialog;
		JSONGenericReader reader = new JSONGenericReader(new StringReader(text));
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
		
		JButton btnSign = new JButton("Sign");
		btnSign.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
				OK = true;
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
		
	}

	public boolean isOK() {
		return OK;
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
