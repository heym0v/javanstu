package ru.nstu.poi.io;

/** Формат CSV-файла: разделитель, заголовок и номера колонок. */
public final class CsvFormat {
    public static final String DELIMITER = ";";
    public static final String HEADER =
            String.join(DELIMITER, "type", "id", "name", "category", "latitude", "longitude", "website", "phone");
    public static final int FIELD_COUNT = 8;

    public static final int COL_TYPE = 0;
    public static final int COL_ID = 1;
    public static final int COL_NAME = 2;
    public static final int COL_CATEGORY = 3;
    public static final int COL_LATITUDE = 4;
    public static final int COL_LONGITUDE = 5;
    public static final int COL_WEBSITE = 6;
    public static final int COL_PHONE = 7;

    private CsvFormat() {
    }
}
