package noise.phonocardiographygraph.signal;


import java.util.Arrays;

public abstract class ByteArray {

    public static final int BITS_PER_BYTE = 8;


    public static Integer[] byteArrayToIntArrayChannelsAvg(byte[] bytes, int numBytesPerInt, int channels, int useChannel, int avgN, boolean bigEndian) {
        Integer[] iarr = new Integer[(bytes.length / numBytesPerInt) / (channels * avgN)];
        Arrays.fill(iarr, 0);
        byte[] temp = new byte[numBytesPerInt];
        for (int i = 0; i < iarr.length; i++) {
            for (int k = 0; k < avgN; k++) {
                for (int j = 0; j < numBytesPerInt; j++) {
                    temp[j] = bytes[(((i * avgN) + k) * numBytesPerInt * channels) + (useChannel * numBytesPerInt) + j];
                }
                iarr[i] += byteArrayToInt(temp, bigEndian);
            }
            iarr[i] /= avgN;
        }
        return (Integer[]) iarr;
    }

    public static Double[] byteArrayToDoubleArrayChannelsAvg(byte[] bytes, int numBytesPerInt, int channels, int useChannel, int avgN, boolean bigEndian) {
        Double[] darr = new Double[(bytes.length / numBytesPerInt) / (channels * avgN)];
        Arrays.fill(darr, 0.0);
        double scale = 1.0 / Math.pow(2, (numBytesPerInt * 8) - 1);
        byte[] temp = new byte[numBytesPerInt];
        for (int i = 0; i < darr.length; i++) {
            for (int k = 0; k < avgN; k++) {
                for (int j = 0; j < numBytesPerInt; j++) {
                    temp[j] = bytes[(((i * avgN) + k) * numBytesPerInt * channels) + (useChannel * numBytesPerInt) + j];
                }
                darr[i] += byteArrayToInt(temp, bigEndian) * scale;
            }
            darr[i] /= avgN;
        }
        return darr;
    }

    public static String byteArrayToString(byte[] bytes) {

        String str = "";
        for (int i = 0; i < bytes.length; i++) {
            str += ASCII.toString(bytes[i]);
        }
        return str;
    }

    public static int byteArrayToInt(byte[] bytes) {
        int ret = 0;
        int mask;
        for (int i = bytes.length - 1; i >= 0; i--) {
            mask = 0xFF << ((bytes.length - 1) - i) * BITS_PER_BYTE;
            ret = ret | ((bytes[i] << (((bytes.length - 1) - i) * BITS_PER_BYTE)) & mask);
        }
        if (bytes[0] < 0) {
            for (int i = 0; i < 4 - bytes.length; i++) {
                ret = ret | (0xFF << ((bytes.length + i) * BITS_PER_BYTE));
            }
        }
        return ret;
    }

    public static int byteArrayToInt(byte[] bytes, boolean bigEndian) {
        int ret = 0;
        int mask;
        if (bigEndian) {
            for (int i = bytes.length - 1; i >= 0; i--) {
                mask = 0xFF << ((bytes.length - 1) - i) * BITS_PER_BYTE;
                ret = ret
                        | ((bytes[i] << (((bytes.length - 1) - i) * BITS_PER_BYTE)) & mask);
            }
            if (bytes[0] < 0) {
                for (int i = 0; i < 4 - bytes.length; i++) {
                    ret = ret | (0xFF << ((bytes.length + i) * BITS_PER_BYTE));
                }
            }
        } else {
            for (int i = 0; i < bytes.length; i++) {
                mask = 0xFF << (i) * BITS_PER_BYTE;
                ret = ret
                        | ((bytes[i] << (i * BITS_PER_BYTE)) & mask);
            }
            if (bytes[bytes.length - 1] < 0) {
                for (int i = 0; i < 4 - bytes.length; i++) {
                    ret = ret | (0xFF << ((bytes.length + i) * BITS_PER_BYTE));
                }
            }
        }
        return ret;
    }

    public static short byteArrayToShort(byte[] bytes) {
        int ret = 0;
        int mask;
        for (int i = bytes.length - 1; i >= 0; i--) {
            mask = 0xFF << ((bytes.length - 1) - i) * BITS_PER_BYTE;
            ret = ret | ((bytes[i] << (((bytes.length - 1) - i) * BITS_PER_BYTE)) & mask);
        }
        if (bytes[0] < 0) {
            for (int i = 0; i < 4 - bytes.length; i++) {
                ret = ret | (0xFF << ((bytes.length + i) * BITS_PER_BYTE));
            }
        }
        return (short) ret;
    }

    public static Short[] byteArrayToShortArray(byte[] bytes, int numBytesPerInt) {
        Short[] iarr = new Short[bytes.length / numBytesPerInt];
        byte[] temp = new byte[numBytesPerInt];
        for (int i = 0; i < iarr.length; i++) {
            for (int j = 0; j < numBytesPerInt; j++) {
                temp[j] = bytes[(i * numBytesPerInt) + j];
            }
            iarr[i] = byteArrayToShort(temp);
        }
        return (Short[]) iarr;
    }


}
