package com.cryptoregistry.workbench;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import asia.redact.bracket.properties.Properties;

import com.cryptoregistry.MapData;

public class EditAttributePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final JFrame parent;
	private final JTable table;
	JLabel uuidText;
	int rowIndex;
	
	public EditAttributePanel(final JFrame frame, String id, Map<String,String> map){
		this(frame);
		uuidText.setText(id);
		AttributeTableModel model = (AttributeTableModel) table.getModel();
		model.load(id, map);
	}

	public EditAttributePanel(final JFrame frame) {
		this.parent = frame;
		JLabel lblUuid = new JLabel("UUID: ");
		
		uuidText = new JLabel("...");
		
		JPanel panel = new JPanel();
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
				
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});
		
		JButton btnAddRow = new JButton("+");
		btnAddRow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttributeTableModel model = (AttributeTableModel)table.getModel();
				if(rowIndex != -1) model.addRow(rowIndex, "", "");
				rowIndex = -1;
			}
		});
		
		JButton btnDeleteRow = new JButton("-");
		btnDeleteRow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AttributeTableModel model = (AttributeTableModel)table.getModel();
				if(rowIndex != -1) model.deleteRow(rowIndex);
				rowIndex = -1;
			}
		});
		
		JButton btnRevert = new JButton("Revert");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, groupLayout.createSequentialGroup()
							.addComponent(lblUuid)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(uuidText))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnRevert)
							.addPreferredGap(ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
							.addComponent(btnDeleteRow)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnAddRow)
							.addGap(18)
							.addComponent(btnCancel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnOk)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUuid)
						.addComponent(uuidText))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 211, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnOk)
						.addComponent(btnCancel)
						.addComponent(btnAddRow)
						.addComponent(btnDeleteRow)
						.addComponent(btnRevert))
					.addContainerGap())
		);
		
		table = new JTable();
		table.setCellSelectionEnabled(true);
		final AttributeTableModel model = new AttributeTableModel();
		table.setModel(model);
		//table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent e) {
				rowIndex = table.getSelectedRow();
			}
		});
		JScrollPane scroll = new JScrollPane(table);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		panel.add(scroll);
		setLayout(groupLayout);
		frame.getRootPane().setDefaultButton(btnOk);
	}
	
	public static final void main(String [] str){
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	InputStream in = Thread.currentThread().getContextClassLoader()
							.getResourceAsStream("regwizard.properties");
					Properties props = Properties.Factory.getInstance(in);
					JFrame frame = new JFrame();
					frame.getContentPane().setLayout(new BorderLayout());
					MapData data = new MapData(UUID.randomUUID().toString(),props.getFlattenedMap());
					frame.getContentPane().add(new EditAttributePanel(frame, data.uuid,data.data), BorderLayout.CENTER);
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.pack();
					frame.setVisible(true);
	            }
	        });
		
	}
}
