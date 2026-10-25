package ru.nstu.poi.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CsvLoaderTest {
    private final CsvLoader loader = new CsvLoader();

    @TempDir
    Path dir;

    private Path write(String content) throws IOException {
        Path file = dir.resolve("test.csv");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void load_brokenLine_isSkipped() throws Exception {
        Path file = write(CsvFormat.HEADER + "\n"
                + "PLACE;1;Место;OTHER;55.0;82.9;;\n"
                + "PLACE;2;Битая\n");
        LoadResult result = loader.load(file);
        assertEquals(1, result.places().size());
    }

    @Test
    void load_brokenLine_isReportedWithLineNumber() throws Exception {
        Path file = write(CsvFormat.HEADER + "\n"
                + "PLACE;1;Место;OTHER;55.0;82.9;;\n"
                + "PLACE;2;Битая\n");
        LoadResult result = loader.load(file);
        assertEquals(3, result.skipped().get(0).getLineNumber());
    }

    @Test
    void load_duplicateId_skipsSecondLine() throws Exception {
        Path file = write(CsvFormat.HEADER + "\n"
                + "PLACE;1;Первое;OTHER;55.0;82.9;;\n"
                + "PLACE;1;Второе;OTHER;55.1;82.8;;\n");
        LoadResult result = loader.load(file);
        assertEquals(CsvErrorCode.DUPLICATE_ID, result.skipped().get(0).getCode());
    }

    @Test
    void load_wrongHeader_throwsBadHeader() throws IOException {
        Path file = write("id,name\n1,Место\n");
        CsvException e = assertThrows(CsvException.class, () -> loader.load(file));
        assertEquals(CsvErrorCode.BAD_HEADER, e.getCode());
    }

    @Test
    void load_missingFile_throwsFileReadError() {
        CsvException e = assertThrows(CsvException.class, () -> loader.load(dir.resolve("nope.csv")));
        assertEquals(CsvErrorCode.FILE_READ_ERROR, e.getCode());
    }
}
