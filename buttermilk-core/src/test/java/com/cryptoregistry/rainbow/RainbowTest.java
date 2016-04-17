package com.cryptoregistry.rainbow;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

import net.iharder.Base64;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.pqc.crypto.DigestingMessageSigner;
import org.bouncycastle.pqc.crypto.rainbow.Layer;
import org.bouncycastle.pqc.crypto.rainbow.RainbowKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowKeyPairGenerator;
import org.bouncycastle.pqc.crypto.rainbow.RainbowParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowSigner;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Assert;
import org.junit.Test;

import com.cryptoregistry.util.ArrayUtil;

public class RainbowTest {
	
	@Test
	public void test3dEncode() {
		
		RainbowParameters params = new RainbowParameters();

		RainbowKeyPairGenerator rainbowKeyGen = new RainbowKeyPairGenerator();
		RainbowKeyGenerationParameters genParam = new RainbowKeyGenerationParameters(
				new SecureRandom(), params);

		rainbowKeyGen.init(genParam);

		AsymmetricCipherKeyPair pair = rainbowKeyGen.generateKeyPair();
		RainbowPrivateKeyParameters confidentialKey = (RainbowPrivateKeyParameters) pair.getPrivate();
		Layer [] layers = confidentialKey.getLayers();
		short [][][] primary = layers[0].getCoeffAlpha();
	    
	    System.err.println(Arrays.deepToString(primary));
	    
	    String encoded = ArrayUtil.encode3dShort(primary);
		short[][][]result = ArrayUtil.decode3dShort(encoded);
		Assert.assertArrayEquals(primary, result);
	   
	}
	
	@Test
	public void test2dEncode() {
		short[][] array = new short[3][];
		short[] item0 = { 0, 1, 2, 3, 4 };
		short[] item1 = { 5, 6, 7, 8, 9 };
		short[] item2 = { 10, 11, 12, 13, 14 };
		array[0] = item0;
		array[1] = item1;
		array[2] = item2;
		String encoded = ArrayUtil.encode2dShort(array);
		short[][]result = ArrayUtil.decode2dShort(encoded);
		Assert.assertArrayEquals(array, result);
	}
	
	@Test
	public void test1dEncode() {
		
		short[] item0 = { 0, 1, 2, 3, 4 };
		String encoded = ArrayUtil.encode1dShort(item0);
		short[]result = ArrayUtil.decode1dShort(encoded);
		Assert.assertArrayEquals(item0, result);
	}

	@Test
	public void test2DArrayScratchpad() {
		short[][] array = new short[3][];
		short[] item0 = { 0, 1, 2, 3, 4 };
		short[] item1 = { 5, 6, 7, 8, 9 };
		short[] item2 = { 10, 11, 12, 13, 14 };
		array[0] = item0;
		array[1] = item1;
		array[2] = item2;

		String output = "";
		int outerSize = array.length;
		int innerSize = array[0].length;
		System.err.println(outerSize);
		System.err.println(innerSize);
		
		try (ByteArrayOutputStream orig = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(orig);) {
			out.writeInt(outerSize);
			out.writeInt(innerSize);
			for (int i = 0; i < outerSize; i++) {
				short[] inner = array[i];
				for (int j = 0; j < innerSize; j++) {
					out.writeShort(inner[j]);
				}
			}
			output = Base64.encodeBytes(orig.toByteArray(), Base64.URL_SAFE);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		short[][] outArray = null;
		try {
			byte [] inbytes = Base64.decode(output, Base64.URL_SAFE);
			ByteArrayInputStream in = new ByteArrayInputStream(inbytes);
			DataInputStream instream = new DataInputStream(in);
			int os = instream.readInt();
			int is = instream.readInt();
			Assert.assertEquals(outerSize, os);
			Assert.assertEquals(innerSize, is);
			outArray = new short[os][is];
			for (int i = 0; i < os; i++) {
				short [] inner = new short[is];
				for(int j = 0; j<is;j++){
					inner[j] = instream.readShort();
				}
				outArray[i] = inner;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.assertArrayEquals(array, outArray);

	}

	/**
	 * Basic BC code taken from the test suite. 
	 * 
	 */
	@Test
	public void test0() {
		byte[] keyData = Hex.decode("b5014e4b60ef2ba8b6211b4062ba3224e0427dd3");
		SecureRandom keyRandom = new FixedSecureRandom(new byte[][] { keyData,
				keyData });
		RainbowParameters params = new RainbowParameters();

		RainbowKeyPairGenerator rainbowKeyGen = new RainbowKeyPairGenerator();
		RainbowKeyGenerationParameters genParam = new RainbowKeyGenerationParameters(
				keyRandom, params);

		rainbowKeyGen.init(genParam);

		AsymmetricCipherKeyPair pair = rainbowKeyGen.generateKeyPair();

		ParametersWithRandom paramWithRandom = new ParametersWithRandom(
				pair.getPrivate(), keyRandom);

		DigestingMessageSigner rainbowSigner = new DigestingMessageSigner(
				new RainbowSigner(), new SHA224Digest());
		rainbowSigner.init(true, paramWithRandom);

		byte[] message = BigIntegers.asUnsignedByteArray(new BigInteger(
				"968236873715988614170569073515315707566766479517"));
		rainbowSigner.update(message, 0, message.length);
		byte[] sig = rainbowSigner.generateSignature();

		rainbowSigner.init(false, pair.getPublic());
		rainbowSigner.update(message, 0, message.length);
		if (!rainbowSigner.verifySignature(sig)) {
			fail("verification fails");
		}
	}
	
	@Test
	public void test1() {
		RainbowKeyContents contents = CryptoFactory.INSTANCE.generateKeys();
		
	}

}
