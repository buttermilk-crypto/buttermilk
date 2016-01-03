package com.cryptoregistry.workbench.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.cryptoregistry.workbench.RegHandleEvent;
import com.cryptoregistry.workbench.RegHandleListener;
import com.cryptoregistry.workbench.UUIDTextPane;

public class AddSkeletonAction extends AbstractAction implements RegHandleListener {

	private static final long serialVersionUID = 1L;
	
	JTabbedPane tabs;
	String skeletonText;
	String regHandle;
	String adminEmail;

	public AddSkeletonAction(JTabbedPane tabs, String regHandle, String adminEmail) {
		this.putValue(Action.NAME, "Create Skeleton Format");
		
		this.tabs = tabs;
		this.regHandle = regHandle;
		this.adminEmail = adminEmail;
		InputStream in = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("skeleton.txt");
		 final char[] buffer = new char[64];
		    final StringBuilder out = new StringBuilder();
		    try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
		        for (;;) {
		            int rsz = reader.read(buffer, 0, buffer.length);
		            if (rsz < 0)
		                break;
		            out.append(buffer, 0, rsz);
		        }
		        skeletonText = out.toString();
		    
		    } catch (IOException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		int currentIndex = tabs.getSelectedIndex();
		if(currentIndex == -1){
			JOptionPane.showMessageDialog((Component)evt.getSource(),
				    "Please create a tab for this operation to write into and then try again.",
				    "Request",
				    JOptionPane.WARNING_MESSAGE);
			return;
		}
		UUIDTextPane pane = (UUIDTextPane) ((JScrollPane)tabs.getComponentAt(currentIndex)).getViewport().getView();
		
		if(regHandle == null) regHandle = "";
		if(adminEmail == null) adminEmail = "";
		
		String s0 = skeletonText.replace("$regHandle$", regHandle);
		String s1 = s0.replace("$adminEmail$", adminEmail);
		 
		pane.setText(s1);
		
	}

	@Override
	public void registrationHandleChanged(EventObject evt) {
		RegHandleEvent event = (RegHandleEvent)evt;
		regHandle = event.getRegHandle();
		
	}

}
