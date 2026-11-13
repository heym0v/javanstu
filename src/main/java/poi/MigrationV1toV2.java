package poi;

/** ЛР 4. Версия 1 -> 2: появился рейтинг. В старых местах его нет — ставим 0.0. */
public class MigrationV1toV2 implements Migration {

    @Override
    public int fromVersion() {
        return 1;
    }

    @Override
    public void apply(PlaceData place) {
        if (place.rating == null) {
            place.rating = Place.DEFAULT_RATING; // не null, а значение по умолчанию
        }
    }
}
