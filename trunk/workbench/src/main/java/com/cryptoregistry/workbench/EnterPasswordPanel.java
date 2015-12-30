package com.cryptoregistry.workbench;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;

import com.cryptoregistry.util.Check10K;
import com.cryptoregistry.util.entropy.TresBiEntropy;
import com.cryptoregistry.util.entropy.TresBiEntropy.Result;

public class EnterPasswordPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final EnterPasswordDialog parent;
	private JPasswordField password0;
	private JPasswordField password1;
	private JLabel lblEntropy;
	private JLabel passwordEqualityMsg;
	
	private Check10K tenK;
	private JButton btnOk;
	private List<PasswordListener> listeners = new ArrayList<PasswordListener>();

	public EnterPasswordPanel(EnterPasswordDialog par) {
		this.parent = par;
		tenK = new Check10K();
		JLabel lblEnterPassword = new JLabel("Enter Password:");
		JLabel lblAgain = new JLabel("Again:");
		
		password0 = new JPasswordField();
		password0.setColumns(10);
		
		password1 = new JPasswordField();
		password1.setColumns(10);
		
		btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				firePasswordChangedEvent();
				parent.setEnabled(false);
				parent.dispose();
				
			}
		});
		btnOk.setEnabled(false);
		
		password0.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkPassword();
				checkEntropy();
			}
		});
		
		password1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				checkPassword();
				checkEntropy();
			}
		});
		
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.setVisible(false);
				parent.dispose();
			}
		});
		
		lblEntropy = new JLabel("...");
		
		passwordEqualityMsg = new JLabel("...");
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(lblEnterPassword)
						.addComponent(lblAgain))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblEntropy)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(passwordEqualityMsg)
							.addPreferredGap(ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
							.addComponent(btnCancel)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(btnOk))
						.addComponent(password1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
						.addComponent(password0, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblEnterPassword)
						.addComponent(password0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblAgain)
						.addComponent(password1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnOk)
								.addComponent(btnCancel)))
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblEntropy)
								.addComponent(passwordEqualityMsg))))
					.addContainerGap(202, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
		// TODO Auto-generated constructor stub
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
			this.btnOk.setEnabled(true);
			if(tenK.contains(new String(pass0))){
				this.passwordEqualityMsg.setForeground(Color.RED);
				this.passwordEqualityMsg.setText("In 10K List!");
				this.btnOk.setEnabled(false);
			}
		}else{
			this.passwordEqualityMsg.setForeground(Color.RED);
			this.passwordEqualityMsg.setText("Input not equal");
			this.btnOk.setEnabled(false);
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

	public void addPasswordChangedListener(PasswordListener pw){
		listeners.add(pw);
	}
	
	private void firePasswordChangedEvent(){
		for(PasswordListener pw: listeners){
			PasswordEvent evt = new PasswordEvent(this);
			char [] pass = password0.getPassword();
			char [] copy = Arrays.copyOf(pass,pass.length);
			evt.setPasswordValue(copy);
			pw.passwordChanged(evt);
		}
	}
	
}
