package poi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

/**
 * ЛР 3. Бинарный формат: числа и строки пишутся байтами, как они лежат в памяти.
 * Устройство файла:
 *   int    MAGIC   — «подпись» нашего формата, чтобы отличить его от чужих файлов
 *   int    VERSION — версия формата
 *   int    count   — сколько мест
 *   count раз: type, id, name, category, latitude, longitude, website, phone
 */
public class BinarySerializer implements Serializer {
    public static final int MAGIC = 0x504F4921; // байты 'P' 'O' 'I' '!'

    @Override
    public String getName() {
        return "Бинарный";
    }

    @Override
    public String getExtension() {
        return "bin";
    }

    @Override
    public void save(File file, SimpleArray<Place> places) throws IOException {
        // DataOutputStream умеет писать int, double, String; Buffered — копит байты и пишет большими кусками
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(places.size());
            for (int i = 0; i < places.size(); i++) {
                PlaceData d = PlaceData.from(places.get(i));
                out.writeUTF(d.type);     // writeUTF: сначала длина строки (2 байта), потом сами буквы
                out.writeInt(d.id);       // 4 байта
                out.writeUTF(d.name);
                out.writeUTF(d.category);
                out.writeDouble(d.latitude);  // 8 байт
                out.writeDouble(d.longitude);
                out.writeUTF(d.website);
                out.writeUTF(d.phone);
            }
        }
    }

    @Override
    public SimpleArray<Place> load(File file) throws IOException, FormatException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            // Читать нужно строго в том же порядке, в каком писали
            if (in.readInt() != MAGIC) {
                throw new FormatException(ErrorCode.BROKEN_FILE, "это не файл нашего бинарного формата");
            }
            int version = in.readInt();
            if (version != VERSION) {
                throw new FormatException(ErrorCode.UNSUPPORTED_VERSION,
                        "версия файла " + version + ", а программа понимает только " + VERSION);
            }
            int count = in.readInt();
            if (count < 0) {
                throw new FormatException(ErrorCode.BROKEN_FILE, "отрицательное количество мест: " + count);
            }
            SimpleArray<Place> places = new SimpleArray<>();
            for (int i = 0; i < count; i++) {
                PlaceData d = new PlaceData();
                d.type = in.readUTF();
                d.id = in.readInt();
                d.name = in.readUTF();
                d.category = in.readUTF();
                d.latitude = in.readDouble();
                d.longitude = in.readDouble();
                d.website = in.readUTF();
                d.phone = in.readUTF();
                places.add(d.toPlace());
            }
            return places;
        } catch (EOFException | UTFDataFormatException e) {
            // EOF — файл кончился раньше времени (обрезан); UTFDataFormat — вместо строки мусор
            throw new FormatException(ErrorCode.BROKEN_FILE, "файл обрезан или испорчен");
        }
    }
}
