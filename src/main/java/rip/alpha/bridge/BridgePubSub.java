// This file was designed and is an original Plugin for AlphaMC
// Copyright (C) 2021 Foxtrot LLC
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package rip.alpha.bridge;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import redis.clients.jedis.JedisPubSub;

import java.lang.invoke.MethodHandle;
import java.util.Set;

@RequiredArgsConstructor
public class BridgePubSub extends JedisPubSub {

    private final Bridge instance;

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(this.instance.getChannel())) {
            try {
                String[] split = message.split(";");

                //Check if its a bridge string first
                if (split.length <= 1){
                    return;
                }

                String messageId = split[0];
                Set<MethodHandle> methods = this.instance.getListeners().get(messageId);

                //Check if there are any registered listeners for this messageId
                if (methods == null){
                    return;
                }

                String json = split[1];
                Document document = Document.parse(json);

                //Check if the data provided was parsable as a document.
                if (document == null){
                    return;
                }

                //Loop through the MethodHandles and invoking the method
                for (MethodHandle method : methods) {
                    method.invoke(document);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
