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
import java.io.InputStream

/**
 * @author Dmitry Skiba
 * <p>
 * Binary xml files parser.
 * <p>
 * Parser has only two states:
 * (1) Operational state, which parser obtains after first successful call
 * to next() and retains until open(), close(), or failed call to next().
 * (2) Closed state, which parser obtains after open(), close(), or failed
 * call to next(). In this state methods return invalid values or throw exceptions.
 * <p>
 *
 */
class AXmlResourceParser {

	private var reader: IntReader? = null
	private var operational = false
	private var strings: StringBlock? = null
	private val namespaces = NamespaceStack()
	private var decreaseDepth = false
	private var event = -1
	private var lineNumber = -1
	private var name = -1
	private var namespaceUri = -1
	private var attributes: IntArray? = null
	private var classAttribute = -1

	init {
		resetEventInfo()
	}

	fun open(stream: InputStream?) {
		close()
		if (stream!=null) {
			reader = IntReader(stream,false)
		}
	}

	fun close() {
		if (!operational) {
			return
		}
		operational=false
        reader?.close()
		reader=null
		strings=null
		namespaces.reset()
		resetEventInfo()
	}

	// iteration

	fun next(): Int {
		if (reader==null) {
			throw XmlPullParserException("Parser is not opened.",this,null)
        }
		try {
			doNext()
            return event
        }
		catch (e: IOException) {
			close()
            throw e
        }
	}

	fun getDepth(): Int {
		return namespaces.depth-1
    }

	fun getLineNumber(): Int {
		return lineNumber
    }

	fun getName(): String? {
		if (name==-1 || (event!=START_TAG && event!=END_TAG)) {
			return null
        }
		return strings!!.getString(name)
	}

	fun getText(): String? {
		if (name==-1 || event!=TEXT) {
			return null
		}
		return strings!!.getString(name)
	}

	fun getPrefix(): String? {
		val prefix=namespaces.findPrefix(namespaceUri)
		return strings!!.getString(prefix)
	}

	fun getPositionDescription(): String {
		return "XML line #"+getLineNumber()
	}

	fun getNamespaceCount(depth: Int): Int {
		return namespaces.getAccumulatedCount(depth)
	}

	fun getNamespacePrefix(pos: Int): String? {
		val prefix=namespaces.getPrefix(pos)
		return strings!!.getString(prefix)
	}

	fun getNamespaceUri(pos: Int): String? {
		val uri=namespaces.getUri(pos)
		return strings!!.getString(uri)
	}

	// attributes

	fun getAttributeCount(): Int {
		if (event!=START_TAG) {
			return -1
		}
		return attributes!!.size.div(ATTRIBUTE_LENGTH)
	}

	fun getAttributePrefix(index: Int): String? {
		val offset=getAttributeOffset(index)
		val uri=attributes!![offset+ATTRIBUTE_IX_NAMESPACE_URI]
		val prefix=namespaces.findPrefix(uri)
		if (prefix==-1) {
			return ""
		}
		return strings!!.getString(prefix)
	}

	fun getAttributeName(index: Int): String? {
		val offset=getAttributeOffset(index)
		val name=attributes!![offset+ATTRIBUTE_IX_NAME]
		if (name==-1) {
			return ""
		}
		return strings!!.getString(name)
	}

	fun getAttributeValueType(index: Int): Int {
		val offset=getAttributeOffset(index)
		return attributes!![offset+ATTRIBUTE_IX_VALUE_TYPE]
	}

	fun getAttributeValueData(index: Int): Int {
		val offset=getAttributeOffset(index)
		return attributes!![offset+ATTRIBUTE_IX_VALUE_DATA]
	}

	fun getAttributeValue(index: Int): String? {
		val offset=getAttributeOffset(index)
		val valueType=attributes!![offset+ATTRIBUTE_IX_VALUE_TYPE]
		if (valueType== TypedValue.TYPE_STRING) {
			val valueString=attributes!![offset+ATTRIBUTE_IX_VALUE_STRING]
			return strings!!.getString(valueString)
		}
		return "" //TypedValue.coerceToString(valueType,valueData)
	}

	fun getColumnNumber(): Int {
		return -1
    }

	private class NamespaceStack {
		private var data: IntArray = IntArray(32)
		private var dataLength = 0
		var depth = 0

