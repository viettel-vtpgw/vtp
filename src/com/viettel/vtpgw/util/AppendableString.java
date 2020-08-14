package com.viettel.vtpgw.util;

import java.util.Arrays;

public class AppendableString{
	public static AppendableString get(String str){
		return new AppendableString (str.toCharArray());
	}

	/**
	 * The count is the number of characters used.
	 */
	int count;

	/**
	 * The value is used for character storage.
	 */
	char[] value;
	/**
	 * This no-arg constructor is necessary for serialization of subclasses.
	 */
	public AppendableString() {
		this(32);
	}
	public AppendableString(char[] str) {
		value = str;
		count = str.length;
	}

	/**
	 * Creates an ComparableStringBuilder of the specified capacity.
	 */
	public AppendableString(int capacity) {
		value = new char[capacity];
	}

	/**
	 * Appends the string representation of the {@code boolean} argument to the
	 * sequence.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string
	 * by the method {@link String#valueOf(boolean)}, and the characters of that
	 * string were then {@link #append(String) appended} to this character
	 * sequence.
	 *
	 * @param b
	 *          a {@code boolean}.
	 * @return a reference to this object.
	 */
	public AppendableString append(boolean b) {
		if (b) {
			ensureCapacityInternal(count + 4);
			value[count++] = 't';
			value[count++] = 'r';
			value[count++] = 'u';
			value[count++] = 'e';
		} else {
			ensureCapacityInternal(count + 5);
			value[count++] = 'f';
			value[count++] = 'a';
			value[count++] = 'l';
			value[count++] = 's';
			value[count++] = 'e';
		}
		return this;
	}

	/**
	 * Appends the string representation of the {@code char} argument to this
	 * sequence.
	 * <p>
	 * The argument is appended to the contents of this sequence. The length of
	 * this sequence increases by {@code 1}.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string
	 * by the method {@link String#valueOf(char)}, and the character in that
	 * string were then {@link #append(String) appended} to this character
	 * sequence.
	 *
	 * @param c
	 *          a {@code char}.
	 * @return a reference to this object.
	 */
	public AppendableString append(char c) {
		ensureCapacityInternal(count + 1);
		value[count++] = c;
		return this;
	}

	/**
	 * Appends the string representation of a subarray of the {@code char} array
	 * argument to this sequence.
	 * <p>
	 * Characters of the {@code char} array {@code str}, starting at index
	 * {@code offset}, are appended, in order, to the contents of this sequence.
	 * The length of this sequence increases by the value of {@code len}.
	 * <p>
	 * The overall effect is exactly as if the arguments were converted to a
	 * string by the method {@link String#valueOf(char[],int,int)}, and the
	 * characters of that string were then {@link #append(String) appended} to
	 * this character sequence.
	 *
	 * @param str
	 *          the characters to be appended.
	 * @param offset
	 *          the index of the first {@code char} to append.
	 * @param len
	 *          the number of {@code char}s to append.
	 * @return a reference to this object.
	 * @throws IndexOutOfBoundsException
	 *           if {@code offset < 0} or {@code len < 0} or
	 *           {@code offset+len > str.length}
	 */
	public AppendableString append(char str[], int offset, int len) {
		if (len > 0) // let arraycopy report AIOOBE for len < 0
			ensureCapacityInternal(count + len);
		System.arraycopy(str, offset, value, count, len);
		count += len;
		return this;
	}

	/**
	 * Appends the string representation of the {@code char} array argument to
	 * this sequence.
	 * <p>
	 * The characters of the array argument are appended, in order, to the
	 * contents of this sequence. The length of this sequence increases by the
	 * length of the argument.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string
	 * by the method {@link String#valueOf(char[])}, and the characters of that
	 * string were then {@link #append(String) appended} to this character
	 * sequence.
	 *
	 * @param str
	 *          the characters to be appended.
	 * @return a reference to this object.
	 */
	public AppendableString append(char[] str) {
		int len = str.length;
		ensureCapacityInternal(count + len);
		System.arraycopy(str, 0, value, count, len);
		count += len;
		return this;
	}

	/**
	 * Appends the specified string to this character sequence.
	 * <p>
	 * The characters of the {@code String} argument are appended, in order,
	 * increasing the length of this sequence by the length of the argument. If
	 * {@code str} is {@code null}, then the four characters {@code "null"} are
	 * appended.
	 * <p>
	 * Let <i>n</i> be the length of this character sequence just prior to
	 * execution of the {@code append} method. Then the character at index
	 * <i>k</i> in the new character sequence is equal to the character at index
	 * <i>k</i> in the old character sequence, if <i>k</i> is less than <i>n</i>;
	 * otherwise, it is equal to the character at index <i>k-n</i> in the argument
	 * {@code str}.
	 *
	 * @param str
	 *          a string.
	 * @return a reference to this object.
	 */
	public AppendableString append(String str) {
		if (str == null)
			return appendNull();
		int len = str.length();
		ensureCapacityInternal(count + len);
		str.getChars(0, len, value, count);
		count += len;
		return this;
	}

