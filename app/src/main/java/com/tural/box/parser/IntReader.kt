package com.tural.box.parser

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