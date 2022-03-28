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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

public class BridgeCodec extends BaseCodec {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final BridgeEncoder encoder = new BridgeEncoder(gson);
    private final BridgeDecoder decoder = new BridgeDecoder(gson);

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (gson.getClass().getClassLoader() != null) {
            return gson.getClass().getClassLoader();
        }
        return super.getClassLoader();
    }
}