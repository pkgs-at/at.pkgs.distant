/*
 * Copyright (c) 2009-2015, Architector Inc., Japan
 * All rights reserved.
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

package at.pkgs.web.client;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HostnameVerifier;

public class Connections {

	private static final boolean disableCertificateVerify =
			Boolean.getBoolean(
					Connections.class.getName() +
					".disableCertificateVerify");

	public static HttpsURLConnection disableCertificateVerify(
			HttpsURLConnection connection) {
		try {
			SSLContext context;

			context = SSLContext.getInstance("SSL");
			context.init(
					null,
					new X509TrustManager[] {
							new X509TrustManager() {

								@Override
								public void checkClientTrusted(
										X509Certificate[] chain,
										String authType)
												throws CertificateException {
									// do nothing
								}

								@Override
								public void checkServerTrusted(
										X509Certificate[] chain,
										String authType)
												throws CertificateException {
									// do nothing
								}

								@Override
								public X509Certificate[] getAcceptedIssuers() {
									return null;
								}

							},
					},
					new SecureRandom());
			connection.setSSLSocketFactory(context.getSocketFactory());
			connection.setHostnameVerifier(new HostnameVerifier() {

				@Override
				public boolean verify(
						String hostname,
						SSLSession session) {
					return true;
				}

			});
			return connection;
		}
		catch (RuntimeException cause) {
			throw cause;
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
	}

	public static HttpURLConnection disableCertificateVerify(
			HttpURLConnection connection) {
		if (connection instanceof HttpsURLConnection)
			return Connections.disableCertificateVerify(
					(HttpsURLConnection)connection);
		else
			return connection;
	}

	public static URLConnection disableCertificateVerify(
			URLConnection connection) {
		if (connection instanceof HttpsURLConnection)
			return Connections.disableCertificateVerify(
					(HttpsURLConnection)connection);
		else
			return connection;
	}

	public static HttpURLConnection openHttpURLConnection(
			String location)
					throws IOException {
		HttpURLConnection connection;

		connection = (HttpURLConnection)new URL(location).openConnection();
		if (Connections.disableCertificateVerify)
			Connections.disableCertificateVerify(connection);
		return connection;
	}

}
