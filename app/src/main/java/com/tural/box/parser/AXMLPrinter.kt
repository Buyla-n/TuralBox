package com.tural.box.parser

import com.tural.box.parser.AXmlResourceParser
import com.tural.box.parser.TypedValue
import java.io.FileInputStream

/**
 * @author Dmitry Skiba
 */
object AXMLPrinter {

	const val START_DOCUMENT = 0
	const val END_DOCUMENT = 1
	const val START_TAG = 2
	const val END_TAG = 3
	const val TEXT = 4

	fun print(path: String): String {
		if (path.isEmpty()) {
			return "Err: Path is empty"
        }
		val result = StringBuilder()
		try {
			val parser= AXmlResourceParser()
			parser.open(FileInputStream(path))
            val indent= StringBuilder(10)
			val indentStep = "\t"
			var type: Int
            while (true) {
				type = parser.next()
                if (type== END_DOCUMENT) {
					break
                }
				when (type) {
					START_DOCUMENT -> {
						result.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
					}

					START_TAG -> {
						result.append(
							String.format(
								"%s<%s%s\n",
								indent,
								getNamespacePrefix(parser.getPrefix()),
								parser.getName()
							)
						)
						indent.append(indentStep)

						val namespaceCountBefore = parser.getNamespaceCount(parser.getDepth())
						val namespaceCount = parser.getNamespaceCount(parser.getDepth() - 1)
						for (i in namespaceCountBefore until namespaceCount) {
							result.append(
								String.format(
									"%sxmlns:%s=\"%s\"\n",
									indent,
									parser.getNamespacePrefix(i),
									parser.getNamespaceUri(i)
								)
							)
						}

						for (i in 0 until parser.getAttributeCount()) {
							result.append(
								String.format(
									"%s%s%s=\"%s\"",
									indent,
									getNamespacePrefix(parser.getAttributePrefix(i)),
									parser.getAttributeName(i),
									getAttributeValue(parser, i)
								)
							)
							if (i < parser.getAttributeCount() - 1) {
								result.append("\n")
							}
						}
						result.append(">\n")
					}

					END_TAG -> {
						indent.setLength(indent.length - indentStep.length)
						result.append(
							String.format(
								"%s</%s%s>\n",
								indent,
								getNamespacePrefix(parser.getPrefix()),
								parser.getName()
							)
						)
					}

					TEXT -> {
						result.append(String.format("%s%s\n", indent, parser.getText()))
					}
				}
			}
			return result.toString()
        }
		catch (e: Exception) {
			return e.toString()
        }
	}

	private fun getNamespacePrefix(prefix: String?): String {
		return if (prefix.isNullOrEmpty()) { "" } else "$prefix:"
	}

	private fun getAttributeValue(parser: AXmlResourceParser, index: Int): String? {
		val type=parser.getAttributeValueType(index)
        val data=parser.getAttributeValueData(index)
        if (type== TypedValue.Companion.TYPE_STRING) {
			return parser.getAttributeValue(index)
        }
		if (type== TypedValue.Companion.TYPE_ATTRIBUTE) {
			return String.format("?%s%08X",getPackage(data),data)
        }
		if (type== TypedValue.Companion.TYPE_REFERENCE) {
			return String.format("@%s%08X",getPackage(data),data)
        }
		if (type== TypedValue.Companion.TYPE_FLOAT) {
			return Float.fromBits(data).toString()
		}
		if (type== TypedValue.Companion.TYPE_INT_HEX) {
			return String.format("0x%08X",data)
        }
		if (type == TypedValue.Companion.TYPE_INT_BOOLEAN) {
			return if (data != 0) "true" else "false"
		}
		if (type== TypedValue.Companion.TYPE_DIMENSION) {
			return complexToFloat(data).toString() + DIMENSION_UNITS[data and TypedValue.Companion.COMPLEX_UNIT_MASK]
		}
		if (type== TypedValue.Companion.TYPE_FRACTION) {
			return complexToFloat(data).toString() + FRACTION_UNITS[data and TypedValue.Companion.COMPLEX_UNIT_MASK]
		}
		if (type>= TypedValue.Companion.TYPE_FIRST_COLOR_INT && type<= TypedValue.Companion.TYPE_LAST_COLOR_INT) {
			return String.format("#%08X",data)
        }
		if (type>= TypedValue.Companion.TYPE_FIRST_INT && type<= TypedValue.Companion.TYPE_LAST_INT) {
			return data.toString()
		}
		return String.format("<0x%X, type 0x%02X>",data,type)
    }

	private fun getPackage(id: Int): String {
		return if (id ushr 24 == 1) { "android:" } else ""
    }

	//ILLEGAL STUFF, DONT LOOK :)
	//OKAY, I WONT LOOK (^~^)

	fun complexToFloat(complex: Int): Float {
		return (complex and 0xFFFFFF00.toInt()).toFloat() * RADIX_MULTS[(complex shr 4) and 3]
	}

	private val RADIX_MULTS = floatArrayOf(
		0.00390625F,
		3.051758E-005F,
		1.192093E-007F,
		4.656613E-010F
	)

	private val DIMENSION_UNITS = arrayOf(
		"px", "dp", "sp", "pt", "in", "mm", "", ""
	)

	private val FRACTION_UNITS = arrayOf(
		"%", "%p", "", "", "", "", "", ""
	)
}