package ru.nstu.poi.service;

import ru.nstu.poi.io.CsvException;
import ru.nstu.poi.io.CsvLoader;
import ru.nstu.poi.io.CsvWriter;
import ru.nstu.poi.io.LoadResult;
import ru.nstu.poi.model.Editable;
import ru.nstu.poi.model.NetworkPlace;
import ru.nstu.poi.model.Place;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Хранит места и отвечает за операции над ними. GUI только вызывает эти методы.
 * Методы, меняющие список, вызываются из потока GUI; readCsv/saveCsv состояние
 * не трогают, поэтому их можно выполнять в фоне.
 */
public class PlaceService {
    private final List<Place> places = new ArrayList<>();
    private final CsvLoader loader;
    private final CsvWriter writer;

    public PlaceService() {
        this(new CsvLoader(), new CsvWriter());
    }

    public PlaceService(CsvLoader loader, CsvWriter writer) {
        this.loader = loader;
        this.writer = writer;
    }

    public List<Place> getAll() {
        return Collections.unmodifiableList(places);
    }

    public List<Place> snapshot() {
        return List.copyOf(places);
    }

    public LoadResult readCsv(Path file) throws CsvException {
        return loader.load(file);
    }

    public void saveCsv(Path file, List<Place> snapshot) throws CsvException {
        writer.save(file, snapshot);
    }

    public void replaceAll(List<Place> newPlaces) {
        places.clear();
        places.addAll(newPlaces);
    }

    public long nextId() {
        return places.stream().mapToLong(Place::getId).max().orElse(0L) + 1;
    }

    public void add(NetworkPlace place) throws ValidationException {
        requireValid(place);
        boolean idTaken = places.stream().anyMatch(p -> p.getId() == place.getId());
        if (idTaken) {
            throw new ValidationException(List.of("Место с id " + place.getId() + " уже существует"));
        }
        places.add(place);
    }

    public void update(NetworkPlace target, NetworkPlace changes) throws ValidationException {
        requireValid(changes);
        target.updateFrom(changes);
    }

    private static void requireValid(Editable entity) throws ValidationException {
        List<String> errors = entity.validate();
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
