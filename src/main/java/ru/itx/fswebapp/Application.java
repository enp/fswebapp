/*
 * Copyright (c) 2011 Eugene Prokopiev <enp@itx.ru>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.itx.fswebapp;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.freeswitch.esl.client.inbound.Client;
import org.freeswitch.esl.client.transport.message.EslMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;

public class Application {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Client fs;
	private HttpServer server;

	public Application() throws Exception {
		logger.info("starting freeswitch client ...");
		fs = new Client();
		fs.connect("pbx", 8021, "ClueCon", 2);
		logger.info("starting http server ...");
		server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", new Handler());
		server.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("stopping http server ...");
				server.stop(0);
				logger.info("stopping freeswitch client ...");
				fs.close();
			}
		});
	}

	private class Handler implements HttpHandler {
		public void handle(HttpExchange request) throws IOException {
			logger.info("http request received from "+request.getRemoteAddress());
			EslMessage message = fs.sendSyncApiCommand("sofia", "xmlstatus profile stc");
			String messageBody = "", htmlUsers = "";
			for (String messageBodyLine : message.getBodyLines())
				messageBody += messageBodyLine;
			try {
				Document document = DocumentHelper.parseText(messageBody);
				for(Node node : (List<Node>)document.selectNodes("/profile/registrations/registration/user"))
					htmlUsers += "<li>"+node.getText()+"</li>";
				String response =
					"<html><head>"+
					"<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\">"+
					"<meta http-equiv=\"refresh\" content=\"5; url=/\">"+
					"<title>Test page</title>"+
					"</head><body><ul>"+htmlUsers+"</ul></body></html>";
				request.sendResponseHeaders(200, response.length());
				OutputStream os = request.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Application();
	}
}
