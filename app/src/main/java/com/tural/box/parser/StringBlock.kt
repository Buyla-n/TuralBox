package com.tural.box.parser

import java.io.IOException

/**
 * @author Dmitry Skiba
 * <p>
 * Block of strings, used in binary xml and arsc.
 * <p>
 */
class StringBlock private constructor() {
	private var stringOffsets: IntArray? = null
	private var strings: IntArray? = null
	private var isUtf8: Boolean = false

	companion object {
		private const val CHUNK_TYPE = 0x001C0001

		@Throws(IOException::class)
		fun read(reader: IntReader): StringBlock {
			val type = reader.readInt()
			if (type != CHUNK_TYPE) {
				throw IOException(
                    "Expected chunk type 0x${CHUNK_TYPE.toString(16)}, got 0x${
                        type.toString(
                            16
                        )
                    }"
                )
			}
			val chunkSize = reader.readInt()
			val stringCount = reader.readInt()
			reader.skipInt()
			val flag = reader.readInt()
			val stringsOffset = reader.readInt()
			reader.skipInt()

			val block = StringBlock()
			block.isUtf8 = flag != 0
			block.stringOffsets = reader.readIntArray(stringCount)

			val size = chunkSize - stringsOffset
			if (size % 4 != 0) {
				throw IOException("String data size is not multiple of 4 ($size).")
			}
			block.strings = reader.readIntArray(size / 4)

			return block
		}

		private fun getShort(array: IntArray, offset: Int): Int {
			val value = array[offset / 4]
			return if ((offset % 4) / 2 == 0) {
				value and 0xFFFF
			} else {
				value ushr 16
			}
		}
	}

	fun getString(index: Int): String? {
		if (index < 0 || stringOffsets == null || index >= stringOffsets!!.size) {
			return null
		}
		var offset = stringOffsets!![index]
		if (isUtf8) {
			return null
		} else {
			val length = getShort(strings!!, offset)
			val result = StringBuilder(length)
			var remainingLength = length
			while (remainingLength != 0) {
				offset += 2
				result.append(getShort(strings!!, offset).toChar())
				remainingLength--
			}
			return result.toString()
		}
	}
}