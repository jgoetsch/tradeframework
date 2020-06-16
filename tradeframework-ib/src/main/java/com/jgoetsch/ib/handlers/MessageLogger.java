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
package com.jgoetsch.ib.handlers;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.TickType;
import com.jgoetsch.ib.TWSMapper;

/**
 * Logs responses received from TWS to various loggers at the DEBUG level.
 * This handler is automatically registered so you can get logging output just by
 * enabling DEBUG logging on com.jgoetsch.ib.handlers.MessageLogger or its
 * sublevels.
 * 
 * @author jgoetsch
 *
 */
public class MessageLogger {

	private static final Logger log = LoggerFactory.getLogger(MessageLogger.class);
	private static final TWSMapper mapper = TWSMapper.INSTANCE;

	private static final Class<EWrapper> wrapperInterface = EWrapper.class;

	private static final String PARAM_SEPARATOR = ", ";
	private static final String PARAM_VALUE_SEPARATOR = ":";
	private static final Set<Object> SUPPRESSED_VALUES = Set.of(0, 0d, -1);

	// EWrapper interface from tws api may not have been compiled with parameter name info,
	// so get them from BaseHandler implementation class
	private static String[] handlerArgumentNames(Method m) {
		try {
			return Arrays.stream(BaseHandler.class.getDeclaredMethod(m.getName(), m.getParameterTypes()).getParameters())
					.map(Parameter::getName).toArray(String[]::new);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Interface method not implemented in BaseHandler", e);
		}
	}

	public static EWrapper createLoggingHandler() {
		Map<Method, String[]> methodArgNames = Arrays.stream(wrapperInterface.getDeclaredMethods()).collect(
				Collectors.toMap(Function.identity(), MessageLogger::handlerArgumentNames));

		return (EWrapper) Proxy.newProxyInstance(
				wrapperInterface.getClassLoader(), new Class[] { wrapperInterface },
			(proxy, method, args) -> {
				if (log.isDebugEnabled()) {
					try {
						StringBuilder sb = new StringBuilder();
						if (args != null) {
							String[] argNames = methodArgNames.get(method);
							for (int i = 0; i < args.length; i++) {
								if (args[i] != null && !SUPPRESSED_VALUES.contains(args[i])) {
									String argVal;
									if (args[i] instanceof Contract)
										argVal = mapper.fromTWSContract((Contract)args[i]).toString();
									else if (args[i] instanceof Order)
										argVal = mapper.fromTWSOrder((Order)args[i]).toString();
									else if (args[i] instanceof Execution)
										argVal = mapper.fromTWSExecution((Execution)args[i]).toString();
									else if (args[i] instanceof OrderState)
										argVal = ((OrderState)args[i]).getStatus();
									else if (args[i] instanceof CommissionReport) {
										argVal = mapper.fromTWSCommissionReport((CommissionReport)args[i]).toString();
									}
									else if (args[i] instanceof Integer && argNames[i].equals("field"))
										argVal = TickType.getField((Integer)args[i]);
									else
										argVal = args[i].toString();
		
									if (argVal != null && !argVal.isBlank()) {
										if (sb.length() > 0)
											sb.append(PARAM_SEPARATOR);
										sb.append(argNames[i]).append(PARAM_VALUE_SEPARATOR);
										sb.append(argVal);
									}
								}
							}
						}
						log.debug("{} {}", method.getName(), sb.toString());
					} catch (Exception e) {
						log.error("Exception logging message", e);
					}
				}
				return null;
			}
		);
	}

}
