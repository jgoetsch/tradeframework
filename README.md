tradeframework
==============

A Java API for automated trading, market data, and historical data retrieval.

It is modeled after the Interactive Brokers API and a base implementation is provided
to connect to IB through Trader Workstation (TWS). However, implementations for other
brokers or data sources could be implemented as well providing an abstraction for
automated trading programs from any specific broker or data service.

Features include
 * Simple blocking calls for data snapshots as well as a nonblocking subscription model
   for streaming data.
 * Separate interfaces for the various capabilities that an implementation might offer,
   such as trading, market data, account data, historical data, and fundamental data.
 * Various forms of simulated implementations useful for historical back-testing or paper
   trading strategies as well as unit testing application code.

Component projects
-----------

### tradeframework-core
The API interfaces, common classes and simulated implementations.

### tradeframework-ib
Interactive Brokers TWS API implementation.

<b>Note:</b> Has a dependency on TWS API version 9.76, which must be downloaded from 
https://interactivebrokers.github.io and installed into the maven local repository
(using `mvn install` command) or made available in a private repository, as IB does
not publish the API artifacts on any public repositories.

### tradeframework-yahoo
HistoricalDataSource implementation which fetches historical data at a daily
or greater level from csv web service available at Yahoo Finance.

### event-trader
An application framework built on tradeframework for quickly building systems that
monitor any type of source for a message of some kind and perform an action based
on the message.

License
-------
Copyright (c) 2012 Jeremy Goetsch
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Any securities, symbols, trades, or ideas referenced within any of the files
herein are for illustrative purposes only and are not intended to be recommendations
of any kind.
