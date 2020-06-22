/*
 * SPDX-FileCopyrightText: 2015, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.wearable;

import com.squareup.wire.Wire;

import org.microg.wearable.proto.MessagePiece;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SocketWearableConnection extends WearableConnection {
    private final int MAX_PIECE_SIZE = 20 * 1024 * 1024;
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
        if (len > MAX_PIECE_SIZE) {
            throw new IOException("Piece size " + len + " exceeded limit of " + MAX_PIECE_SIZE + " bytes.");
        }
        System.out.println("Reading piece of length " + len);
        byte[] bytes = new byte[len];
        is.readFully(bytes);
        return new Wire().parseFrom(bytes, MessagePiece.class);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
