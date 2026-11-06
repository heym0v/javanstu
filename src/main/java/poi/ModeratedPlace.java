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

    @Override
    public String getType() {
        return "MODERATED";
    }

    @Override
    public String getTypeName() {
        return "На модерации";
    }
}
