package ru.nstu.poi.ui;

import ru.nstu.poi.model.NetworkPlace;
import ru.nstu.poi.model.Place;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/** Модель таблицы: только отображение, никакой логики. */
public class PlaceTableModel extends AbstractTableModel {
    private static final String[] COLUMNS =
            {"Тип", "ID", "Название", "Категория", "Широта", "Долгота", "Сайт", "Телефон"};
    private static final int COL_TYPE = 0;
    private static final int COL_ID = 1;
    private static final int COL_NAME = 2;
    private static final int COL_CATEGORY = 3;
    private static final int COL_LATITUDE = 4;
    private static final int COL_LONGITUDE = 5;
    private static final int COL_WEBSITE = 6;
    private static final int COL_PHONE = 7;

    private List<Place> places = List.of();

    public void setPlaces(List<Place> places) {
        this.places = List.copyOf(places);
        fireTableDataChanged();
    }

    public Place getPlaceAt(int modelRow) {
        return places.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return places.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return switch (column) {
            case COL_ID -> Long.class;
            case COL_LATITUDE, COL_LONGITUDE -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int row, int column) {
        Place place = places.get(row);
        return switch (column) {
            case COL_TYPE -> place.getType().getDisplayName();
            case COL_ID -> place.getId();
            case COL_NAME -> place.getName();
            case COL_CATEGORY -> place.getCategory().getDisplayName();
            case COL_LATITUDE -> place.getLatitude();
            case COL_LONGITUDE -> place.getLongitude();
            case COL_WEBSITE -> place instanceof NetworkPlace network ? network.getWebsite() : "";
            case COL_PHONE -> place instanceof NetworkPlace network ? network.getPhone() : "";
            default -> throw new IllegalArgumentException("Нет колонки " + column);
        };
    }
}
