

import java.util.*;

/**
 * The main driver class for checking password strength.
 * Handles user I/O, exception catching, and loops until a valid non-empty password is provided.
 * Demonstrates exception handling with our custom InvalidPasswordException, prompting the user to re-enter if necessary.
 */
public class pwstrengthchecker {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Password password = null;

        // Loop until we get a valid non-empty password (throws InvalidPasswordException otherwise)
        while (password == null) {
            System.out.print("Enter your password: ");
            String input = scanner.nextLine();

            try {
                password = new Password(input);
            } catch (InvalidPasswordException e) {
                // Catch the custom exception and prompt again
                System.out.println("Invalid input: " + e.getMessage());
            }
        }

        // Once we have a valid Password object, evaluate its strength
        StrengthEvaluator evaluator = new StrengthEvaluator();
        Pair<String, SuggestionReport> result = evaluator.evaluate(password);

        String rating = result.getFirst();
        SuggestionReport report = result.getSecond();

        // Print out the simple rating
        System.out.println("\nPassword Rating: " + rating);

        // If not “Strong,” print suggestions
        if (report.hasSuggestions()) {
            System.out.println("Suggestions to improve your password:");
            List<String> suggestions = report.getSuggestions();  // Unmodifiable list from SuggestionReport
            for (String suggestion : suggestions) {
                System.out.println(" - " + suggestion);
            }
        } else {
            System.out.println("Great job! Your password meets all strength criteria.");
        }

        scanner.close();
    }
}

/**
 * Encapsulates the raw password string. Throws InvalidPasswordException if constructed with null or empty.
 * Demonstrates encapsulation: raw is private, only accessible via getter.
 */
class Password {
    private String raw; // private for encapsulation

    // Constructor validates the input; if invalid, throws our custom exception.
    public Password(String raw) throws InvalidPasswordException {
        if (raw == null || raw.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be null or empty.");
        }
        this.raw = raw;
    }

    // Public getter for the raw password.
    public String getRaw() {
        return raw;
    }
}

/**
 * Custom checked exception thrown when the Password object is constructed with invalid input.
 * Demonstrates exception handling by extending Exception (checked exception).
 */
class InvalidPasswordException extends Exception {
    public InvalidPasswordException(String message) {
        super(message);
    }
}

/**
 * An abstract base class representing a single "strength criterion".
 * We use an abstract class (instead of an interface) because we want to share a protected
 * field (suggestion) across all subclasses. This demonstrates the use of protected visibility
 * and common code placement. Subclasses override test(...) to implement specific rules.
 */
abstract class Criterion {
    // Protected so that subclasses can access or modify if extended further.
    protected String suggestion;

    // Constructor sets the suggestion message for any failing password.
    public Criterion(String suggestion) {
        this.suggestion = suggestion;
    }

    // Abstract method each subclass must implement to test its own rule.
    public abstract boolean test(String password);

    // Returns the suggestion message. Kept public so external classes (e.g., StrengthEvaluator)
    // can retrieve it when a test fails.
    public String getSuggestion() {
        return suggestion;
    }
}

/**
 * Checks that a password meets a minimum length requirement.
 * Demonstrates inheritance from Criterion and method overriding.
 * Uses a protected field minLength, which could be accessed by future subclasses if extended.
 */
class LengthCriterion extends Criterion {
    // Protected to allow potential subclasses (e.g., a more complex length-checking rule) to use it.
    protected int minLength;

    // Single-argument constructor initializes the minimum length and sets the suggestion.
    public LengthCriterion(int minLength) {
        super("Make your password at least " + minLength + " characters long");
        this.minLength = minLength;
    }

    // Overrides the abstract test method to check length.
    @Override
    public boolean test(String password) {
        if (password == null) {
            // We could throw an exception here, but we'll let StrengthEvaluator handle null-check separately.
            return false;
        }
        // Here is a trivial "safe" example of an impossible scenario check:
        if (minLength < 0) {
            throw new IllegalArgumentException("Minimum length cannot be negative");
        }
        return password.length() >= minLength;
    }
}

/**
 * Checks that a password contains at least one uppercase letter.
 * Inherits from Criterion and overrides test(...). Demonstrates encapsulation
 * by keeping no fields public and using the base class constructor for suggestion.
 */
class UppercaseCriterion extends Criterion {

    // No extra fields needed (no-arg constructor).
    public UppercaseCriterion() {
        super("Add at least one uppercase letter (A-Z)");
    }

