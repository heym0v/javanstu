package poi;

/**
 * ЛР 4. Версия 2 -> 3: появилась заметка. В старых местах её нет — ставим пустую строку.
 * Заодно проверяем рейтинг из версии 2: если он неправильный, мигрировать нельзя.
 */
public class MigrationV2toV3 implements Migration {

    @Override
    public int fromVersion() {
        return 2;
    }

    @Override
    public void apply(PlaceData place) throws FormatException {
        if (place.rating == null || !PlaceData.isGoodRating(place.rating)) {
            throw new FormatException(ErrorCode.MIGRATION_FAILED,
                    "у места " + place.id + " в версии 2 неправильный рейтинг: " + place.rating);
        }
        if (place.note == null) {
            place.note = Place.DEFAULT_NOTE;
        }
    }
}
