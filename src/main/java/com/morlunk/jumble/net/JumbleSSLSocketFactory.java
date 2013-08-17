/*
 * Copyright (C) 2013 Andrew Comminos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.morlunk.jumble.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLSocket;

import ch.boye.httpclientandroidlib.conn.ssl.SSLSocketFactory;
import ch.boye.httpclientandroidlib.params.BasicHttpParams;
import ch.boye.httpclientandroidlib.params.HttpParams;
import socks.Socks5Proxy;
import socks.SocksSocket;

public class JumbleSSLSocketFactory {
    private SSLSocketFactory mSocketFactory;

    public JumbleSSLSocketFactory(KeyStore keystore, String keystorePassword) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException,
            UnrecoverableKeyException, NoSuchProviderException {
        mSocketFactory = new SSLSocketFactory(SSLSocketFactory.TLS,
                keystore,
                keystorePassword,
                null,
                new SecureRandom(),
                SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // No need for trust store at the moment. FIXME security
    }

    /**
     * Creates a new SSLSocket that runs through a SOCKS5 proxy to reach its destination.
     */
    public SSLSocket createTorSocket(String host, int port, String proxyHost, int proxyPort) throws IOException {
        Socks5Proxy proxy = new Socks5Proxy(proxyHost, proxyPort);
        proxy.resolveAddrLocally(false); // Let SOCKS5 proxy resolve host. Useful for Tor.
        SocksSocket socksSocket = new SocksSocket(proxy, host, port);
        return (SSLSocket) mSocketFactory.createLayeredSocket(socksSocket, proxyHost, proxyPort, new BasicHttpParams());
    }

    public SSLSocket createSocket(String host, int port) throws IOException {
        HttpParams params = new BasicHttpParams();
        Socket socket = mSocketFactory.createSocket(params);
        return (SSLSocket) mSocketFactory.connectSocket(socket, new InetSocketAddress(host, port), null, params);
    }
}