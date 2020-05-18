package cn.org.y24.entity;

import cn.org.y24.util.StringUtil;

public class Variable {
    public static final int[] counter = {16, 16, 16, 16, 16, 16};
    final String name;
    final String proc;
    final int kind;
    final String type = "integer";
    final int level;
    final int address;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return StringUtil.paddingRightAll(new String[]{name, proc, kind + "", type, level + "", address + ""}, counter);
    }

    public String getProc() {
        return proc;
    }

    public int getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getAddress() {
        return address;
    }

    public Variable(String name, String proc, int kind, int level, int address) {
        this.name = name;
        this.proc = proc;
        this.kind = kind;
        this.level = level;
        this.address = address;
    }

}
