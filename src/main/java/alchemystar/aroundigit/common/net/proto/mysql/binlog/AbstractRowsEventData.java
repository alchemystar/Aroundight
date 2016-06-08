/*
 * Copyright (C) 2016 alchemystar, Inc. All Rights Reserved.
 */
package alchemystar.aroundigit.common.net.proto.mysql.binlog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Calendar;
import java.util.TimeZone;

import alchemystar.aroundigit.common.net.handler.backend.codec.DecodeUtil;
import alchemystar.aroundigit.common.net.proto.mysql.MySQLMessage;

/**
 * @Author lizhuyang
 */
public class AbstractRowsEventData {

    private static final int[] DIG_TO_BYTES = {0, 1, 1, 2, 2, 3, 3, 4, 4, 4};
    private static final int DIG_PER_DEC = 9;

    protected Object[] decodeRows(TableMapEventData tableMapEvent, ColumnSet columnSet,
                                        MySQLMessage mm) {
        byte[] types = tableMapEvent.getColumnTypes();
        int[] metadata = tableMapEvent.getColumnMetadata();
        Object[] result = new Serializable[columnSet.size()];
        BitSet nullColumns = DecodeUtil.readBitSet(mm, result.length, true);
        for (int i = 0, numberOfSkippedColumns = 0; i < types.length; i++) {
            if (!columnSet.contains(i)) {
                numberOfSkippedColumns++;
                continue;
            }
            int index = i - numberOfSkippedColumns;
            if (!nullColumns.get(index)) {
                // mysql-5.6.24 sql/log_event.cc log_event_print_value (line 1980)
                int typeCode = types[i] & 0xFF, meta = metadata[i], length = 0;
                if (typeCode == ColumnType.STRING.getCode()) {
                    if (meta >= 256) {
                        int meta0 = meta >> 8, meta1 = meta & 0xFF;
                        if ((meta0 & 0x30) != 0x30) {
                            typeCode = meta0 | 0x30;
                            length = meta1 | (((meta0 & 0x30) ^ 0x30) << 4);
                        } else {
                            // mysql-5.6.24 sql/rpl_utility.h enum_field_types (line 278)
                            if (meta0 == ColumnType.ENUM.getCode() || meta0 == ColumnType.SET.getCode()) {
                                typeCode = meta0;
                            }
                            length = meta1;
                        }
                    } else {
                        length = meta;
                    }
                }
                result[index] = decodeCell(ColumnType.byCode(typeCode), meta, length, mm);
            }
        }
        return result;
    }

    protected Object decodeCell(ColumnType type, int meta, int length, MySQLMessage mm) {
        switch (type) {
            case BIT:
                return decodeBit(meta, mm);
            case TINY:
                return decodeTiny(mm);
            case SHORT:
                return decodeShort(mm);
            case INT24:
                return decodeInt24(mm);
            case LONG:
                return decodeLong(mm);
            case LONGLONG:
                return decodeLongLong(mm);
            case FLOAT:
                return decodeFloat(mm);
            case DOUBLE:
                return decodeDouble(mm);
            case NEWDECIMAL:
                return decodeNewDecimal(meta, mm);
            case DATE:
                return decodeDate(mm);
            case TIME:
                return decodeTime(mm);
            case TIME_V2:
                return decodeTimeV2(meta, mm);
            case TIMESTAMP:
                return decodeTimestamp(mm);
            case TIMESTAMP_V2:
                return decodeTimestampV2(meta, mm);
            case DATETIME:
                return decodeDatetime(mm);
            case DATETIME_V2:
                return decodeDatetimeV2(meta, mm);
            case YEAR:
                return decodeYear(mm);
            case STRING: // CHAR or BINARY
               // return decodeString(length, mm);
                // STRING
                return new String(decodeString(meta,mm));
            case VARCHAR:
            case VAR_STRING: // VARCHAR or VARBINARY
               // return decodeVarString(meta, mm);
               // if varchar
                return new String(decodeVarString(meta,mm));
            case BLOB:
                return decodeBlob(meta, mm);
            case ENUM:
                return decodeEnum(length, mm);
            case SET:
                return decodeSet(length, mm);
            case GEOMETRY:
                return decodeGeometry(meta, mm);
            default:
                throw new RuntimeException("Unsupported type " + type);
        }
    }

