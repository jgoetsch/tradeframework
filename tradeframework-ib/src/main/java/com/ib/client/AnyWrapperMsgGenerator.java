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
package com.ib.client;

public class AnyWrapperMsgGenerator {
    public static String error( Exception ex) { return "Error - " + ex;}
    public static String error( String str) { return str;}

	public static String error(int id, int errorCode, String errorMsg) {
		String err = Integer.toString(id);
        err += " | ";
        err += Integer.toString(errorCode);
        err += " | ";
        err += errorMsg;
        return err;
	}

	public static String connectionClosed() {
		return "Connection Closed";
	}
}
