package com.dix.codec.bkv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UtilTest {

    @Test
    @DisplayName("encode number")
    void encodeNumber() {
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeNumber(0x01)), "01");
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeNumber(0x010203)), "010203");
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeNumber(0x0000000000000001)), "01");
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeNumber(0x12345678)), "12345678");
    }

    @Test
    void decodeNumber() {
        Assertions.assertEquals(Util.decodeNumber(CodecUtil.hexToBytes("010203")), 0x010203);
        Assertions.assertEquals(Util.decodeNumber(CodecUtil.hexToBytes("01")), 0x001);

        System.out.println(Util.decodeNumber(CodecUtil.hexToBytes("0001")));
        Assertions.assertEquals(Util.decodeNumber(CodecUtil.hexToBytes("0001")), 0x01);
    }

    @Test
    void encodeLength() {
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeLength(2)), "02");
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeLength(666)), "851A");
        Assertions.assertEquals(CodecUtil.bytesToHex(Util.encodeLength(88888888)), "AAB1AC38");
        System.out.println(String.format("encode length: %s", CodecUtil.bytesToHex(Util.encodeLength(88888888))));
    }

    @Test
    void decodeLength() {
        Assertions.assertEquals(Util.decodeLength(CodecUtil.hexToBytes("02")).getLength(), 2L);
        Assertions.assertEquals(Util.decodeLength(CodecUtil.hexToBytes("851A")).getLength(), 666L);
        Assertions.assertEquals(Util.decodeLength(CodecUtil.hexToBytes("AAB1AC38")).getLength(), 88888888L);
    }
}