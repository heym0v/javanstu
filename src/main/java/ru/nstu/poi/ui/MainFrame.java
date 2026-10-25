package ru.nstu.poi.ui;

import ru.nstu.poi.io.CsvException;
import ru.nstu.poi.io.LoadResult;
import ru.nstu.poi.model.Editable;
import ru.nstu.poi.model.NetworkPlace;
import ru.nstu.poi.model.Place;
import ru.nstu.poi.service.PlaceService;
import ru.nstu.poi.service.ValidationException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Главное окно. Только связывает кнопки с {@link PlaceService} и показывает результат.
 * Файловые операции выполняются в фоне через SwingWorker, чтобы не блокировать GUI.
 */
public final class MainFrame extends JFrame {
    private static final String TITLE = "Карта мест (POI)";
    private static final int WINDOW_WIDTH = 1050;
    private static final int WINDOW_HEIGHT = 550;
    private static final String CSV_EXTENSION = "csv";
    private static final int MAX_ERRORS_SHOWN = 50;
    private static final int REPORT_ROWS = 12;
    private static final int REPORT_COLUMNS = 70;
    private static final int DOUBLE_CLICK = 2;
    private static final String READ_ONLY_HINT = "Это место только для чтения";

    private final PlaceService service;
    private final PlaceTableModel tableModel = new PlaceTableModel();
    private final JTable table = new JTable(tableModel);
    private final JButton loadButton = new JButton("Загрузить CSV");
    private final JButton saveButton = new JButton("Сохранить CSV");
    private final JButton addButton = new JButton("Добавить");
    private final JButton editButton = new JButton("Изменить");
    private final JLabel statusLabel = new JLabel(" ");
    private final JFileChooser fileChooser = new JFileChooser(Path.of(".").toAbsolutePath().toFile());

    public MainFrame(PlaceService service) {
        super(TITLE);
        this.service = service;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);

        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV-файлы (*.csv)", CSV_EXTENSION));
        configureTable();
        configureButtons();
        layoutComponents();
        refreshTable();
    }

    private void configureTable() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateEditButton();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == DOUBLE_CLICK && editButton.isEnabled()) {
                    onEdit();
                }
            }
        });
    }

    private void configureButtons() {
        loadButton.addActionListener(e -> onLoad());
        saveButton.addActionListener(e -> onSave());
        addButton.addActionListener(e -> onAdd());
        editButton.addActionListener(e -> onEdit());
    }

    private void layoutComponents() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(loadButton);
        toolbar.add(saveButton);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(addButton);
        toolbar.add(editButton);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT));
        status.add(statusLabel);

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------------- действия

    private void onLoad() {
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = fileChooser.getSelectedFile().toPath();
        setBusy(true, "Загрузка " + file.getFileName() + "…");

        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws CsvException {
                return service.readCsv(file);
            }

            @Override
            protected void done() {
                setBusy(false, " ");
                try {
                    LoadResult result = get();
                    service.replaceAll(result.places());
                    refreshTable();
                    showLoadReport(file, result);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError("Ошибка загрузки", "Загрузка прервана");
                } catch (ExecutionException e) {
                    showError("Ошибка загрузки", describe(e.getCause()));
                }
            }
        }.execute();
    }

    private void onSave() {
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = withCsvExtension(fileChooser.getSelectedFile().toPath());
        if (Files.exists(file) && !confirmOverwrite(file)) {
            return;
        }
        List<Place> snapshot = service.snapshot();
        setBusy(true, "Сохранение " + file.getFileName() + "…");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws CsvException {
                service.saveCsv(file, snapshot);
                return null;
            }

            @Override
            protected void done() {
                setBusy(false, " ");
                try {
                    get();
                    statusLabel.setText("Сохранено мест: " + snapshot.size() + " → " + file.getFileName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError("Ошибка сохранения", "Сохранение прервано");
                } catch (ExecutionException e) {
                    showError("Ошибка сохранения", describe(e.getCause()));
                }
            }
        }.execute();
    }

    private void onAdd() {
        Optional<NetworkPlace> created =
                PlaceDialog.showDialog(this, "Новое сетевое место", service.nextId(), null);
        if (created.isEmpty()) {
            return;
        }
        try {
            service.add(created.get());
            refreshTable();
        } catch (ValidationException e) {
            showError("Ошибка добавления", e.getMessage());
        }
    }

    private void onEdit() {
        // Кнопка активна только для Editable; в модели редактируемый тип один — NetworkPlace
        if (!(selectedPlace().orElse(null) instanceof NetworkPlace target)) {
            return;
        }
        Optional<NetworkPlace> changes =
                PlaceDialog.showDialog(this, "Изменение места", target.getId(), target);
        if (changes.isEmpty()) {
            return;
        }
        try {
            service.update(target, changes.get());
            refreshTable();
        } catch (ValidationException e) {
            showError("Ошибка изменения", e.getMessage());
        }
    }

    // ---------------------------------------------------------------- вспомогательное

    private Optional<Place> selectedPlace() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return Optional.empty();
        }
        return Optional.of(tableModel.getPlaceAt(table.convertRowIndexToModel(viewRow)));
    }

    private void updateEditButton() {
        Optional<Place> selected = selectedPlace();
        boolean editable = selected.map(place -> place instanceof Editable).orElse(false);
        editButton.setEnabled(editable);
        editButton.setToolTipText(selected.isPresent() && !editable ? READ_ONLY_HINT : null);
    }

    private void refreshTable() {
        tableModel.setPlaces(service.getAll());
        statusLabel.setText("Мест: " + service.getAll().size());
        updateEditButton();
    }

    private void setBusy(boolean busy, String message) {
        loadButton.setEnabled(!busy);
        saveButton.setEnabled(!busy);
        addButton.setEnabled(!busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        statusLabel.setText(message);
    }

    private void showLoadReport(Path file, LoadResult result) {
        String summary = "Загружено мест: " + result.places().size() + " из " + file.getFileName();
        statusLabel.setText(summary);
        List<CsvException> skipped = result.skipped();
        if (skipped.isEmpty()) {
            return;
        }
        StringBuilder text = new StringBuilder();
        skipped.stream().limit(MAX_ERRORS_SHOWN).forEach(e -> text.append(e.getMessage()).append('\n'));
        if (skipped.size() > MAX_ERRORS_SHOWN) {
            text.append("…и ещё ").append(skipped.size() - MAX_ERRORS_SHOWN).append('\n');
        }
        JTextArea area = new JTextArea(text.toString(), REPORT_ROWS, REPORT_COLUMNS);
        area.setEditable(false);
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel(summary + ". Пропущено битых строк: " + skipped.size()), BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, panel, "Отчёт о загрузке", JOptionPane.WARNING_MESSAGE);
    }

    private static String describe(Throwable cause) {
        if (cause instanceof CsvException csv) {
            return csv.getMessage();
        }
        cause.printStackTrace();   // непредвиденная ошибка — оставляем след в консоли
        return "Непредвиденная ошибка: " + cause;
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private boolean confirmOverwrite(Path file) {
        int answer = JOptionPane.showConfirmDialog(this, "Файл " + file.getFileName() + " уже существует. Перезаписать?",
                "Подтверждение", JOptionPane.YES_NO_OPTION);
        return answer == JOptionPane.YES_OPTION;
    }

    private static Path withCsvExtension(Path file) {
        String name = file.getFileName().toString();
        return name.toLowerCase().endsWith("." + CSV_EXTENSION)
                ? file
                : file.resolveSibling(name + "." + CSV_EXTENSION);
    }
}
