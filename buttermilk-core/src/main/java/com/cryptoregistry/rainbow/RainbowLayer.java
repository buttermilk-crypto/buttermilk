package com.cryptoregistry.rainbow;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.pqc.crypto.rainbow.Layer;

public class RainbowLayer {

	int vi;
	int viNext; 
	Coefficient3D coeffAlpha;
	Coefficient3D coeffBeta; 
	Coefficient2D coeffGamma;
	Coefficient1D coeffEta;
	
	public RainbowLayer(int vi, int viNext, Coefficient3D coeffAlpha,
			Coefficient3D coeffBeta, Coefficient2D coeffGamma,
			Coefficient1D coeffEta) {
		super();
		this.vi = vi;
		this.viNext = viNext;
		this.coeffAlpha = coeffAlpha;
		this.coeffBeta = coeffBeta;
		this.coeffGamma = coeffGamma;
		this.coeffEta = coeffEta;
	}
	
	public static List<RainbowLayer> convert(Layer[]layers){
		List<RainbowLayer> list = new ArrayList<RainbowLayer>();
		for(Layer l: layers){
			RainbowLayer rl = new RainbowLayer(
					l.getVi(),
					l.getViNext(),
					new Coefficient3D(l.getCoeffAlpha()),
					new Coefficient3D(l.getCoeffBeta()),
					new Coefficient2D(l.getCoeffGamma()),
					new Coefficient1D(l.getCoeffEta())
			);
			list.add(rl);
		}
		return list;
	}
	
	public static Layer [] convert(List<RainbowLayer> layers){
		Layer [] array = new Layer[layers.size()];
		int i = 0;
		for(RainbowLayer l: layers){
			Layer layer = new Layer(
					(byte)l.vi, 
					(byte)l.viNext, 
					l.coeffAlpha.getCoeff(),
	                l.coeffBeta.getCoeff(), 
	                l.coeffGamma.getCoeff(),
	                l.coeffEta.getCoeff());
			array[i] = layer;
			i++;
		}
		return array;
	}
}