    protected BitSet decodeBit(int meta, MySQLMessage mm) {
        int bitSetLength = (meta >> 8) * 8 + (meta & 0xFF);
        return DecodeUtil.readBitSet(mm, bitSetLength, false);
    }

    protected Integer decodeTiny(MySQLMessage mm) {
        return (int) (mm.read());
    }

    protected Integer decodeShort(MySQLMessage mm) {
        return mm.readUB2();
    }

    protected Integer decodeInt24(MySQLMessage mm) {
        return (mm.readUB3() << 8) >> 8;
    }

    protected Integer decodeLong(MySQLMessage mm) {
        return (int) mm.readUB4();
    }

    protected Long decodeLongLong(MySQLMessage mm) {
        return mm.readLong();
    }

    protected Float decodeFloat(MySQLMessage mm) {
        return Float.intBitsToFloat((int) mm.readUB4());
    }

    protected Double decodeDouble(MySQLMessage mm) {
        return Double.longBitsToDouble(mm.readLong());
    }

    protected BigDecimal decodeNewDecimal(int meta, MySQLMessage mm) {
        int precision = meta & 0xFF, scale = meta >> 8, x = precision - scale;
        int ipd = x / DIG_PER_DEC, fpd = scale / DIG_PER_DEC;
        int decimalLength = (ipd << 2) + DIG_TO_BYTES[x - ipd * DIG_PER_DEC] +
                (fpd << 2) + DIG_TO_BYTES[scale - fpd * DIG_PER_DEC];
        return asBigDecimal(precision, scale, mm.readBytes(decimalLength));
    }

    protected Long decodeDate(MySQLMessage mm) {
        int value = mm.readUB3();
        int day = value % 32;
        value >>>= 5;
        int month = value % 16;
        int year = value >> 4;
        return asUnixTime(year, month, day, 0, 0, 0, 0);
    }

    protected Long decodeTime(MySQLMessage mm) {
        int value = mm.readUB3();
        int[] split = split(value, 100, 3);
        return asUnixTime(1970, 1, 1, split[2], split[1], split[0], 0);
    }

    protected Long decodeTimeV2(int meta, MySQLMessage mm) {
        /*
            (in big endian)

            1 bit sign (1= non-negative, 0= negative)
            1 bit unused (reserved for future extensions)
            10 bits hour (0-838)
            6 bits minute (0-59)
            6 bits second (0-59)

            (3 bytes in total)

            + fractional-seconds storage (size depends on meta)
        */
        long time = bigEndianLong(mm.readBytes(3), 0, 3);
        return asUnixTime(1970, 1, 1,
                extractBits(time, 2, 10, 24),
                extractBits(time, 12, 6, 24),
                extractBits(time, 18, 6, 24),
                getFractionalSeconds(meta, mm)
        );
    }

    protected Long decodeTimestamp(MySQLMessage mm) {
        return mm.readLong(4) * 1000;
    }

    protected Long decodeTimestampV2(int meta, MySQLMessage mm) {
        return bigEndianLong(mm.readBytes(4), 0, 4) * 1000 + getFractionalSeconds(meta, mm);
    }

    protected Long decodeDatetime(MySQLMessage mm) {
        int[] split = split(mm.readLong(8), 100, 6);
        return asUnixTime(split[5], split[4], split[3], split[2], split[1], split[0], 0);
    }

    protected Long decodeDatetimeV2(int meta, MySQLMessage mm) {
        /*
            (in big endian)

            1 bit sign (1= non-negative, 0= negative)
            17 bits year*13+month (year 0-9999, month 0-12)
            5 bits day (0-31)
            5 bits hour (0-23)
            6 bits minute (0-59)
            6 bits second (0-59)

            (5 bytes in total)

            + fractional-seconds storage (size depends on meta)
        */
        long datetime = bigEndianLong(mm.readBytes(5), 0, 5);
        int yearMonth = extractBits(datetime, 1, 17, 40);
        return asUnixTime(
                yearMonth / 13,
                yearMonth % 13,
                extractBits(datetime, 18, 5, 40),
                extractBits(datetime, 23, 5, 40),
                extractBits(datetime, 28, 6, 40),
                extractBits(datetime, 34, 6, 40),
                getFractionalSeconds(meta, mm)
        );
    }

