package com.tural.box.decoder.axml

import java.io.IOException

/**
 * @author Dmitry Skiba
 *
 */
@Throws(IOException::class)
fun readCheckType(reader: IntReader, expectedType: Int) {
	val type = reader.readInt()
	require(type == expectedType) {
		"Expected chunk of type 0x${expectedType.toHexString()}, read 0x${type.toHexString()}."
	}
}