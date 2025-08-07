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
package com.tural.box.decoder.axml;

import static com.tural.box.decoder.axml.ChunkUtilKt.readCheckType;
import static com.tural.box.decoder.axml.XmlPullParser.END_DOCUMENT;
import static com.tural.box.decoder.axml.XmlPullParser.END_TAG;
import static com.tural.box.decoder.axml.XmlPullParser.START_DOCUMENT;
import static com.tural.box.decoder.axml.XmlPullParser.START_TAG;
import static com.tural.box.decoder.axml.XmlPullParser.TEXT;

import java.io.IOException;
import java.io.InputStream;

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
public class AXmlResourceParser {

	public AXmlResourceParser() {
		resetEventInfo();
	}

	public void open(InputStream stream) {
		close();
		if (stream!=null) {
			m_reader=new IntReader(stream,false);
		}
	}

	public void close() {
		if (!m_operational) {
			return;
		}
		m_operational=false;
		m_reader.close();
		m_reader=null;
		m_strings=null;
		m_namespaces.reset();
		resetEventInfo();
	}

	/////////////////////////////////// iteration

	public int next() throws XmlPullParserException,IOException {
		if (m_reader==null) {
			throw new XmlPullParserException("Parser is not opened.",this,null);
		}
		try {
			doNext();
			return m_event;
		}
		catch (IOException e) {
			close();
			throw e;
		}
	}

	public int getDepth() {
		return m_namespaces.getDepth()-1;
	}

	public int getLineNumber() {
		return m_lineNumber;
	}

	public String getName() {
		if (m_name==-1 || (m_event!=START_TAG && m_event!=END_TAG)) {
			return null;
		}
		return m_strings.getString(m_name);
	}

	public String getText() {
		if (m_name==-1 || m_event!=TEXT) {
			return null;
		}
		return m_strings.getString(m_name);
	}

	public String getPrefix() {
		int prefix=m_namespaces.findPrefix(m_namespaceUri);
		return m_strings.getString(prefix);
	}

	public String getPositionDescription() {
		return "XML line #"+getLineNumber();
	}

	public int getNamespaceCount(int depth) {
		return m_namespaces.getAccumulatedCount(depth);
	}

	public String getNamespacePrefix(int pos) {
		int prefix=m_namespaces.getPrefix(pos);
		return m_strings.getString(prefix);
	}

	public String getNamespaceUri(int pos) {
		int uri=m_namespaces.getUri(pos);
		return m_strings.getString(uri);
	}

	/////////////////////////////////// attributes

	public int getAttributeCount() {
		if (m_event!=START_TAG) {
			return -1;
		}
		return m_attributes.length/ATTRIBUTE_LENGHT;
	}

	public String getAttributePrefix(int index) {
		int offset=getAttributeOffset(index);
		int uri=m_attributes[offset+ATTRIBUTE_IX_NAMESPACE_URI];
		int prefix=m_namespaces.findPrefix(uri);
		if (prefix==-1) {
			return "";
		}
		return m_strings.getString(prefix);
	}

	public String getAttributeName(int index) {
		int offset=getAttributeOffset(index);
		int name=m_attributes[offset+ATTRIBUTE_IX_NAME];
		if (name==-1) {
			return "";
		}
		return m_strings.getString(name);
	}

	public int getAttributeValueType(int index) {
		int offset=getAttributeOffset(index);
		return m_attributes[offset+ATTRIBUTE_IX_VALUE_TYPE];
	}

	public int getAttributeValueData(int index) {
		int offset=getAttributeOffset(index);
		return m_attributes[offset+ATTRIBUTE_IX_VALUE_DATA];
	}

	public String getAttributeValue(int index) {
		int offset=getAttributeOffset(index);
		int valueType=m_attributes[offset+ATTRIBUTE_IX_VALUE_TYPE];
		if (valueType== TypedValue.TYPE_STRING) {
			int valueString=m_attributes[offset+ATTRIBUTE_IX_VALUE_STRING];
			return m_strings.getString(valueString);
		}
        return "";//TypedValue.coerceToString(valueType,valueData);
	}

	public int getColumnNumber() {
		return -1;
	}

	private static final class NamespaceStack {
		public NamespaceStack() {
			m_data=new int[32];
		}

		public void reset() {
			m_dataLength=0;
			m_depth=0;
		}

		public int getCurrentCount() {
			if (m_dataLength==0) {
				return 0;
			}
			int offset=m_dataLength-1;
			return m_data[offset];
		}

