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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import lombok.RequiredArgsConstructor;
import org.redisson.client.protocol.Encoder;

import java.io.IOException;

@RequiredArgsConstructor
public class BridgeEncoder implements Encoder {

    @Override
    public ByteBuf encode(Object in) throws IOException {
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        try {
            ByteBufOutputStream os = new ByteBufOutputStream(out);
            os.writeUTF(Bridge.getGsonSupplier().get().toJson(in));
            os.writeUTF(in.getClass().getName());
            return os.buffer();
        } catch (IOException e) {
            out.release();
            throw e;
        } catch (Exception e) {
            out.release();
            throw new IOException(e);
        }
    }
}
