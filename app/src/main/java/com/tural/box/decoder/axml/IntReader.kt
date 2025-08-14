/*
 * Copyright 2008 Android4ME
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tural.box.decoder.axml

import java.io.EOFException
import java.io.IOException
import java.io.InputStream

/**
 * @author Dmitry Skiba
 * <p>
 * Simple helper class that allows reading of integers.
 * <p>
 */
class IntReader(private var stream: InputStream?, private var bigEndian: Boolean) {

	fun reset(stream: InputStream?, bigEndian: Boolean) {
		this.stream = stream
		this.bigEndian = bigEndian
	}

	fun close() {
		if (stream == null) {
			return
		}
		try {
			stream?.close()
		} catch (_: IOException) {
		}
		reset(null, false)
	}

	@Throws(IOException::class)
	fun readInt(): Int {
		val length = 4
		var result = 0
		if (bigEndian) {
			for (i in (length - 1) * 8 downTo 0 step 8) {
				val b = stream?.read() ?: throw EOFException()
				result = result or (b shl i)
			}
		} else {
			val bits = length * 8
			for (i in 0 until bits step 8) {
				val b = stream?.read() ?: throw EOFException()
				result = result or (b shl i)
			}
		}
		return result
	}

	@Throws(IOException::class)
	fun readIntArray(length: Int): IntArray {
		val array = IntArray(length)
		readIntArray(array, 0, length)
		return array
	}

	@Throws(IOException::class)
	fun readIntArray(array: IntArray, offset: Int, length: Int) {
		var currentOffset = offset
		var remaining = length
		while (remaining > 0) {
			array[currentOffset++] = readInt()
			remaining--
		}
	}

	@Throws(IOException::class)
	fun skipInt() {
		val skipped = stream?.skip(4) ?: throw EOFException()
		if (skipped != 4L) {
			throw EOFException()
		}
	}
}
