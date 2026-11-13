package poi;

/**
 * Место «на модерации»: только для чтения.
 * У него нет сеттеров, и оно не реализует Editable,
 * поэтому кнопка «Изменить» для него недоступна.
 * Такие места появляются только из файла.
 */
public class ModeratedPlace extends Place {

    public ModeratedPlace(int id, String name, Category category, double latitude, double longitude) {
        super(id, name, category, latitude, longitude); // вызов конструктора Place
    }

    /** ЛР 4: с рейтингом и заметкой (нужен при чтении из файла). */
    public ModeratedPlace(int id, String name, Category category, double latitude, double longitude,
                          double rating, String note) {
        super(id, name, category, latitude, longitude, rating, note);
    }

    @Override
    public String getType() {
        return "MODERATED";
    }

    @Override
    public String getTypeName() {
        return "На модерации";
    }
}
