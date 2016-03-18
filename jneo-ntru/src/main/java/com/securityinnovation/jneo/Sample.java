package com.securityinnovation.jneo;

/******************************************************************************
 * NTRU Cryptography Reference Source Code
 * Copyright (c) 2009-2013, by Security Innovation, Inc. All rights reserved.
 *
 * Copyright (C) 2009-2013  Security Innovation
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *********************************************************************************/

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import com.securityinnovation.jneo.ntruencrypt.NtruEncryptKey;

/**
 * This class is sample code for the jNeo toolkit. It supports 3 operations:
 * <ul>
 * creating an NtruEncrypt key
 * <ul>
 * encrypting a file with a dynamically-generated AES key and wrapping
 * (encrypting) the AES key with an NtruEncrypt key.
 * <ul>
 * decrypting an encrypted file.
 */
public class Sample {
	/**
	 * Creates a public/private key pair and saves the two components to disk.
	 *
	 * @param prng
	 *            the source of randomness to use during key creation.
	 * @param oid
	 *            identifies the NtruEncrypt parameter set to use.
	 * @param pubFileName
	 *            where to store the public key.
	 * @param privFileName
	 *            where to store the private key.
	 */
	public static void setupNtruEncryptKey(Random prng, OID oid,
			String pubFileName, String privFileName) throws IOException,
			NtruException {
		NtruEncryptKey k = NtruEncryptKey.genKey(oid, prng);

		FileOutputStream pubFile = new FileOutputStream(pubFileName);
		pubFile.write(k.getPubKey());
		pubFile.close();

		FileOutputStream privFile = new FileOutputStream(privFileName);
		privFile.write(k.getPrivKey());
		privFile.close();
	}

	/**
	 * Load a public or private NtruEncrypt key blob from disk and instantiate
	 * an NtruEncryptKey object from it.
	 */
	public static NtruEncryptKey loadKey(String keyFileName)
			throws IOException, NtruException {
		// Get the file length
		File keyFile = new File(keyFileName);
		long fileLength = keyFile.length();
		if (fileLength > Integer.MAX_VALUE)
			throw new IOException("file to be encrypted is too large");

		// Load the bytes from the file, instantiate an NtruEncryptKey object,
		// then clean up and return.
		InputStream in = new FileInputStream(keyFile);
		byte buf[] = new byte[(int) fileLength];
		in.read(buf);
		in.close();
		NtruEncryptKey k = new NtruEncryptKey(buf);
		java.util.Arrays.fill(buf, (byte) 0);
		return k;
	}

	/**
	 * Encrypt a file, protecting it using the supplied NtruEncrypt key.
	 *
	 * <p>
	 * This method actually performs two levels of encryption. First, the file
	 * contents are encrypted using a dynamically-generated AES-256 key in CCM
	 * mode. Then the AES key is encrypted with the supplied NtruEncrypt key.
	 * The two encrypted blobs, as well as any other non-sensitive data needed
	 * for decryption, are writen to disk as "filename.enc".
	 *
	 * @param ntruKey
	 *            the NtruEncrypt key to use to wrap the AES key.
	 * @param prng
	 *            the source of randomness used during the NtruEncrypt operation
	 *            and to generate the AES key.
	 * @param filename
	 *            the name of the soure file. The encrypted data will be written
	 *            to "filename.enc".
	 */
	public static void encryptFile(NtruEncryptKey ntruKey, Random prng,
			String inFileName, String outFileName) throws IOException,
			NtruException {
		// Get the input size
		File inFile = new File(inFileName);
		long fileLength = inFile.length();
		if (fileLength > Integer.MAX_VALUE)
			throw new IOException("file to be encrypted is too large");

		// Read the contents of the file
		InputStream in = new FileInputStream(inFile);
		byte buf[] = new byte[(int) fileLength];
		in.read(buf);
		in.close();

		byte ivBytes[] = null;
		byte encryptedBuf[] = null;
		byte wrappedAESKey[] = null;
		try {
			// Get an AES key
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			keygen.init(128);
			SecretKey aesKey = keygen.generateKey();

			// Get an IV
			ivBytes = new byte[16];
			prng.read(ivBytes);
			IvParameterSpec iv = new IvParameterSpec(ivBytes);

			// Encrypt the plaintext, then zero it out
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, aesKey, iv);
			encryptedBuf = cipher.doFinal(buf);
			java.util.Arrays.fill(buf, (byte) 0);

			// Wrap the AES key with the NtruEncrypt key
			byte aesKeyBytes[] = aesKey.getEncoded();
			wrappedAESKey = ntruKey.encrypt(aesKeyBytes, prng);
			java.util.Arrays.fill(aesKeyBytes, (byte) 0);

		} catch (java.security.GeneralSecurityException e) {
			System.out.println("AES error: " + e);
		}

