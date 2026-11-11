package poi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

/**
 * ЛР 3. JSON-формат через библиотеку Jackson. Файл — обычный текст:
 * {
 *   "version" : 1,
 *   "places" : [ { "type" : "NETWORK", "id" : 1, "name" : "...", ... }, ... ]
 * }
 */
public class JsonSerializer implements Serializer {

    /** Что лежит в файле целиком: версия и массив мест. Jackson сам превращает это в JSON и обратно. */
    public static class PlacesFile {
        public int version;
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
    public void save(File file, SimpleArray<Place> places) throws IOException {
        PlacesFile data = new PlacesFile();
        data.version = VERSION;
        data.places = new PlaceData[places.size()];
        for (int i = 0; i < places.size(); i++) {
            data.places[i] = PlaceData.from(places.get(i));
        }
        mapper.writeValue(file, data); // одна строка — и весь объект записан в JSON
    }

    @Override
    public SimpleArray<Place> load(File file) throws IOException, FormatException {
        PlacesFile data;
        try {
            data = mapper.readValue(file, PlacesFile.class); // JSON -> объект PlacesFile
        } catch (JsonProcessingException e) {
            // Jackson бросает это, если текст — не JSON или в нём неизвестные поля
            throw new FormatException(ErrorCode.BROKEN_FILE, "файл не похож на наш JSON");
        }
        if (data.version != VERSION) {
            throw new FormatException(ErrorCode.UNSUPPORTED_VERSION,
                    "версия файла " + data.version + ", а программа понимает только " + VERSION);
        }
        if (data.places == null) {
            throw new FormatException(ErrorCode.BROKEN_FILE, "в файле нет списка places");
        }
        SimpleArray<Place> places = new SimpleArray<>();
        for (int i = 0; i < data.places.length; i++) {
            if (data.places[i] == null) {
                throw new FormatException(ErrorCode.BROKEN_FILE, "пустое место номер " + (i + 1));
            }
            places.add(data.places[i].toPlace());
        }
        return places;
    }
}
