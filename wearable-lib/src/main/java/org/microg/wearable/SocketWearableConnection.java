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

package org.microg.wearable;

import com.squareup.wire.Wire;

import org.microg.wearable.proto.MessagePiece;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketWearableConnection extends WearableConnection {
    private final Socket socket;
    private final DataInputStream is;
    private final DataOutputStream os;

    public SocketWearableConnection(Socket socket, Listener listener) throws IOException {
        super(listener);
        this.socket = socket;
        this.is = new DataInputStream(socket.getInputStream());
        this.os = new DataOutputStream(socket.getOutputStream());
    }

    protected void writeMessagePiece(MessagePiece piece) throws IOException {
        byte[] bytes = piece.toByteArray();
        os.writeInt(bytes.length);
        os.write(bytes);
    }

    protected MessagePiece readMessagePiece() throws IOException {
        int len = is.readInt();
        byte[] bytes = new byte[len];
        is.readFully(bytes);
        return new Wire().parseFrom(bytes, MessagePiece.class);
    }

    @Override
    protected void close() throws IOException {
        socket.close();
    }


    public static void serverListen(int port, final Listener listener) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket;
                    while ((socket = serverSocket.accept()) != null) {
                        SocketWearableConnection connection = new SocketWearableConnection(socket, listener);
                        connection.run();
                    }
                } catch (IOException e) {
                    // quit
                }
            }
        }).start();
    }

    public static void clientConnect(final int port, final Listener listener) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("127.0.0.1", port);
                    SocketWearableConnection connection = new SocketWearableConnection(socket, listener);
                    connection.run();
                } catch (IOException e) {
                    // quit
                }
            }
        }).start();
    }
}
