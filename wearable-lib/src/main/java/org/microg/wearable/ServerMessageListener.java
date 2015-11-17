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

import org.microg.wearable.proto.Connect;
import org.microg.wearable.proto.RootMessage;

import java.io.IOException;

public abstract class ServerMessageListener extends MessageListener {
    private Connect localConnect;
    private Connect remoteConnect;

    public ServerMessageListener(Connect localConnect) {
        this.localConnect = localConnect;
    }

    @Override
    public void onConnected(WearableConnection connection) {
        super.onConnected(connection);
        try {
            connection.writeMessage(new RootMessage.Builder().connect(localConnect).build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onConnect(Connect connect) {
        this.remoteConnect = connect;
    }

    public Connect getRemoteConnect() {
        return remoteConnect;
    }
}
