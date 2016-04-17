/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.iharder.Base64;

public class ArrayUtil {

	private static final Lock lock = new ReentrantLock();

	/**
	 * Given an arbitrary number of byte arrays, concatenate them and return a
	 * new array with a copy of all the bytes in it
	 * 
	 * @param bytes
	 * @return
	 */
	public static byte[] concatenate(byte[]... bytes) {
		lock.lock();
		try {
			int length = 0;
			for (byte[] item : bytes) {
				length += item.length;
			}
			byte[] array = new byte[length];
			int current = 0;
			for (byte[] item : bytes) {
				System.arraycopy(item, 0, array, current, item.length);
				current += item.length;
			}
			return array;
		} finally {
			lock.unlock();
		}
	}

	// various permutations of compression on int arrays

	public static byte[] compressGzip(int[] array) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(array);
		byte[] uncompressed = byteBuffer.array();
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream out = new GZIPOutputStream(bout);
		out.write(uncompressed, 0, uncompressed.length);
		out.finish();
		return bout.toByteArray();
	}

	public static int[] uncompressGzip(byte[] compressed) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPInputStream in = null;
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(compressed);
			in = new GZIPInputStream(bin);
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = in.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, count);
			}

		} catch (IOException x) {
			x.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		IntBuffer intBuf = ByteBuffer.wrap(out.toByteArray()).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		return array;
	}

	public static ArmoredCompressedString wrapAndCompress(int[] array) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(array);
		return new ArmoredCompressedString(byteBuffer.array());
	}

	public static int[] unwrapCompressed(ArmoredCompressedString in) {
		byte[] encoded = in.decodeToBytes();
		IntBuffer intBuf = ByteBuffer.wrap(encoded).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		return array;
	}

	public static ArmoredString wrap(int[] array) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(array.length * 4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(array);
		return new ArmoredString(byteBuffer.array());
	}

	public static int[] unwrap(ArmoredString in) {
		byte[] encoded = in.decodeToBytes();
		IntBuffer intBuf = ByteBuffer.wrap(encoded).asIntBuffer();
		int[] array = new int[intBuf.remaining()];
		intBuf.get(array);
		return array;
	}

	/**
	 * handling for multi-dimensional array encoding for short integers. Wraps the length of the dimensions
	 * in integers. 
	 * 
	 * @param array
	 * @return
	 */
	
	public static String encode3dShort(short[][][] array) {
		lock.lock();
		try {
			int firstLevelSize = array.length;
			int secondarySize = array[0].length;
			int tertiarySize = array[0][0].length;

			try (ByteArrayOutputStream orig = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(orig);) {
				out.writeShort(firstLevelSize);
				out.writeShort(secondarySize);
				out.writeShort(tertiarySize);
				for (short[][] item : array) {
					for (short[] item0 : item) {
						for(short s: item0){
							out.writeShort(s);
						}
					}
				}
				return Base64.encodeBytes(orig.toByteArray(), Base64.URL_SAFE|Base64.GZIP);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			lock.unlock();
		}
	}
	
	public static short[][][] decode3dShort(String encoded) {
		lock.lock();
		try {
			short[][][] primary = null;
		
			try {
				byte[] inbytes = Base64.decode(encoded, Base64.URL_SAFE|Base64.GZIP);
				ByteArrayInputStream in = new ByteArrayInputStream(inbytes);
				DataInputStream instream = new DataInputStream(in);
				int firstLevel = instream.readShort();
				int secondLevel = instream.readShort();
				int thirdLevel = instream.readShort();
				
				if (firstLevel == 0 || secondLevel == 0 || thirdLevel == 0)
					throw new RuntimeException(
						"array dimensions look incorrect, should be non-zero: "
					+firstLevel+", "+secondLevel+", "+thirdLevel);
				primary = new short[firstLevel][secondLevel][thirdLevel];
				for (short[][] secondary : primary) {
					for (short[] tertiary : secondary) {
						for(int i = 0;i<thirdLevel;i++){
							tertiary[i] = instream.readShort();
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		
			return primary;
			
		} finally {
			lock.unlock();
		}
	}

	public static String encode2dShort(short[][] array) {
		lock.lock();
		try {
			int outerSize = array.length;
			int innerSize = array[0].length;

			try (ByteArrayOutputStream orig = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(orig);) {
				out.writeShort(outerSize);
				out.writeShort(innerSize);
				for (int i = 0; i < outerSize; i++) {
					short[] inner = array[i];
					for (int j = 0; j < innerSize; j++) {
						out.writeShort(inner[j]);
					}
				}
				return Base64.encodeBytes(orig.toByteArray(), Base64.URL_SAFE|Base64.GZIP);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			lock.unlock();
		}
	}

	public static short[][] decode2dShort(String encoded) {
		lock.lock();
		try {
			short[][] primary = null;
			try {
				byte[] inbytes = Base64.decode(encoded, Base64.URL_SAFE|Base64.GZIP);
				ByteArrayInputStream in = new ByteArrayInputStream(inbytes);
				DataInputStream instream = new DataInputStream(in);
				int firstLevel = instream.readShort();
				int secondlevel = instream.readShort();
				if (firstLevel == 0 || secondlevel == 0)
					throw new RuntimeException(
						"array dimensions look incorrect, should be non-zero:"
						+ firstLevel 
						+ ", " 
						+ secondlevel);
				primary = new short[firstLevel][secondlevel];
				for (int i = 0; i < firstLevel; i++) {
					short[] inner = new short[secondlevel];
					for (int j = 0; j < secondlevel; j++) {
						inner[j] = instream.readShort();
					}
					primary[i] = inner;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return primary;
			
		} finally {
			lock.unlock();
		}
	}
	
	public static String encode1dShort(short[] array) {
		lock.lock();
		try {
			int outerSize = array.length;

			try (ByteArrayOutputStream orig = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(orig);) {
				out.writeShort(outerSize);
				for (int i = 0; i < outerSize; i++) {
						out.writeShort(array[i]);
				}
				return Base64.encodeBytes(orig.toByteArray(), Base64.URL_SAFE|Base64.GZIP);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} finally {
			lock.unlock();
		}
	}
	
	public static short[] decode1dShort(String encoded) {
		lock.lock();
		try {
			short[] primary = null;
			try {
				byte[] inbytes = Base64.decode(encoded, Base64.URL_SAFE|Base64.GZIP);
				ByteArrayInputStream in = new ByteArrayInputStream(inbytes);
				DataInputStream instream = new DataInputStream(in);
				int firstLevel = instream.readShort();
				if (firstLevel == 0)
					throw new RuntimeException(
						"array dimensions look incorrect, should be non-zero:"+ firstLevel);
				primary = new short[firstLevel];
				for (int i = 0; i < firstLevel; i++) {
						primary[i] = instream.readShort();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return primary;
			
		} finally {
			lock.unlock();
		}
	}

}
