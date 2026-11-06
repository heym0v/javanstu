package poi;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Панель с рисунком k-d дерева.
 * Узлы, которые прошла последняя операция, подсвечены красным.
 * Рисуем только верхние уровни: 100 000 узлов на экран не поместятся.
 */
public class TreePanel extends JPanel {
    private static final int MAX_LEVELS = 6;
    private static final int LEVEL_HEIGHT = 60;
    private static final int RADIUS = 14;

    private final KdTree<Place> tree;

    public TreePanel(KdTree<Place> tree) {
        this.tree = tree;
        setBackground(Color.WHITE);
    }

    /** Swing вызывает этот метод сам, когда нужно перерисовать панель (после repaint()). */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // очистить фон
        // Пока рисуем, другой поток не должен менять дерево (тот же замок, что у SyncKdTree)
        synchronized (tree) {
            g.setColor(Color.BLACK);
            g.drawString("Мест в дереве: " + tree.size() + " (показано " + MAX_LEVELS
                    + " уровней; красные — пройдены последней операцией)", 10, 15);
            drawNode(g, tree.root, 0, 0, getWidth(), -1, -1);
        }
    }

    /** Узел рисуется в середине своей полосы [left; right], дети делят полосу пополам. */
    private void drawNode(Graphics g, KdTree.Node<Place> node, int level, int left, int right,
                          int parentX, int parentY) {
        if (node == null || level >= MAX_LEVELS) {
            return;
        }
        int x = (left + right) / 2;
        int y = 50 + level * LEVEL_HEIGHT;
        if (parentX >= 0) {
            g.setColor(Color.GRAY);
            g.drawLine(parentX, parentY, x, y);
        }
        drawNode(g, node.left, level + 1, left, x, x, y);
        drawNode(g, node.right, level + 1, x, right, x, y);

        boolean highlighted = tree.getVisited().contains(node.value);
        g.setColor(highlighted ? Color.RED : new Color(150, 200, 255));
        g.fillOval(x - RADIUS, y - RADIUS, 2 * RADIUS, 2 * RADIUS);
        g.setColor(Color.BLACK);
        String text = String.valueOf(node.value.getId());
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x - textWidth / 2, y + 5); // номер по центру кружка
    }
}
