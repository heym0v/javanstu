package ru.nstu.poi.model;

/**
 * Место «на модерации» — только для чтения.
 * Класс final и без сеттеров; Editable не реализует, поэтому кнопка «Изменить»
 * для него заблокирована, а создать его можно только загрузкой из файла.
 */
public final class ModeratedPlace extends Place {

    public ModeratedPlace(long id, String name, Category category, double latitude, double longitude) {
        super(id, name, category, latitude, longitude);
    }

    @Override
    public PlaceType getType() {
        return PlaceType.MODERATED;
    }
}
