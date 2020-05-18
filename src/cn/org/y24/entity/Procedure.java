package cn.org.y24.entity;

import cn.org.y24.util.StringUtil;

public class Procedure {
    public final static int[] counter = {16, 16, 16, 16, 16};
    final String name;
    final String type = "integer";
    final int level;
    int firstVarAddress;
    int lastVarAddress;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Procedure && ((Procedure) obj).name.equals(name) &&
                ((Procedure) obj).level == level;
    }

    @Override
    public String toString() {
        return StringUtil.paddingRightAll(new String[]{name, type, level + "", firstVarAddress + "", lastVarAddress + ""}, counter);
    }

    public void setFirstVarAddress(int firstVarAddress) {
        this.firstVarAddress = firstVarAddress;
    }

    public void setLastVarAddress(int lastVarAddress) {
        this.lastVarAddress = lastVarAddress;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public int getFirstVarAddress() {
        return firstVarAddress;
    }

    public int getLastVarAddress() {
        return lastVarAddress;
    }

    public Procedure(String name, int level) {
        this.name = name;
        this.level = level;
    }
}
