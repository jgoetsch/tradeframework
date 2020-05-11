/*
 * Copyright (c) 2012 Jeremy Goetsch
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
package com.jgoetsch.eventtrader.source.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.jgoetsch.eventtrader.source.MsgHandler;

public class FullBufferingMsgParser implements MsgParser {
	private int maxLength = -1;
	
	private final BufferedMsgParser bufferedMsgParser;
	
	public FullBufferingMsgParser(BufferedMsgParser bufferedMsgParser) {
		this.bufferedMsgParser = bufferedMsgParser;
	}

	public boolean parseContent(InputStream input, long length, String contentType, MsgHandler handler) throws IOException, MsgParseException {
        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("entity too large to be buffered in memory");
        }
        int i = (int)length;
        if (i < 0)
        	i = 4096;
        Reader reader = new InputStreamReader(input);
        //CharArrayBuffer buffer = new CharArrayBuffer((int)length);
        StringBuilder buffer = new StringBuilder(length > 0 ? (int)length : 4096);
        try {
            char[] tmp = new char[1024];
            int l;
            while((l = reader.read(tmp)) != -1) {
                buffer.append(tmp, 0, l);
            }
        } finally {
            reader.close();
        }
        if (maxLength > -1)
        	buffer.setLength(maxLength);
        return bufferedMsgParser.parseContent(buffer.toString(), contentType, handler);
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public int getMaxLength() {
		return maxLength;
	}

}