		public int getAccumulatedCount(int depth) {
			if (m_dataLength==0 || depth<0) {
				return 0;
			}
			if (depth>m_depth) {
				depth=m_depth;
			}
			int accumulatedCount=0;
			int offset=0;
			for (;depth!=0;--depth) {
				int count=m_data[offset];
				accumulatedCount+=count;
				offset+=(2+count*2);
			}
			return accumulatedCount;
		}

		public void push(int prefix, int uri) {
			if (m_depth==0) {
				increaseDepth();
			}
			ensureDataCapacity();
			int offset=m_dataLength-1;
			int count=m_data[offset];
			m_data[offset-1-count*2]=count+1;
			m_data[offset]=prefix;
			m_data[offset+1]=uri;
			m_data[offset+2]=count+1;
			m_dataLength+=2;
		}

		public void pop() {
			if (m_dataLength==0) {
				return;
			}
			int offset=m_dataLength-1;
			int count=m_data[offset];
			if (count==0) {
				return;
			}
			count-=1;
			offset-=2;
			m_data[offset]=count;
			offset-=(1+count*2);
			m_data[offset]=count;
			m_dataLength-=2;
		}

		public int getPrefix(int index) {
			return get(index,true);
		}

		public int getUri(int index) {
			return get(index,false);
		}

		public int findPrefix(int uri) {
			return find(uri);
		}

		public int getDepth() {
			return m_depth;
		}

		public void increaseDepth() {
			ensureDataCapacity();
			int offset=m_dataLength;
			m_data[offset]=0;
			m_data[offset+1]=0;
			m_dataLength+=2;
			m_depth+=1;
		}
		public void decreaseDepth() {
			if (m_dataLength==0) {
				return;
			}
			int offset=m_dataLength-1;
			int count=m_data[offset];
			if ((offset-1-count*2)==0) {
				return;
			}
			m_dataLength-=2+count*2;
			m_depth-=1;
		}

		private void ensureDataCapacity() {
			int available=(m_data.length-m_dataLength);
			if (available> 2) {
				return;
			}
			int newLength=(m_data.length+available)*2;
			int[] newData=new int[newLength];
			System.arraycopy(m_data,0,newData,0,m_dataLength);
			m_data=newData;
		}

		private int find(int prefixOrUri) {
			if (m_dataLength==0) {
				return -1;
			}
			int offset=m_dataLength-1;
			for (int i=m_depth;i!=0;--i) {
				int count=m_data[offset];
				offset-=2;
				for (;count!=0;--count) {
                    if (m_data[offset + 1] == prefixOrUri) {
                        return m_data[offset];
                    }
                    offset-=2;
				}
			}
			return -1;
		}

		private int get(int index, boolean prefix) {
			if (m_dataLength==0 || index<0) {
				return -1;
			}
			int offset=0;
			for (int i=m_depth;i!=0;--i) {
				int count=m_data[offset];
				if (index>=count) {
					index-=count;
					offset+=(2+count*2);
					continue;
				}
				offset+=(1+index*2);
				if (!prefix) {
					offset+=1;
				}
				return m_data[offset];
			}
			return -1;
		}

		private int[] m_data;
		private int m_dataLength;
		private int m_depth;
	}

	private int getAttributeOffset(int index) {
		if (m_event!=START_TAG) {
			throw new IndexOutOfBoundsException("Current event is not START_TAG.");
		}
		int offset=index*5;
		if (offset>=m_attributes.length) {
			throw new IndexOutOfBoundsException("Invalid attribute index ("+index+").");
		}
		return offset;
	}

	private void resetEventInfo() {
		m_event=-1;
		m_lineNumber=-1;
		m_name=-1;
		m_namespaceUri=-1;
		m_attributes=null;
		m_classAttribute=-1;
	}