    protected Integer decodeYear(MySQLMessage mm) {
        return 1900 + mm.read();
    }

    protected byte[] decodeString(int length, MySQLMessage mm) {
        // charset is not present in the binary log (meaning there is no way to distinguish between CHAR / BINARY)
        // as a result - return byte[] instead of an actual String
        int stringLength = length < 256 ? mm.read() : mm.readUB2();
        return mm.readBytes(stringLength);
    }

    protected byte[] decodeVarString(int meta, MySQLMessage mm) {
        int varcharLength = meta < 256 ? mm.read() : mm.readUB2();
        return mm.readBytes(varcharLength);
    }

    protected byte[] decodeBlob(int meta, MySQLMessage mm) {
        int blobLength = mm.readInteger(meta);
        return mm.readBytes(blobLength);
    }

    protected Integer decodeEnum(int length, MySQLMessage mm) {
        return mm.readInteger(length);
    }

    protected Long decodeSet(int length, MySQLMessage mm) {
        return mm.readLong(length);
    }

    protected byte[] decodeGeometry(int meta, MySQLMessage mm) {
        int dataLength = mm.readInteger(meta);
        return mm.readBytes(dataLength);
    }

    // checkstyle, please ignore ParameterNumber for the next line
    private static Long asUnixTime(int year, int month, int day, int hour, int minute, int second, int millis) {
        // https://dev.mysql.com/doc/refman/5.0/en/datetime.html
        if (year == 0 || month == 0 || day == 0) {
            return null;
        }
        return UnixTime.from(year, month, day, hour, minute, second, millis);
    }

    private static int getFractionalSeconds(int meta, MySQLMessage mm) {
        int fractionalSecondsStorageSize = getFractionalSecondsStorageSize(meta);
        if (fractionalSecondsStorageSize > 0) {
            long fractionalSeconds = bigEndianLong(mm.readBytes(fractionalSecondsStorageSize), 0,
                    fractionalSecondsStorageSize);
            if (meta % 2 == 1) {
                fractionalSeconds /= 10;
            }
            return (int) (fractionalSeconds / 1000);
        }
        return 0;
    }

    private static int getFractionalSecondsStorageSize(int fsp) {
        if (fsp == 1 || fsp == 2) {
            return 1;
        }
        if (fsp == 3 || fsp == 4) {
            return 2;
        }
        if (fsp == 5 || fsp == 6) {
            return 3;
        }
        return 0;
    }

    private static int extractBits(long value, int bitOffset, int numberOfBits, int payloadSize) {
        long result = value >> payloadSize - (bitOffset + numberOfBits);
        return (int) (result & ((1 << numberOfBits) - 1));
    }

    private static int[] split(long value, int divider, int length) {
        int[] result = new int[length];
        for (int i = 0; i < length - 1; i++) {
            result[i] = (int) (value % divider);
            value /= divider;
        }
        result[length - 1] = (int) value;
        return result;
    }

    /**
     * see mysql/strings/decimal.c
     */
    private static BigDecimal asBigDecimal(int precision, int scale, byte[] value) {
        boolean positive = (value[0] & 0x80) == 0x80;
        value[0] ^= 0x80;
        if (!positive) {
            for (int i = 0; i < value.length; i++) {
                value[i] ^= 0xFF;
            }
        }
        int x = precision - scale;
        int ipDigits = x / DIG_PER_DEC;
        int ipDigitsX = x - ipDigits * DIG_PER_DEC;
        int ipSize = (ipDigits << 2) + DIG_TO_BYTES[ipDigitsX];
        int offset = DIG_TO_BYTES[ipDigitsX];
        BigDecimal ip = offset > 0 ? BigDecimal.valueOf(bigEndianInteger(value, 0, offset)) : BigDecimal.ZERO;
        for (; offset < ipSize; offset += 4) {
            int i = bigEndianInteger(value, offset, 4);
            ip = ip.movePointRight(DIG_PER_DEC).add(BigDecimal.valueOf(i));
        }
        int shift = 0;
        BigDecimal fp = BigDecimal.ZERO;
        for (; shift + DIG_PER_DEC <= scale; shift += DIG_PER_DEC, offset += 4) {
            int i = bigEndianInteger(value, offset, 4);
            fp = fp.add(BigDecimal.valueOf(i).movePointLeft(shift + DIG_PER_DEC));
        }
        if (shift < scale) {
            int i = bigEndianInteger(value, offset, DIG_TO_BYTES[scale - shift]);
            fp = fp.add(BigDecimal.valueOf(i).movePointLeft(scale));
        }
        BigDecimal result = ip.add(fp);
        return positive ? result : result.negate();
    }

