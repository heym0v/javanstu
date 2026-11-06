package poi;

/** Коды ошибок при чтении строки CSV-файла. */
public enum ErrorCode {
    WRONG_FIELD_COUNT,  // в строке не 8 полей
    BAD_NUMBER,         // id, широта или долгота — не число
    BAD_COORDINATE,     // широта не в [-90; 90] или долгота не в [-180; 180]
    UNKNOWN_CATEGORY,   // такой категории нет в Category
    UNKNOWN_TYPE        // тип не PLACE, MODERATED или NETWORK
}
