package com.example.padim.util;

import java.util.Arrays;

public final class Base64 {
    private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            .toCharArray();
    private static final int[] IA = new int[256];
    private static final byte[] encodingTable;
    private static final byte[] decodingTable;

    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++) {
            IA[CA[i]] = i;
        }
        IA[61] = 0;

        encodingTable = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104,
                105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
                121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47 };

        decodingTable = new byte[''];
        for (int i = 0; i < 128; i++) {
            decodingTable[i] = -1;
        }
        for (int i = 65; i <= 90; i++) {
            decodingTable[i] = (byte) (i - 65);
        }
        for (int i = 97; i <= 122; i++) {
            decodingTable[i] = (byte) (i - 97 + 26);
        }
        for (int i = 48; i <= 57; i++) {
            decodingTable[i] = (byte) (i - 48 + 52);
        }
        decodingTable[43] = 62;
        decodingTable[47] = 63;
    }

    public static final char[] encodeToChar(byte[] sArr, boolean lineSep) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new char[0];
        }
        int eLen = sLen / 3 * 3;
        int cCnt = (sLen - 1) / 3 + 1 << 2;
        int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0);
        char[] dArr = new char[dLen];

        int s = 0;
        int d = 0;
        for (int cc = 0; s < eLen;) {
            int i = (sArr[(s++)] & 0xFF) << 16 | (sArr[(s++)] & 0xFF) << 8 | sArr[(s++)] & 0xFF;

            dArr[(d++)] = CA[(i >>> 18 & 0x3F)];
            dArr[(d++)] = CA[(i >>> 12 & 0x3F)];
            dArr[(d++)] = CA[(i >>> 6 & 0x3F)];
            dArr[(d++)] = CA[(i & 0x3F)];

            if (!lineSep)
                continue;
            cc++;
            if ((cc == 19) && (d < dLen - 2)) {
                dArr[(d++)] = '\r';
                dArr[(d++)] = '\n';
                cc = 0;
            }

        }

        int left = sLen - eLen;
        if (left > 0) {
            int i = (sArr[eLen] & 0xFF) << 10 | (left == 2 ? (sArr[(sLen - 1)] & 0xFF) << 2 : 0);

            dArr[(dLen - 4)] = CA[(i >> 12)];
            dArr[(dLen - 3)] = CA[(i >>> 6 & 0x3F)];
            dArr[(dLen - 2)] = (left == 2 ? CA[(i & 0x3F)] : '=');
            dArr[(dLen - 1)] = '=';
        }
        return dArr;
    }

    public static final byte[] decode(char[] sArr) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new byte[0];
        }

        int sepCnt = 0;
        for (int i = 0; i < sLen; i++) {
            if (IA[sArr[i]] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        int i = sLen;
        do {
            if (sArr[i] == '=')
                pad++;
            if (i <= 1)
                break;
            i--;
        } while (IA[sArr[i]] <= 0);

        int len = ((sLen - sepCnt) * 6 >> 3) - pad;

        byte[] dArr = new byte[len];

        int s = 0;
        for (int d = 0; d < len;) {
            i = 0;
            for (int j = 0; j < 4; j++) {
                int c = IA[sArr[(s++)]];
                if (c >= 0)
                    i |= c << 18 - j * 6;
                else {
                    j--;
                }
            }
            dArr[(d++)] = (byte) (i >> 16);
            if (d < len) {
                dArr[(d++)] = (byte) (i >> 8);
                if (d < len)
                    dArr[(d++)] = (byte) i;
            }
        }
        return dArr;
    }

    public static final byte[] decodeFast(char[] sArr) {
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx = 0;
        int eIx = sLen - 1;
        do {
            sIx++;

            if (sIx >= eIx)
                break;
        } while (IA[sArr[sIx]] < 0);

        while ((eIx > 0) && (IA[sArr[eIx]] < 0)) {
            eIx--;
        }

        int pad = sArr[(eIx - 1)] == '=' ? 2 : sArr[eIx] == '=' ? 1 : 0;
        int cCnt = eIx - sIx + 1;
        int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        byte[] dArr = new byte[len];

        int d = 0;
        int cc = 0;
        for (int eLen = len / 3 * 3; d < eLen;) {
            int i = IA[sArr[(sIx++)]] << 18 | IA[sArr[(sIx++)]] << 12 | IA[sArr[(sIx++)]] << 6
                    | IA[sArr[(sIx++)]];

            dArr[(d++)] = (byte) (i >> 16);
            dArr[(d++)] = (byte) (i >> 8);
            dArr[(d++)] = (byte) i;

            if (sepCnt <= 0)
                continue;
            cc++;
            if (cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[sArr[(sIx++)]] << 18 - j * 6;
            }
            for (int r = 16; d < len; r -= 8) {
                dArr[(d++)] = (byte) (i >> r);
            }
        }
        return dArr;
    }

    public static final byte[] encodeToByte(byte[] sArr, boolean lineSep) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new byte[0];
        }
        int eLen = sLen / 3 * 3;
        int cCnt = (sLen - 1) / 3 + 1 << 2;
        int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0);
        byte[] dArr = new byte[dLen];

        int s = 0;
        int d = 0;
        for (int cc = 0; s < eLen;) {
            int i = (sArr[(s++)] & 0xFF) << 16 | (sArr[(s++)] & 0xFF) << 8 | sArr[(s++)] & 0xFF;

            dArr[(d++)] = (byte) CA[(i >>> 18 & 0x3F)];
            dArr[(d++)] = (byte) CA[(i >>> 12 & 0x3F)];
            dArr[(d++)] = (byte) CA[(i >>> 6 & 0x3F)];
            dArr[(d++)] = (byte) CA[(i & 0x3F)];

            if (!lineSep)
                continue;
            cc++;
            if ((cc == 19) && (d < dLen - 2)) {
                dArr[(d++)] = 13;
                dArr[(d++)] = 10;
                cc = 0;
            }

        }

        int left = sLen - eLen;
        if (left > 0) {
            int i = (sArr[eLen] & 0xFF) << 10 | (left == 2 ? (sArr[(sLen - 1)] & 0xFF) << 2 : 0);

            dArr[(dLen - 4)] = (byte) CA[(i >> 12)];
            dArr[(dLen - 3)] = (byte) CA[(i >>> 6 & 0x3F)];
            dArr[(dLen - 2)] = (left == 2 ? (byte) CA[(i & 0x3F)] : 61);
            dArr[(dLen - 1)] = 61;
        }
        return dArr;
    }

    public static final byte[] decode(byte[] sArr) {
        int sLen = sArr.length;

        int sepCnt = 0;
        for (int i = 0; i < sLen; i++) {
            if (IA[(sArr[i] & 0xFF)] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        int i = sLen;
        do {
            if (sArr[i] == 61)
                pad++;
            if (i <= 1)
                break;
            i--;
        } while (IA[(sArr[i] & 0xFF)] <= 0);

        int len = ((sLen - sepCnt) * 6 >> 3) - pad;

        byte[] dArr = new byte[len];

        int s = 0;
        for (int d = 0; d < len;) {
            i = 0;
            for (int j = 0; j < 4; j++) {
                int c = IA[(sArr[(s++)] & 0xFF)];
                if (c >= 0)
                    i |= c << 18 - j * 6;
                else {
                    j--;
                }
            }

            dArr[(d++)] = (byte) (i >> 16);
            if (d < len) {
                dArr[(d++)] = (byte) (i >> 8);
                if (d < len) {
                    dArr[(d++)] = (byte) i;
                }
            }
        }
        return dArr;
    }

    public static final byte[] decodeFast(byte[] sArr) {
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx = 0;
        int eIx = sLen - 1;
        do {
            sIx++;

            if (sIx >= eIx)
                break;
        } while (IA[(sArr[sIx] & 0xFF)] < 0);

        while ((eIx > 0) && (IA[(sArr[eIx] & 0xFF)] < 0)) {
            eIx--;
        }

        int pad = sArr[(eIx - 1)] == 61 ? 2 : sArr[eIx] == 61 ? 1 : 0;
        int cCnt = eIx - sIx + 1;
        int sepCnt = sLen > 76 ? (sArr[76] == 13 ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        byte[] dArr = new byte[len];

        int d = 0;
        int cc = 0;
        for (int eLen = len / 3 * 3; d < eLen;) {
            int i = IA[sArr[(sIx++)]] << 18 | IA[sArr[(sIx++)]] << 12 | IA[sArr[(sIx++)]] << 6
                    | IA[sArr[(sIx++)]];

            dArr[(d++)] = (byte) (i >> 16);
            dArr[(d++)] = (byte) (i >> 8);
            dArr[(d++)] = (byte) i;

            if (sepCnt <= 0)
                continue;
            cc++;
            if (cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[sArr[(sIx++)]] << 18 - j * 6;
            }
            for (int r = 16; d < len; r -= 8) {
                dArr[(d++)] = (byte) (i >> r);
            }
        }
        return dArr;
    }

    public static final String encodeToString(byte[] sArr, boolean lineSep) {
        return new String(encodeToChar(sArr, lineSep));
    }

    private static String discardNonBase64Chars(String data) {
        StringBuffer sb = new StringBuffer();
        int length = data.length();
        for (int i = 0; i < length; i++) {
            if (isValidBase64Byte((byte) data.charAt(i))) {
                sb.append(data.charAt(i));
            }
        }
        return sb.toString();
    }

    private static boolean isValidBase64Byte(byte b) {
        if (b == 61)
            return true;
        if ((b < 0) || (b >= 128)) {
            return false;
        }
        return decodingTable[b] != -1;
    }

    public static final byte[] decode(String data) {
        data = discardNonBase64Chars(data);
        byte[] bytes = null;
        if (data.charAt(data.length() - 2) == '=') {
            bytes = new byte[(data.length() / 4 - 1) * 3 + 1];
        } else {
            if (data.charAt(data.length() - 1) == '=')
                bytes = new byte[(data.length() / 4 - 1) * 3 + 2];
            else
                bytes = new byte[data.length() / 4 * 3];
        }
        int i = 0;
        for (int j = 0; i < data.length() - 4; j += 3) {
            byte b1 = decodingTable[data.charAt(i)];
            byte b2 = decodingTable[data.charAt(i + 1)];
            byte b3 = decodingTable[data.charAt(i + 2)];
            byte b4 = decodingTable[data.charAt(i + 3)];
            bytes[j] = (byte) (b1 << 2 | b2 >> 4);
            bytes[(j + 1)] = (byte) (b2 << 4 | b3 >> 2);
            bytes[(j + 2)] = (byte) (b3 << 6 | b4);

            i += 4;
        }

        if (data.charAt(data.length() - 2) == '=') {
            byte b1 = decodingTable[data.charAt(data.length() - 4)];
            byte b2 = decodingTable[data.charAt(data.length() - 3)];
            bytes[(bytes.length - 1)] = (byte) (b1 << 2 | b2 >> 4);
        } else if (data.charAt(data.length() - 1) == '=') {
            byte b1 = decodingTable[data.charAt(data.length() - 4)];
            byte b2 = decodingTable[data.charAt(data.length() - 3)];
            byte b3 = decodingTable[data.charAt(data.length() - 2)];
            bytes[(bytes.length - 2)] = (byte) (b1 << 2 | b2 >> 4);
            bytes[(bytes.length - 1)] = (byte) (b2 << 4 | b3 >> 2);
        } else {
            byte b1 = decodingTable[data.charAt(data.length() - 4)];
            byte b2 = decodingTable[data.charAt(data.length() - 3)];
            byte b3 = decodingTable[data.charAt(data.length() - 2)];
            byte b4 = decodingTable[data.charAt(data.length() - 1)];
            bytes[(bytes.length - 3)] = (byte) (b1 << 2 | b2 >> 4);
            bytes[(bytes.length - 2)] = (byte) (b2 << 4 | b3 >> 2);
            bytes[(bytes.length - 1)] = (byte) (b3 << 6 | b4);
        }
        return bytes;
    }

    public static final byte[] decodeFast(String s) {
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }
        int sIx = 0;
        int eIx = sLen - 1;
        do {
            sIx++;

            if (sIx >= eIx)
                break;
        } while (IA[(s.charAt(sIx) & 0xFF)] < 0);

        while ((eIx > 0) && (IA[(s.charAt(eIx) & 0xFF)] < 0)) {
            eIx--;
        }

        int pad = s.charAt(eIx - 1) == '=' ? 2 : s.charAt(eIx) == '=' ? 1 : 0;
        int cCnt = eIx - sIx + 1;
        int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad;
        byte[] dArr = new byte[len];

        int d = 0;
        int cc = 0;
        for (int eLen = len / 3 * 3; d < eLen;) {
            int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12
                    | IA[s.charAt(sIx++)] << 6 | IA[s.charAt(sIx++)];

            dArr[(d++)] = (byte) (i >> 16);
            dArr[(d++)] = (byte) (i >> 8);
            dArr[(d++)] = (byte) i;

            if (sepCnt <= 0)
                continue;
            cc++;
            if (cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[s.charAt(sIx++)] << 18 - j * 6;
            }
            for (int r = 16; d < len; r -= 8) {
                dArr[(d++)] = (byte) (i >> r);
            }
        }
        return dArr;
    }
}