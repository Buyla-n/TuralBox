package com.tural.box.parser

/**
 * This exception is thrown to signal XML Pull Parser related faults.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
open class XmlPullParserException(
    message: String?,
    parser: AXmlResourceParser?,
    chain: Throwable?
) : Exception(
    "${message ?: ""} " +
            (parser?.let { "(position:${it.getPositionDescription()}) " } ?: "") +
            (chain?.let { "caused by: $it" } ?: "")
) {
    protected var detail: Throwable? = chain
    protected var row: Int = -1
    protected var column: Int = -1

    init {
        parser?.let {
            row = it.getLineNumber()
            column = it.getColumnNumber()
        }
        detail = chain
    }

    override fun printStackTrace() {
        if (detail == null) {
            super.printStackTrace()
        } else {
            synchronized(System.err) {
                System.err.println("${super.message}; nested exception is:")
                detail?.printStackTrace()
            }
        }
    }
}