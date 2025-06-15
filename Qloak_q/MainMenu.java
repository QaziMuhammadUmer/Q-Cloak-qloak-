

import java.util.Scanner;

/**
 *
 * @author user
 */
public class MainMenu {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== ALL-IN-ONE TOOLKIT ===");
            System.out.println("1) File Integrity Checker");
            System.out.println("2) Keylogger");
            System.out.println("3) Metadata Remover");
            System.out.println("4) Password Manager");
            System.out.println("5) Password Strength Checker");
            System.out.println("6) Exit");
            System.out.print("Select an option [1–6]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    // call your existing FileIntegrityChecker
                    FileIntegrityChecker.main(new String[]{});
                    break;
                case "2":
                    Keylogger.main(new String[]{});
                    break;
                case "3":
                    MetadataRemoverApp.main(new String[]{});
                    break;
                case "4":
                    pwmanager.main(new String[]{});
                    break;
                case "5":
                    pwstrengthchecker.main(new String[]{});
                    break;
                case "6":
                    System.out.println("Goodbye!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
