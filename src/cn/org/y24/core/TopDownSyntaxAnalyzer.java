package cn.org.y24.core;

import cn.org.y24.entity.Token;
import cn.org.y24.enums.FileType;
import cn.org.y24.enums.SyntaxExceptResult;
import cn.org.y24.interfaces.IAnalyzer;
import cn.org.y24.util.FileProcessor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cn.org.y24.util.StringUtil.getErrorOutFormat;

public class TopDownSyntaxAnalyzer implements IAnalyzer {
    final String filename;
    List<String> errorMessage = new LinkedList<>();
    Token[] tokens;
    int index;
    int lineNumber;
    private final List<String> infoList = new ArrayList<>(30) {{
        for (int i = 0; i < 30; i++)
            this.add("");
        this.add(1, "Miss keyword: begin");
        this.add(2, "Miss keyword: end");
        this.add(3, "Miss keyword: integer");
        this.add(4, "Miss keyword: if");
        this.add(5, "Miss keyword: then");
        this.add(6, "Miss keyword: else");
        this.add(7, "Miss keyword: function");
        this.add(8, "Miss keyword: read");
        this.add(9, "Miss keyword: write");
        this.add(10, "Miss an identifier");
        this.add(11, "Miss a constant");
        this.add(12, "Miss boolean operator");
        this.add(18, "Miss a -");
        this.add(19, "Miss a *");
        this.add(20, "Miss a :=");
        this.add(21, "Miss a (");
        this.add(22, "Miss a )");
        this.add(23, "Miss a ;");

    }};

    public TopDownSyntaxAnalyzer(String filename) {
        this.filename = filename;
    }

