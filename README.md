# PDF Utilities

## Overview

The **PDF Utilities** is a simple Java Swing-based GUI tool that allows users to compress PDF files. It provides a user-friendly interface with theme switching, file selection, and progress tracking.

---

## Features

- **PDF Compression:** Select and compress PDF files with ease.
- **Multi-language Support:** Automatically adapts to the system's language.
- **Dark/Light Theme Toggle:** Switch between dark and light themes.
- **Progress Bar:** Visual indication of compression progress.
- **Error Handling:** Displays appropriate error messages for invalid files.

---

## Technologies Used

- **Java Swing:** GUI implementation.
- **FlatLaf:** Modern look and feel for Swing applications.
- **iText Library:** PDF compression capabilities.
- **Localization:** Properties files for multi-language support.

---

## Installation

### Prerequisites

- **Java 8+**

---

## Usage

1**Select a PDF File:**

    - Click the `Select PDF` button and choose a file.
    - Selected file will be displayed.

2**Compress the PDF:**

    - Click the `Compress PDF` button to start compression.
    - A save dialog will prompt you to select the destination.

3**Change Themes:**

    - Use the `Options` menu to switch between light and dark themes.

---

## Folder Structure

```
project-root/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── matus/mesko/
│   │   │   │   ├── App.java
│   │   │   │   ├── LangManager.java
│   │   ├── resources/
│   │   │   ├── messages_cs.properties
│   │   │   ├── messages_de.properties
│   │   │   ├── messages_en.properties
│   │   │   ├── messages_fr.properties
│   │   │   ├── messages_hu.properties
│   │   │   ├── messages_pl.properties
│   │   │   ├── messages_sk.properties
│   │   │   ├── pdf.png
├── pom.xml
└── README.md
```

---

## Language Support

The application automatically detects the system language and loads the corresponding localization file. Supported languages include:

- **Czech** (cs)
- **German** (de)
- **English** (en)
- **French** (fr)
- **Hungarian** (hu)
- **Polish** (pl)
- **Slovak** (sk)

If the language file is not found, the application falls back to English.

---

## License

This project is licensed under the MIT License.

---

## Acknowledgements

Libraries that I used in this project:

- [FlatLaf](https://www.formdev.com/flatlaf/)
- [iText PDF](https://itextpdf.com/)

---

## Contact

For any issues or suggestions, feel free to contact:

- Email: [meskomatusko@gmail.com](mailto\:meskomatusko@gmail.com)
- GitHub: [Project Repository](https://github.com/matusmesko/PDF-Utilities)

