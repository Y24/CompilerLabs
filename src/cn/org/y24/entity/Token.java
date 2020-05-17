package cn.org.y24.entity;

public class Token {
    final int kind;
    final String value;

    public int getKind() {
        return kind;
    }

    public String getValue() {
        return value;
    }

    private Token(int kind, String value) {
        this.kind = kind;
        this.value = value;
    }

    public static Token from(String target) {
        assert (target.length() == 19);
        return new Token(Integer.parseInt(target.substring(17, 19).trim()), target.substring(0, 16).trim());
    }
}
