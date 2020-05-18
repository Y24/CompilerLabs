package cn.org.y24.util;

public class StringUtil {
    public static String paddingRight(String target, int count) {
        return target + " ".repeat(Math.max(0, count - target.length()));
    }

    public static String paddingRightAll(String[] target, int[] count) {
        assert target.length == count.length;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < target.length; i++)
            stringBuilder.append(paddingRight(target[i], count[i]));
        return stringBuilder.toString();
    }


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
