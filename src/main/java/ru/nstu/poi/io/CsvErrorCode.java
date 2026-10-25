package ru.nstu.poi.io;

/** Коды ошибок загрузки и сохранения CSV. */
public enum CsvErrorCode {
    FILE_READ_ERROR("Не удалось прочитать файл"),
    FILE_WRITE_ERROR("Не удалось записать файл"),
    BAD_ENCODING("Файл не в кодировке UTF-8"),
    BAD_HEADER("Неверная строка-заголовок"),
    WRONG_FIELD_COUNT("Неверное число полей"),
    EMPTY_FIELD("Пустое обязательное поле"),
    BAD_NUMBER("Некорректное число"),
    OUT_OF_RANGE("Значение вне допустимого диапазона"),
    UNKNOWN_TYPE("Неизвестный тип места"),
    UNKNOWN_CATEGORY("Неизвестная категория"),
    INVALID_VALUE("Недопустимое значение поля"),
    DUPLICATE_ID("Повторяющийся id");

    private final String description;

    CsvErrorCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
