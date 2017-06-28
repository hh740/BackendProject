package com.codelab.common.utils;

/**
 * Created by wangke on 17/6/28.
 */

import sun.security.action.GetPropertyAction;

import java.io.ByteArrayOutputStream;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Random;

public class StringUtil {
    public static String LINE_SEPARATOR = (String) AccessController.doPrivileged(new GetPropertyAction("line.separator"));
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final char[] c = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M'};

    public StringUtil() {
    }

    public static String getRandomString(int size) {
        Random random = new Random();
        StringBuffer sb = new StringBuffer(size);

        for(int i = 0; i < size; ++i) {
            sb.append(c[Math.abs(random.nextInt()) % c.length]);
        }

        return sb.toString();
    }

    public static String safeToString(Object object) {
        try {
            return object.toString();
        } catch (Throwable var2) {
            return "<toString() failure: " + var2 + ">";
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String intervalToString(long millis) {
        StringBuilder buf = new StringBuilder();
        boolean started = false;
        long days = millis / 86400000L;
        if(days != 0L) {
            buf.append(days).append("d ");
            started = true;
        }

        long hours = millis / 3600000L % 24L;
        if(started || hours != 0L) {
            buf.append(hours).append("h ");
        }

        long minutes = millis / 60000L % 60L;
        if(started || minutes != 0L) {
            buf.append(minutes).append("m ");
        }

        long seconds = millis / 1000L % 60L;
        if(started || seconds != 0L) {
            buf.append(seconds).append("s ");
        }

        buf.append(millis % 1000L).append("ms");
        return buf.toString();
    }

    public static final String dumpAsHex(byte[] byteBuffer, int length) {
        StringBuffer outputBuf = new StringBuffer(length * 4);
        int p = 0;
        int rows = length / 8;

        int n;
        int i;
        int b;
        for(n = 0; n < rows && p < length; ++n) {
            i = p;

            for(b = 0; b < 8; ++b) {
                String b1 = Integer.toHexString(byteBuffer[i] & 255);
                if(b1.length() == 1) {
                    b1 = "0" + b1;
                }

                outputBuf.append(b1 + " ");
                ++i;
            }

            outputBuf.append("    ");

            for(b = 0; b < 8; ++b) {
                int var10 = 255 & byteBuffer[p];
                if(var10 > 32 && var10 < 127) {
                    outputBuf.append((char)var10 + " ");
                } else {
                    outputBuf.append(". ");
                }

                ++p;
            }

            outputBuf.append("\n");
        }

        n = 0;

        for(i = p; i < length; ++i) {
            String var9 = Integer.toHexString(byteBuffer[i] & 255);
            if(var9.length() == 1) {
                var9 = "0" + var9;
            }

            outputBuf.append(var9 + " ");
            ++n;
        }

        for(i = n; i < 8; ++i) {
            outputBuf.append("   ");
        }

        outputBuf.append("    ");

        for(i = p; i < length; ++i) {
            b = 255 & byteBuffer[i];
            if(b > 32 && b < 127) {
                outputBuf.append((char)b + " ");
            } else {
                outputBuf.append(". ");
            }
        }

        outputBuf.append("\n");
        return outputBuf.toString();
    }

    public static byte[] escapeEasternUnicodeByteStream(byte[] origBytes, String origString, int offset, int length) {
        if(origBytes != null && origBytes.length != 0) {
            int bytesLen = origBytes.length;
            int bufIndex = 0;
            int strIndex = 0;
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);

            while(true) {
                if(origString.charAt(strIndex) == 92) {
                    bytesOut.write(origBytes[bufIndex++]);
                } else {
                    int loByte = origBytes[bufIndex];
                    if(loByte < 0) {
                        loByte += 256;
                    }

                    bytesOut.write(loByte);
                    int hiByte;
                    if(loByte >= 128) {
                        if(bufIndex < bytesLen - 1) {
                            hiByte = origBytes[bufIndex + 1];
                            if(hiByte < 0) {
                                hiByte += 256;
                            }

                            bytesOut.write(hiByte);
                            ++bufIndex;
                            if(hiByte == 92) {
                                bytesOut.write(hiByte);
                            }
                        }
                    } else if(loByte == 92 && bufIndex < bytesLen - 1) {
                        hiByte = origBytes[bufIndex + 1];
                        if(hiByte < 0) {
                            hiByte += 256;
                        }

                        if(hiByte == 98) {
                            bytesOut.write(92);
                            bytesOut.write(98);
                            ++bufIndex;
                        }
                    }

                    ++bufIndex;
                }

                if(bufIndex >= bytesLen) {
                    return bytesOut.toByteArray();
                }

                ++strIndex;
            }
        } else {
            return origBytes;
        }
    }

    public static String toString(byte[] bytes) {
        if(bytes != null && bytes.length != 0) {
            StringBuffer buffer = new StringBuffer();
            byte[] arr$ = bytes;
            int len$ = bytes.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                byte byt = arr$[i$];
                buffer.append((char)byt);
            }

            return buffer.toString();
        } else {
            return "";
        }
    }

