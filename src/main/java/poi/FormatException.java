package poi;

/**
 * ЛР 3. Своё исключение: «файл бинарного или JSON-формата битый».
 * Устроено так же, как CsvException: хранит код ошибки из ErrorCode.
 */
public class FormatException extends Exception {
    private final ErrorCode code;

    public FormatException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
