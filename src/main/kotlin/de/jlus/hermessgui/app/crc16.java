/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.jlus.hermessgui.app;

/**
 * CRC-16 class is a helper that calculates different types of CRC.
 * Catalogue of CRC-16 algorithms:
 * <a href="http://reveng.sourceforge.net/crc-catalogue/16.htm">http://reveng.sourceforge.net/crc-catalogue/16.htm</a>
 * <p>Testing is based on 'check' from the link above and
 * <a href="https://www.lammertbies.nl/comm/info/crc-calculation.html">https://www.lammertbies.nl/comm/info/crc-calculation.html</a>.
 */
@SuppressWarnings("WeakerAccess")
public final class crc16 {

    private crc16() {
        // empty private constructor
    }

    /**
     * Calculates CRC CCITT-FALSE over given range of bytes from the block of data.
     * It is using the 0x1021 polynomial and 0xFFFF initial value.
     * <p>
     * See: <a href="http://reveng.sourceforge.net/crc-catalogue/16.htm#crc.cat.crc-16-ccitt-false">...</a>
     * See: <a href="http://srecord.sourceforge.net/crc16-ccitt.html">...</a>
     *
     * @param data   The input data block for computation.
     * @param offset Offset from where the range starts.
     * @param length Length of the range in bytes.
     * @return the CRC-16 CCITT-FALSE.
     */
    public static int CCITT_FALSE(final byte[] data, final int offset, final int length) {
		int crc = 0xFFFF;

		for (int i = offset; i < offset + length && i < data.length; ++i) {
			crc = (((crc & 0xFFFF) >> 8) | (crc << 8));
			crc ^= data[i];
			crc ^= (crc & 0xFF) >> 4;
			crc ^= (crc << 8) << 4;
			crc ^= ((crc & 0xFF) << 4) << 1;
		}

        return crc;
    }
}