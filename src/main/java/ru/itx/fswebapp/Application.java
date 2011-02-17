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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;

public class Application {

	private static class Handler implements HttpHandler {
		public void handle(HttpExchange request) throws IOException {
			String response =
				"<html><head>"+
				"<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\">"+
				"<meta http-equiv=\"refresh\" content=\"5; url=/\">"+
				"<title>Test page</title>"+
				"</head><body>"+
				"<b>current date/time - "+new Date()+"</b>"+
				"</body></html>";
			request.sendResponseHeaders(200, response.length());
			OutputStream os = request.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

    public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
		server.createContext("/", new Handler());
		server.start();
	}
}
