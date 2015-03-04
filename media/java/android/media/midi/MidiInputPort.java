/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.media.midi;

import android.os.ParcelFileDescriptor;

import libcore.io.IoUtils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This class is used for sending data to a port on a MIDI device
 *
 * CANDIDATE FOR PUBLIC API
 * @hide
 */
public class MidiInputPort extends MidiReceiver implements Closeable {

    private final int mPortNumber;
    private final FileOutputStream mOutputStream;

    // buffer to use for sending data out our output stream
    private final byte[] mBuffer = new byte[MidiPortImpl.MAX_PACKET_SIZE];

  /* package */ MidiInputPort(ParcelFileDescriptor pfd, int portNumber) {
        mPortNumber = portNumber;
        mOutputStream = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
    }

    /**
     * Returns the port number of this port
     *
     * @return the port's port number
     */
    public final int getPortNumber() {
        return mPortNumber;
    }

    /**
     * Writes MIDI data to the input port
     *
     * @param msg byte array containing the data
     * @param offset offset of first byte of the data in msg byte array
     * @param count size of the data in bytes
     * @param timestamp future time to post the data (based on
     *                  {@link java.lang.System#nanoTime}
     */
    public void receive(byte[] msg, int offset, int count, long timestamp) throws IOException {
        assert(offset >= 0 && count >= 0 && offset + count <= msg.length);

        synchronized (mBuffer) {
            while (count > 0) {
                int length = MidiPortImpl.packMessage(msg, offset, count, timestamp, mBuffer);
                mOutputStream.write(mBuffer, 0, length);
                int sent = MidiPortImpl.getMessageSize(mBuffer, length);
                assert(sent >= 0 && sent <= length);

                offset += sent;
                count -= sent;
            }
        }
    }

    @Override
    public void close() throws IOException {
        mOutputStream.close();
    }
}
