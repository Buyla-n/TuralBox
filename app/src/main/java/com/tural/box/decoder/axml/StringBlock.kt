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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Dmitry Skiba
 * <p>
 * Block of strings, used in binary xml and arsc.
 * <p>
 */
public class StringBlock {
	public static StringBlock read(IntReader reader) throws IOException {
		readCheckType(reader, CHUNK_TYPE);
		int chunkSize = reader.readInt();
		int stringCount = reader.readInt();
		reader.skipInt();
		int flag = reader.readInt();
		int stringsOffset = reader.readInt();
		reader.skipInt();

		StringBlock block = new StringBlock();
		block.isUtf8 = flag != 0;
		block.m_stringOffsets = reader.readIntArray(stringCount);
		{
			int size = chunkSize - stringsOffset;
			if ((size % 4) != 0) {
				throw new IOException("String data size is not multiple of 4 (" + size + ").");
			}
			block.m_strings = reader.readIntArray(size / 4);
		}

		return block;
	}

	public String getString(int index) {
		if (index < 0 || m_stringOffsets == null || index >= m_stringOffsets.length) {
			return null;
		}
		int offset = m_stringOffsets[index];
		int firstInt = m_strings[offset / 4];
		if (this.isUtf8) {
			// 1. 正确读取2字节长度（小端序）
			int lengthWord = m_strings[offset / 4];
			int bytePos = offset % 4;
// 低字节 (LSB)
			int lowByte = (lengthWord >> (8 * bytePos)) & 0xFF;
// 高字节 (MSB) - 注意offset+1可能跨int边界
			offset++;
			int highByte;
			if (offset % 4 != 0) { // 未跨int边界
				highByte = (lengthWord >> (8 * (offset % 4))) & 0xFF;
			} else { // 已跨边界
				highByte = m_strings[offset / 4] & 0xFF; // 新int的最低字节
			}
			int byteLength = (highByte << 8) | lowByte; // 组合为16位长度
			offset++; // 长度字段共跳过2字节

// 2. 按大端序读取UTF-8字节（修复跨边界问题）
			byte[] utf8Bytes = new byte[byteLength];
			int bytesCopied = 0;
			while (bytesCopied < byteLength) {
				int word = m_strings[offset / 4];
				// 计算当前int中有效字节数（避免越界）
				int bytesInWord = Math.min(4 - (offset % 4), byteLength - bytesCopied);
				// 按大端序提取字节（高位字节在低地址）
				for (int i = 0; i < bytesInWord; i++) {
					int shift = 24 - 8 * ((offset % 4) + i); // 大端序移位
					utf8Bytes[bytesCopied++] = (byte) ((word >> shift) & 0xFF);
				}
				offset += bytesInWord; // 按实际读取字节数推进
			}

			try {
				return new String(utf8Bytes, StandardCharsets.UTF_8);
			} catch (Exception e) {
				return null;
			}
        } else {
			// origin utf16 decoder
			int length = getShort(m_strings, offset);
			StringBuilder result = new StringBuilder(length);
			for (; length != 0; length -= 1) {
				offset += 2;
				result.append((char) getShort(m_strings, offset));
			}
			return result.toString();
		}
	}

	private StringBlock() {
	}

	private static int getShort(int[] array, int offset) {
		int value = array[offset / 4];
		if ((offset % 4) / 2 == 0) {
			return (value & 0xFFFF);
		} else {
			return (value >>> 16);
		}
	}

	private int[] m_stringOffsets;
	private int[] m_strings;
	private boolean isUtf8;
	private static final int CHUNK_TYPE=0x001C0001;
}
