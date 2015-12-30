package com.cryptoregistry.workbench;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EventObject;

import asia.redact.bracket.properties.OutputAdapter;
import asia.redact.bracket.properties.PlainOutputFormat;
import asia.redact.bracket.properties.Properties;

public class ExternalPropsManager implements RegHandleListener {

	public static final String FILENAME = "regwizard.properties";
	
	private Properties props;
	File file;
	
	public ExternalPropsManager() {
		super();
		File home = new File(System.getProperty("user.home"));
		file = new File(home,"regwizard.properties");
	}

	public ExternalPropsManager(Properties props) {
		this();
		this.props = props;
	}
	
	public boolean externalFileExists() {
		return file.exists();
	}
	
	public boolean hasDefaultKeyDirectoryLocation() {
		return file.exists() && props.containsKey("default.key.directory");
	}
	
	public boolean hasRegistrationHandleSerialized() {
		return file.exists() && props.containsKey("registration.handle");
	}
	
	/**
	 * Return true if file created
	 * 
	 * @return
	 * @throws Exception
	 */
	public void write(){
		try {
			OutputAdapter outAdapter = new OutputAdapter(props);
			FileOutputStream out = new FileOutputStream(file);
			outAdapter.writeTo(out, new PlainOutputFormat(),StandardCharsets.UTF_8);
		}catch(IOException x){
			x.printStackTrace();
		}
	}
	
	public void read() {
		if(file.exists()){
			if(props == null) {
				props = Properties.Factory.getInstance(file, StandardCharsets.UTF_8);
			}else{
				Properties p = Properties.Factory.getInstance(file, StandardCharsets.UTF_8);
				props.merge(p);
			}
		}
	}

	public Properties getProps() {
		return props;
	}

	public void put(String key, String... values) {
		props.put(key, values);
	}
	
	

	public String get(String key) {
		return props.get(key);
	}

	@Override
	public void registrationHandleChanged(EventObject evt) {
		String regHandle = ((RegHandleEvent)evt).getRegHandle();
		put("registration.handle",regHandle);
		try {
			write();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
