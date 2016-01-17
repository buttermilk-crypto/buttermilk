package com.cryptoregistry.workbench;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import javax.swing.JFrame;

public class SignatureItemSelectionDialogTest {

	
	public static final void main(String [] str){
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
					JFrame frame = new JFrame();
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					InputStream in = frame.getClass().getResourceAsStream("/believed-good-c2.json");
	            	SignatureItemSelectionDialog dialog = new SignatureItemSelectionDialog(frame,read(in), new HashSet<KeyWrapper>());
	            	System.err.println(dialog.isOK());
	            }
	        });
		
	}
	
	 public static String read(InputStream input){
		 
		 	StringWriter writer = new StringWriter(); 
	        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
	            String line = null;
	        	while((line = buffer.readLine())!=null){
	        		writer.write(line);
	        		writer.write("\n");
	        	}
	        } catch (IOException e) {
				e.printStackTrace();
			}
	        
	        return writer.toString();
	    }

}