    @Override
    public boolean test(String password) {
        if (password == null) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Checks that a password contains at least one lowercase letter.
 * Similar to UppercaseCriterion—demonstrates code reuse via inheritance.
 */
class LowercaseCriterion extends Criterion {

    public LowercaseCriterion() {
        super("Add at least one lowercase letter (a-z)");
    }

    @Override
    public boolean test(String password) {
        if (password == null) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Checks that a password contains at least one digit (0–9).
 */
class DigitCriterion extends Criterion {

    public DigitCriterion() {
        super("Add at least one digit (0-9)");
    }

    @Override
    public boolean test(String password) {
        if (password == null) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Checks that a password contains at least one special symbol (non-alphanumeric).
 */
class SymbolCriterion extends Criterion {

    public SymbolCriterion() {
        super("Add at least one special symbol (e.g., !, @, #, $, %, ^, &, *)");
    }

    @Override
    public boolean test(String password) {
        if (password == null) {
            return false;
        }
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }
}

/**
 * Aggregates suggestion messages. Demonstrates aggregation: it holds a List<String> of suggestions
 * that were generated elsewhere (by Criterion objects) but does not "own" how they were created.
 * In a UML sense, SuggestionReport aggregates suggestions without deep composition.
 */
class SuggestionReport {
    // We use a generic collection here: List<String> to hold suggestion messages.
    private List<String> suggestions;

    public SuggestionReport() {
        // Initialize with an empty ArrayList
        this.suggestions = new ArrayList<>();
    }

    // Adds a single suggestion to the list.
    public void addSuggestion(String suggestion) {
        // We could trim and check for duplicates if desired. For now, simply add.
        suggestions.add(suggestion);
    }

    // Returns an unmodifiable view of suggestions to maintain encapsulation.
    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    // Convenience method to check if there are any suggestions.
    public boolean hasSuggestions() {
        return !suggestions.isEmpty();
    }
}

/**
 * A generic helper class demonstrating the use of Java generics.
 * It holds two related objects (first and second). We include it to show generic types
 * but do not strictly need it for the core functionality. This satisfies the requirement
 * to create and demonstrate at least one generic helper class.
 */
class Pair<A, B> {
    private A first;   // Encapsulated
    private B second;  // Encapsulated

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}

/**
 * Responsible for running all Criteria on a given Password and returning both a rating and a report.
 * Demonstrates composition: it holds a List<Criterion> that it owns and manages.
 * Demonstrates dynamic dispatch: iterating over List<Criterion> and invoking test(...) calls the right subclass.
 * Also shows upcasting: storing each concrete criterion (e.g., new LengthCriterion(8)) into a Criterion reference.
 */
class StrengthEvaluator {
    // Composition: StrengthEvaluator composes (owns) a list of Criterion objects.
    private List<Criterion> criteria;

    // Constructor initializes and populates the criteria list.
    public StrengthEvaluator() {
        this.criteria = new ArrayList<>();

        // Up-casting: each concrete Criterion is stored as a Criterion reference.
        criteria.add(new LengthCriterion(8));    // Minimum length = 8
        criteria.add(new UppercaseCriterion());
        criteria.add(new LowercaseCriterion());
        criteria.add(new DigitCriterion());
        criteria.add(new SymbolCriterion());
        // In the future, you can swap in new Criterion subclasses without modifying other code.
    }

    /**
     * Evaluates the given Password and returns a Pair containing:
     *  - First: a String rating ("Weak", "Moderate", or "Strong")
     *  - Second: a SuggestionReport with any suggestions needed to improve the password.
     *
     * @param pw The validated Password object
     * @return Pair<String, SuggestionReport>
     */
    public Pair<String, SuggestionReport> evaluate(Password pw) {
        if (pw == null) {
            // In an impossible scenario if null is passed, we throw IllegalArgumentException.
            throw new IllegalArgumentException("Password object cannot be null");
        }

        String raw = pw.getRaw();
        SuggestionReport report = new SuggestionReport();

        // Loop through all criteria. Dynamic dispatch ensures the correct test method is called.
        for (Criterion criterion : criteria) {
            boolean passed = criterion.test(raw);
            if (!passed) {
                // If the password fails this criterion, get its suggestion.
                report.addSuggestion(criterion.getSuggestion());
            }
        }

        // Determine overall rating based on number of unmet criteria (i.e., suggestions).
        String rating;
        int failures = report.getSuggestions().size();
        if (failures == 0) {
            rating = "Strong";      // Met all criteria
        } else if (failures <= 2) {
            rating = "Moderate";    // Missed 1–2 criteria
        } else {
            rating = "Weak";        // Missed 3 or more criteria
        }

        // We could return a Pair<String, SuggestionReport>, demonstrating usage of our generic Pair.
        return new Pair<>(rating, report);
    }
}
