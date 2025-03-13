import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Minesweeper extends JFrame {
    private final int SIZE = 10;
    // Removed fixed MINES constant; now dynamic based on difficulty
    private int minesCount;
    private JButton[][] buttons = new JButton[SIZE][SIZE];
    private boolean[][] mines = new boolean[SIZE][SIZE];
    private boolean[][] revealed = new boolean[SIZE][SIZE];
    private boolean[][] flagged = new boolean[SIZE][SIZE];
    private boolean gameOver = false;
    private ImageIcon bombIcon = new ImageIcon("D:/Minesweeper/Public/Assets/mine1.png");
    
    // New variables for home screen, game screen header, and timer logic
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel homePanel;
    private JPanel gamePanel;
    private JPanel gridPanel;
    private JPanel headerPanel;
    private JLabel timerLabel;
    private JLabel difficultyLabel;
    private javax.swing.Timer gameTimer;
    private long startTime;
    private String currentDifficulty;
    
    private ImageIcon scaleImageIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width-20, height-20, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }
    
    public Minesweeper() {
        setTitle("Minesweeper");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Use CardLayout to switch between home screen and game screen
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        add(mainPanel);
        
        createHomeScreen();
        createGameScreen();
        
        setVisible(true);
    }
    
    // Home screen with three difficulty options
    private void createHomeScreen() {
        homePanel = new JPanel();
        homePanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Select Difficulty");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        homePanel.add(titleLabel, gbc);
        
        gbc.gridy++;
        JButton easyButton = new JButton("Easy");
        homePanel.add(easyButton, gbc);
        
        gbc.gridy++;
        JButton mediumButton = new JButton("Medium");
        homePanel.add(mediumButton, gbc);
        
        gbc.gridy++;
        JButton hardButton = new JButton("Good Luck Winning");
        homePanel.add(hardButton, gbc);
        
        easyButton.addActionListener(e -> startGame("Easy"));
        mediumButton.addActionListener(e -> startGame("Medium"));
        hardButton.addActionListener(e -> startGame("Hard"));
        
        mainPanel.add(homePanel, "Home");
    }
    
    // Game screen with a header (timer on left, difficulty on right) above the grid
    private void createGameScreen() {
        gamePanel = new JPanel(new BorderLayout());
        
        // Header panel with timer and difficulty level labels
        headerPanel = new JPanel(new BorderLayout());
        timerLabel = new JLabel("Timer: 0");
        difficultyLabel = new JLabel("Difficulty Level: ");
        headerPanel.add(timerLabel, BorderLayout.WEST);
        headerPanel.add(difficultyLabel, BorderLayout.EAST);
        gamePanel.add(headerPanel, BorderLayout.NORTH);
        
        // Grid panel containing the game tiles
        gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        gamePanel.add(gridPanel, BorderLayout.CENTER);
        
        mainPanel.add(gamePanel, "Game");
    }
    
    // Starts a new game with the selected difficulty level
    private void startGame(String difficulty) {
        currentDifficulty = difficulty;
        difficultyLabel.setText("Difficulty Level: " + difficulty);
        
        // Set mines count based on difficulty logic:
        // Easy: 10% mines, Medium: 15% mines, Hard: 20% mines
        if (difficulty.equals("Easy")) {
            minesCount = (int)(SIZE * SIZE * 0.10);
        } else if (difficulty.equals("Medium")) {
            minesCount = (int)(SIZE * SIZE * 0.15);
        } else if (difficulty.equals("Hard")) {
            minesCount = (int)(SIZE * SIZE * 0.20);
        }
        
        // Reset game variables
        gameOver = false;
        mines = new boolean[SIZE][SIZE];
        revealed = new boolean[SIZE][SIZE];
        flagged = new boolean[SIZE][SIZE];
        buttons = new JButton[SIZE][SIZE];
        
        gridPanel.removeAll();
        initializeGame();
        gridPanel.revalidate();
        gridPanel.repaint();
        
        // Start timer from game start
        startTime = System.currentTimeMillis();
        if (gameTimer != null) {
            gameTimer.stop();
        }
        gameTimer = new Timer(1000, e -> updateTimer());
        gameTimer.start();
        
        cardLayout.show(mainPanel, "Game");
    }
    
    private void updateTimer() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        timerLabel.setText("Timer: " + elapsed);
    }
    
    // Initializes the game board (tiles)
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
                gridPanel.add(buttons[row][col]);
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
    
    // Places mines using the dynamic minesCount value
    private void placeMines() {
        Random rand = new Random();
        int placedMines = 0;
        while (placedMines < minesCount) {
            int r = rand.nextInt(SIZE);
            int c = rand.nextInt(SIZE);
            if (!mines[r][c]) {
                mines[r][c] = true;
                placedMines++;
            }
        }
    }
    
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
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE || revealed[row][col] || flagged[row][col])
            return;
        
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
            revealAllMines(); // Optional: Show mines in different color
            if (gameTimer != null) {
                gameTimer.stop();
            }
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            int choice = JOptionPane.showConfirmDialog(this, 
                "Game Over! You won!\nTotal Time: " + elapsed + " seconds\nDifficulty Level: " + currentDifficulty + "\nPlay again?", 
                "Game Over", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        }
    }
    
    private void resetGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        // Return to the home screen for difficulty selection
        cardLayout.show(mainPanel, "Home");
    }
    
    private void gameOver() {
        gameOver = true;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (mines[i][j]) {
                    buttons[i][j].setBackground(Color.RED);
                    buttons[i][j].setIcon(scaleImageIcon(bombIcon, buttons[i][j].getWidth(), buttons[i][j].getHeight()));
                }
            }
        }
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        int choice = JOptionPane.showConfirmDialog(this, 
            "Game Over! You hit a mine.\nTotal Time: " + elapsed + " seconds\nDifficulty Level: " + currentDifficulty + "\nPlay again?", 
            "Game Over", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
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