	private AppendableString appendNull() {
		int c = count;
		ensureCapacityInternal(c + 4);
		final char[] value = this.value;
		value[c++] = 'n';
		value[c++] = 'u';
		value[c++] = 'l';
		value[c++] = 'l';
		count = c;
		return this;
	}

	/**
	 * Returns the current capacity. The capacity is the amount of storage
	 * available for newly inserted characters, beyond which an allocation will
	 * occur.
	 *
	 * @return the current capacity
	 */
	public int capacity() {
		return value.length;
	}

	/**
	 * Ensures that the capacity is at least equal to the specified minimum. If
	 * the current capacity is less than the argument, then a new internal array
	 * is allocated with greater capacity. The new capacity is the larger of:
	 * <ul>
	 * <li>The {@code minimumCapacity} argument.
	 * <li>Twice the old capacity, plus {@code 2}.
	 * </ul>
	 * If the {@code minimumCapacity} argument is nonpositive, this method takes
	 * no action and simply returns. Note that subsequent operations on this
	 * object can reduce the actual capacity below that requested here.
	 *
	 * @param minimumCapacity
	 *          the minimum desired capacity.
	 */
	public void ensureCapacity(int minimumCapacity) {
		if (minimumCapacity > 0)
			ensureCapacityInternal(minimumCapacity);
	}



	/**
	 * This method has the same contract as ensureCapacity, but is never
	 * synchronized.
	 */
	private void ensureCapacityInternal(int minimumCapacity) {
		if (minimumCapacity - value.length > 0)
			expandCapacity(minimumCapacity);
	}

	@Override
	public boolean equals(Object anObject) {
    if (this == anObject) {
        return true;
    }
    if (anObject instanceof AppendableString) {
    	AppendableString anotherString = (AppendableString)anObject;
        int n = count;
        if (n == anotherString.count) {
            char v1[] = value;
            char v2[] = anotherString.value;
            int i = 0;            
            while (n-- != 0) {
                if (v1[i] != v2[i])
                    return false;
                i++;
            }
            return true;
        }
    }
    return false;
	}

	/**
	 * This implements the expansion semantics of ensureCapacity with no size
	 * check or synchronization.
	 */
	void expandCapacity(int minimumCapacity) {
		int newCapacity = value.length * 2 + 2;
		if (newCapacity - minimumCapacity < 0)
			newCapacity = minimumCapacity;
		if (newCapacity < 0) {
			if (minimumCapacity < 0) // overflow
				throw new OutOfMemoryError();
			newCapacity = Integer.MAX_VALUE;
		}
		value = Arrays.copyOf(value, newCapacity);
	}

	//int hash;
	@Override
	public int hashCode() {
    int h = 0;
    if (count > 0) {
        char val[] = value;
        for (int i = 0; i < count; i++) {
            h = 31 * h + val[i];
        }        
    }
    return h;
	}
	/**
	 * Returns the length (character count).
	 *
	 * @return the length of the sequence of characters currently represented by
	 *         this object
	 */
	public int length() {
		return count;
	}
	/**
	 * Sets the length of the character sequence. The sequence is changed to a new
	 * character sequence whose length is specified by the argument. For every
	 * nonnegative index <i>k</i> less than {@code newLength}, the character at
	 * index <i>k</i> in the new character sequence is the same as the character
	 * at index <i>k</i> in the old sequence if <i>k</i> is less than the length
	 * of the old character sequence; otherwise, it is the null character
	 * {@code '\u005Cu0000'}.
	 *
	 * In other words, if the {@code newLength} argument is less than the current
	 * length, the length is changed to the specified length.
	 * <p>
	 * If the {@code newLength} argument is greater than or equal to the current
	 * length, sufficient null characters ({@code '\u005Cu0000'}) are appended so
	 * that length becomes the {@code newLength} argument.
	 * <p>
	 * The {@code newLength} argument must be greater than or equal to {@code 0}.
	 *
	 * @param newLength
	 *          the new length
	 * @throws IndexOutOfBoundsException
	 *           if the {@code newLength} argument is negative.
	 */
	public void setLength(int newLength) {
		if (newLength < 0)
			throw new StringIndexOutOfBoundsException(newLength);
		ensureCapacityInternal(newLength);

		if (count < newLength) {
			Arrays.fill(value, count, newLength, '\0');
		}

		count = newLength;
	}
	@Override
	public String toString() {
		return new String(value,0,count);
	}
	/**
	 * Attempts to reduce storage used for the character sequence. If the buffer
	 * is larger than necessary to hold its current sequence of characters, then
	 * it may be resized to become more space efficient. Calling this method may,
	 * but is not required to, affect the value returned by a subsequent call to
	 * the {@link #capacity()} method.
	 */
	public void trimToSize() {
		if (count < value.length) {
			value = Arrays.copyOf(value, count);
		}
	}
	
}
