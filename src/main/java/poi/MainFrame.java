package poi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/** Главное окно: таблица мест и кнопки. */
public class MainFrame extends JFrame {
    private static final String[] COLUMNS =
            {"Тип", "ID", "Название", "Категория", "Широта", "Долгота", "Сайт", "Телефон"};

    private ArrayList<Place> places = new ArrayList<>(); // все места программы
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton editButton = new JButton("Изменить");

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
        editButton.setEnabled(false);

        JPanel buttons = new JPanel();
        buttons.add(loadButton);
        buttons.add(saveButton);
        buttons.add(addButton);
        buttons.add(editButton);

        add(buttons, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setSize(900, 500);
        setLocationRelativeTo(null); // окно по центру экрана
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /** Заново заполняет таблицу из списка мест. */
    private void refreshTable() {
        tableModel.setRowCount(0); // очистить таблицу
        for (Place p : places) {
            String website = "";
            String phone = "";
            if (p instanceof NetworkPlace) {
                NetworkPlace np = (NetworkPlace) p;
                website = np.getWebsite();
                phone = np.getPhone();
            }
            tableModel.addRow(new Object[]{p.getTypeName(), p.getId(), p.getName(), p.getCategory(),
                    p.getLatitude(), p.getLongitude(), website, phone});
        }
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
        return places.get(row);
    }

    private void loadFile() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return; // пользователь нажал «Отмена»
        }
        ArrayList<String> errors = new ArrayList<>();
        try {
            places = CsvFile.load(chooser.getSelectedFile(), errors);
        } catch (IOException e) {
            showError("Не удалось прочитать файл: " + e.getMessage());
            return;
        }
        refreshTable();
        String message = "Загружено мест: " + places.size() + "\nПропущено битых строк: " + errors.size();
        if (errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, message);
        } else {
            message += "\n\n" + String.join("\n", errors);
            JOptionPane.showMessageDialog(this, message, "Есть битые строки", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser(new File("data"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            CsvFile.save(chooser.getSelectedFile(), places);
            JOptionPane.showMessageDialog(this, "Сохранено мест: " + places.size());
        } catch (IOException e) {
            showError("Не удалось сохранить файл: " + e.getMessage());
        }
    }

    /** «Добавить» создаёт только сетевое место — единственный редактируемый тип. */
    private void addPlace() {
        NetworkPlace place = new NetworkPlace(nextId(), "", Category.OTHER, 0, 0, "", "");
        if (showPlaceDialog(place, "Добавить место")) {
            places.add(place);
            refreshTable();
        }
    }

    private void editPlace() {
        Place selected = getSelectedPlace();
        if (selected instanceof NetworkPlace) {
            if (showPlaceDialog((NetworkPlace) selected, "Изменить место")) {
                refreshTable();
            }
        }
    }

    /** Новый номер: на 1 больше самого большого из имеющихся. */
    private int nextId() {
        int max = 0;
        for (Place p : places) {
            max = Math.max(max, p.getId());
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