		fun reset() {
			dataLength = 0
			depth = 0
		}

		val currentCount: Int
			get() = if (dataLength == 0) 0 else data[dataLength - 1]

		fun getAccumulatedCount(depth: Int): Int {
			if (dataLength == 0 || depth < 0) return 0
			var targetDepth = if (depth > this.depth) this.depth else depth

			var accumulatedCount = 0
			var offset = 0
			while (targetDepth != 0) {
				val count = data[offset]
				accumulatedCount += count
				offset += 2 + count * 2
				targetDepth--
			}
			return accumulatedCount
		}

		fun push(prefix: Int, uri: Int) {
			if (depth == 0) {
				increaseDepth()
			}
			ensureDataCapacity()
			val offset = dataLength - 1
			val count = data[offset]
			data[offset - 1 - count * 2] = count + 1
			data[offset] = prefix
			data[offset + 1] = uri
			data[offset + 2] = count + 1
			dataLength += 2
		}

		fun pop() {
			if (dataLength == 0) return

			var offset = dataLength - 1
			var count = data[offset]
			if (count == 0) return

			count--
			offset -= 2
			data[offset] = count
			offset -= 1 + count * 2
			data[offset] = count
			dataLength -= 2
		}

		fun getPrefix(index: Int): Int = get(index, true)
		fun getUri(index: Int): Int = get(index, false)

		fun findPrefix(uri: Int): Int {
			if (dataLength == 0) return -1

			var offset = dataLength - 1
			repeat(depth) {
				val count = data[offset]
				offset -= 2
				var currentCount = count
				while (currentCount != 0) {
					if (data[offset + 1] == uri) {
						return data[offset]
					}
					offset -= 2
					currentCount--
				}
			}
			return -1
		}

		private fun get(index: Int, prefix: Boolean): Int {
			if (dataLength == 0 || index < 0) return -1

			var offset = 0
			var remainingIndex = index
			repeat(depth) {
				val count = data[offset]
				if (remainingIndex >= count) {
					remainingIndex -= count
					offset += 2 + count * 2
					return@repeat
				}
				offset += 1 + remainingIndex * 2
				if (!prefix) {
					offset += 1
				}
				return data[offset]
			}
			return -1
		}

		fun increaseDepth() {
			ensureDataCapacity()
			val offset = dataLength
			data[offset] = 0
			data[offset + 1] = 0
			dataLength += 2
			depth += 1
		}

		fun decreaseDepth() {
			if (dataLength == 0) return

			val offset = dataLength - 1
			val count = data[offset]
			if (offset - 1 - count * 2 == 0) return

			dataLength -= 2 + count * 2
			depth -= 1
		}

		private fun ensureDataCapacity() {
			val available = data.size - dataLength
			if (available > 2) return

			val newLength = (data.size + available) * 2
			val newData = IntArray(newLength)
			System.arraycopy(data, 0, newData, 0, dataLength)
			data = newData
		}
	}

	private fun getAttributeOffset(index: Int): Int {
		if (event!=START_TAG) {
			throw IndexOutOfBoundsException("Current event is not START_TAG.")
		}
		val offset=index*5
		if (offset>=attributes!!.size) {
			throw IndexOutOfBoundsException("Invalid attribute index ($index).")
		}
		return offset
	}

	private fun resetEventInfo() {
		event=-1
		lineNumber=-1
		name=-1
		namespaceUri=-1
		attributes=null
		classAttribute=-1
	}

