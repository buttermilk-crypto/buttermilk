package com.cryptoregistry.signature;

import java.io.IOException;
import java.io.Writer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Used when formatting the signature data as this data is different (might have different keys or numbers of parts)
 *  for different types of signatures
 * 
 * @author Dave
 *
 */
public interface CanFormatSignatureData {
	public void formatSignaturePrimitivesJSON(JsonGenerator g, Writer writer) throws JsonGenerationException, IOException ;
}