		// Write it to the output file
		FileOutputStream fileOS = new FileOutputStream(outFileName);
		DataOutputStream out = new DataOutputStream(fileOS);
		out.writeInt(ivBytes.length);
		out.write(ivBytes);
		out.writeInt(wrappedAESKey.length);
		out.write(wrappedAESKey);
		out.writeInt(encryptedBuf.length);
		out.write(encryptedBuf);
		out.close();
		fileOS.close();
	}

	/**
	 * Decrypt a file, reversing the <code>encryptFile</code> operation.
	 *
	 * @param ntruKey
	 *            the NtruEncrypt key to use to wrap the AES key.
	 * @param prng
	 *            the source of randomness used during the NtruEncrypt operation
	 *            and to generate the AES key.
	 * @param filename
	 *            the name of the soure file. The encrypted data will be written
	 *            to "filename.enc".
	 */
	public static void decryptFile(NtruEncryptKey ntruKey, String inFileName,
			String outFileName) throws IOException, NtruException {
		// Get the input size
		File inFile = new File(inFileName);
		//long fileLength = inFile.length();

		// Parse the contents of the encrypted file
		DataInputStream in = new DataInputStream(new FileInputStream(inFile));
		byte ivBytes[] = new byte[in.readInt()];
		in.readFully(ivBytes);
		byte wrappedKey[] = new byte[in.readInt()];
		in.readFully(wrappedKey);
		byte encFileContents[] = new byte[in.readInt()];
		in.readFully(encFileContents);

		byte fileContents[] = null;
		try {
			// Unwrap the AES key
			byte aesKeyBytes[] = ntruKey.decrypt(wrappedKey);
			SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
			java.util.Arrays.fill(aesKeyBytes, (byte) 0);

			// Decrypt the file contents
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, aesKey, iv);
			fileContents = cipher.doFinal(encFileContents);
		} catch (java.security.GeneralSecurityException e) {
			System.out.println("AES error: " + e);
		}

		// Write it
		OutputStream out = new FileOutputStream(outFileName);
		out.write(fileContents);
		out.close();
	}

	/**
	 * Creates a com.securityinnovation.jNeo.Random object seeded with entropy
	 * from java.util.Random.
	 */
	static Random createSeededRandom() {
		byte seed[] = new byte[32];
		java.util.Random sysRand = new java.util.Random();
		sysRand.nextBytes(seed);
		Random prng = new Random(seed);
		return prng;
	}

	/**
	 * Print usage information and exit indicating an error.
	 */
	static void usage() {
		System.out.println("Arguments:");
		System.out.println("  -setup <oidName>");
		System.out.println("  -encrypt <inFileName> <outFileName>");
		System.out.println("  -decrypt <inFileName> <outFileName>");
		System.exit(1);
	}

	/**
	 * Given a string containing the name of an OID (e.g. "ees401ep1"), return
	 * the OID enum with that name. If there is no OID, exit with an informative
	 * message.
	 */
	static OID parseOIDName(String requestedOid) {
		try {
			return OID.valueOf(requestedOid);
		} catch (IllegalArgumentException e) {
			System.out.println("Invalid OID! Valid values are:");
			for (OID oid : OID.values())
				System.out.println("  " + oid);
			System.exit(1);
		}
		return null;
	}

	public static void main(String[] args) throws IOException, NtruException {
		if (args.length == 0)
			usage();

		// Standard key file names.
		String pubkeyFile = "pubKey";
		String privkeyFile = "privKey";

		if (args[0].equals("-setup")) {
			// Setup PRNG
			if (args.length != 2)
				usage();
			Random prng = createSeededRandom();
			OID oid = parseOIDName(args[1]);
			setupNtruEncryptKey(prng, oid, pubkeyFile, privkeyFile);
		} else if (args[0].equals("-encrypt")) {
			if (args.length != 3)
				usage();
			// Setup PRNG
			Random prng = createSeededRandom();
			NtruEncryptKey pubKey = loadKey(pubkeyFile);
			encryptFile(pubKey, prng, args[1], args[2]);
		} else if (args[0].equals("-decrypt")) {
			if (args.length != 3)
				usage();
			NtruEncryptKey privKey = loadKey(privkeyFile);
			decryptFile(privKey, args[1], args[2]);
		} else
			usage();
	}
}