    /* (1) 缺少符号错;
          (2) 符号匹配错;
          (3) 符号无定义或重复定义。
       */
    @Override
    public void analyze() throws IOException {

        // check if error occurs in lexical analyse.
        if (hasLexicalError(filename)) {
            System.out.println("Syntax analyse cannot continue because of lexical error!");
        }
        // clean work.
        errorMessage.clear();
        // init tokens and index
        final BufferedReader reader = FileProcessor.getInstance().bufferedReadFile(filename + FileProcessor.getSuffix(FileType.lexicalFile));
        final BufferedWriter writer = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.syntaxFile), false);
        String line;
        List<Token> list = new ArrayList<>();
        do {
            line = reader.readLine();
            if (line == null) {
                break;
            }
            writer.write(line);
            writer.newLine();
            list.add(Token.from(line));
        } while (true);
        reader.close();
        writer.flush();
        writer.close();
        tokens = list.toArray(new Token[0]);
        index = -1;
        lineNumber = 1;
        program();
        final BufferedWriter bufferedWriter = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.errorFile), true);

        errorMessage.forEach(s -> {
            try {
                bufferedWriter.write(s);
                bufferedWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Exception occurs when writing syntax error file.");
            }
        });
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    // program -> subProgram
    private void program() {
        subProgram();
    }

    // subProgram -> begin varStats ; execStats end
    private void subProgram() {
        checkIfEnd();
        tryMove(1);
        descriptionStats();
        execStats();
        tryMove(2);
    }

    private void descriptionStats() {
        checkIfEnd();
        if (next().getKind() != 3)
            return;
        descriptionStat();
        tryMove(23);
        descriptionStats();
    }

    private void descriptionStat() {
        checkIfEnd();
        tryMove(3);
        checkIfEnd();
        switch (next().getKind()) {
            case 10:
                varDescriptionStat();
                break;
            case 7:
                funcDescriptionStat();
                break;
            default:
                errorMessage.add(getErrorOutFormat("Miss identifier or function keyword", lineNumber));
        }
    }

    private void funcDescriptionStat() {
        tryMove(7);
        tryMove(10);
        tryMove(21);
        tryMove(10);
        tryMove(22);
        tryMove(23);
        funcBody();
    }

    private void funcBody() {
        tryMove(1);
        descriptionStats();
        execStats();
        tryMove(2);
    }

    private void varDescriptionStat() {
        tryMove(10);
        checkIfEnd();
    }


    private void execStats() {
        checkIfEnd();
        final int kind = next().getKind();
        if (List.of(8, 9, 10).contains(kind)) {
            execStat();
            tryMove(23);
            execStats();
        } else if (kind == 4) {
            execStat();
            execStats();
        }
    }

    private void execStat() {
        switch (next().getKind()) {
            case 8:
                readStat();
                break;
            case 9:
                writeStat();
                break;
            case 10:
                assignStat();
                break;
            case 4:
                ifStat();
                break;
            default:
                errorMessage.add(getErrorOutFormat("Miss identifier or keywords read,write,if", lineNumber));
        }
    }

    private void ifStat() {
        checkIfEnd();
        tryMove(4);
        conditionExp();
        tryMove(5);
        execStats();
        tryMove(6);
        execStats();
    }

    private void conditionExp() {
        checkIfEnd();
        arithmeticExp();
        final int kind = moveForward().getKind();
        if (kind > 17 || kind < 12)
            errorMessage.add(getErrorOutFormat("Miss boolean operator", lineNumber));
        arithmeticExp();
    }

    private void assignStat() {
        checkIfEnd();
        tryMove(10);
        tryMove(20);
        arithmeticExp();
    }

    private void arithmeticExp() {
        checkIfEnd();
        item();
        subArithmeticExp();
    }

    private void subArithmeticExp() {
        checkIfEnd();
        if (next().getKind() == 18) {
            moveForward();
            item();
            subArithmeticExp();
        }
    }


    private void item() {
        checkIfEnd();
        factor();
        subItem();
    }

    private void subItem() {
        checkIfEnd();
        if (next().getKind() == 19) {
            moveForward();
            factor();
            subItem();
        }
    }

    private void factor() {
        checkIfEnd();
        switch (moveForward().getKind()) {
            case 11:
                break;
            case 10:
                if (next().getKind() == 21) {
                    moveForward();
                    arithmeticExp();
                    tryMove(22);

                }
                break;
            default:
                errorMessage.add(getErrorOutFormat("Miss constant or identifier", lineNumber));
        }
    }


    private void writeStat() {
        checkIfEnd();
        tryMove(9);
        tryMove(21);
        tryMove(10);
        tryMove(22);
    }

    private void readStat() {
        checkIfEnd();
        tryMove(8);
        tryMove(21);
        tryMove(10);
        tryMove(22);
    }

    private void drop() {
        int kind;
        do {
            kind = tokens[index].getKind();
            index++;
            if (kind == 23 || kind == 25)
                continue;
            if (kind == 24) {
                lineNumber++;
                continue;
            }
            break;
        } while (true);

    }

    private Token next() {
        int temp = index + 1;
        while (tokens[temp].getKind() == 24)
            temp++;
        return tokens[temp];
    }

    private Token moveForward() {
        index++;
        while (tokens[index].getKind() == 24) {
            lineNumber++;
            index++;
        }
        return tokens[index];
    }

    private void tryMove(int kind) {
        if (next().getKind() != kind)
            errorMessage.add(getErrorOutFormat(infoList.get(kind), lineNumber));
        else moveForward();
    }

    private SyntaxExceptResult expect(int kind) {
        return moveForward().getKind() == kind ? SyntaxExceptResult.success : SyntaxExceptResult.normalFail;
    }

    private void checkIfEnd() {
        if (index >= tokens.length - 1) {
            try {
                endWord();
            } catch (IOException e) {
                System.err.println("Exception occurs when write syntax error file.");
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    private void endWord() throws IOException {
        final BufferedWriter writer = FileProcessor.getInstance().bufferedWriteFile(filename + FileProcessor.getSuffix(FileType.errorFile), false);
        errorMessage.forEach(s -> {
            try {
                writer.write(s);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Exception when writing syntax error result");
            }

        });
        writer.flush();
        writer.close();
    }

    private boolean hasLexicalError(String file) throws IOException {
        FileProcessor fileProcessor = FileProcessor.getInstance();
        final Reader reader = fileProcessor.bufferedReadFile(file + FileProcessor.getSuffix(FileType.errorFile));
        int errorFirstChar = reader.read();
        reader.close();
        return errorFirstChar != -1;
    }
}
