package poi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/** Главное окно: таблица мест, рисунок дерева и кнопки. */
public class MainFrame extends JFrame {
    private static final String[] COLUMNS =
            {"Тип", "ID", "Название", "Категория", "Широта", "Долгота", "Сайт", "Телефон"};
    private static final int MAX_LINES_IN_DIALOG = 20; // длинные списки в окне сообщения обрезаем

    // Главное хранилище — своё k-d дерево (защищённое от одновременного доступа потоков)
    private final SyncKdTree<Place> tree = new SyncKdTree<>();
    private SimpleArray<Place> shown = new SimpleArray<>(); // места в том же порядке, что строки таблицы

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TreePanel treePanel = new TreePanel(tree);
    private final JButton editButton = new JButton("Изменить");
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private final JButton cancelButton = new JButton("Отмена");
    private SwingWorker<Integer, Void> currentTask; // фоновая задача, которую можно отменить

    public MainFrame() {
        super("Карта мест (POI)");

        // Модель таблицы: ячейки нельзя править прямо в таблице
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // При выборе строки решаем, можно ли нажать «Изменить»
        table.getSelectionModel().addListSelectionListener(e -> updateEditButton());

        JButton loadButton = new JButton("Загрузить CSV");
        JButton saveButton = new JButton("Сохранить CSV");
        JButton addButton = new JButton("Добавить");
        loadButton.addActionListener(e -> loadFile());
        saveButton.addActionListener(e -> saveFile());
        addButton.addActionListener(e -> addPlace());
        editButton.addActionListener(e -> editPlace());
        cancelButton.addActionListener(e -> currentTask.cancel(false));
        editButton.setEnabled(false);
        cancelButton.setEnabled(false);

        JPanel buttons = new JPanel();
        buttons.add(loadButton);
        buttons.add(saveButton);
        buttons.add(addButton);
        buttons.add(editButton);

        // Нижняя строка: полоса прогресса и кнопка отмены фоновой задачи
        JPanel bottom = new JPanel(new BorderLayout());
        progressBar.setStringPainted(true); // показывать проценты
        bottom.add(progressBar, BorderLayout.CENTER);
        bottom.add(cancelButton, BorderLayout.EAST);

        // Слева таблица, справа рисунок дерева
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), treePanel);
        split.setDividerLocation(650);

        add(buttons, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setSize(1350, 650);
        setLocationRelativeTo(null); // окно по центру экрана
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /** Заново заполняет таблицу из дерева и перерисовывает рисунок дерева. */
    private void refreshTable() {
        shown = tree.toArray();
        Object[][] rows = new Object[shown.size()][];
        for (int i = 0; i < shown.size(); i++) {
            Place p = shown.get(i);
            String website = "";
            String phone = "";
            if (p instanceof NetworkPlace) {
                NetworkPlace np = (NetworkPlace) p;
                website = np.getWebsite();
                phone = np.getPhone();
            }
            rows[i] = new Object[]{p.getTypeName(), p.getId(), p.getName(), p.getCategory(),
                    p.getLatitude(), p.getLongitude(), website, phone};
        }
        tableModel.setDataVector(rows, COLUMNS); // все строки разом — быстро даже для 100 000
        treePanel.repaint();
        updateEditButton();
    }

    /** «Изменить» активна, только если выбрано место, реализующее Editable. */
    private void updateEditButton() {
        Place selected = getSelectedPlace();
        editButton.setEnabled(selected instanceof Editable);
    }

    /** Возвращает выбранное в таблице место или null, если ничего не выбрано. */
    private Place getSelectedPlace() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return shown.get(row);
    }

    /** Запускает фоновую задачу и связывает её с полосой прогресса. */
    private void startTask(SwingWorker<Integer, Void> task) {
        if (currentTask != null) {
            showError("Дождитесь окончания текущей операции или отмените её");
            return;
        }
        currentTask = task;
        cancelButton.setEnabled(true);
        progressBar.setValue(0);
        // Когда задача вызывает setProgress(...), сдвигаем полосу
        task.addPropertyChangeListener(e -> {
            if ("progress".equals(e.getPropertyName())) {
                progressBar.setValue((Integer) e.getNewValue());
            }
        });
        task.execute(); // doInBackground() начнёт работать в другом потоке
    }

    /** Вызывается из done(): задача закончилась (сама или по отмене). */
    private void finishTask() {
        currentTask = null;
        cancelButton.setEnabled(false);
        refreshTable();
    }

    private void loadFile() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // пользователь нажал «Отмена»
        }
        File file = chooser.getSelectedFile();
        SimpleArray<String> errors = new SimpleArray<>();
        tree.clear();

        // SwingWorker: долгая работа в фоновом потоке, чтобы окно не зависало
        SwingWorker<Integer, Void> task = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                SimpleArray<String> lines = CsvFile.readLines(file);
                int loaded = 0;
                for (int i = 0; i < lines.size() && !isCancelled(); i++) {
                    String line = lines.get(i);
                    if (!line.isBlank()) {
                        try {
                            tree.insert(CsvFile.parseLine(line));
                            loaded++;
                        } catch (CsvException e) {
                            // i + 2: нумерация строк с 1, и ещё первая строка — заголовок
                            errors.add("Строка " + (i + 2) + ": " + e.getCode() + " — " + e.getMessage());
                        }
                    }
                    setProgress((i + 1) * 100 / lines.size());
                }
                return loaded;
            }

            // done() выполняется в потоке интерфейса, когда фоновая работа закончилась
            @Override
            protected void done() {
                finishTask();
                if (isCancelled()) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Загрузка отменена. Загружено мест: " + tree.size());
                    return;
                }
                try {
                    int loaded = get(); // результат doInBackground()
                    String message = "Загружено мест: " + loaded + "\nПропущено битых строк: " + errors.size();
                    for (int i = 0; i < errors.size() && i < MAX_LINES_IN_DIALOG; i++) {
                        message += "\n" + errors.get(i);
                    }
                    JOptionPane.showMessageDialog(MainFrame.this, message);
                } catch (Exception e) {
                    // сюда попадёт IOException из doInBackground (файла нет, нет доступа)
                    showError("Не удалось прочитать файл: " + e.getCause().getMessage());
                }
            }
        };
        startTask(task);
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            CsvFile.save(chooser.getSelectedFile(), tree.toArray());
            JOptionPane.showMessageDialog(this, "Сохранено мест: " + tree.size());
        } catch (Exception e) {
            showError("Не удалось сохранить файл: " + e.getMessage());
        }
    }

    /** «Добавить» создаёт только сетевое место — единственный редактируемый тип. */
    private void addPlace() {
        NetworkPlace place = new NetworkPlace(nextId(), "", Category.OTHER, 0, 0, "", "");
        if (showPlaceDialog(place, "Добавить место")) {
            tree.insert(place);
            refreshTable();
        }
    }

    private void editPlace() {
        Place selected = getSelectedPlace();
        if (selected instanceof NetworkPlace) {
            if (showPlaceDialog((NetworkPlace) selected, "Изменить место")) {
                // Координаты могли измениться, а от них зависит место узла в дереве.
                // Поэтому вынимаем место из дерева и вставляем заново.
                tree.remove(selected);
                tree.insert(selected);
                refreshTable();
            }
        }
    }

    /** Новый номер: на 1 больше самого большого из имеющихся. */
    private int nextId() {
        SimpleArray<Place> all = tree.toArray();
        int max = 0;
        for (int i = 0; i < all.size(); i++) {
            max = Math.max(max, all.get(i).getId());
        }
        return max + 1;
    }

    /**
     * Диалог с полями места. Если нажали OK и всё введено верно —
     * записывает значения в place и возвращает true.
     */
    private boolean showPlaceDialog(NetworkPlace place, String title) {
        JTextField nameField = new JTextField(place.getName());
        JComboBox<Category> categoryBox = new JComboBox<>(Category.values());
        categoryBox.setSelectedItem(place.getCategory());
        JTextField latField = new JTextField(String.valueOf(place.getLatitude()));
        JTextField lonField = new JTextField(String.valueOf(place.getLongitude()));
        JTextField websiteField = new JTextField(place.getWebsite());
        JTextField phoneField = new JTextField(place.getPhone());

        // Сетка: слева подписи, справа поля
        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Название:"));
        panel.add(nameField);
        panel.add(new JLabel("Категория:"));
        panel.add(categoryBox);
        panel.add(new JLabel("Широта:"));
        panel.add(latField);
        panel.add(new JLabel("Долгота:"));
        panel.add(lonField);
        panel.add(new JLabel("Сайт:"));
        panel.add(websiteField);
        panel.add(new JLabel("Телефон:"));
        panel.add(phoneField);

        int answer = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (answer != JOptionPane.OK_OPTION) {
            return false;
        }
        double lat;
        double lon;
        try {
            lat = Double.parseDouble(latField.getText());
            lon = Double.parseDouble(lonField.getText());
        } catch (NumberFormatException e) {
            showError("Широта и долгота должны быть числами, например 55.03");
            return false;
        }
        place.setName(nameField.getText());
        place.setCategory((Category) categoryBox.getSelectedItem());
        place.setLatitude(lat);
        place.setLongitude(lon);
        place.setWebsite(websiteField.getText());
        place.setPhone(phoneField.getText());
        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }
}
