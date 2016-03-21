package com.cryptoregistry.ntru;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import junit.framework.Assert;

import org.junit.Test;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionKeyGenerationParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionKeyPairGenerator;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.ntru.NTRUEncryptionPublicKeyParameters;
import org.bouncycastle.util.Arrays;

import com.cryptoregistry.ntru.bc.ButtermilkDenseTernaryPolynomial;
import com.cryptoregistry.ntru.bc.ButtermilkProductFormPolynomial;
import com.cryptoregistry.ntru.bc.ButtermilkSparseTernaryPolynomial;
import com.cryptoregistry.ntru.bc.CryptoFactory;
import com.cryptoregistry.ntru.bc.NTRUKeyContents;
import com.cryptoregistry.ntru.bc.NTRUNamedParameters;
import com.cryptoregistry.util.ArrayUtil;

public class CryptoFactoryTest {
	
	@Test
	public void testPoly() {
		// this is the new plan for key encoding
		NTRUEncryptionKeyPairGenerator gen = new NTRUEncryptionKeyPairGenerator();
		gen.init(NTRUEncryptionKeyGenerationParameters.EES1087EP2);
		AsymmetricCipherKeyPair pair = gen.generateKeyPair();
		NTRUEncryptionPublicKeyParameters pub  = (NTRUEncryptionPublicKeyParameters) pair.getPublic();
		NTRUEncryptionPrivateKeyParameters priv =  (NTRUEncryptionPrivateKeyParameters) pair.getPrivate();
		NTRUKeyContents contents = new NTRUKeyContents(NTRUNamedParameters.EES1087EP2,pub.h,priv.t,priv.fp);
		// raw data
		String paramSetName = NTRUNamedParameters.EES1087EP2.name();
		int [] h_coefficients = pub.h.coeffs;
		int [] fp_coefficients = priv.fp.coeffs;
		
		// h and fp can be turned into bytes using a ByteBuffer
		// for h:
		  ByteBuffer h_byteBuffer = ByteBuffer.allocate(h_coefficients.length * 4);        
	      IntBuffer intBuffer = h_byteBuffer.asIntBuffer();
	      intBuffer.put(h_coefficients);
	      byte[] h_bytes = h_byteBuffer.array();
		
	   // for h:
		  ByteBuffer fp_byteBuffer = ByteBuffer.allocate(fp_coefficients.length * 4);        
	      IntBuffer fp_intBuffer = fp_byteBuffer.asIntBuffer();
	      fp_intBuffer.put(fp_coefficients);
	      byte[] fp_bytes = fp_byteBuffer.array();
	      
	      String token = "";
	      // need to know java type so we can reconst. later
	     switch( priv.t.getClass().getSimpleName()){
	     	case "ButtermilkDenseTernaryPolynomial": {
	     		token = "td";
	     		ButtermilkDenseTernaryPolynomial td = (ButtermilkDenseTernaryPolynomial) priv.t;
	     		String wrapped_td = ArrayUtil.wrapAndCompress(td.coeffs).data;
	     		break;
	     	}
	     	case "ButtermilkSparseTernaryPolynomial": {
		    	 token = "ts";
		    	 ButtermilkSparseTernaryPolynomial ts = (ButtermilkSparseTernaryPolynomial) priv.t;
		    	 String wrapped_ts = ArrayUtil.wrapAndCompress(ts.toIntegerPolynomial().coeffs).data;
	     		break;
		    }
	     	case "ButtermilkProductFormPolynomial": {
		    	 token = "tp";
		    	 ButtermilkProductFormPolynomial tp = (ButtermilkProductFormPolynomial) priv.t;
		    	 ButtermilkSparseTernaryPolynomial tp1 = tp.getF1();
		    	 String wrapped_tp1 = ArrayUtil.wrapAndCompress(tp1.toIntegerPolynomial().coeffs).data;
		    	 ButtermilkSparseTernaryPolynomial tp2 = tp.getF2();
		    	 String wrapped_tp2 = ArrayUtil.wrapAndCompress(tp2.toIntegerPolynomial().coeffs).data;
		    	 ButtermilkSparseTernaryPolynomial tp3 = tp.getF3();
		    	 String wrapped_tp3 = ArrayUtil.wrapAndCompress(tp3.toIntegerPolynomial().coeffs).data;
	     		break;
		    }
	     }
	      
	      
	         
	}

	@Test
	public void test0() {
		
		byte [] in = "Hello NTRU world".getBytes(Charset.forName("UTF-8"));
		NTRUKeyContents sKey = CryptoFactory.INSTANCE.generateKeys();
		
		byte [] encrypted = CryptoFactory.INSTANCE.encrypt(sKey, in);
		byte [] out = CryptoFactory.INSTANCE.decrypt(sKey, encrypted);
		Assert.assertTrue(Arrays.areEqual(in, out));
	}
	

}
