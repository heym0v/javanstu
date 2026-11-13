package poi;

/**
 * ЛР 4. «Реестр» миграций: знает все шаги и применяет их по очереди.
 * Место из файла версии 1 проходит шаги 1 -> 2 -> 3, из версии 2 — только 2 -> 3,
 * из текущей версии 3 — ни одного.
 */
public class Migrator {
    // Шаги по порядку: STEPS[0] переводит 1 -> 2, STEPS[1] переводит 2 -> 3.
    // Новая версия 4 = новый класс MigrationV3toV4 в конце массива, остальной код не меняется
    private static final Migration[] STEPS = {new MigrationV1toV2(), new MigrationV2toV3()};

    /** Проверяет, что программа умеет читать файл этой версии. */
    public static void checkVersion(int version) throws FormatException {
        if (version < 1) {
            throw new FormatException(ErrorCode.UNSUPPORTED_VERSION, "неправильная версия файла: " + version);
        }
        if (version > Serializer.VERSION) {
            throw new FormatException(ErrorCode.VERSION_TOO_NEW, "файл версии " + version
                    + " создан более новой программой, а эта понимает версии 1–" + Serializer.VERSION);
        }
    }

    /** Переводит место из версии fromVersion в текущую, шаг за шагом. */
    public static void migrate(PlaceData place, int fromVersion) throws FormatException {
        checkVersion(fromVersion);
        for (Migration step : STEPS) {
            if (step.fromVersion() >= fromVersion) { // шаги для более старых версий пропускаем
                step.apply(place);
            }
        }
    }

    /**
     * Обратное действие для сохранения в старую версию: убирает поля, которых в ней ещё не было.
     * Эти данные теряются — поэтому окно об этом предупреждает.
     */
    public static void downgrade(PlaceData place, int toVersion) {
        if (toVersion < 3) {
            place.note = null;   // заметки нет в версиях 1 и 2
        }
        if (toVersion < 2) {
            place.rating = null; // рейтинга нет в версии 1
        }
    }

    /** Какие поля потеряются при сохранении в эту версию (для предупреждения). Пусто — ничего. */
    public static String lostFields(int toVersion) {
        if (toVersion < 2) {
            return "рейтинг и заметка";
        }
        if (toVersion < 3) {
            return "заметка";
        }
        return "";
    }
}
