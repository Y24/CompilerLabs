package cn.org.y24.util;


public class SyntaxAnalyzerRecorder {
    public int index;
    public int lineNumber;
    public String varProc;
    public String varType;
    public int varLevel;
    public int varAddress;
    public String procType;
    public int procLevel;

    public void init() {
        index = -1;
        lineNumber = 1;
        varProc = "main";
        varType = "integer";
        varLevel = 0;
        varAddress = -1;
        procType = "integer";
        procLevel = 0;
    }
}
