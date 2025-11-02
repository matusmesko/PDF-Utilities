package matus.mesko;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

public class App {
    private static File selectedFile = null;


    public static void main(String[] args) {
        UserSettingsManager settingsManager = new UserSettingsManager();
        LangManager l = new LangManager(getSystemLanguage());
        String savedTheme = settingsManager.getTheme();
        try {
            if (savedTheme.equals("light")) {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } else {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            }
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        URL imageUrl = App.class.getClassLoader().getResource("pdf.png");
        ImageIcon pdfIcon = (imageUrl != null) ? new ImageIcon(imageUrl) : new ImageIcon();
        JFrame frame = new JFrame("PDF Utilities");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setResizable(false);
        frame.setIconImage(pdfIcon.getImage());


        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMenu = new JMenu(l.getString("options"));
        JToggleButton themeToggle = new JToggleButton();
        themeToggle.setText(savedTheme.equals("light") ? l.getString("darkmode") : l.getString("lightmode"));
        themeToggle.setSelected(savedTheme.equals("light"));

        themeToggle.addActionListener(e -> {
            try {
                if (themeToggle.isSelected()) {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    themeToggle.setText(l.getString("darkmode"));
                    settingsManager.setTheme("light");
                } else {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    themeToggle.setText(l.getString("lightmode"));
                    settingsManager.setTheme("dark");
                }
                SwingUtilities.updateComponentTreeUI(frame);
            } catch (Exception ex) {
                System.err.println("Failed to switch theme: " + ex.getMessage());
            }
        });
        optionsMenu.add(themeToggle);
        menuBar.add(optionsMenu);
        frame.setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("PDF Compressor");
        titleLabel.setFont(new Font("Calibri", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JLabel imageLabel = new JLabel(pdfIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JButton selectButton = new JButton(l.getString("selectfile"));
        selectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton compressButton = new JButton(l.getString("compressfile"));
        compressButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        compressButton.setEnabled(false);

        JLabel fileLabel = new JLabel(l.getString("selectedfile") + " " + l.getString("filenull"));
        fileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(27, 145, 50));
        statusLabel.setFont(new Font("Calibri", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setVisible(false);
        progressBar.setForeground(Color.GREEN);

        JButton previewButton = new JButton(l.getString("previewButton"));
        previewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        previewButton.setEnabled(false);
        JButton printButton = new JButton(l.getString("printButton"));
        printButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        printButton.setEnabled(false);

        JButton lockButton = new JButton(l.getString("lockButton"));
        lockButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        lockButton.setEnabled(false);

        JButton mergeButton = new JButton(l.getString("mergeButton"));
        mergeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fileDialog = new FileDialog((Frame) null, "Select PDF", FileDialog.LOAD);
                fileDialog.setVisible(true);
                String directory = fileDialog.getDirectory();
                String filename = fileDialog.getFile();
                if (filename != null && directory != null) {
                    selectedFile = new File(directory, filename);
                    if (selectedFile.getName().toLowerCase().endsWith(".pdf")) {
                        fileLabel.setText(l.getString("selectedfile") + " " + filename);
                        compressButton.setEnabled(true);
                        previewButton.setEnabled(true);
                        printButton.setEnabled(true);
                        lockButton.setEnabled(true);
                    } else {
                        fileLabel.setText(l.getString("selectedfile") + " " + l.getString("filenull"));
                        compressButton.setEnabled(false);
                        previewButton.setEnabled(false);
                        printButton.setEnabled(false);
                        lockButton.setEnabled(false);
                        JOptionPane.showMessageDialog(frame, l.getString("errorfile"), l.getString("errorfiletitle"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile != null) {
                    String outputFileName = selectedFile.getName().replace(".pdf", "_compressed.pdf");
                    FileDialog saveDialog = new FileDialog(frame, "Save Compressed PDF", FileDialog.SAVE);
                    saveDialog.setFile(outputFileName);
                    saveDialog.setVisible(true);
                    String saveDirectory = saveDialog.getDirectory();
                    String saveFilename = saveDialog.getFile();
                    if (saveDirectory != null && saveFilename != null) {
                        File saveFile = new File(saveDirectory, saveFilename);
                        progressBar.setVisible(true);
                        progressBar.setIndeterminate(false);
                        progressBar.setValue(0);
                        SwingWorker<Void, Void> worker = new SwingWorker<>() {
                            @Override
                            protected Void doInBackground() throws Exception {
                                for (int i = 0; i <= 100; i += 10) {
                                    Thread.sleep(50);
                                    progressBar.setValue(i);
                                }
                                compressPdf(selectedFile, saveFile);
                                return null;
                            }
                            @Override
                            protected void done() {
                                progressBar.setVisible(false);
                                JOptionPane.showMessageDialog(frame, l.getString("success") + " " + saveFile.getAbsolutePath(), "Info", JOptionPane.INFORMATION_MESSAGE);

                            }
                        };
                        worker.execute();
                    }
                }
            }
        });

        lockButton.addActionListener(e -> {
            if (selectedFile != null) {
                String password = JOptionPane.showInputDialog(frame, l.getString("enterPassword"), l.getString("lockButton"), JOptionPane.PLAIN_MESSAGE);

                if (password != null && !password.trim().isEmpty()) {
                    // File dialog to save the locked file
                    FileDialog saveDialog = new FileDialog(frame, l.getString("saveLockedFile"), FileDialog.SAVE);
                    saveDialog.setFile(selectedFile.getName().replace(".pdf", "_locked.pdf"));
                    saveDialog.setVisible(true);
                    String saveDirectory = saveDialog.getDirectory();
                    String saveFilename = saveDialog.getFile();

                    if (saveDirectory != null && saveFilename != null) {
                        File outputFile = new File(saveDirectory, saveFilename);
                        try {
                            pdfLock(selectedFile, outputFile, password, password);
                            JOptionPane.showMessageDialog(frame, l.getString("successLock") + " " + outputFile.getAbsolutePath(), l.getString("lockButton"), JOptionPane.INFORMATION_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, l.getString("failedLock"), l.getString("lockButton"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, l.getString("invalidPassword"), l.getString("lockButton"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        previewButton.addActionListener((ActionEvent e) -> {
            if (selectedFile != null) {
                showPdfPreviewFrame(selectedFile,l);
            }
        });

        printButton.addActionListener((ActionEvent e) -> {
            if (selectedFile != null) {
                printPdf(selectedFile,l);
            }
        });

        mergeButton.addActionListener(e -> {
            showMergeWindow(l);
        });

        panel.add(Box.createVerticalStrut(20));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(selectButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(fileLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(compressButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(10));
        panel.add(statusLabel);

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(previewButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(printButton);
        buttonBox.add(Box.createHorizontalStrut(10));
        buttonBox.add(lockButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(buttonBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(mergeButton);
        panel.add(Box.createVerticalStrut(10));

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void compressPdf(File inputFile, File outputFile) throws IOException, DocumentException {
        PdfReader reader = new PdfReader(new FileInputStream(inputFile));
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputFile));
        stamper.setFullCompression();
        stamper.close();
        reader.close();
    }

    private static void showPdfPreviewFrame(File file, LangManager l) {
        JFrame previewFrame = new JFrame("PDF Preview");
        previewFrame.setSize(800, 1000);
        previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel loadingLabel = new JLabel("Loading", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Calibri", Font.BOLD, 24));
        previewFrame.add(loadingLabel, BorderLayout.CENTER);

        Timer timer = new Timer(500, new ActionListener() {
            private int dotCount = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                dotCount = (dotCount + 1) % 4;
                String dots = "";
                for (int i = 0; i < dotCount; i++) {
                    dots += ".";
                }
                loadingLabel.setText("Loading" + dots);
            }
        });
        timer.start();

        new Thread(() -> {
            try (PDDocument document = PDDocument.load(file)) {
                PDFRenderer renderer = new PDFRenderer(document);
                JPanel previewPanel = new JPanel();
                previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));

                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 100, ImageType.RGB);
                    JLabel pageLabel = new JLabel(new ImageIcon(image));
                    previewPanel.add(pageLabel);
                }

                SwingUtilities.invokeLater(() -> {
                    timer.stop();
                    previewFrame.remove(loadingLabel);
                    previewFrame.add(new JScrollPane(previewPanel), BorderLayout.CENTER);
                    previewFrame.revalidate();
                    previewFrame.repaint();
                });
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, l.getString("failedpreview"), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();

        previewFrame.setVisible(true);
    }

    private static String getSystemLanguage() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();

        if (language == null || language.isEmpty()) {
            System.err.println("Language detection failed, defaulting to English.");
            return "en";
        }
        return language;
    }

    private static void printPdf(File file, LangManager l) {
        try (PDDocument document = PDDocument.load(file)) {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document));
            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(null, l.getString("successprint"), "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException | PrinterException e) {
            JOptionPane.showMessageDialog(null, l.getString("failedprint"), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void pdfLock(File inputFile, File outputFile, String ownerPassword, String userPassword) throws IOException {
        try (PDDocument document = PDDocument.load(inputFile)) {
            AccessPermission accessPermission = new AccessPermission();
            StandardProtectionPolicy protectionPolicy =
                    new StandardProtectionPolicy(ownerPassword, userPassword, accessPermission);
            protectionPolicy.setEncryptionKeyLength(128);
            protectionPolicy.setPermissions(accessPermission);
            document.protect(protectionPolicy);

            document.save(outputFile);
        } catch (IOException e) {
            throw new IOException("Failed to lock the PDF file.", e);
        }
    }

    private static void showMergeWindow(LangManager l) {
        JFrame mergeFrame = new JFrame(l.getString("mergeButton"));
        mergeFrame.setSize(600, 500);
        mergeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // List to store selected files
        ArrayList<File> selectedFiles = new ArrayList<>();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(fileList);

        // Label showing number of files
        JLabel fileCountLabel = new JLabel("0 " + l.getString("filesSelected"));
        fileCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton addFilesButton = new JButton(l.getString("addMoreFiles"));
        addFilesButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton clearButton = new JButton(l.getString("clearList"));
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton mergeButton = new JButton(l.getString("mergeFiles"));
        mergeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add files action
        addFilesButton.addActionListener(e -> {
            FileDialog fileDialog = new FileDialog(mergeFrame, l.getString("selectFilesToMerge"), FileDialog.LOAD);
            fileDialog.setMultipleMode(true);
            fileDialog.setVisible(true);
            File[] files = fileDialog.getFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.getName().toLowerCase().endsWith(".pdf")) {
                        selectedFiles.add(file);
                        listModel.addElement(file.getName());
                    }
                }
                fileCountLabel.setText(selectedFiles.size() + " " + l.getString("filesSelected"));
            }
        });

        // Clear list action
        clearButton.addActionListener(e -> {
            selectedFiles.clear();
            listModel.clear();
            fileCountLabel.setText("0 " + l.getString("filesSelected"));
        });

        // Merge action
        mergeButton.addActionListener(e -> {
            if (selectedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(mergeFrame, l.getString("noFilesSelected"),
                    l.getString("mergeButton"), JOptionPane.WARNING_MESSAGE);
                return;
            }

            FileDialog saveDialog = new FileDialog(mergeFrame, l.getString("saveMergedFile"), FileDialog.SAVE);
            saveDialog.setFile("merged.pdf");
            saveDialog.setVisible(true);
            String saveDirectory = saveDialog.getDirectory();
            String saveFilename = saveDialog.getFile();

            if (saveDirectory != null && saveFilename != null) {
                File outputFile = new File(saveDirectory, saveFilename);

                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        mergePdfs(selectedFiles, outputFile);
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            get();
                            JOptionPane.showMessageDialog(mergeFrame,
                                l.getString("mergeSuccess") + " " + outputFile.getAbsolutePath(),
                                l.getString("mergeButton"), JOptionPane.INFORMATION_MESSAGE);
                            mergeFrame.dispose();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            String errorMsg = l.getString("mergeFailed");
                            if (ex.getCause() != null) {
                                errorMsg += "\n" + ex.getCause().getMessage();
                            }
                            JOptionPane.showMessageDialog(mergeFrame,
                                errorMsg,
                                l.getString("mergeButton"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        });

        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(addFilesButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(mergeButton);
        buttonPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(fileCountLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        mergeFrame.add(mainPanel);
        mergeFrame.setVisible(true);
    }

    private static void mergePdfs(ArrayList<File> files, File outputFile) throws IOException {
        try {
            PDFMergerUtility pdfMerger = new PDFMergerUtility();
            pdfMerger.setDestinationFileName(outputFile.getAbsolutePath());

            for (File file : files) {
                pdfMerger.addSource(file);
            }

            pdfMerger.mergeDocuments(null);
        } catch (IOException e) {
            throw new IOException("Failed to merge PDF files: " + e.getMessage(), e);
        }
    }

}
