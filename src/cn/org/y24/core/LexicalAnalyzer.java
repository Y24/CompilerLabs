package cn.org.y24.core;

import cn.org.y24.enums.FileType;
import cn.org.y24.interfaces.IAnalyzer;
import cn.org.y24.table.ReservedWordTable;
import cn.org.y24.util.FileProcessor;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import static cn.org.y24.util.StringUtil.getErrorOutFormat;
import static cn.org.y24.util.StringUtil.getLexicalOutFormat;

public class LexicalAnalyzer implements IAnalyzer {
    final String filename;

    public LexicalAnalyzer(String filename) {
        this.filename = filename;
    }

    @Override
    public void analyze() throws IOException {
        FileProcessor fileProcessor = FileProcessor.getInstance();
        final PushbackReader reader = fileProcessor.pushBackReadFile(filename + FileProcessor.getSuffix(FileType.srcFile));
        List<String> analyseResult = new LinkedList<>();
        List<String> errorMessage = new LinkedList<>();
        int line = 1, column = 1;
        int current;
        do {
            current = reader.read();
            if (current == -1 || current == 0xffff) {
                analyseResult.add(getLexicalOutFormat("EOF", 25));
                break;
            }
            switch (current) {
                case ' ':
                    column++;
                    break;
                // For Win Linux Mac all.
                case '\r':
                    current = reader.read();
                    if (current != '\n')
                        reader.unread(current);
                    analyseResult.add(getLexicalOutFormat("EOLN", 24));
                    line++;
                    column = 1;
                    break;
                case '\n':
                    analyseResult.add(getLexicalOutFormat("EOLN", 24));
                    line++;
                    column = 1;
                    break;
                // single char token is the first guy to kill.
                case '*', ')', '(', ';', '=', '-':
                    analyseResult.add(getLexicalOutFormat((char) current + "", ReservedWordTable.getInstance().getSingleKind((char) current)));
                    column++;
                    break;
                case '<':
                    current = reader.read();
                    switch (current) {
                        case '>':
                            column += 2;
                            analyseResult.add(getLexicalOutFormat("<>", 13));
                            break;
                        case '=':
                            column += 2;
                            analyseResult.add(getLexicalOutFormat("<=", 14));
                            break;
                        default:
                            column++;
                            reader.unread(current);
                            analyseResult.add(getLexicalOutFormat("<", 15));
                    }
                    break;
                case '>':
                    current = reader.read();
                    if (current == '=') {
                        column += 2;
                        analyseResult.add(getLexicalOutFormat(">=", 16));
                    } else {
                        column++;
                        reader.unread(current);
                        analyseResult.add(getLexicalOutFormat(">", 17));
                    }
                    break;
                case ':':
                    current = reader.read();
                    if (current != '=') {
                        column++;
                        reader.unread(current);
                        errorMessage.add(getErrorOutFormat(":缺少=配对", line));
                    } else {
                        column += 2;
                        analyseResult.add(getLexicalOutFormat(":=", 20));
                    }
                    break;
                // Well, only low letter and digit is allowed from now on.
                default:
                    // keywords or identifiers
                    if (Character.isLowerCase(current)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        do {
                            stringBuilder.append((char) current);
                            column++;
                            current = reader.read();
                            if (!Character.isLowerCase(current) && !Character.isDigit(current)) {
                                String result = stringBuilder.toString();
                                if (result.length() > 16)
                                    errorMessage.add(getErrorOutFormat(String.format("标识符: %s 太长!", result), line));
                                else
                                    analyseResult.add(getLexicalOutFormat(result, ReservedWordTable.getInstance().getKeyWordKind(result)));
                                reader.unread(current);
                                column--;
                                break;
                            }
                        } while (true);
                    }
                    // constants.
                    else if (Character.isDigit(current)) {
                        StringBuilder stringBuilder = new StringBuilder();
                        do {
                            stringBuilder.append((char) current);
                            column++;
                            current = reader.read();
                            if (!Character.isDigit(current)) {
                                String result = stringBuilder.toString();
                                if (result.length() > 16)
                                    errorMessage.add(getErrorOutFormat(String.format("常数: %s 太长!", result), line));
                                else
                                    analyseResult.add(getLexicalOutFormat(result, 11));
                                reader.unread(current);
                                column--;
                                break;
                            }
                        } while (true);
                    }
                    // Unknown chars.
                    else {
                        column++;
                        errorMessage.add(getErrorOutFormat(String.format("未知字符：%c", current), line));
                    }
            }

        }
        while (true);
        // Now, deal with the output.

        final BufferedWriter lexicalOutWriter = fileProcessor.bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.lexicalFile), false);
        final BufferedWriter errorOutWriter = fileProcessor.bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.errorFile), false);
        errorOutWriter.write("");
        errorMessage.forEach(s -> {
            try {
                errorOutWriter.write(s);
                errorOutWriter.newLine();
            } catch (IOException e) {
                System.err.println("Exception occurs when writing lexical error message!");
                System.exit(1);
            }
        });
        analyseResult.forEach(s -> {
            try {
                lexicalOutWriter.write(s);
                lexicalOutWriter.newLine();
            } catch (IOException e) {
                System.err.println("Exception occurs when writing lexical analyse message!");
                System.exit(1);
            }

        });
        errorOutWriter.flush();
        errorOutWriter.close();
        lexicalOutWriter.flush();
        lexicalOutWriter.close();
    }
}
