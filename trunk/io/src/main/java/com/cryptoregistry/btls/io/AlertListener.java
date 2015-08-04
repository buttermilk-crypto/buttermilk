/*
 *  This file is part of Buttermilk
 *  Copyright 2011-2014 David R. Smith All Rights Reserved.
 *
 */
package com.cryptoregistry.btls.io;

/**
 * Implement this class to participate in alerts
 * 
 * @author Dave
 * @see FrameInputStream
 *
 */
public interface AlertListener {

	public void alertReceived(AlertEvent evt);
}
