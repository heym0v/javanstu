package poi;

/**
 * Своё исключение: «эта строка CSV битая».
 * Хранит код ошибки, чтобы окно могло показать, что именно не так.
 * extends Exception — значит, это проверяемое исключение:
 * компилятор заставит его поймать (try/catch) или объявить (throws).
 */
public class CsvException extends Exception {
    private final ErrorCode code;

    public CsvException(ErrorCode code, String message) {
        super(message); // текст ошибки хранит родительский класс Exception
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
