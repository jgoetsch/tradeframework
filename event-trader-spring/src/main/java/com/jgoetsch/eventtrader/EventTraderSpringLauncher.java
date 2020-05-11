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
package com.jgoetsch.eventtrader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A simple launcher class with a main method to launch EventTrader apps defined in
 * Spring bean xml files.
 * 
 * @author jgoetsch
 *
 */
public class EventTraderSpringLauncher {
	private static Logger log = LoggerFactory.getLogger(EventTraderSpringLauncher.class);

	public static void main(String[] a) {
		List<String> args = a.length < 1 ? Arrays.asList("config/*.xml") : Arrays.asList(a);
		if (args.get(0).equalsIgnoreCase("-h") || args.get(0).equalsIgnoreCase("help")) {
			System.out.println("Usage: " + EventTraderSpringLauncher.class.getSimpleName() + " <files>...");
			System.out.println("       files - List of paths to spring bean definition xml files.");
			System.out.println("               Each object defined that implements Runnable will be executed");
			System.out.println("               in its own thread.");
		}
		else {
			args.forEach(arg -> log.info("Using bean configs: {}", arg));
			AbstractApplicationContext context = new ClassPathXmlApplicationContext(args.toArray(new String[0]));

			// auto register growl notifications after all GrowlNotification objects have been instantiated
			// if it is found on the classpath
			try {
				Class.forName("com.jgoetsch.eventtrader.processor.GrowlNotification").getMethod("autoRegister").invoke(null);
			} catch (Exception e) {
				log.warn("Growl not found, cannot autoRegister notifications: {}", e.getMessage());
			}

			Map<String, Runnable> runnables = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, Runnable.class);
			List<Thread> threads = new ArrayList<Thread>(runnables.size());
			for (final Map.Entry<String, Runnable> runner : runnables.entrySet()) {
				log.info("Running bean {} [{}]", runner.getKey(), runner.getValue().getClass().getSimpleName());
				final Thread th = new Thread(runner.getValue(), runner.getKey());
				threads.add(th);
				th.start();
			}

			// close spring context on JVM shutdown
			// this causes all @PreDestroy methods in the runnables to be called to allow for
			// them to shutdown gracefully
			context.registerShutdownHook();

			// wait for launched threads to finish before cleaning up beans
			for (Thread th : threads) {
				try {
					th.join();
				} catch (InterruptedException e) { }
			}
		}
	}

}
