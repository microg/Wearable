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

package org.microg.wearable.tools;

import org.microg.wearable.ServerMessageListener;
import org.microg.wearable.SocketConnectionThread;
import org.microg.wearable.proto.AckAsset;
import org.microg.wearable.proto.Connect;
import org.microg.wearable.proto.FetchAsset;
import org.microg.wearable.proto.FilePiece;
import org.microg.wearable.proto.Heartbeat;
import org.microg.wearable.proto.Request;
import org.microg.wearable.proto.SetAsset;
import org.microg.wearable.proto.SetDataItem;
import org.microg.wearable.proto.SyncStart;

import java.io.IOException;
import java.util.Random;

public class SocketServer {
    private static final String TAG = "WearableSocketServerTest";

    static class Log {
        static void d(String tag, String msg) {
            System.out.println(msg);
        }
    }

    static class Listener extends ServerMessageListener {

        public Listener(Connect localConnect) {
            super(localConnect);
        }

        @Override
        public void onSetAsset(SetAsset setAsset) {
            Log.d(TAG, "onSetAsset: " + setAsset);
        }

        @Override
        public void onAckAsset(AckAsset ackAsset) {
            Log.d(TAG, "onAckAsset: " + ackAsset);
        }

        @Override
        public void onFetchAsset(FetchAsset fetchAsset) {
            Log.d(TAG, "onFetchAsset: " + fetchAsset);
        }

        @Override
        public void onSyncStart(SyncStart syncStart) {
            Log.d(TAG, "onSyncStart: " + syncStart);
        }

        @Override
        public void onSetDataItem(SetDataItem setDataItem) {
            Log.d(TAG, "onSetDataItem: " + setDataItem);
        }

        @Override
        public void onRpcRequest(Request rpcRequest) {
            Log.d(TAG, "onRcpRequest: " + rpcRequest);
        }

        @Override
        public void onHeartbeat(Heartbeat heartbeat) {
            Log.d(TAG, "onHeartbeat: " + heartbeat);
        }

        @Override
        public void onFilePiece(FilePiece filePiece) {
            Log.d(TAG, "onFilePiece: " + filePiece);
        }

        @Override
        public void onChannelRequest(Request channelRequest) {
            Log.d(TAG, "onChannelRequest:" + channelRequest);
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Random r = new Random();
        final String nodeId = Integer.toHexString(r.nextInt());
        final long peerAndroidId = r.nextLong();
        final String networkId = Long.toString(r.nextLong());

        SocketConnectionThread.serverListen(5601, new Listener(new Connect.Builder()
                .id(nodeId)
                .name("Emulator Test")
                .peerAndroidId(peerAndroidId)
                .unknown4(3)
                .peerVersion(1)
                .peerMinimumVersion(0)
                .networkId(networkId)
                .build())).start();
    }
}
