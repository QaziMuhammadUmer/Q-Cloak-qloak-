// Main.java

import java.io.*;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class pwmanager {
    public static void main(String[] args) {
        PasswordManager manager = new PasswordManager();
        Scanner scanner = new Scanner(System.in);
        
        while(true) {
            System.out.println("\nPassword Manager Menu:");
            System.out.println("1. Save Passwords");
            System.out.println("2. Retrieve Passwords");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            switch(choice) {
                case 1:
                    manager.savePasswords(scanner);
                    break;
                case 2:
                    manager.retrievePasswords(scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }
}


 class PasswordManager {
    // Hard-coded master password (set by programmer)
    private static final String MASTER_PASSWORD = "secret123";
    
    public void savePasswords(Scanner scanner) {
        try {
            System.out.print("\nEnter number of username/password pairs to save: ");
            int count = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            String[][] credentials = new String[count][2];
            
            // Collect credentials
            for(int i = 0; i < count; i++) {
                System.out.print("Enter username #" + (i+1) + ": ");
                credentials[i][0] = scanner.nextLine();
                System.out.print("Enter password #" + (i+1) + ": ");
                credentials[i][1] = scanner.nextLine();
            }
            
            // Choose encryption method
            System.out.print("Choose encryption method (AES/DES): ");
            String method = scanner.nextLine().toUpperCase();
            
            // Encrypt passwords
            for(int i = 0; i < count; i++) {
                credentials[i][1] = EncryptionUtil.encrypt(credentials[i][1], method, MASTER_PASSWORD);
            }
            
            // Save to file
            System.out.print("Enter file path to save: ");
            String filePath = scanner.nextLine();
            writeToFile(credentials, filePath);
            System.out.println("Credentials saved successfully!");
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private void writeToFile(String[][] data, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for(String[] pair : data) {
                writer.println(pair[0] + ":" + pair[1]);
            }
        }
    }
    
    public void retrievePasswords(Scanner scanner) {
        try {
            System.out.print("\nEnter encryption method used (AES/DES): ");
            String method = scanner.nextLine().toUpperCase();
            System.out.print("Enter file path: ");
            String filePath = scanner.nextLine();
            
            String[][] credentials = readFromFile(filePath);
            
            System.out.println("\nStored Credentials:");
            for(String[] pair : credentials) {
                System.out.println("Username: " + pair[0] + " | Encrypted Password: " + pair[1]);
            }
            
            System.out.print("\nDo you want to decrypt passwords? (yes/no): ");
            String choice = scanner.nextLine().toLowerCase();
            
            if(choice.equals("yes")) {
                System.out.print("Enter master password: ");
                String inputPassword = scanner.nextLine();
                
                if(inputPassword.equals(MASTER_PASSWORD)) {
                    System.out.println("\nDecrypted Passwords:");
                    for(String[] pair : credentials) {
                        String decrypted = EncryptionUtil.decrypt(pair[1], method, MASTER_PASSWORD);
                        System.out.println("Username: " + pair[0] + " | Password: " + decrypted);
                    }
                } else {
                    System.out.println("Incorrect master password!");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    private String[][] readFromFile(String filePath) throws IOException {
        // First count the number of lines
        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while(reader.readLine() != null) lineCount++;
        }
        
        // Now read the actual content
        String[][] credentials = new String[lineCount][2];
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int index = 0;
            while((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if(parts.length == 2) {
                    credentials[index][0] = parts[0];
                    credentials[index][1] = parts[1];
                    index++;
                }
            }
        }
        return credentials;
    }
}

// EncryptionUtil.java


 class EncryptionUtil {
    public static String encrypt(String plainText, String algorithm, String password) throws Exception {
        SecretKeySpec key = generateKey(algorithm, password);
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    public static String decrypt(String encryptedText, String algorithm, String password) throws Exception {
        SecretKeySpec key = generateKey(algorithm, password);
        Cipher cipher = Cipher.getInstance(algorithm + "/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }
    
    private static SecretKeySpec generateKey(String algorithm, String password) throws Exception {
        // Create SHA-1 hash of password
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] keyBytes = sha.digest(password.getBytes());
        
        // Truncate to appropriate length
        int keyLength = algorithm.equals("AES") ? 16 : 8; // 16 bytes for AES-128, 8 bytes for DES
        keyBytes = Arrays.copyOf(keyBytes, keyLength);
        
        return new SecretKeySpec(keyBytes, algorithm);
    }
}
