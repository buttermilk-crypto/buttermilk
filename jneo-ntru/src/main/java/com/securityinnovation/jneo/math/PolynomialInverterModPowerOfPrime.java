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

package com.securityinnovation.jneo.math;

/**
 * This class implements the algorithm for finding the inverse of a polynomial
 * in the ring (Z/p^rZ)[X]/(X^N-1) for some prime p and some exponent r, as
 * defined in the NTRU Cryptosystems Technical Report #14
 * "Almost Inverses and Fast NTRU Key Creation".
 *
 * <p>
 * The prime p and the exponent r are supplied to the constructor, as a result
 * each instance of this class can compute inverses for the modulus p^r.
 *
 * <p>
 * This algorithm will not work if the modulus is not a power of a prime number.
 */
public class PolynomialInverterModPowerOfPrime extends
		PolynomialInverterModPrime {
	/**
	 * The exponent the prime p must be raised to to compute the modulus. That
	 * is, prime ^ powerOfPrime = modulus.
	 */
	protected int powerOfPrime;

	/**
	 * This constructor initializes the object to calculate inverses modulo a
	 * particular prime.
	 *
	 * @param _powerOfPrime
	 *            the exponent used to define the modulus.
	 * @param _prime
	 *            the prime base of the modulus
	 * @param _invModPrime
	 *            a precomputed table of integer inverses modulo _prime. The
	 *            table should be initialized so that invModPrime[i] * i = 1
	 *            (mod prime) if the inverse of i exists, invModPrime[i] = 0 if
	 *            the inverse of i does not exist. This table should not be
	 *            modified after it has been passed to the constructor.
	 */
	public PolynomialInverterModPowerOfPrime(int _powerOfPrime, int _prime,
			short _invModPrime[]) {
		super(_prime, _invModPrime);
		powerOfPrime = _powerOfPrime;
	}

	/**
	 * Compute the inverse of a polynomial in (Z/p^rZ)[X]/(X^N-1) See NTRU
	 * Cryptosystems Tech Report #014 "Almost Inverses and Fast NTRU Key
	 * Creation."
	 */
	public FullPolynomial invert(FullPolynomial a) {
		// b = a inverse mod prime
		FullPolynomial b = super.invert(a);

		// Make sure a was invertible
		if (b == null)
			return null;

		int q = prime;
		while (q < powerOfPrime) {
			q *= q;

			// b(X) = b(X) * (2-a(X)b(X)) (mod q)
			// i: c = a*b
			FullPolynomial c = FullPolynomial.convolution(a, b, q);
			// ii: c = 2-a*b
			c.p[0] = (short) (2 - c.p[0]);
			if (c.p[0] < 0)
				c.p[0] += (short) q;
			for (int i = 1; i < b.p.length; i++)
				c.p[i] = (short) (q - c.p[i]); // This is -c (mod q)
			// iii: b = b*(2-a*b) mod q
			b = FullPolynomial.convolution(b, c, q);
		}
		return b;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + powerOfPrime;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PolynomialInverterModPowerOfPrime other = (PolynomialInverterModPowerOfPrime) obj;
		if (powerOfPrime != other.powerOfPrime)
			return false;
		return true;
	}
	
	
}
