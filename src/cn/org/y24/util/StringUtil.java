package cn.org.y24.util;

public class StringUtil {
    public static String getLexicalOutFormat(String target, int kind) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append(" ".repeat(Math.max(0, 16 - target.length())));
        stringBuffer.append(target).append(" ");
        if (kind < 10) {
            stringBuffer.append(' ');
        }
        stringBuffer.append(kind);
        return stringBuffer.toString();
    }

    public static String getErrorOutFormat(String description, int line) {
        return "***LINE:" +
                line +
                "  " +
                description;
    }
}
