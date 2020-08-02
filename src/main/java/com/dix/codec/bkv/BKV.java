package com.dix.codec.bkv;

import com.dix.codec.bkv.exception.UnpackKVFailException;
import javax.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BKV {
    private List<KV> kvs = new ArrayList<>();

    public void add(KV kv) {
        this.kvs.add(kv);
    }

    public void add(Object key, Object value) {
        KV kv = new KV(key, value);
        this.add(kv);
    }

    @Nullable
    public KV get(String key) {
        for (KV kv : kvs) {
            if (kv.isStringKey() && kv.getStringKey().equals(key)) {
                return kv;
            }
        }

        return null;
    }

    @Nullable
    public KV get(int key) {
        return get((long)key);
    }

    @Nullable
    public KV get(long key) {
        for (KV kv : kvs) {
            if (!kv.isStringKey() && (kv.getNumberKey() == key)) {
                return kv;
            }
        }

        return null;
    }

    @Nullable
    public KV getByIndex(int index) {
        return this.kvs.get(index);
    }

    @Nullable
    public Long getNumberValue(String key) {
        KV kv = get(key);
        if (kv == null) {
            return null;
        }

        return kv.getNumberValue();
    }

    @Nullable
    public Long getNumberValue(long key) {
        KV kv = get(key);
        if (kv == null) {
            return null;
        }

        return kv.getNumberValue();
    }

    @Nullable
    public String getStringValue(String key) {
        KV kv = get(key);
        if (kv == null) {
            return null;
        }

        return kv.getStringValue();
    }

    @Nullable
    public String getStringValue(long key) {
        KV kv = get(key);
        if (kv == null) {
            return null;
        }

        return kv.getStringValue();
    }

    public List<KV> getItems() {
        return kvs;
    }

    public boolean containsKey(Object key) {
        Object validKey;
        if (key instanceof Integer) {
            validKey = ((Integer) key).longValue();
        } else if (key instanceof Long || key instanceof String) {
            validKey = key;
        } else {
            return false;
        }

        for (KV kv : kvs) {
            if (kv.getKey().equals(validKey)) {
                return true;
            }
        }

        return false;
    }

    public void dump() {
        for (KV kv : kvs) {
            if (kv.isStringKey()) {
                System.out.printf("[BKV] kv: %s -> %s%n", kv.getStringKey(), CodecUtil.bytesToHex(kv.getValue()));
            } else {
                System.out.printf("[BKV] kv: %d -> %s%n", kv.getNumberKey(), CodecUtil.bytesToHex(kv.getValue()));
            }
        }
    }

    public byte[] pack() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (KV kv : kvs) {
            buffer.write(kv.pack());
        }

        return buffer.toByteArray();
    }

    public static UnpackBKVResult unpack(byte[] buf) {
        BKV bkv = new BKV();
        while (true) {
            if (buf == null || buf.length == 0) {
                return new UnpackBKVResult(bkv, null);
            }

            try {
                // System.out.println(String.format("packing: %s", CodecUtil.bytesToHex(buf)));
                UnpackKVResult unpackKVResult = KV.unpack(buf);
                if (unpackKVResult == null) {
                    return new UnpackBKVResult(bkv, null);
                }
                if (unpackKVResult.getKV() != null) {
                    bkv.add(unpackKVResult.getKV());
                }
                buf = unpackKVResult.getRemainingBuffer();
            } catch (UnpackKVFailException e) {
                return new UnpackBKVResult(bkv, buf);
            }
        }
    }
}