    public static String toString(Object[] objs) {
        if(objs != null && objs.length != 0) {
            StringBuffer buffer = new StringBuffer("[");

            for(int i = 0; i < objs.length; ++i) {
                buffer.append(String.valueOf(objs[i]));
                if(i != objs.length - 1) {
                    buffer.append(",");
                }
            }

            buffer.append("]");
            return buffer.toString();
        } else {
            return "[]";
        }
    }

    public static boolean equals(String str1, String str2) {
        return str1 == null?str2 == null:str1.equals(str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null?str2 == null:str1.equalsIgnoreCase(str2);
    }

    public static String[] split(String str) {
        return split(str, (String)null, -1);
    }

    public static String[] split(String str, char separatorChar) {
        if(str == null) {
            return null;
        } else {
            int length = str.length();
            if(length == 0) {
                return EMPTY_STRING_ARRAY;
            } else {
                ArrayList list = new ArrayList();
                int i = 0;
                int start = 0;
                boolean match = false;

                while(i < length) {
                    if(str.charAt(i) == separatorChar) {
                        if(match) {
                            list.add(str.substring(start, i));
                            match = false;
                        }

                        ++i;
                        start = i;
                    } else {
                        match = true;
                        ++i;
                    }
                }

                if(match) {
                    list.add(str.substring(start, i));
                }

                return (String[])((String[])list.toArray(new String[list.size()]));
            }
        }
    }

    public static String[] split(String str, String separatorChars) {
        return split(str, separatorChars, -1);
    }

    public static String[] split(String str, String separatorChars, int max) {
        if(str == null) {
            return null;
        } else {
            int length = str.length();
            if(length == 0) {
                return EMPTY_STRING_ARRAY;
            } else {
                ArrayList list = new ArrayList();
                int sizePlus1 = 1;
                int i = 0;
                int start = 0;
                boolean match = false;
                if(separatorChars == null) {
                    while(i < length) {
                        if(Character.isWhitespace(str.charAt(i))) {
                            if(match) {
                                if(sizePlus1++ == max) {
                                    i = length;
                                }

                                list.add(str.substring(start, i));
                                match = false;
                            }

                            ++i;
                            start = i;
                        } else {
                            match = true;
                            ++i;
                        }
                    }
                } else if(separatorChars.length() == 1) {
                    char sep = separatorChars.charAt(0);

                    while(i < length) {
                        if(str.charAt(i) == sep) {
                            if(match) {
                                if(sizePlus1++ == max) {
                                    i = length;
                                }

                                list.add(str.substring(start, i));
                                match = false;
                            }

                            ++i;
                            start = i;
                        } else {
                            match = true;
                            ++i;
                        }
                    }
                } else {
                    while(i < length) {
                        if(separatorChars.indexOf(str.charAt(i)) >= 0) {
                            if(match) {
                                if(sizePlus1++ == max) {
                                    i = length;
                                }

                                list.add(str.substring(start, i));
                                match = false;
                            }

                            ++i;
                            start = i;
                        } else {
                            match = true;
                            ++i;
                        }
                    }
                }

                if(match) {
                    list.add(str.substring(start, i));
                }

                return (String[])((String[])list.toArray(new String[list.size()]));
            }
        }
    }

    public static String replaceOnce(String text, String repl, String with) {
        return replace(text, repl, with, 1);
    }

    public static String replace(String text, String repl, String with) {
        return replace(text, repl, with, -1);
    }

    public static String replace(String text, String repl, String with, int max) {
        if(text != null && repl != null && with != null && repl.length() != 0 && max != 0) {
            StringBuffer buf = new StringBuffer(text.length());
            int start = 0;
            boolean end = false;

            int var7;
            while((var7 = text.indexOf(repl, start)) != -1) {
                buf.append(text.substring(start, var7)).append(with);
                start = var7 + repl.length();
                --max;
                if(max == 0) {
                    break;
                }
            }

            buf.append(text.substring(start));
            return buf.toString();
        } else {
            return text;
        }
    }

    public static String replaceChars(String str, char searchChar, char replaceChar) {
        return str == null?null:str.replace(searchChar, replaceChar);
    }

    public static String replaceChars(String str, String searchChars, String replaceChars) {
        if(str != null && str.length() != 0 && searchChars != null && searchChars.length() != 0) {
            char[] chars = str.toCharArray();
            int len = chars.length;
            boolean modified = false;
            int i = 0;

            for(int isize = searchChars.length(); i < isize; ++i) {
                char searchChar = searchChars.charAt(i);
                int j;
                if(replaceChars != null && i < replaceChars.length()) {
                    for(j = 0; j < len; ++j) {
                        if(chars[j] == searchChar) {
                            chars[j] = replaceChars.charAt(i);
                            modified = true;
                        }
                    }
                } else {
                    j = 0;

                    for(int j1 = 0; j1 < len; ++j1) {
                        if(chars[j1] != searchChar) {
                            chars[j++] = chars[j1];
                        } else {
                            modified = true;
                        }
                    }

                    len = j;
                }
            }

            if(!modified) {
                return str;
            } else {
                return new String(chars, 0, len);
            }
        } else {
            return str;
        }
    }
}
