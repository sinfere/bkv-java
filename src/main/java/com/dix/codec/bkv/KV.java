package com.dix.codec.bkv;

import com.dix.codec.bkv.exception.InvalidKeyTypeException;
import com.dix.codec.bkv.exception.PackKVFailException;
import com.dix.codec.bkv.exception.UnpackKVFailException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class KV {
    private final boolean isStringKey;
    private final byte[] key;
    private final byte[] value;

    public KV(Object key, Object value) {
        String keySimpleClassName = key.getClass().getSimpleName();
        String valueSimpleClassName = value.getClass().getSimpleName();

        switch (keySimpleClassName) {
            case "Integer": {
                this.isStringKey = false;
                Integer number = (Integer) key;
                this.key = CodecUtil.encodeNumber(number);
            } break;


            case "Long": {
                this.isStringKey = false;
                Long number = (Long) key;
                this.key = CodecUtil.encodeNumber(number);
            } break;

            case "String": {
                this.isStringKey = true;
                String string = (String) key;
                this.key = string.getBytes();
            } break;

            default:
                throw new InvalidKeyTypeException("unsupported key type: " + keySimpleClassName);
        }

        switch (valueSimpleClassName) {
            case "Integer": {
                Integer number = (Integer) value;
                this.value = CodecUtil.encodeNumber(number);
            } break;


            case "Long": {
                Long number = (Long) value;
                this.value = CodecUtil.encodeNumber(number);
            } break;

            case "String": {
                String string = (String) value;
                this.value = string.getBytes();
            } break;

            case "byte[]": {
                assert value instanceof byte[];
                this.value = (byte[]) value;
            } break;

            default:
                throw new InvalidKeyTypeException("unsupported value type: " + valueSimpleClassName);
        }

    }

    public KV(byte[] key, boolean isStringKey, byte[] value) {
        this.isStringKey = isStringKey;
        this.key = key;
        this.value = value;
    }

    public boolean isStringKey() {
        return this.isStringKey;
    }

    public String getStringKey() {
        return new String(this.key);
    }

    public long getNumberKey() {
        return CodecUtil.decodeNumber(this.key);
    }

    public Object getKey() {
        if (this.isStringKey) {
            return getStringKey();
        }

        return getNumberKey();
    }

    public String getStringValue() {
        return new String(this.value);
    }

    public long getNumberValue() {
        return CodecUtil.decodeNumber(this.value);
    }

    public byte[] getValue() {
        return this.value;
    }

    public byte[] pack() throws IOException, PackKVFailException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int keyLength = this.key.length;
        if (keyLength > 128) {
            throw new PackKVFailException(String.format("key length is bigger than 128: %d", keyLength));
        }

        int totalLength = 1 + keyLength + this.value.length;

        byte keyLengthByte = (byte) (keyLength & 0x7F);
        if (this.isStringKey) {
            keyLengthByte |= 0x80;
        }

        buffer.write(CodecUtil.encodeLength(totalLength));
        buffer.write(keyLengthByte);
        buffer.write(this.key);
        buffer.write(this.value);

        return buffer.toByteArray();
    }

    public static UnpackKVResult unpack(byte[] buf) {
        if (buf == null || buf.length == 0) {
            return null;
        }

        DecodeLengthResult decodeLengthResult = CodecUtil.decodeLength(buf);
        long totalLength = decodeLengthResult.getLength();
        byte[] pb = decodeLengthResult.getRemainingBuffer(); // pb => pending parse buffer

        int remainingBufLength = pb.length;
        if (remainingBufLength <= 0 || remainingBufLength < totalLength) {
            throw new UnpackKVFailException(String.format("invalid total length, totalLength=%d, pendingParseBufLength=%d", totalLength, remainingBufLength));
        }

        byte keyLengthByte = pb[0];
        int keyLength = keyLengthByte & 0x7F;
        boolean isStringKey = false;
        if ((keyLengthByte & 0x80) != 0) {
            isStringKey = true;
        }

        if (keyLength + 1 > totalLength) {
            throw new UnpackKVFailException(String.format("key length bigger than total length, keyLength=%d, totalLength=%d", keyLength, totalLength));
        }

        byte[] keyBuf = Arrays.copyOfRange(pb, 1, 1 + keyLength);
        byte[] valueBuf = Arrays.copyOfRange(pb, 1 + keyLength, (int) totalLength);

        KV kv = new KV(keyBuf, isStringKey, valueBuf);
        byte[] remainingBuffer = Arrays.copyOfRange(pb, (int) totalLength, pb.length);
        return new UnpackKVResult(kv, remainingBuffer);
    }
}
