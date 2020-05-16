package cn.org.y24.uitl;

import cn.org.y24.enums.FileType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileProcessor {
    private final static FileProcessor instance = new FileProcessor();

    private FileProcessor() {
    }

    public static FileProcessor getInstance() {
        return instance;
    }

    static final Map<FileType, String> suffixTable = new HashMap<>() {{
        this.put(FileType.srcFile, ".pas");
        this.put(FileType.lexicalFile, ".dyd");
        this.put(FileType.syntaxFile, ".dys");
        this.put(FileType.semanticFile, ".dys");
        this.put(FileType.variableFile, ".var");
        this.put(FileType.procedureFile, ".pro");
        this.put(FileType.errorFile, ".err");
    }};

    public static String getSuffix(FileType type) {
        return suffixTable.get(type);
    }

    public PushbackReader readFile(String filename) {
        try {
            return new PushbackReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.err.println(String.format("File not found: %s", filename));
            e.printStackTrace();
        }
        return null;
    }

    public BufferedWriter writeFile(String filename) {
        try {
            return new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            System.err.println(String.format("IO exception: %s", filename));
            e.printStackTrace();
        }
        return null;
    }

}
