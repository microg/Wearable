/*
 * SPDX-FileCopyrightText: 2015, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.wearable.tools;

import org.microg.wearable.SocketConnectionThread;
import org.microg.wearable.WearableConnection;
import org.microg.wearable.proto.RootMessage;

import java.io.IOException;

public class SocketProxy {
    private static WearableConnection server;
    private static RootMessage serverConnect;
    private static WearableConnection client;
    private static RootMessage clientConnect;

    public static void main(String[] args) throws IOException {
        SocketConnectionThread.serverListen(5601, new ProxyServerListener()).start();
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
                        System.out.println(e);
                    }
                }
            }
            System.out.println("[Server]onConnected: " + connection);
        }

        @Override
        public void onMessage(WearableConnection connection, RootMessage message) {
            describeMessage("Server", message);
            if (message.connect != null) {
                synchronized (this) {
                    if (client == null) {
                        serverConnect = message;
                        SocketConnectionThread.clientConnect(5602, new ProxyClientListener()).start();
                        return;
                    }
                }
            }
            try {
                client.writeMessage(message);
            } catch (IOException e) {
                System.out.println(e);
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
                        System.out.println(e);
                    }
                }
            }
            System.out.println("[Client]onConnected: " + connection);
        }

        @Override
        public void onMessage(WearableConnection connection, RootMessage message) {
            describeMessage("Client", message);
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
                System.out.println(e);
            }
        }

        @Override
        public void onDisconnected() {
            System.out.println("[Client]onDisconnected");
        }
    }

    private static void describeMessage(String prefix, RootMessage m) {
        System.out.println("[" + prefix + "]onMessage: " + m);
        if (m.setDataItem != null && m.setDataItem.data != null) {
            System.out.println("[" + prefix + "]data: " + m.setDataItem.data.base64());
        }
        if (m.setAsset != null && m.setAsset.data != null) {
            System.out.println("[" + prefix + "]asset: " + m.setAsset.data.base64());
        }
        if (m.filePiece != null && m.filePiece.piece != null) {
            System.out.println("[" + prefix + "]file: " + m.filePiece.piece.base64());
        }
    }
}
