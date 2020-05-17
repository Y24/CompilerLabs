package cn.org.y24;

import cn.org.y24.core.LexicalAnalyzer;
import cn.org.y24.core.TopDownSyntaxAnalyzer;
import cn.org.y24.interfaces.IAnalyzer;
import cn.org.y24.interfaces.ICompiler;
import cn.org.y24.table.SymbolTable;

import java.io.IOException;


public class SimpleCompiler implements ICompiler {
    final String filename;
    final IAnalyzer lexicalAnalyzer;
    final IAnalyzer syntaxAnalyzer;
    final SymbolTable symbolTable;

    public SimpleCompiler(String filename) {
        this.filename = filename;
        lexicalAnalyzer = new LexicalAnalyzer(filename);
        syntaxAnalyzer = new TopDownSyntaxAnalyzer(filename);
        symbolTable = new SymbolTable();
    }

    @Override
    public void compile() {
        try {
            lexicalAnalyzer.analyze();
            syntaxAnalyzer.analyze();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
