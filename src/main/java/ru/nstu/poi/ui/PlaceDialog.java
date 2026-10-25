package ru.nstu.poi.ui;

import ru.nstu.poi.model.Category;
import ru.nstu.poi.model.NetworkPlace;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Диалог добавления / редактирования сетевого места с проверкой полей. */
public final class PlaceDialog extends JDialog {
    private static final int FIELD_COLUMNS = 28;
    private static final int GAP = 6;
    private static final String ERROR_TITLE = "Ошибка ввода";

    private final JTextField nameField = new JTextField(FIELD_COLUMNS);
    private final JComboBox<Category> categoryBox = new JComboBox<>(Category.values());
    private final JTextField latitudeField = new JTextField(FIELD_COLUMNS);
    private final JTextField longitudeField = new JTextField(FIELD_COLUMNS);
    private final JTextField websiteField = new JTextField(FIELD_COLUMNS);
    private final JTextField phoneField = new JTextField(FIELD_COLUMNS);

    private final long placeId;
    private NetworkPlace result;

    private PlaceDialog(Window owner, String title, long placeId, NetworkPlace initial) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.placeId = placeId;
        if (initial != null) {
            fillFrom(initial);
        }
        setContentPane(buildContent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    /**
     * Показывает диалог.
     *
     * @param initial данные для редактирования или null для нового места
     * @return введённое место или пустой Optional, если нажата «Отмена»
     */
    public static Optional<NetworkPlace> showDialog(Component parent, String title, long placeId,
                                                    NetworkPlace initial) {
        Window owner = parent instanceof Window window ? window : SwingUtilities.getWindowAncestor(parent);
        PlaceDialog dialog = new PlaceDialog(owner, title, placeId, initial);
        dialog.setVisible(true);   // модальный: вернёмся сюда после закрытия
        return Optional.ofNullable(dialog.result);
    }

    private void fillFrom(NetworkPlace place) {
        nameField.setText(place.getName());
        categoryBox.setSelectedItem(place.getCategory());
        latitudeField.setText(Double.toString(place.getLatitude()));
        longitudeField.setText(Double.toString(place.getLongitude()));
        websiteField.setText(place.getWebsite());
        phoneField.setText(place.getPhone());
    }

    private JPanel buildContent() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP, GAP, GAP, GAP);
        c.anchor = GridBagConstraints.WEST;

        String[] labels = {"ID:", "Название:", "Категория:", "Широта:", "Долгота:", "Сайт:", "Телефон:"};
        JComponent[] fields = {new JLabel(Long.toString(placeId)), nameField, categoryBox,
                latitudeField, longitudeField, websiteField, phoneField};
        for (int row = 0; row < labels.length; row++) {
            c.gridy = row;
            c.gridx = 0;
            c.fill = GridBagConstraints.NONE;
            form.add(new JLabel(labels[row]), c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            form.add(fields[row], c);
        }
        latitudeField.setToolTipText("Например 55.0306 (можно через запятую)");
        longitudeField.setToolTipText("Например 82.9236 (можно через запятую)");
        websiteField.setToolTipText("https://...");
        phoneField.setToolTipText("Необязательно, например +7 383 222-00-00");

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Отмена");
        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(okButton);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        JPanel content = new JPanel(new BorderLayout());
        content.add(form, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        return content;
    }

    private void onOk() {
        List<String> errors = new ArrayList<>();
        Double latitude = parseCoordinate(latitudeField.getText(), "Широта", errors);
        Double longitude = parseCoordinate(longitudeField.getText(), "Долгота", errors);
        if (!errors.isEmpty()) {
            showErrors(errors);
            return;
        }
        NetworkPlace candidate = new NetworkPlace(placeId, nameField.getText().trim(),
                (Category) categoryBox.getSelectedItem(), latitude, longitude,
                websiteField.getText().trim(), phoneField.getText().trim());
        errors = candidate.validate();
        if (!errors.isEmpty()) {
            showErrors(errors);
            return;
        }
        result = candidate;
        dispose();
    }

    /** @return число или null, если ввод некорректен (ошибка добавляется в список) */
    private static Double parseCoordinate(String text, String label, List<String> errors) {
        String normalized = text.trim().replace(',', '.');
        if (normalized.isEmpty()) {
            errors.add(label + ": поле не заполнено");
            return null;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            errors.add(label + ": «" + text.trim() + "» не является числом");
            return null;
        }
    }

    private void showErrors(List<String> errors) {
        JOptionPane.showMessageDialog(this, "Исправьте ошибки:\n• " + String.join("\n• ", errors),
                ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
    }
}
