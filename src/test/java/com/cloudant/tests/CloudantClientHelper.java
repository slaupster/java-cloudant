/*
 * Copyright (c) 2015 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.cloudant.tests;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.tests.util.SimpleHttpServer;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Rhys Short on 21/05/15.
 */
public abstract class CloudantClientHelper {

    //some tests need access to the URI with user info (e.g. replication)
    public static final String SERVER_URI_WITH_USER_INFO;
    //some tests need access to the credentials (e.g. auth interceptors)
    public static final String COUCH_USERNAME;
    public static final String COUCH_PASSWORD;

    protected static final CloudantClient CLIENT_INSTANCE;

    private static final String COUCH_HOST;
    private static final String COUCH_PORT;
    private static final String HTTP_PROTOCOL;
    private static final URL SERVER_URL;

    static {

        try {
            //a URL might be supplied, otherwise use the separate properties
            String URL = System.getProperty("test.couch.url");
            if (URL != null) {
                URL couch = new URL(URL);
                HTTP_PROTOCOL = couch.getProtocol();
                COUCH_HOST = couch.getHost();
                COUCH_PORT = (couch.getPort() < 0) ? Integer.toString(couch.getPort()) : null;
                String userInfo = couch.getUserInfo();
                if (userInfo != null) {
                    COUCH_USERNAME = userInfo.substring(0, userInfo.indexOf(":"));
                    COUCH_PASSWORD = userInfo.substring(userInfo.indexOf(":") + 1);
                } else {
                    COUCH_USERNAME = null;
                    COUCH_PASSWORD = null;
                }
            } else {
                COUCH_USERNAME = System.getProperty("test.couch.username");
                COUCH_PASSWORD = System.getProperty("test.couch.password");
                COUCH_HOST = System.getProperty("test.couch.host", "localhost");
                COUCH_PORT = System.getProperty("test.couch.port", "5984");
                HTTP_PROTOCOL = System.getProperty("test.couch.http", "http"); //should either be
                // http or https
            }

            //now build the URLs
            SERVER_URL = new URL(HTTP_PROTOCOL + "://"
                    + COUCH_HOST
                    + ((COUCH_PORT != null) ? ":" + COUCH_PORT : "")); //port if supplied
            
            SERVER_URI_WITH_USER_INFO = HTTP_PROTOCOL + "://"
                    + ((COUCH_USERNAME != null) ? COUCH_USERNAME + ":" + COUCH_PASSWORD + "@" : "")
                    + COUCH_HOST
                    + ((COUCH_PORT != null) ? ":" + COUCH_PORT : ""); //port if supplied
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        CLIENT_INSTANCE = getClientBuilder()
                .connectionTimeout(ClientBuilder.TimeoutOption.minutes(1))
                .readTimeout(ClientBuilder.TimeoutOption.minutes(3))
                .build();
    }

    public static CloudantClient getClient() {
        return CLIENT_INSTANCE;
    }

    private static ClientBuilder testAddressClient(boolean isHttpsProtocolClient)
            throws MalformedURLException {
        URL url = null;
        if(isHttpsProtocolClient) {
            url = new URL("https://192.0.2.0");
        } else {
            url = new URL("http://192.0.2.0");
        }
        return ClientBuilder.url(url);
    }

    public static ClientBuilder newHttpsTestAddressClient() throws MalformedURLException {
        return testAddressClient(true);
    }

    public static ClientBuilder newTestAddressClient() throws MalformedURLException {
        return testAddressClient(false);
    }

    public static ClientBuilder newSimpleHttpServerClient(SimpleHttpServer httpServer) throws
            MalformedURLException {
        return ClientBuilder.url(new URL(httpServer.getUrl()));
    }

    public static ClientBuilder getClientBuilder() {
        return ClientBuilder.url(SERVER_URL)
                .username(COUCH_USERNAME)
                .password(COUCH_PASSWORD);
    }

}
