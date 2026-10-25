package ru.nstu.poi.io;

import ru.nstu.poi.model.Place;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Читает CSV построчно. Битые строки пропускаются и попадают в отчёт. */
public final class CsvLoader {
    private static final char BOM = '\uFEFF';
    private static final int HEADER_LINE = 1;

    private final PlaceCsvCodec codec = new PlaceCsvCodec();

    /**
     * @throws CsvException если файл нельзя прочитать целиком (нет доступа, кодировка, заголовок)
     */
    public LoadResult load(Path file) throws CsvException {
        List<Place> places = new ArrayList<>();
        List<CsvException> skipped = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            checkHeader(reader.readLine());
            String line;
            int lineNumber = HEADER_LINE;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    Place place = codec.parse(line, lineNumber);
                    if (!seenIds.add(place.getId())) {
                        throw new CsvException(CsvErrorCode.DUPLICATE_ID, lineNumber, "id = " + place.getId());
                    }
                    places.add(place);
                } catch (CsvException e) {
                    skipped.add(e);   // строка битая — пропускаем, но запоминаем причину
                }
            }
        } catch (MalformedInputException e) {
            throw new CsvException(CsvErrorCode.BAD_ENCODING, file.toString(), e);
        } catch (IOException e) {
            throw new CsvException(CsvErrorCode.FILE_READ_ERROR, file + ": " + e.getMessage(), e);
        }
        return new LoadResult(places, skipped);
    }

    private static void checkHeader(String header) throws CsvException {
        if (header == null) {
            throw new CsvException(CsvErrorCode.BAD_HEADER, HEADER_LINE, "файл пуст");
        }
        String cleaned = header.isEmpty() || header.charAt(0) != BOM ? header : header.substring(1);
        if (!CsvFormat.HEADER.equalsIgnoreCase(cleaned.trim())) {
            throw new CsvException(CsvErrorCode.BAD_HEADER, HEADER_LINE, "ожидалось: " + CsvFormat.HEADER);
        }
    }
}
