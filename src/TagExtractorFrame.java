import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class TagExtractorFrame extends JFrame {

    private JTextArea displayTextArea;
    private JLabel statusLabel;

    private File selectedTextFile;
    private File selectedStopWordsFile;

    private Set<String> stopWordsSet;
    private Map<String, Integer> wordFrequencyMap;

    public TagExtractorFrame() {
        setTitle("Tag / Keyword Extractor");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        stopWordsSet = new TreeSet<>();
        wordFrequencyMap = new TreeMap<>();

        initComponents();
    }

    private void initComponents() {
        // Top Panel for Buttons & Status
        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton loadStopWordsButton = new JButton("1. Load Stop Words File");
        JButton loadTextButton = new JButton("2. Load Text File & Extract Tags");
        JButton saveButton = new JButton("3. Save Extracted Tags to File");

        statusLabel = new JLabel("Status: Please load a stop words file and a text file.", SwingConstants.CENTER);

        topPanel.add(loadStopWordsButton);
        topPanel.add(loadTextButton);
        topPanel.add(saveButton);

        // Center Panel for Text Display
        displayTextArea = new JTextArea();
        displayTextArea.setEditable(false);
        displayTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayTextArea);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // Action Listeners
        loadStopWordsButton.addActionListener(e -> loadStopWordsFile());
        loadTextButton.addActionListener(e -> processTextFile());
        saveButton.addActionListener(e -> saveOutputFile());
    }

    private void loadStopWordsFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedStopWordsFile = chooser.getSelectedFile();
            try {
                List<String> lines = Files.readAllLines(selectedStopWordsFile.toPath());
                stopWordsSet.clear();
                for (String line : lines) {
                    String trimmed = line.trim().toLowerCase();
                    if (!trimmed.isEmpty()) {
                        stopWordsSet.add(trimmed);
                    }
                }
                statusLabel.setText("Status: Loaded " + stopWordsSet.size() + " stop words from " + selectedStopWordsFile.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading stop words file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processTextFile() {
        if (stopWordsSet.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please load your stop words file first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = newJFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedTextFile = chooser.getSelectedFile();
            wordFrequencyMap.clear();

            try {
                List<String> lines = Files.readAllLines(selectedTextFile.toPath());
                for (String line : lines) {
                    // Tokenize by whitespace
                    String[] tokens = line.split("\\s+");
                    for (String token : tokens) {
                        // Normalize: remove non-letter characters and lowercase
                        String cleaned = token.replaceAll("[^a-zA-Z]", "").toLowerCase();

                        if (!cleaned.isEmpty() && !stopWordsSet.contains(cleaned)) {
                            wordFrequencyMap.put(cleaned, wordFrequencyMap.getOrDefault(cleaned, 0) + 1);
                        }
                    }
                }

                displayResults();
                statusLabel.setText("Status: Extracted tags from " + selectedTextFile.getName());

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading text file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JFileChooser newJFileChooser() {
        JFileChooser chooser = new JFileChooser();
        // Default to current directory or user home if desired
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        return chooser;
    }

    private void displayResults() {
        StringBuilder sb = new StringBuilder();
        sb.append("File Processed: ").append(selectedTextFile.getName()).append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-25s | %s\n", "Tag / Word", "Frequency"));
        sb.append("--------------------------------------------------\n");

        for (Map.Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
            sb.append(String.format("%-25s | %d\n", entry.getKey(), entry.getValue()));
        }

        displayTextArea.setText(sb.toString());
        displayTextArea.setCaretPosition(0);
    }

    private void saveOutputFile() {
        if (wordFrequencyMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data to save! Process a text file first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();
            try {
                List<String> lines = new ArrayList<>();
                lines.add("File Processed: " + selectedTextFile.getName());
                lines.add("Tag,Frequency");
                for (Map.Entry<String, Integer> entry : wordFrequencyMap.entrySet()) {
                    lines.add(entry.getKey() + "," + entry.getValue());
                }
                Files.write(outputFile.toPath(), lines);
                JOptionPane.showMessageDialog(this, "Successfully saved output file!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TagExtractorFrame().setVisible(true);
        });
    }
}