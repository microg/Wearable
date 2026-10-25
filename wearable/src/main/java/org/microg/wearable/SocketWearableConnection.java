/*
 * SPDX-FileCopyrightText: 2015-2026, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.wearable;

import java.io.IOException;
import java.net.Socket;

public class SocketWearableConnection extends StreamWearableConnection {
    private final Socket socket;

    public SocketWearableConnection(Socket socket, Listener listener) throws IOException {
        super(socket.getInputStream(), socket.getOutputStream(), socket, listener);
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
