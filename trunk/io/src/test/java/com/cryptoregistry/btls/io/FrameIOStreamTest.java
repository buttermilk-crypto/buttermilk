package com.cryptoregistry.btls.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

import com.cryptoregistry.btls.BTLSProtocol;
import com.cryptoregistry.protocol.frame.StringOutputFrame;

public class FrameIOStreamTest implements AlertListener {
	
	final static byte [] iv = {0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x10,0x11,0x12,0x13,0x14,0x15};
	final static byte [] key = 
	{
		0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,
		0x08,0x09,0x10,0x11,0x12,0x13,0x14,0x15,
		0x16,0x17,0x18,0x19,0x20,0x21,0x22,0x23,
		0x24,0x25,0x26,0x27,0x28,0x29,0x30,0x32
	};
	
	
	
	@Test
	public void testThreadedReadBuffer() {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try (
			FrameOutputStream fout = new FrameOutputStream(out,key,iv);
		){
			fout.write("Hello".getBytes(StandardCharsets.UTF_8));
			fout.flush();
			fout.write("Again".getBytes(StandardCharsets.UTF_8));
			fout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
	
		try (
			FrameInputStream fin = new FrameInputStream(in,key);
		){
			fin.addAlertListener(this);
			fin.start();
			byte [] buffer = new byte [192]; 
			StringBuffer buf = new StringBuffer();
			int total = 0, count = 0;
			while((count = fin.read(buffer)) != -1){
				total+=count;
				buf.append(new String(buffer,0,count, "UTF-8"));
			}
			
			Assert.assertEquals(10, total);
			Assert.assertEquals("HelloAgain", buf.toString());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadMethods() {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String expected = "Hello";
		
		try (
			FrameOutputStream fout = new FrameOutputStream(out,key,iv);
		){
			fout.write(expected.getBytes(StandardCharsets.UTF_8));
			fout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
	
		try (
			FrameInputStream fin = new FrameInputStream(in,key);
		){
			fin.start();
			fin.addAlertListener(this);
			int b = fin.read();
			Assert.assertEquals('H', (char)b);
			
			byte [] second = new byte[2];
			
			int lengthRead = fin.read(second);
			Assert.assertEquals(2, lengthRead);
			Assert.assertEquals("el", new String(second, "UTF-8"));
			
			byte [] third = new byte[2];
			lengthRead = fin.read(third,0,2);
			Assert.assertEquals(2, lengthRead);
			Assert.assertEquals("lo", new String(third, "UTF-8"));
			
			lengthRead = fin.read(third,0,2);
			Assert.assertEquals(-1, lengthRead);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Assert.assertEquals(expected, actual);
		
	}
	
	@Test
	public void testSmallBuffer() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FrameOutputStream fout = new FrameOutputStream(out,key,iv);
		String hello0 = "Hi there, how are you?";
		byte [] inputBytes = hello0.getBytes(StandardCharsets.UTF_8);
		try {
			fout.write(inputBytes);
			fout.flush();
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		FrameInputStream fin = new FrameInputStream(in,key);
		fin.addAlertListener(this);
		fin.start();
		StringBuffer mybuf = new StringBuffer();
		byte [] buf = new byte[8];
		int ct = 0;
		try {
			
			while((ct = fin.read(buf)) != -1){
					byte [] got = new byte[ct];
					System.arraycopy(buf, 0, got, 0, ct);
					mybuf.append(new String(got, "UTF-8"));
			}
		
			fin.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(hello0, mybuf.toString());
		
	}
	
	

	@Override
	public void alertReceived(AlertEvent evt) {
		System.err.println("Got Alert: "+evt);
	}
	
	
	
	@Test
	public void test0Firstlensman2() throws IOException {
		
		ByteArrayOutputStream collector = new ByteArrayOutputStream();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FrameOutputStream fout = new FrameOutputStream(out,key,iv);
		File firstlensman = new File("./src/test/resources/firstlensman.txt");
		long length = firstlensman.length();
		byte [] fromFile = Files.readAllBytes(firstlensman.toPath());
		
		Assert.assertEquals(length, fromFile.length);
		
		fout.write(fromFile);
		fout.flush();
		fout.close();
		
		System.err.println("encrypted");
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		FrameInputStream fin = new FrameInputStream(in,key);
		fin.addAlertListener(this);
		fin.start();
		
		byte [] buf = new byte[1024];
		int ct = 0;
		try {
			
		
			while((ct = fin.read(buf)) != -1) {
					byte [] got = new byte[ct];
					System.arraycopy(buf, 0, got, 0, ct);
					collector.write(got);
			}
			fin.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte [] collectorBytes = collector.toByteArray();
		
		Assert.assertEquals(length, collectorBytes.length);
	}
	
	@Test
	public void test0FirstlensmanWithAlerts() throws IOException {
		
		ByteArrayOutputStream collector = new ByteArrayOutputStream();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		FrameOutputStream fout = new FrameOutputStream(out,key,iv);
		File firstlensman = new File("./src/test/resources/firstlensman.txt");
		long length = firstlensman.length();
		byte [] fromFile = Files.readAllBytes(firstlensman.toPath());
		
		Assert.assertEquals(length, fromFile.length);
		
		fout.writeAlert(BTLSProtocol.INFORMATION, "Now sending big file: "+length+" bytes");
		fout.write(fromFile);
		fout.flush();
		fout.writeAlert(BTLSProtocol.INFORMATION, "Complete");
		fout.close();
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		FrameInputStream fin = new FrameInputStream(in,key);
		fin.addAlertListener(this);
		fin.start();
		
		byte [] buf = new byte[1024];
		int ct = 0;
		try {
			while((ct = fin.read(buf)) != -1) {
					byte [] got = new byte[ct];
					System.arraycopy(buf, 0, got, 0, ct);
					collector.write(got);
			}
			fin.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		byte [] collectorBytes = collector.toByteArray();
		
		Assert.assertEquals(length, collectorBytes.length);
	}
	
	
	@Test
	public void testReadStringFrame() {
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String expected = "Hello", actual = "";
		String expected1 = "普通话/普通話", actual1 = "";
		StringOutputFrame frame = new StringOutputFrame(BTLSProtocol.STRING, expected);
		StringOutputFrame frame1 = 
				new StringOutputFrame(
						BTLSProtocol.STRING, 
						StandardCharsets.UTF_8, 
						expected1.getBytes(StandardCharsets.UTF_8)
				);
		
		try (
			FrameOutputStream fout = new FrameOutputStream(out,key,iv);
		){
			fout.writeFrameContents(frame);
			fout.writeFrameContents(frame1);
			fout.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
	
		try (
			FrameInputStream fin = new FrameInputStream(in,key);
		){
			fin.start();
			fin.addAlertListener(this);
			actual = fin.readStringFrame();
			actual1 = fin.readStringFrame();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(expected, actual);
		Assert.assertEquals(expected1, actual1);
		
	}
	
	
}
