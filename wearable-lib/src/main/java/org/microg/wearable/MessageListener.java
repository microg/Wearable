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

import org.microg.wearable.proto.AckAsset;
import org.microg.wearable.proto.Connect;
import org.microg.wearable.proto.FetchAsset;
import org.microg.wearable.proto.FilePiece;
import org.microg.wearable.proto.Heartbeat;
import org.microg.wearable.proto.Request;
import org.microg.wearable.proto.RootMessage;
import org.microg.wearable.proto.SetAsset;
import org.microg.wearable.proto.SetDataItem;
import org.microg.wearable.proto.SyncStart;

public abstract class MessageListener implements WearableConnection.Listener {
    private WearableConnection connection;

    @Override
    public void onConnected(WearableConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onDisconnected() {
        this.connection = null;
    }

    public WearableConnection getConnection() {
        return connection;
    }

    @Override
    public void onMessage(WearableConnection connection, RootMessage message) {
        if (message.setAsset != null) {
            onSetAsset(message.setAsset);
        } else if (message.ackAsset != null) {
            onAckAsset(message.ackAsset);
        } else if (message.fetchAsset != null) {
            onFetchAsset(message.fetchAsset);
        } else if (message.connect != null) {
            onConnect(message.connect);
        } else if (message.syncStart != null) {
            onSyncStart(message.syncStart);
        } else if (message.setDataItem != null) {
            onSetDataItem(message.setDataItem);
        } else if (message.rpcRequest != null) {
            onRpcRequest(message.rpcRequest);
        } else if (message.heartbeat != null) {
            onHeartbeat(message.heartbeat);
        } else if (message.filePiece != null) {
            onFilePiece(message.filePiece);
        } else if (message.channelRequest != null) {
            onChannelRequest(message.channelRequest);
        }
    }

    public abstract void onSetAsset(SetAsset setAsset);

    public abstract void onAckAsset(AckAsset ackAsset);

    public abstract void onFetchAsset(FetchAsset fetchAsset);

    public abstract void onConnect(Connect connect);

    public abstract void onSyncStart(SyncStart syncStart);

    public abstract void onSetDataItem(SetDataItem setDataItem);

    public abstract void onRpcRequest(Request rpcRequest);

    public abstract void onHeartbeat(Heartbeat heartbeat);

    public abstract void onFilePiece(FilePiece filePiece);

    public abstract void onChannelRequest(Request channelRequest);
}
