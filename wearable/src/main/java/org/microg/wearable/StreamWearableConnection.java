/*
 * SPDX-FileCopyrightText: 2026, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.wearable;

import com.squareup.wire.Wire;
import org.microg.wearable.proto.MessagePiece;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A generalized stream-based Wearable connection.
 * Can wrap any communication channel providing an InputStream, OutputStream, and Closeable
 * (e.g. BluetoothSocket, TCP Socket, USB Accessory AOA, or Unix Domain Sockets).
 */
public class StreamWearableConnection extends WearableConnection {
    private static final int MAX_PIECE_SIZE = 20 * 1024 * 1024;

    private final DataInputStream is;
    private final DataOutputStream os;
    private final Closeable closeable;

    public StreamWearableConnection(InputStream in, OutputStream out, Closeable closeable, Listener listener) {
        super(listener);
        this.is = in instanceof DataInputStream ? (DataInputStream) in : new DataInputStream(in);
        this.os = out instanceof DataOutputStream ? (DataOutputStream) out : new DataOutputStream(out);
        this.closeable = closeable;
    }

    @Override
    protected void writeMessagePiece(MessagePiece piece) throws IOException {
        byte[] bytes = piece.toByteArray();
        os.writeInt(bytes.length);
        os.write(bytes);
        os.flush();
    }

    @Override
    protected MessagePiece readMessagePiece() throws IOException {
        int len = is.readInt();
        if (len < 0 || len > MAX_PIECE_SIZE) {
            throw new IOException("Piece size " + len + " is invalid or exceeded limit of " + MAX_PIECE_SIZE + " bytes.");
        }
        byte[] bytes = new byte[len];
        is.readFully(bytes);
        return wire.parseFrom(bytes, MessagePiece.class);
    }

    @Override
    public void close() throws IOException {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
            try {
                os.close();
            } catch (Exception ignored) {
            }
        }
    }
}
