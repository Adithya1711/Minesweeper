import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Minesweeper extends JFrame {
    private final int SIZE = 10; 
    private final int MINES = 10;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private boolean[][] mines = new boolean[SIZE][SIZE];
    private boolean[][] revealed = new boolean[SIZE][SIZE];
    private boolean[][] flagged = new boolean[SIZE][SIZE];
    private boolean gameOver = false;
    private ImageIcon bombIcon = new ImageIcon("D:/Minesweeper/Public/Assets/mine1.png");

    private ImageIcon scaleImageIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width-20, height-20, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }
    


    public Minesweeper() {
        setTitle("Minesweeper");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(SIZE, SIZE));
        initializeGame();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeGame() {
        placeMines();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                buttons[row][col] = new JButton();
                styleButton(buttons[row][col]);
                buttons[row][col].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) return;
                        JButton clicked = (JButton) e.getSource();
                        int[] pos = findButtonPosition(clicked);
                        if (pos == null) return;
                        
                        if (SwingUtilities.isRightMouseButton(e)) {
                            toggleFlag(pos[0], pos[1]);
                        } else {
                            revealCell(pos[0], pos[1]);
                        }
                    }
                });
                add(buttons[row][col]);
            }
        }
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 14));
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(40, 40));
    }

    private int[] findButtonPosition(JButton button) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (buttons[i][j] == button) {
                    return new int[]{i, j};
                }
            }
        }
        return null;
    }

    private void placeMines() {
        Random rand = new Random();
        int placedMines = 0;
        while (placedMines < MINES) {
            int r = rand.nextInt(SIZE);
            int c = rand.nextInt(SIZE);
            if (!mines[r][c]) {
                mines[r][c] = true;
                placedMines++;
            }
        }
    }

    private boolean isGameWon() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                // Check if non-mine cell is not revealed
                if (!mines[i][j] && !revealed[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void checkGameWin() {
        if (!gameOver && isGameWon()) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, "Congratulations! You won the game!");
            revealAllMines(); // Optional: Show mines in different color
        }
    }
    
    // Optional helper to mark mines when game is won
    private void revealAllMines() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (mines[i][j] && !flagged[i][j]) {
                    buttons[i][j].setBackground(Color.GREEN);
                    buttons[i][j].setIcon(scaleImageIcon(bombIcon, 
                        buttons[i][j].getWidth(), 
                        buttons[i][j].getHeight()));
                }
            }
        }
    }

    private void revealCell(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || revealed[row][col] || flagged[row][col]) return;

        revealed[row][col] = true;
        buttons[row][col].setEnabled(false);
        buttons[row][col].setBackground(new Color(200, 200, 200));

        if (mines[row][col]) {
            buttons[row][col].setIcon(scaleImageIcon(bombIcon, buttons[row][col].getWidth(), buttons[row][col].getHeight()));
            gameOver();
            return;
        }

        int count = countAdjacentMines(row, col);
        if (count > 0) {
            buttons[row][col].setText("<html><font color='" + getHexColor(count) + "'>" + count + "</font></html>");
        } else {
            buttons[row][col].setText("");
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    revealCell(row + dr, col + dc);
                }
            }
        }
        checkGameWin();
    }

    private String getHexColor(int count) {
        return switch (count) {
            case 1 -> "#0000FF"; // Blue
            case 2 -> "#008000"; // Green
            case 3 -> "#FF0000"; // Red
            case 4 -> "#800080"; // Purple
            case 5 -> "#FFA500"; // Orange
            default -> "#000000"; // Black
        };
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr, nc = col + dc;
                if (nr >= 0 && nr < SIZE && nc >= 0 && nc < SIZE && mines[nr][nc]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void toggleFlag(int row, int col) {
        if (revealed[row][col]) return;
        flagged[row][col] = !flagged[row][col];
        buttons[row][col].setText(flagged[row][col] ? "M" : "");
        checkGameWin();
    }

    private void gameOver() {
        gameOver = true;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (mines[i][j]) {
                    buttons[i][j].setBackground(Color.RED);
                    buttons[i][j].setIcon(scaleImageIcon(bombIcon, buttons[i][j].getWidth(), buttons[i][j].getHeight()));
                }
            }
        }
        JOptionPane.showMessageDialog(this, "Game Over! You hit a mine.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Minesweeper();
        });
    }
}