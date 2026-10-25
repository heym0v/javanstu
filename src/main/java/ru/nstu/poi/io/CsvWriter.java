package ru.nstu.poi.io;

import ru.nstu.poi.model.Place;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Сохраняет места в CSV в том же формате, что читает {@link CsvLoader}. */
public final class CsvWriter {
    private final PlaceCsvCodec codec = new PlaceCsvCodec();

    public void save(Path file, List<? extends Place> places) throws CsvException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(CsvFormat.HEADER);
            writer.newLine();
            for (Place place : places) {
                writer.write(codec.format(place));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new CsvException(CsvErrorCode.FILE_WRITE_ERROR, file + ": " + e.getMessage(), e);
        }
    }
}
