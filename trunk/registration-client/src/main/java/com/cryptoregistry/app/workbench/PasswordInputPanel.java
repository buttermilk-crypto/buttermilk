package com.cryptoregistry.app.workbench;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;

import com.cryptoregistry.util.Check10K;

import asia.redact.bracket.properties.Properties;

public class PasswordInputPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final JDialog dialog;
	
	@SuppressWarnings("unused")
	private Properties props;
	
	private JLabel lblPleaseEnterYour;
	private JPasswordField password0;
	private JPasswordField password1;
	private JLabel passwordEqualityMsg;
	
	private JButton btnSave, btnCancel;
	
	private Check10K tenK;
	private boolean check10K;
	
	public PasswordInputPanel(JDialog parent, Properties props, boolean check10K) {
		super();
		this.dialog = parent;
		this.props = props;
		tenK = new Check10K();
		this.check10K = check10K;
		
		lblPleaseEnterYour = new JLabel("Please enter your password, and then again:");
		password0 = new JPasswordField();
		password1 = new JPasswordField();
		password1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkPassword();
			}
		});
		
		btnSave = new JButton("OK");
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int count = password0.getPassword().length;
				if(count == 0) {
					JOptionPane.showMessageDialog((JButton)e.getSource(),
						    "Nonzero-length password required here to proceed.",
						    "Hold on...",
						    JOptionPane.WARNING_MESSAGE);
					password0.requestFocusInWindow();
					return;
				}
				dialog.setVisible(false);
				dialog.dispose();
			}
			
		});
		
		btnSave.setEnabled(false);
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				password0.setText("");
				password1.setText("");
				dialog.setVisible(false);
				dialog.dispose();
			}
			
		});
		
		passwordEqualityMsg = new JLabel("...");
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblPleaseEnterYour)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnCancel)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnSave))
						.addComponent(password0, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
						.addComponent(passwordEqualityMsg, Alignment.LEADING)
						.addComponent(password1, Alignment.LEADING))
					.addContainerGap(60, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblPleaseEnterYour)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(password0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(password1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnSave)
								.addComponent(btnCancel)))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(passwordEqualityMsg)))
					.addContainerGap(30, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
	}
	
	/**
	 * Call only from event-loop thread
	 */
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
			this.btnSave.setEnabled(true);
			if(check10K && tenK.contains(new String(pass0))){
				this.passwordEqualityMsg.setForeground(Color.RED);
				this.passwordEqualityMsg.setText("In 10K List!");
				this.btnSave.setEnabled(false);
			}
		}else{
			this.passwordEqualityMsg.setForeground(Color.RED);
			this.passwordEqualityMsg.setText("Input not equal");
			this.btnSave.setEnabled(false);
		}
	}
	
	public char[] getPassword() {
		return this.password1.getPassword();
	}

}