	private void doNext() throws IOException {
		// Delayed initialization.
		if (m_strings==null) {
			readCheckType(m_reader,CHUNK_AXML_FILE);
			/*chunkSize*/m_reader.skipInt();
			m_strings= StringBlock.read(m_reader);
			m_namespaces.increaseDepth();
			m_operational=true;
		}

		if (m_event==END_DOCUMENT) {
			return;
		}

		int event=m_event;
		resetEventInfo();

		while (true) {
			if (m_decreaseDepth) {
				m_decreaseDepth=false;
				m_namespaces.decreaseDepth();
			}

			// Fake END_DOCUMENT event.
			if (event==END_TAG &&
				m_namespaces.getDepth()==1 &&
				m_namespaces.getCurrentCount()==0)
			{
				m_event=END_DOCUMENT;
				break;
			}

			int chunkType;
			if (event==START_DOCUMENT) {
				// Fake event, see CHUNK_XML_START_TAG handler.
				chunkType=CHUNK_XML_START_TAG;
			} else {
				chunkType=m_reader.readInt();
			}

			if (chunkType==CHUNK_RESOURCEIDS) {
				int chunkSize=m_reader.readInt();
				if (chunkSize<8 || (chunkSize%4)!=0) {
					throw new IOException("Invalid resource ids size ("+chunkSize+").");
				}
				m_reader.readIntArray(chunkSize/4-2);
				continue;
			}

			if (chunkType<CHUNK_XML_FIRST || chunkType>CHUNK_XML_LAST) {
				throw new IOException("Invalid chunk type ("+chunkType+").");
			}

			// Fake START_DOCUMENT event.
			if (chunkType==CHUNK_XML_START_TAG && event==-1) {
				m_event=START_DOCUMENT;
				break;
			}

			// Common header.
			/*chunkSize*/m_reader.skipInt();
			int lineNumber=m_reader.readInt();
			/*0xFFFFFFFF*/m_reader.skipInt();

			if (chunkType==CHUNK_XML_START_NAMESPACE ||
				chunkType==CHUNK_XML_END_NAMESPACE)
			{
				if (chunkType==CHUNK_XML_START_NAMESPACE) {
					int prefix=m_reader.readInt();
					int uri=m_reader.readInt();
					m_namespaces.push(prefix,uri);
				} else {
					/*prefix*/m_reader.skipInt();
					/*uri*/m_reader.skipInt();
					m_namespaces.pop();
				}
				continue;
			}

			m_lineNumber=lineNumber;

			if (chunkType==CHUNK_XML_START_TAG) {
				m_namespaceUri=m_reader.readInt();
				m_name=m_reader.readInt();
				/*flags?*/m_reader.skipInt();
				int attributeCount=m_reader.readInt();
				attributeCount&=0xFFFF;
				m_classAttribute=m_reader.readInt();
				m_classAttribute=(m_classAttribute & 0xFFFF)-1;
				m_attributes=m_reader.readIntArray(attributeCount*ATTRIBUTE_LENGHT);
				for (int i=ATTRIBUTE_IX_VALUE_TYPE;i<m_attributes.length;) {
					m_attributes[i]=(m_attributes[i]>>>24);
					i+=ATTRIBUTE_LENGHT;
				}
				m_namespaces.increaseDepth();
				m_event=START_TAG;
				break;
			}

			if (chunkType==CHUNK_XML_END_TAG) {
				m_namespaceUri=m_reader.readInt();
				m_name=m_reader.readInt();
				m_event=END_TAG;
				m_decreaseDepth=true;
				break;
			}

			if (chunkType==CHUNK_XML_TEXT) {
				m_name=m_reader.readInt();
				/*?*/m_reader.skipInt();
				/*?*/m_reader.skipInt();
				m_event=TEXT;
				break;
			}
		}
	}

	/////////////////////////////////// data

	/*
	 * All values are essentially indices, e.g. m_name is
	 * an index of name in m_strings.
	 */

	private IntReader m_reader;
	private boolean m_operational=false;
	private StringBlock m_strings;
	private final NamespaceStack m_namespaces=new NamespaceStack();
	private boolean m_decreaseDepth;
	private int m_event;
	private int m_lineNumber;
	private int m_name;
	private int m_namespaceUri;
	private int[] m_attributes;
	private int m_classAttribute;

	private static final int
		ATTRIBUTE_IX_NAMESPACE_URI	=0,
		ATTRIBUTE_IX_NAME			=1,
		ATTRIBUTE_IX_VALUE_STRING	=2,
		ATTRIBUTE_IX_VALUE_TYPE		=3,
		ATTRIBUTE_IX_VALUE_DATA		=4,
		ATTRIBUTE_LENGHT			=5;

	private static final int
		CHUNK_AXML_FILE				=0x00080003,
		CHUNK_RESOURCEIDS			=0x00080180,
		CHUNK_XML_FIRST				=0x00100100,
		CHUNK_XML_START_NAMESPACE	=0x00100100,
		CHUNK_XML_END_NAMESPACE		=0x00100101,
		CHUNK_XML_START_TAG			=0x00100102,
		CHUNK_XML_END_TAG			=0x00100103,
		CHUNK_XML_TEXT				=0x00100104,
		CHUNK_XML_LAST				=0x00100104;
}