	private fun doNext() {
		// Delayed initialization.
		if (strings==null) {
			readCheckType(reader!!,CHUNK_AXML_FILE)
			reader!!.skipInt() //chunkSize
			strings= StringBlock.Companion.read(reader!!)
			namespaces.increaseDepth()
			operational=true
		}

		if (event==END_DOCUMENT) {
			return
		}

		val event=event
		resetEventInfo()

		while (true) {
			if (decreaseDepth) {
				decreaseDepth=false
				namespaces.decreaseDepth()
			}

			// Fake END_DOCUMENT event
			if (event==END_TAG &&
				namespaces.depth==1 &&
				namespaces.currentCount==0)
			{
				this@AXmlResourceParser.event =END_DOCUMENT
				break
			}

            val chunkType: Int = if (event==START_DOCUMENT) {
                // Fake event, see CHUNK_XML_START_TAG handler
                CHUNK_XML_START_TAG
            } else {
                reader!!.readInt()
            }

			if (chunkType==CHUNK_RESOURCE_IDS) {
				val chunkSize=reader!!.readInt()
				if (chunkSize<8 || (chunkSize%4)!=0) {
					throw IOException("Invalid resource ids size ($chunkSize).")
				}
				reader!!.readIntArray(chunkSize/4-2)
				continue
			}

			if (chunkType<CHUNK_XML_FIRST || chunkType>CHUNK_XML_LAST) {
				throw IOException("Invalid chunk type ($chunkType).")
			}

			// Fake START_DOCUMENT event
			if (chunkType==CHUNK_XML_START_TAG && event==-1) {
				this@AXmlResourceParser.event =START_DOCUMENT
				break
			}

			// Common header
			reader!!.skipInt() //chunkSize
			val lineNumber=reader!!.readInt()
			reader!!.skipInt() //0xFFFFFFFF

			if (chunkType==CHUNK_XML_START_NAMESPACE ||
				chunkType==CHUNK_XML_END_NAMESPACE)
			{
				if (chunkType==CHUNK_XML_START_NAMESPACE) {
					val prefix=reader!!.readInt()
					val uri=reader!!.readInt()
					namespaces.push(prefix,uri)
				} else {
					reader!!.skipInt() //prefix
					reader!!.skipInt() //uri
					namespaces.pop()
				}
				continue
			}

			this@AXmlResourceParser.lineNumber =lineNumber

			when(chunkType) {
				CHUNK_XML_START_TAG -> {
					namespaceUri = reader!!.readInt()
					name = reader!!.readInt()
					reader!!.skipInt() //flags
					var attributeCount = reader!!.readInt()
					attributeCount = attributeCount and 0xFFFF
					classAttribute = reader!!.readInt()
					classAttribute = (classAttribute and 0xFFFF) - 1
					attributes = reader!!.readIntArray(attributeCount * ATTRIBUTE_LENGTH)
					for (i in ATTRIBUTE_IX_VALUE_TYPE until attributes!!.size step ATTRIBUTE_LENGTH) {
						attributes!![i] = attributes!![i] ushr 24
					}
					namespaces.increaseDepth()
					this@AXmlResourceParser.event = START_TAG
					break
				}

				CHUNK_XML_END_TAG -> {
					namespaceUri = reader!!.readInt()
					name = reader!!.readInt()
					this@AXmlResourceParser.event = END_TAG
					decreaseDepth = true
					break
				}

				CHUNK_XML_TEXT -> {
					name = reader!!.readInt()
					reader!!.skipInt()
					reader!!.skipInt()
					this@AXmlResourceParser.event = TEXT
					break
				}
			}
		}
	}

	/////////////////////////////////// data

	/*
	 * All values are essentially indices, e.g. m_name is
	 * an index of name in m_strings.
	 */

	companion object {
		private const val ATTRIBUTE_IX_NAMESPACE_URI = 0
		private const val ATTRIBUTE_IX_NAME = 1
		private const val ATTRIBUTE_IX_VALUE_STRING = 2
		private const val ATTRIBUTE_IX_VALUE_TYPE = 3
		private const val ATTRIBUTE_IX_VALUE_DATA = 4
		private const val ATTRIBUTE_LENGTH = 5

		private const val CHUNK_AXML_FILE = 0x00080003
		private const val CHUNK_RESOURCE_IDS = 0x00080180
		private const val CHUNK_XML_FIRST = 0x00100100
		private const val CHUNK_XML_START_NAMESPACE = 0x00100100
		private const val CHUNK_XML_END_NAMESPACE = 0x00100101
		private const val CHUNK_XML_START_TAG = 0x00100102
		private const val CHUNK_XML_END_TAG = 0x00100103
		private const val CHUNK_XML_TEXT = 0x00100104
		private const val CHUNK_XML_LAST = 0x00100104

		const val START_DOCUMENT = 0
		const val END_DOCUMENT = 1
		const val START_TAG = 2
		const val END_TAG = 3
		const val TEXT = 4
	}
}