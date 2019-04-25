package com.xuge.svgparser;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created at 2019/4/25 上午10:47.
 *
 * @author yixu.wang
 */
public class CustomInputStream extends InputStream {

    private static final String TAG = CustomInputStream.class.getSimpleName();

    protected volatile InputStream in;

    private int key;

    public CustomInputStream(InputStream in) throws IOException {
        this.in = in;
        key = getDecryptKey();
    }

    @Override
    public int read() throws IOException {
        int originByte = in.read();
        int finalByte = (byte) (originByte ^ key);
        return originByte == -1 ? -1 : finalByte;
    }

    @Override
    public int read(byte[] b) throws IOException {
        byte[] originBytes = new byte[b.length];
        int length = in.read(originBytes);

        for (int i = 0; i < length; i++) {
            // 通过异或运算某个数字或字符串（这里以2为例）
            b[i] = (byte) (originBytes[i] ^ key);
        }
        Log.d(TAG, "read: length = " + length);
        return length;
    }

    private static final int[] keyArray = {34, 187, 134, 43, 75, 141, 205, 247, 125, 189, 241, 53, 103, 116, 250, 229, 49, 109, 255, 133, 240,
            92, 137, 199, 144, 65, 69, 30, 8, 22, 214, 197, 32, 200, 51, 102, 213, 219, 204, 118, 242, 41, 201,
            12, 70, 228, 163, 59, 244, 24, 157, 61, 18, 93, 164, 9, 234, 136, 160, 131, 10, 117, 110, 253, 142,
            235, 132, 171, 73, 11, 57, 77, 52, 29, 167, 46, 111, 88, 168, 27, 37, 6, 76, 215, 232, 154, 158, 64,
            230, 104, 28, 198, 48, 95, 122, 181, 44, 56, 4, 47, 1, 107, 203, 196, 210, 86, 222, 67, 68, 156, 71,
            239, 130, 45, 128, 236, 74, 124, 2, 16, 78, 114, 99, 63, 148, 165, 3, 191, 245, 155, 209, 192, 202,
            140, 84, 254, 66, 207, 188, 251, 180, 252, 216, 123, 119, 72, 58, 182, 194, 89, 17, 115, 238, 212,
            186, 184, 38, 246, 101, 106, 60, 94, 80, 129, 218, 145, 0, 23, 151, 138, 162, 20, 159, 50, 217, 25,
            31, 206, 147, 83, 120, 135, 248, 190, 108, 224, 176, 170, 174, 223, 91, 13, 231, 35, 121, 127, 79,
            81, 208, 100, 98, 14, 42, 82, 96, 193, 19, 152, 226, 90, 7, 97, 221, 146, 112, 178, 195, 227, 166,
            15, 225, 26, 233, 55, 183, 161, 143, 179, 33, 62, 243, 173, 40, 149, 54, 175, 172, 249, 237, 169,
            113, 36, 139, 150, 220, 126, 85, 153, 185, 105, 177, 211, 87, 5, 21, 39};

    private int getDecryptKey() throws IOException {
        if (in == null) {
            throw new IOException("InputStream is null when getDecryptKey.");
        }
        byte[] headerBytes = new byte[5];
        int index = in.read(headerBytes);
        if (index == -1 || headerBytes.length < 5) {
            throw new IOException("Data Error when getDecryptKey.");
        }
        int keyIndex = ((headerBytes[0] & 0x0f) << 4) | ((headerBytes[3] & 0xf0) >> 4);

        int key = keyArray[keyIndex];
        Log.d("wyx", "getDecryptKey: keyIndex = " + keyIndex + "   key = " + key);
        return key;
    }
}
