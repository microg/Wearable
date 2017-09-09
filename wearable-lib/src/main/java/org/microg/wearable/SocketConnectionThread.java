/*
 * Copyright (C) 2013-2017 microG Project Team
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

package org.microg.wearable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class SocketConnectionThread extends Thread {

    private SocketWearableConnection wearableConnection;

    private SocketConnectionThread() {
        super();
    }

    protected void setWearableConnection(SocketWearableConnection wearableConnection) {
        this.wearableConnection = wearableConnection;
    }

    public SocketWearableConnection getWearableConnection() {
        return wearableConnection;
    }

    public abstract void close();

    public static SocketConnectionThread serverListen(final int port, final WearableConnection.Listener listener) {
        return new SocketConnectionThread() {
            private ServerSocket serverSocket = null;

            @Override
            public void close() {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException ignored) {
                    }
                    serverSocket = null;
                }
            }

            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(port);
                    Socket socket;
                    while ((socket = serverSocket.accept()) != null && !Thread.interrupted()) {
                        SocketWearableConnection connection = new SocketWearableConnection(socket, listener);
                        setWearableConnection(connection);
                        connection.run();
                    }
                } catch (IOException e) {
                    // quit
                } finally {
                    try {
                        if (serverSocket != null) serverSocket.close();
                    } catch (IOException e) {
                    }
                }
            }
        };
    }

    public static SocketConnectionThread clientConnect(final int port, final WearableConnection.Listener listener) {
        return new SocketConnectionThread() {
            private Socket socket;

            @Override
            public void close() {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    }
                    socket = null;
                }
            }

            @Override
            public void run() {
                try {
                    socket = new Socket("127.0.0.1", port);
                    SocketWearableConnection connection = new SocketWearableConnection(socket, listener);
                    setWearableConnection(connection);
                    connection.run();
                } catch (IOException e) {
                    // quit
                } finally {
                    try {
                        if (socket != null) socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        };
    }
}
