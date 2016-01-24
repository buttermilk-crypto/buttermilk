package com.cryptoregistry.workbench;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.InputStream;

import javax.swing.JDialog;
import javax.swing.JFrame;

import asia.redact.bracket.properties.Properties;


public class RegHandleSearchDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	RegHandlePanel panel;

	public RegHandleSearchDialog(JFrame parent, String title, Properties props) {
		super(parent,title);
		panel = new RegHandlePanel(this, props);
		getContentPane().add(panel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		// size the window
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double width = screenSize.getWidth() / 2;
		double height = 300;
		setPreferredSize(new Dimension((int) width, (int) height));
		
		pack();
		//setVisible(true);
	}
	
	public void open() {
		setVisible(true);
	}
	
	public void addRegHandleListener(RegHandleListener rhl) {
		panel.addRegHandleListener(rhl);
	}

	public static void main(String[] a) {
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	InputStream in = Thread.currentThread().getContextClassLoader()
							.getResourceAsStream("regwizard.properties");
					Properties props = Properties.Factory.getInstance(in);
					RegHandleSearchDialog dialog = new RegHandleSearchDialog(null,"TEST", props);
					dialog.open();
	            }
	        });
		
	}

}
