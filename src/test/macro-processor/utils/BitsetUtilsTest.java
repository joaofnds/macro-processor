package utils;

import org.junit.jupiter.api.Test;

import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.*;

class BitsetUtilsTest {
    @Test
    public void bitsetToShortWithAllTrueReturnsMaxValue() {
        var bs = new BitSet();
        bs.set(0, 16, true);

        var result = BitsetUtils.toInt(bs);

        assertEquals(Character.MAX_VALUE, result);
    }

    @Test
    public void shortToBitsetWithMaxValueShouldReturnABitSetWithAllTrue() {
        var result = BitsetUtils.fromShort((short) Character.MAX_VALUE);

        for (int i = 0; i < result.length(); i++) {
            assertTrue(result.get(i));
        }
    }

    @Test
    public void getBit() {
        var n = 0b10101;
        assertTrue(BitsetUtils.getBit(n, 0));
        assertFalse(BitsetUtils.getBit(n, 1));
        assertTrue(BitsetUtils.getBit(n, 2));
        assertFalse(BitsetUtils.getBit(n, 3));
        assertTrue(BitsetUtils.getBit(n, 4));
    }
}