    private static int bigEndianInteger(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = offset; i < (offset + length); i++) {
            byte b = bytes[i];
            result = (result << 8) | (b >= 0 ? (int) b : (b + 256));
        }
        return result;
    }

    private static long bigEndianLong(byte[] bytes, int offset, int length) {
        long result = 0;
        for (int i = offset; i < (offset + length); i++) {
            byte b = bytes[i];
            result = (result << 8) | (b >= 0 ? (int) b : (b + 256));
        }
        return result;
    }

    /**
     * Set of columns to be decoded.
     */
    static class ColumnSet {

        private BitSet bitSet;
        private int numberOfBitsSet;

        public ColumnSet(BitSet bitSet) {
            this.bitSet = bitSet;
            for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
                numberOfBitsSet++;
            }
        }

        public boolean contains(int i) {
            return bitSet.get(i);
        }

        public int size() {
            return numberOfBitsSet;
        }
    }

    /**
     * Class for working with Unix time.
     */
    static class UnixTime {

        private static final int[] YEAR_DAYS_BY_MONTH = new int[] {
                0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365
        };
        private static final int[] LEAP_YEAR_DAYS_BY_MONTH = new int[] {
                0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366
        };

        /**
         * Calendar::getTimeInMillis but magnitude faster for all dates starting from October 15, 1582
         * (Gregorian Calendar cutover).
         *
         * @param year   year
         * @param month  month [1..12]
         * @param day    day [1..)
         * @param hour   hour [0..23]
         * @param minute [0..59]
         * @param second [0..59]
         * @param millis [0..999]
         *
         * @return Unix time (number of seconds that have elapsed since 00:00:00 (UTC), Thursday,
         * 1 January 1970, not counting leap seconds)
         */
        // checkstyle, please ignore ParameterNumber for the next line
        public static long from(int year, int month, int day, int hour, int minute, int second, int millis) {
            if (year < 1582 || (year == 1582 && (month < 10 || (month == 10 && day < 15)))) {
                return fallbackToGC(year, month, day, hour, minute, second, millis);
            }
            long timestamp = 0;
            int numberOfLeapYears = leapYears(1970, year);
            timestamp += 366L * 24 * 60 * 60 * numberOfLeapYears;
            timestamp += 365L * 24 * 60 * 60 * (year - 1970 - numberOfLeapYears);
            long daysUpToMonth = isLeapYear(year) ? LEAP_YEAR_DAYS_BY_MONTH[month - 1] : YEAR_DAYS_BY_MONTH[month - 1];
            timestamp += ((daysUpToMonth + day - 1) * 24 * 60 * 60) +
                    (hour * 60 * 60) + (minute * 60) + (second);
            timestamp = timestamp * 1000 + millis;
            return timestamp;
        }

        // checkstyle, please ignore ParameterNumber for the next line
        private static long fallbackToGC(int year, int month, int dayOfMonth, int hourOfDay,
                                         int minute, int second, int millis) {
            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month - 1);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            c.set(Calendar.HOUR_OF_DAY, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, second);
            c.set(Calendar.MILLISECOND, millis);
            return c.getTimeInMillis();
        }

        private static int leapYears(int from, int end) {
            return leapYearsBefore(end) - leapYearsBefore(from + 1);
        }

        private static int leapYearsBefore(int year) {
            year--;
            return (year / 4) - (year / 100) + (year / 400);
        }

        private static boolean isLeapYear(int year) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }

    }
}
