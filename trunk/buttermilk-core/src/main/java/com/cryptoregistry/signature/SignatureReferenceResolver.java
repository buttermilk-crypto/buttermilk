/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.signature;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Interface for classes that resolve the data in signatures so signatures can be validated
 * @author Dave
 *
 */
public interface SignatureReferenceResolver {
	
	/**
	 * Given a ref value, which is a key into a cache of possible items to resolve, find the value for that ref
	 * and write it as UTF-8 encoded bytes to the collector.
	 * 
	 * @param ref
	 * @param collector
	 * @throws RefNotFoundException
	 */
	public void resolve(String ref, ByteArrayOutputStream collector) throws RefNotFoundException ;
	
	/**
	 * Given a list of ref values, which are keya into a cache of possible items to resolve, find the value for that ref
	 * and write it as UTF-8 encoded bytes to the collector in list order.
	 * 
	 * @param ref
	 * @param collector
	 * @throws RefNotFoundException
	 */
	public void resolve(List<String> refs, ByteArrayOutputStream collector) throws RefNotFoundException ;
}
