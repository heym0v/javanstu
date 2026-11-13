package poi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * ЛР 3. JSON-формат через библиотеку Jackson. Файл — обычный текст:
 * {
 *   "schema_version" : 3,
 *   "places" : [ { "type" : "NETWORK", "id" : 1, "name" : "...", ..., "rating" : 4.5, "note" : "..." }, ... ]
 * }
 * ЛР 4: в ЛР 3 поле версии называлось "version". Такие файлы — это версия 1,
 * поэтому при чтении понимаем оба имени, а версию 1 и записываем по-старому: "version".
 */
public class JsonSerializer implements Serializer {

    /** Что лежит в файле целиком: версия и массив мест. Jackson сам превращает это в JSON и обратно. */
    @JsonInclude(JsonInclude.Include.NON_NULL) // поле со значением null в файл не пишется
    @JsonPropertyOrder({"schema_version", "version", "places"}) // версия — в самом начале файла
    public static class PlacesFile {
        @JsonProperty("schema_version") // имя поля в JSON отличается от имени в Java
        public Integer schemaVersion;   // версии 2 и 3
        public Integer version;         // старое имя из ЛР 3 — версия 1
        public PlaceData[] places;
    }

    // ObjectMapper — главный класс Jackson. INDENT_OUTPUT — красиво, с отступами, чтобы человек мог читать
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public String getName() {
        return "JSON";
    }

    @Override
    public String getExtension() {
        return "json";
    }

    @Override
    public void save(File file, SimpleArray<Place> places, int version) throws IOException {
        PlacesFile data = new PlacesFile();
        if (version == 1) {
            data.version = version;       // как в ЛР 3, чтобы старая программа открыла файл
        } else {
            data.schemaVersion = version;
        }
        data.places = new PlaceData[places.size()];
        for (int i = 0; i < places.size(); i++) {
            data.places[i] = PlaceData.from(places.get(i));
            Migrator.downgrade(data.places[i], version); // убираем поля, которых нет в этой версии
        }
        mapper.writeValue(file, data); // одна строка — и весь объект записан в JSON
    }

    /** Читает весь файл в объект PlacesFile. */
    private PlacesFile readFile(File file) throws IOException, FormatException {
        try {
            return mapper.readValue(file, PlacesFile.class); // JSON -> объект PlacesFile
        } catch (JsonProcessingException e) {
            // Jackson бросает это, если текст — не JSON или в нём неизвестные поля
            throw new FormatException(ErrorCode.BROKEN_FILE, "файл не похож на наш JSON");
        }
    }

    /** Версия из файла: новое имя поля или старое. */
    private int versionOf(PlacesFile data) throws FormatException {
        if (data.schemaVersion != null) {
            return data.schemaVersion;
        }
        if (data.version != null) {
            return data.version;
        }
        throw new FormatException(ErrorCode.BROKEN_FILE, "в файле не указана версия");
    }

    @Override
    public int readVersion(File file) throws IOException, FormatException {
        return versionOf(readFile(file));
    }

    @Override
    public SimpleArray<Place> load(File file) throws IOException, FormatException {
        PlacesFile data = readFile(file);
        int version = versionOf(data);
        Migrator.checkVersion(version); // версия из «будущего» — сразу ошибка
        if (data.places == null) {
            throw new FormatException(ErrorCode.BROKEN_FILE, "в файле нет списка places");
        }
        SimpleArray<Place> places = new SimpleArray<>();
        for (int i = 0; i < data.places.length; i++) {
            if (data.places[i] == null) {
                throw new FormatException(ErrorCode.BROKEN_FILE, "пустое место номер " + (i + 1));
            }
            Migrator.migrate(data.places[i], version); // старое место дотягиваем до текущей версии
            places.add(data.places[i].toPlace());
        }
        return places;
    }
}
