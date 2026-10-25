package ru.nstu.poi.io;

/** Ошибка работы с CSV: код ошибки + номер строки (0 — ошибка всего файла). */
public class CsvException extends Exception {
    public static final int NO_LINE = 0;

    private final CsvErrorCode code;
    private final int lineNumber;

    public CsvException(CsvErrorCode code, int lineNumber, String details) {
        super(buildMessage(code, lineNumber, details));
        this.code = code;
        this.lineNumber = lineNumber;
    }

    public CsvException(CsvErrorCode code, String details, Throwable cause) {
        super(buildMessage(code, NO_LINE, details), cause);
        this.code = code;
        this.lineNumber = NO_LINE;
    }

    public CsvErrorCode getCode() {
        return code;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    private static String buildMessage(CsvErrorCode code, int lineNumber, String details) {
        StringBuilder sb = new StringBuilder();
        if (lineNumber != NO_LINE) {
            sb.append("Строка ").append(lineNumber).append(": ");
        }
        sb.append(code.getDescription()).append(" [").append(code.name()).append(']');
        if (details != null && !details.isBlank()) {
            sb.append(" — ").append(details);
        }
        return sb.toString();
    }
}
