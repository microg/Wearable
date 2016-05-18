/*
 * Copyright 2013-2015 microG Project Team
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

package org.microg.wearable.tools;

import org.microg.wearable.SocketWearableConnection;
import org.microg.wearable.WearableConnection;
import org.microg.wearable.proto.RootMessage;

import java.io.IOException;

public class SocketProxy {
    private static WearableConnection server;
    private static RootMessage serverConnect;
    private static WearableConnection client;
    private static RootMessage clientConnect;

    public static void main(String[] args) throws IOException {
        SocketWearableConnection.serverListen(5601, new ProxyServerListener()).start();
        SocketWearableConnection.clientConnect(5602, new ProxyClientListener()).start();
    }

    private static class ProxyServerListener implements WearableConnection.Listener {
        @Override
        public void onConnected(WearableConnection connection) {
            synchronized (this) {
                server = connection;
                if (clientConnect != null) {
                    try {
                        server.writeMessage(clientConnect);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("[Server]onConnected: " + connection);
        }

        @Override
        public void onMessage(WearableConnection connection, RootMessage message) {
            System.out.println("[Server]onMessage: " + message);
            if (message.connect != null) {
                synchronized (this) {
                    if (client == null) {
                        serverConnect = message;
                        return;
                    }
                }
            }
            try {
                client.writeMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onDisconnected() {
            System.out.println("[Server]onDisconnected");
        }
    }

    private static class ProxyClientListener implements WearableConnection.Listener {
        @Override
        public void onConnected(WearableConnection connection) {
            synchronized (this) {
                client = connection;
                if (serverConnect != null) {
                    try {
                        client.writeMessage(serverConnect);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            System.out.println("[Client]onConnected: " + connection);
        }

        @Override
        public void onMessage(WearableConnection connection, RootMessage message) {
            System.out.println("[Client]onMessage: " + message);
            if (message.connect != null) {
                synchronized (this) {
                    if (server == null) {
                        clientConnect = message;
                        return;
                    }
                }
            }
            try {
                server.writeMessage(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onDisconnected() {
            System.out.println("[Server]onDisconnected");
        }
    }
}
