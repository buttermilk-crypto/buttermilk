package com.cryptoregistry.rainbow;

import org.bouncycastle.pqc.crypto.rainbow.RainbowPublicKeyParameters;

import com.cryptoregistry.CryptoKey;
import com.cryptoregistry.CryptoKeyMetadata;
import com.cryptoregistry.Verifier;


public class RainbowKeyForPublication implements CryptoKey, Verifier {
	
	public final RainbowKeyMetadata meta;

	public final int docLength;
    public final Coefficient2D coeffQuadratic;
    public final Coefficient2D coeffSingular;
	public final Coefficient1D coeffScalar;
	
	public RainbowKeyForPublication(int docLength,
			Coefficient2D coeffQuadratic, Coefficient2D coeffSingular,
			Coefficient1D coeffScalar) {
		super();
		this.docLength = docLength;
		this.coeffQuadratic = coeffQuadratic;
		this.coeffSingular = coeffSingular;
		this.coeffScalar = coeffScalar;
		this.meta = RainbowKeyMetadata.createForPublication();
	}
	
	public RainbowKeyForPublication(RainbowKeyMetadata meta, int docLength,
			Coefficient2D coeffQuadratic, Coefficient2D coeffSingular,
			Coefficient1D coeffScalar) {
		super();
		this.docLength = docLength;
		this.coeffQuadratic = coeffQuadratic;
		this.coeffSingular = coeffSingular;
		this.coeffScalar = coeffScalar;
		this.meta = meta;
	}
	
	RainbowKeyForPublication(RainbowPublicKeyParameters pubKey){
		super();
		this.docLength = pubKey.getDocLength();
		this.coeffQuadratic = new Coefficient2D(pubKey.getCoeffQuadratic());
		this.coeffSingular = new Coefficient2D(pubKey.getCoeffSingular());
		this.coeffScalar = new Coefficient1D(pubKey.getCoeffScalar());
		this.meta = RainbowKeyMetadata.createForPublication();
	}
	
	public RainbowPublicKeyParameters getPublicKey() {
		return new RainbowPublicKeyParameters(docLength,
				coeffQuadratic.getCoeff(), 
				coeffSingular.getCoeff(),
				coeffScalar.getCoeff());
	}

	@Override
	public CryptoKeyMetadata getMetadata() {
		return meta;
	}

	@Override
	public String formatJSON() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CryptoKey keyForPublication() {
		return new RainbowKeyForPublication(meta,docLength,
			coeffQuadratic, coeffSingular, coeffScalar);
	}
	
	
	
}
