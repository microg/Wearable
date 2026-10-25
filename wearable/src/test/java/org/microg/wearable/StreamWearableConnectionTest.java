/*
 * SPDX-FileCopyrightText: 2026, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package org.microg.wearable;

import org.junit.Test;
import org.microg.wearable.proto.Connect;
import org.microg.wearable.proto.Heartbeat;
import org.microg.wearable.proto.RootMessage;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StreamWearableConnectionTest {

    @Test
    public void testStreamConnectionBidirectionalMessageExchange() throws Exception {
        // Setup piped streams simulating an abstract bidirectional connection (e.g. Bluetooth RFCOMM socket)
        PipedOutputStream clientOut = new PipedOutputStream();
        PipedInputStream serverIn = new PipedInputStream(clientOut);

        PipedOutputStream serverOut = new PipedOutputStream();
        PipedInputStream clientIn = new PipedInputStream(serverOut);

        CountDownLatch serverConnectedLatch = new CountDownLatch(1);
        CountDownLatch clientReceivedLatch = new CountDownLatch(1);
        AtomicReference<RootMessage> receivedMessageRef = new AtomicReference<RootMessage>();
        AtomicBoolean serverDisconnected = new AtomicBoolean(false);

        WearableConnection.Listener serverListener = new WearableConnection.Listener() {
            @Override
            public void onConnected(WearableConnection connection) {
                serverConnectedLatch.countDown();
            }

            @Override
            public void onMessage(WearableConnection connection, RootMessage message) {
                // Echo heartbeat back to client
                try {
                    connection.writeMessage(new RootMessage.Builder().heartbeat(new Heartbeat.Builder().build()).build());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onDisconnected() {
                serverDisconnected.set(true);
            }
        };

        WearableConnection.Listener clientListener = new WearableConnection.Listener() {
            @Override
            public void onConnected(WearableConnection connection) {
            }

            @Override
            public void onMessage(WearableConnection connection, RootMessage message) {
                receivedMessageRef.set(message);
                clientReceivedLatch.countDown();
            }

            @Override
            public void onDisconnected() {
            }
        };

        StreamWearableConnection serverConn = new StreamWearableConnection(serverIn, serverOut, null, serverListener);
        StreamWearableConnection clientConn = new StreamWearableConnection(clientIn, clientOut, null, clientListener);

        Thread serverThread = new Thread(serverConn, "TestServerWearable");
        Thread clientThread = new Thread(clientConn, "TestClientWearable");

        serverThread.start();
        clientThread.start();

        assertTrue("Server should connect within timeout", serverConnectedLatch.await(5, TimeUnit.SECONDS));

        // Client sends Connect root message
        RootMessage connectMsg = new RootMessage.Builder()
                .connect(new Connect.Builder()
                        .name("PixelWatch")
                        .id("node-12345")
                        .build())
                .build();

        clientConn.writeMessage(connectMsg);

        // Verify client received heartbeat response from server
        assertTrue("Client should receive response message within timeout", clientReceivedLatch.await(5, TimeUnit.SECONDS));
        RootMessage response = receivedMessageRef.get();
        assertNotNull("Response message should not be null", response);
        assertNotNull("Response should contain heartbeat", response.heartbeat);

        // Clean close
        clientConn.close();
        serverConn.close();

        serverThread.join(2000);
        clientThread.join(2000);

        assertTrue("Server listener should have observed disconnect", serverDisconnected.get());
    }
}
