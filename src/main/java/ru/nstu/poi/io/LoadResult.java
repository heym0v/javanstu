package ru.nstu.poi.io;

import ru.nstu.poi.model.Place;

import java.util.List;

/** Результат загрузки: корректные места и ошибки по пропущенным строкам. */
public record LoadResult(List<Place> places, List<CsvException> skipped) {
    public LoadResult {
        places = List.copyOf(places);
        skipped = List.copyOf(skipped);
    }
}
