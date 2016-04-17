package com.cryptoregistry.rainbow;

import java.util.List;

import org.bouncycastle.pqc.crypto.rainbow.RainbowPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.rainbow.RainbowPublicKeyParameters;

import com.cryptoregistry.Signer;
import com.cryptoregistry.passwords.Password;
import com.cryptoregistry.pbe.PBEParams;

public class RainbowKeyContents extends RainbowKeyForPublication implements Signer {

	public final Coefficient2D A1inv;
	public final Coefficient1D b1;
	public final Coefficient2D A2inv;
	public final Coefficient1D b2;
	public final  int[] vi;
	public final List<RainbowLayer> layers;

	public RainbowKeyContents(Coefficient2D A1inv, Coefficient1D b1,
			Coefficient2D A2inv, Coefficient1D b2, int[] vi,
			List<RainbowLayer> layers, int docLength, Coefficient2D coeffQuadratic,
			Coefficient2D coeffSingular, Coefficient1D coeffScalar) {
		super(docLength, coeffQuadratic, coeffSingular, coeffScalar);
		this.A1inv = A1inv;
		this.b1 = b1;
		this.A2inv = A2inv;
		this.b2 = b2;
		this.vi = vi;
		this.layers = layers;
	}

	public RainbowKeyContents(RainbowKeyMetadata meta, Coefficient2D A1inv,
			Coefficient1D b1, Coefficient2D A2inv, Coefficient1D b2, int[] vi,
			List<RainbowLayer> layers, int docLength, Coefficient2D coeffQuadratic,
			Coefficient2D coeffSingular, Coefficient1D coeffScalar) {
		super(meta, docLength, coeffQuadratic, coeffSingular, coeffScalar);
		this.A1inv = A1inv;
		this.b1 = b1;
		this.A2inv = A2inv;
		this.b2 = b2;
		this.vi = vi;
		this.layers = layers;
	}
	/**
	 * Used with key generation
	 * 
	 * @param pubKey
	 * @param privKey
	 */
	RainbowKeyContents(RainbowKeyMetadata meta, RainbowPublicKeyParameters pubKey, RainbowPrivateKeyParameters privKey){
		super(meta, pubKey);	
		this.A1inv= new Coefficient2D(privKey.getInvA1());
		this.b1= new Coefficient1D(privKey.getB1());
		this.A2inv= new Coefficient2D(privKey.getInvA2());
		this.b2=new Coefficient1D(privKey.getB2());
		this.vi = privKey.getVi();
		this.layers = RainbowLayer.convert(privKey.getLayers());
	}

	/**
	 * If a password is set in the KeyFormat, clean that out. This call can be
	 * made once we're done with the key materials in this cycle of use.
	 */
	@Override
	public void scrubPassword() {
		PBEParams params = this.meta.format.pbeParams;
		if (params != null) {
			Password password = params.getPassword();
			if (password != null && password.isAlive())
				password.selfDestruct();
		}
	}
	
	public RainbowPrivateKeyParameters getPrivateKey() {
		RainbowPrivateKeyParameters params = new RainbowPrivateKeyParameters(A1inv.getCoeff(), b1.getCoeff(),
                A2inv.getCoeff(), b2.getCoeff(), vi, RainbowLayer.convert(layers));
		return params;
	}

}
