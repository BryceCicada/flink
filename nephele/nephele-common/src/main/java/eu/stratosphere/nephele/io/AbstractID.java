/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.nephele.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import eu.stratosphere.nephele.util.StringUtils;

/**
 * ID is an abstract base class to provide statistically unique and serializable identification numbers in Nephele.
 * Every component that requires these kinds of IDs provides its own concrete type.
 * 
 * @author warneke
 */
public abstract class AbstractID implements IOReadableWritable {

	/**
	 * The size of the ID in byte.
	 */
	protected static final int SIZE = Long.SIZE * 2;

	/**
	 * The upper part of the actual ID.
	 */
	private long upperPart;

	/**
	 * The lower part of the actual ID.
	 */
	private long lowerPart;

	/**
	 * Constructs a new ID with a specific bytes value.
	 */
	public AbstractID(final byte[] bytes) {

		if (bytes.length != SIZE) {
			throw new IllegalArgumentException("Argument bytes must by an array of " + SIZE + " bytes");
		}

		this.lowerPart = byteArrayToLong(bytes, 0);
		this.upperPart = byteArrayToLong(bytes, Long.SIZE);
	}

	/**
	 * Constructs a new random ID from a uniform distribution.
	 */
	public AbstractID() {

		this.lowerPart = (long) (Math.random() * Long.MAX_VALUE);
		this.upperPart = (long) (Math.random() * Long.MAX_VALUE);
	}

	/**
	 * Converts the given byte array to a long.
	 * 
	 * @param ba
	 *        the byte array to be converted
	 * @param offset
	 *        the offset indicating at which byte inside the array the conversion shall begin
	 * @return the long variable
	 */
	private static long byteArrayToLong(final byte[] ba, final int offset) {

		long l = 0;

		for (int i = 0; i < Long.SIZE; ++i) {
			l |= (ba[offset + Long.SIZE - 1 - i] & 0xffL) << (i << 3);
		}

		return l;
	}

	/**
	 * Converts a long to a byte array.
	 * 
	 * @param l
	 *        the long variable to be converted
	 * @param ba
	 *        the byte array to store the result the of the conversion
	 * @param offset
	 *        the offset indicating at what position inside the byte array the result of the conversion shall be stored
	 */
	private static void longToByteArray(final long l, final byte[] ba, final int offset) {

		for (int i = 0; i < Long.SIZE; ++i) {
			final int shift = i << 3; // i * 8
			ba[offset + Long.SIZE - 1 - i] = (byte) ((l & (0xffL << shift)) >>> shift);
		}
	}

	/**
	 * Sets an ID from another ID by copying its internal byte representation.
	 * 
	 * @param src
	 *        the source ID
	 */
	public void setID(final AbstractID src) {
		this.lowerPart = src.lowerPart;
		this.upperPart = src.upperPart;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {

		if (!(obj instanceof AbstractID)) {
			return false;
		}

		final AbstractID src = (AbstractID) obj;

		if (src.lowerPart != this.lowerPart) {
			return false;
		}

		if (src.upperPart != this.upperPart) {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {

		return (int) (this.lowerPart ^ (this.upperPart >>> 32));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void read(final DataInput in) throws IOException {

		this.lowerPart = in.readLong();
		this.upperPart = in.readLong();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(final DataOutput out) throws IOException {

		out.writeLong(this.lowerPart);
		out.writeLong(this.upperPart);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		final byte[] ba = new byte[SIZE];
		longToByteArray(this.lowerPart, ba, 0);
		longToByteArray(this.upperPart, ba, Long.SIZE);

		return StringUtils.byteToHexString(ba);
	}
}
