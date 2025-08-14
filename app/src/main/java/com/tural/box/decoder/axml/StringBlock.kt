/*
 * Copyright 2008 Android4ME
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tural.box.decoder.axml

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
			readCheckType(reader, CHUNK_TYPE)
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

		private fun readCheckType(reader: IntReader, expectedType: Int) {
			val type = reader.readInt()
			if (type != expectedType) {
				throw IOException("Expected chunk type 0x${expectedType.toString(16)}, got 0x${type.toString(16)}")
			}
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
