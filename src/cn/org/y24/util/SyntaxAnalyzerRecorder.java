package cn.org.y24.util;


import java.util.Stack;

public class SyntaxAnalyzerRecorder {
    public int index;
    public int lineNumber;
    public Stack<String> varProc=new Stack<>();
    public String varType;
    public int varAddress;
    public String procType;
    public int level;

    public void init() {
        index = -1;
        lineNumber = 1;
        varProc.push("main");
        varType = "integer";
        level = -1;
        varAddress = 0;
        procType = "integer";
    }
}
