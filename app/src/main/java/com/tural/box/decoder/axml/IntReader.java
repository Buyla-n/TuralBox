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
package com.tural.box.decoder.axml;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dmitry Skiba
 * <p>
 * Simple helper class that allows reading of integers.
 * <p>
 */
public final class IntReader {

	public IntReader(InputStream stream, boolean bigEndian) {
		reset(stream, bigEndian);
	}

	public void reset(InputStream stream, boolean bigEndian) {
		m_stream = stream;
		m_bigEndian = bigEndian;
	}

	public void close() {
		if (m_stream == null) {
			return;
		}
		try {
			m_stream.close();
		} catch (IOException ignored) {
		}
		reset(null, false);
	}

	public int readInt() throws IOException {
		int length = 4;
		int result = 0;
		if (m_bigEndian) {
			for (int i = (length - 1) * 8; i >= 0; i -= 8) {
				int b = m_stream.read();
				if (b == -1) {
					throw new EOFException();
				}
				result |= (b << i);
			}
		} else {
			length *= 8;
			for (int i = 0; i != length; i += 8) {
				int b = m_stream.read();
				if (b == -1) {
					throw new EOFException();
				}
				result |= (b << i);
			}
		}
		return result;
	}

	public int[] readIntArray(int length) throws IOException {
		int[] array=new int[length];
		readIntArray(array,0,length);
		return array;
	}

	public void readIntArray(int[] array, int offset, int length) throws IOException {
		for (;length>0;length-=1) {
			array[offset++]=readInt();
		}
	}

	public void skipInt() throws IOException {
		long skipped = m_stream.skip(4);
		if (skipped != 4) {
			throw new EOFException();
		}
	}

	private InputStream m_stream;
	private boolean m_bigEndian;
}
