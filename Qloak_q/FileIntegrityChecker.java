

// Import for reading input from user
import java.util.Scanner;

// Import for handling files
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// Import for calculating hash
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Main class that starts the program and manages user interaction
public class FileIntegrityChecker {

    // Main method where the program starts
    public static void main(String[] args) {

        // Create Scanner object to read user input
        Scanner scanner = new Scanner(System.in);

        // Show options to the user
        System.out.println("Welcome to the File Integrity Checker!");
        System.out.println("Type 'generate' to generate and save a checksum.");
        System.out.println("Type 'verify' to verify a file using saved checksum.");

        String choice = "";

        // Keep asking until a non-empty valid option is entered
        while (true) {
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("generate") || choice.equalsIgnoreCase("verify")) {
                break;
            } else {
                System.out.println("Invalid choice. Please type 'generate' or 'verify'.");
            }
        }

        // Ask the user to enter the path of the file they want to use
        File sourceFile = null;
        while (true) {
            System.out.print("Enter the full path of the file: ");
            String filePath = scanner.nextLine().trim();

            if (!filePath.isEmpty()) {
                sourceFile = new File(filePath);

                // Check if file exists and is a file (not directory)
                if (sourceFile.exists() && sourceFile.isFile()) {
                    break;
                } else {
                    System.out.println("Invalid file. Please enter a valid existing file path.");
                }
            } else {
                System.out.println("File path cannot be blank.");
            }
        }

        // Create instances of helper classes
        FileHashCalculator calculator = new FileHashCalculator();
        ChecksumFileManager fileManager = new ChecksumFileManager();

        try {
            if (choice.equalsIgnoreCase("generate")) {
                // Generate hash
                String checksum = calculator.generateChecksum(sourceFile);

                // Ask for target directory
                String targetDirectory = "";
                while (true) {
                    System.out.print("Enter the directory path where you want to save the checksum file: ");
                    targetDirectory = scanner.nextLine().trim();

                    File dir = new File(targetDirectory);
                    if (!targetDirectory.isEmpty() && dir.exists() && dir.isDirectory()) {
                        break;
                    } else {
                        System.out.println("Invalid directory. Please enter a valid directory path.");
                    }
                }

                // Save checksum to specified directory
                fileManager.saveChecksumToDirectory(sourceFile, targetDirectory, checksum);
                System.out.println("Checksum saved successfully.");

            } else if (choice.equalsIgnoreCase("verify")) {
                // Verify checksum
                boolean result = fileManager.verifyChecksum(sourceFile, calculator);
                if (result) {
                    System.out.println("File is intact. Checksums match!");
                } else {
                    System.out.println("WARNING: File may have been changed! Checksums do NOT match.");
                }
            }
        } catch (IOException e) {
            // Handle file input/output errors
            System.out.println("An I/O error occurred: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            // Handle invalid hashing algorithm errors
            System.out.println("Hashing error: SHA-256 not supported.");
        }

        // Close scanner
        scanner.close();
    }
}

// Class to calculate hash values of files
class FileHashCalculator {

    // Constructor
    public FileHashCalculator() {}

    // Method to calculate SHA-256 checksum of a file
    public String generateChecksum(File file) throws IOException, NoSuchAlgorithmException {

        // Create MessageDigest for SHA-256
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Create a buffer to read file data
        byte[] buffer = new byte[1024];
        int bytesRead;

        // Read file content
        try (FileReader fr = new FileReader(file)) {
            while ((bytesRead = fr.read()) != -1) {
                digest.update((byte) bytesRead);
            }
        }

        // Generate hash bytes
        byte[] hashBytes = digest.digest();

        // Convert hash bytes to hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        // Return final checksum
        return hexString.toString();
    }
}

// Class to manage saving and verifying checksum files
class ChecksumFileManager {

    // Constructor
    public ChecksumFileManager() {}

    // Method to save checksum in a .sha256 file
    public void saveChecksumToDirectory(File file, String directoryPath, String checksum) throws IOException {

        // Get original file name
        String originalFileName = file.getName();

        // Build full path: directory + filename + ".sha256"
        File checksumFile = new File(directoryPath + File.separator + originalFileName + ".sha256");

        // Write checksum to file
        try (FileWriter writer = new FileWriter(checksumFile)) {
            writer.write(checksum);
        }
    }

    // Method to read checksum from a saved .sha256 file
    public String readChecksumFromFile(File file) throws IOException {
        // Get checksum file path (same directory, same name, .sha256 extension)
        File checksumFile = new File(file.getParent() + File.separator + file.getName() + ".sha256");

        // Read the first line from the file
        try (BufferedReader reader = new BufferedReader(new FileReader(checksumFile))) {
            return reader.readLine();
        }
    }

    // Method to compare file checksum with saved one
    public boolean verifyChecksum(File file, FileHashCalculator calculator) throws IOException, NoSuchAlgorithmException {

        // Read original checksum from file
        String originalChecksum = readChecksumFromFile(file);

        // Generate current checksum
        String currentChecksum = calculator.generateChecksum(file);

        // Compare and return result
        return originalChecksum != null && originalChecksum.equals(currentChecksum);
    }
}
