package com.cryptoregistry.workbench;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ValidationDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public ValidationDialog(JFrame parent, String title, String msg) {
		super(parent,title);
		getContentPane().add(new ValidationErrorPane(msg));

		// Create a button
		JPanel buttonPane = new JPanel();
		JButton button = new JButton("Close");
		this.getRootPane().setDefaultButton(button);
		buttonPane.add(button);
		// set action listener on the button
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
			
		});
		getContentPane().add(buttonPane, BorderLayout.PAGE_END);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

}
