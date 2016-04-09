/*
 *  This file is part of Buttermilk(TM) 
 *  Copyright 2013-2015 David R. Smith for cryptoregistry.com
 *
 */
package com.cryptoregistry.client.security;

/**
 * Currently we don't use this but the idea is in some cases you want a token which is unique per machine.
 * 
 * @author Dave
 *
 */
final class HardwareData {
	
	public final String baseboardSerialNumber;
	public final String csproductUUID;
	public final String processorId;
	public final String diskDriveSerialNumber;

	public HardwareData(String motherboardSerialNumber,
			String csproductUUID,
			String processorId,
			String diskDriveSerialNumber) {
		baseboardSerialNumber = motherboardSerialNumber;
		this.csproductUUID = csproductUUID; 
		this.processorId = processorId;
		this.diskDriveSerialNumber = diskDriveSerialNumber;
	}
	
	enum Quality {
		COMPLETE,MISSING_SOME,MISSING_ALL,UNKNOWN;
	}
	
	public Quality quality() {
		int count = 4;
		if(baseboardSerialNumber == QueryHardware.UNKNOWN) count--;
		if(csproductUUID == QueryHardware.UNKNOWN) count--;
		if(processorId == QueryHardware.UNKNOWN) count--;
		if(diskDriveSerialNumber == QueryHardware.UNKNOWN) count--;
		
		if(count ==  4) return Quality.COMPLETE;
		if(count == 3 || count == 2 || count == 1) return Quality.MISSING_SOME;
		if(count == 0) return Quality.MISSING_ALL;
		return Quality.UNKNOWN;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((csproductUUID == null) ? 0 : csproductUUID.hashCode());
		result = prime
				* result
				+ ((diskDriveSerialNumber == null) ? 0 : diskDriveSerialNumber
						.hashCode());
		result = prime * result
				+ ((processorId == null) ? 0 : processorId.hashCode());
		result = prime * result
				+ ((baseboardSerialNumber == null) ? 0 : baseboardSerialNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HardwareData other = (HardwareData) obj;
		if (csproductUUID == null) {
			if (other.csproductUUID != null)
				return false;
		} else if (!csproductUUID.equals(other.csproductUUID))
			return false;
		if (diskDriveSerialNumber == null) {
			if (other.diskDriveSerialNumber != null)
				return false;
		} else if (!diskDriveSerialNumber.equals(other.diskDriveSerialNumber))
			return false;
		if (processorId == null) {
			if (other.processorId != null)
				return false;
		} else if (!processorId.equals(other.processorId))
			return false;
		if (baseboardSerialNumber == null) {
			if (other.baseboardSerialNumber != null)
				return false;
		} else if (!baseboardSerialNumber.equals(other.baseboardSerialNumber))
			return false;
		return true;
	}

}
