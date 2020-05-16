package cn.org.y24.table;

import java.util.HashMap;
import java.util.Map;

public class ReservedWordTable {
    private final static Map<Character, Integer> single = new HashMap<>() {{
        this.put('*', 19);
        this.put('=', 12);
        this.put(')', 22);
        this.put('(', 21);
        this.put(';', 23);
        this.put('-', 18);
    }};
    private final static Map<String, Integer> keywords = new HashMap<>() {{
        this.put("begin", 1);
        this.put("end", 2);
        this.put("integer", 3);
        this.put("if", 4);
        this.put("then", 5);
        this.put("else", 6);
        this.put("function", 7);
        this.put("read", 8);
        this.put("write", 9);
    }};

    public int getSingleKind(char c) {
        return single.get(c);
    }

    public int getKeyWordKind(String keyword) {
        return keywords.getOrDefault(keyword, 10);
    }

    private final static ReservedWordTable instance = new ReservedWordTable();

    public static ReservedWordTable getInstance() {
        return instance;
    }
}
