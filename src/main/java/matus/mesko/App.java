package matus.mesko;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

public class App {
    private static File selectedFile = null;


    public static void main(String[] args) {
        LangManager l = new LangManager(getSystemLanguage());
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
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
        JToggleButton themeToggle = new JToggleButton(l.getString("lightmode"));
        themeToggle.addActionListener(e -> {
            try {
                if (themeToggle.isSelected()) {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    themeToggle.setText(l.getString("darkmode"));
                } else {
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    themeToggle.setText(l.getString("lightmode"));
                }
                SwingUtilities.updateComponentTreeUI(frame);
            } catch (Exception ex) {
                System.err.println("Failed to switch theme");
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

        JLabel fileLabel = new JLabel(l.getString("selectedfile") + l.getString("filenull"));
        fileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(new Color(27, 145, 50));
        statusLabel.setFont(new Font("Calibri", Font.BOLD, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setVisible(false);
        progressBar.setForeground(Color.GREEN);



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
                    } else {
                        fileLabel.setText(l.getString("selectedfile") + " " + l.getString("filenull"));
                        compressButton.setEnabled(false);
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
                                statusLabel.setText(l.getString("success") + " " + saveFile.getAbsolutePath());
                            }
                        };
                        worker.execute();
                    }
                }
            }
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

    private static String getSystemLanguage() {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();

        if (language == null || language.isEmpty()) {
            System.err.println("Language detection failed, defaulting to English.");
            return "en";  // Default language fallback
        }
        return language;
    }

}
