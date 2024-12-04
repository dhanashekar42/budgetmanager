import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private String phoneOrEmail;
    private String passwordHash;

    public User(String firstName, String lastName, String phoneOrEmail, String password) throws NoSuchAlgorithmException {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneOrEmail = phoneOrEmail;
        this.passwordHash = hashPassword(password);
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneOrEmail() {
        return phoneOrEmail;
    }

    public boolean validatePassword(String inputPassword) throws NoSuchAlgorithmException {
        return this.passwordHash.equals(hashPassword(inputPassword));
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " | " + phoneOrEmail;
    }
}

class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type; // income or expense
    private String category;
    private double amount;
    private LocalDate date;

    public Transaction(String type, String category, double amount, String dateStr) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        try {
            this.date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format, please use YYYY-MM-DD.");
        }
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return type + " | " + category + " | " + amount + " | " + date;
    }
}

public class BudgetManager {
    private static final String USER_DATA_FILE = "users.dat";
    private static final String TRANSACTION_DATA_FILE = "transactions.dat";
    private List<User> users = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private User loggedInUser;

    public BudgetManager() {
        loadData();
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
            System.out.println("Saved users: " + users); // Debug statement
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRANSACTION_DATA_FILE))) {
            oos.writeObject(transactions);
            System.out.println("Saved transactions: " + transactions); // Debug statement
        } catch (IOException e) {
            System.out.println("Error saving transaction data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
            users = (List<User>) ois.readObject();
            System.out.println("Loaded users: " + users); // Debug statement
        } catch (FileNotFoundException e) {
            System.out.println("User data file not found. A new file will be created.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TRANSACTION_DATA_FILE))) {
            transactions = (List<Transaction>) ois.readObject();
            System.out.println("Loaded transactions: " + transactions); // Debug statement
        } catch (FileNotFoundException e) {
            System.out.println("Transaction data file not found. A new file will be created.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading transaction data: " + e.getMessage());
        }
    }

    public void registerUser(String firstName, String lastName, String phoneOrEmail, String password) throws NoSuchAlgorithmException {
        User user = new User(firstName, lastName, phoneOrEmail, password);
        users.add(user);
        System.out.println("User added: " + user); // Debug statement
    }

    public boolean loginUser(String phoneOrEmail, String password) throws NoSuchAlgorithmException {
        for (User user : users) {
            if (user.getPhoneOrEmail().equals(phoneOrEmail) && user.validatePassword(password)) {
                loggedInUser = user;
                return true;
            }
        }
        return false;
    }

    public void addTransaction(String type, String category, double amount, String date) {
        if (loggedInUser == null) {
            System.out.println("Please log in to add transactions.");
            return;
        }
        Transaction transaction = new Transaction(type, category, amount, date);
        transactions.add(transaction);
        System.out.println("Transaction added: " + transaction); // Debug statement
    }

    public void printSummary() {
        double totalIncome = 0;
        double totalExpense = 0;
        for (Transaction t : transactions) {
            if (t.getType().equalsIgnoreCase("income")) {
                totalIncome += t.getAmount();
            } else if (t.getType().equalsIgnoreCase("expense")) {
                totalExpense += t.getAmount();
            }
        }
        System.out.println("Total Income: " + totalIncome);
        System.out.println("Total Expenses: " + totalExpense);
        System.out.println("Balance: " + (totalIncome - totalExpense));
    }

    public void printTransactions() {
        System.out.println("Type | Category | Amount | Date");
        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BudgetManager budgetManager = new BudgetManager();
    
        while (true) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Add Income");
            System.out.println("4. Add Expense");
            System.out.println("5. View Summary");
            System.out.println("6. View Transactions");
            System.out.println("7. Exit");
    
            int choice = 0;
            boolean validInput = false;
            while (!validInput) {
                try {
                    System.out.print("Enter your choice: ");
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validInput = true;
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input! Please enter a number.");
                    scanner.nextLine(); // Consume the invalid input
                }
            }
    
            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter first name: ");
                        String firstName = scanner.nextLine();
                        System.out.print("Enter last name: ");
                        String lastName = scanner.nextLine();
                        System.out.print("Enter phone/email: ");
                        String phoneOrEmail = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String password = scanner.nextLine();
                        budgetManager.registerUser(firstName, lastName, phoneOrEmail, password);
                        System.out.println("User registered successfully!");
                        break;
                    case 2:
                        System.out.print("Enter phone/email: ");
                        String loginPhoneOrEmail = scanner.nextLine();
                        System.out.print("Enter password: ");
                        String loginPassword = scanner.nextLine();
                        if (budgetManager.loginUser(loginPhoneOrEmail, loginPassword)) {
                            System.out.println("Login successful!");
                        } else {
                            System.out.println("Invalid phone/email or password.");
                        }
                        break;
                    case 3:
                    case 4:
                        if (budgetManager.loggedInUser == null) {
                            System.out.println("Please log in first.");
                            break;
                        }
                        System.out.print("Enter category: ");
                        String category = scanner.nextLine();
                        double amount = 0;
                        boolean validAmount = false;
                        while (!validAmount) {
                            try {
                                System.out.print("Enter amount: ");
                                amount = scanner.nextDouble();
                                scanner.nextLine(); // Consume newline
                                validAmount = true;
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input! Please enter a valid number for amount.");
                                scanner.nextLine(); // Consume the invalid input
                            }
                        }
                        System.out.print("Enter date (YYYY-MM-DD): ");
                        String date = scanner.nextLine();
                        budgetManager.addTransaction(choice == 3 ? "income" : "expense", category, amount, date);
                        break;
                    case 5:
                        budgetManager.printSummary();
                        break;
                    case 6:
                        budgetManager.printTransactions();
                        break;
                    case 7:
                        budgetManager.saveData();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}  