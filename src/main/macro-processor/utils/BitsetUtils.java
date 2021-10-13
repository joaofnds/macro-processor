package utils;

import java.util.BitSet;

public class BitsetUtils {
    public static int toInt(BitSet bs) {
        return bs.stream()
                .map(i -> bs.get(i) ? (int) Math.pow(2, i) : 0)
                .reduce(0, Integer::sum);
    }

    public static BitSet fromShort(short n) {
        var result = new BitSet(16);

        for (int i = 0; i < 16; i++) {
            var bitValue = (n >> i) & 1;
            result.set(i, bitValue == 1);
        }

        return result;
    }

    public static boolean getBit(int n, int i) {
        return (n >> i & 1) == 1;
    }
}
