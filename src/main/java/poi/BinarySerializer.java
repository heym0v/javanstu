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
 *   int    VERSION — версия формата (ЛР 4: заголовок с версией)
 *   int    count   — сколько мест
 *   count раз: type, id, name, category, latitude, longitude, website, phone
 *              + double rating (с версии 2)
 *              + String note   (с версии 3)
 * Версия 1 устроена точно как в ЛР 3, поэтому старая программа сможет её прочитать.
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
    public void save(File file, SimpleArray<Place> places, int version) throws IOException {
        // DataOutputStream умеет писать int, double, String; Buffered — копит байты и пишет большими кусками
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(MAGIC);
            out.writeInt(version);
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
                // ЛР 4: новые поля пишем, только если они есть в этой версии
                if (version >= 2) {
                    out.writeDouble(d.rating);
                }
                if (version >= 3) {
                    out.writeUTF(d.note);
                }
            }
        }
    }

    @Override
    public int readVersion(File file) throws IOException, FormatException {
        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            return readHeader(in);
        } catch (EOFException e) {
            throw new FormatException(ErrorCode.BROKEN_FILE, "файл слишком короткий");
        }
    }

    /** Читает подпись и версию. Возвращает версию. */
    private int readHeader(DataInputStream in) throws IOException, FormatException {
        if (in.readInt() != MAGIC) {
            throw new FormatException(ErrorCode.BROKEN_FILE, "это не файл нашего бинарного формата");
        }
        return in.readInt();
    }

    @Override
    public SimpleArray<Place> load(File file) throws IOException, FormatException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            // Читать нужно строго в том же порядке, в каком писали
            int version = readHeader(in);
            Migrator.checkVersion(version); // версия из «будущего» — сразу ошибка
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
                // ЛР 4: сколько полей читать — зависит от версии файла
                if (version >= 2) {
                    d.rating = in.readDouble();
                }
                if (version >= 3) {
                    d.note = in.readUTF();
                }
                Migrator.migrate(d, version); // старое место дотягиваем до текущей версии
                places.add(d.toPlace());
            }
            return places;
        } catch (EOFException | UTFDataFormatException e) {
            // EOF — файл кончился раньше времени (обрезан); UTFDataFormat — вместо строки мусор
            throw new FormatException(ErrorCode.BROKEN_FILE, "файл обрезан или испорчен");
        }
    }
}
