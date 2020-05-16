package cn.org.y24.core;

import cn.org.y24.interfaces.IAnalyzer;

public class SyntaxAnalyzer implements IAnalyzer {
    final String filename;

    public SyntaxAnalyzer(String filename) {
        this.filename = filename;
    }

    @Override
    public void analyze() {

    }
}
