import java.util.*;
import java.io.*;

class Book {
    private int id;
    private String title;
    private String author;
    private String category;
    private boolean isIssued;

    public Book(int id, String title, String author, String category) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isIssued = false;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public boolean isIssued() {
        return isIssued;
    }

    public void setIssued(boolean issued) {
        isIssued = issued;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Title: %s | Author: %s | Category: %s | Issued: %s",
                id, title, author, category, isIssued ? "Yes" : "No");
    }
}

class Member {
    private int id;
    private String name;
    private String email;
    private List<Book> issuedBooks;
    private double fine;

    public Member(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.issuedBooks = new ArrayList<>();
        this.fine = 0.0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Book> getIssuedBooks() {
        return issuedBooks;
    }

    public double getFine() {
        return fine;
    }

    public void addFine(double amount) {
        fine += amount;
    }

    public void payFine(double amount) {
        if (amount <= fine) {
            fine -= amount;
        }
    }

    public void issueBook(Book book) {
        issuedBooks.add(book);
    }

    public void returnBook(Book book) {
        issuedBooks.remove(book);
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Name: %s | Email: %s | Fine: %.2f | Issued Books: %d",
                id, name, email, fine, issuedBooks.size());
    }
}

class Library {
    private Map<Integer, Book> books;
    private Map<Integer, Member> members;
    private int nextBookId;
    private int nextMemberId;
    private static final String BOOKS_FILE = "books.txt";
    private static final String MEMBERS_FILE = "members.txt";

    public Library() {
        books = new HashMap<>();
        members = new HashMap<>();
        nextBookId = 1;
        nextMemberId = 1;
        loadData();
    }

    // Book management
    public Book addBook(String title, String author, String category) {
        Book book = new Book(nextBookId++, title, author, category);
        books.put(book.getId(), book);
        saveData();
        return book;
    }

    public boolean updateBook(int id, String title, String author, String category) {
        Book book = books.get(id);
        if (book == null) return false;
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        saveData();
        return true;
    }

    public boolean deleteBook(int id) {
        boolean removed = books.remove(id) != null;
        if (removed) saveData();
        return removed;
    }

    public Book getBook(int id) {
        return books.get(id);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public List<Book> getBooksByCategory(String category) {
        List<Book> result = new ArrayList<>();
        for (Book book : books.values()) {
            if (book.getCategory().equalsIgnoreCase(category)) {
                result.add(book);
            }
        }
        return result;
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> result = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Book book : books.values()) {
            if (book.getTitle().toLowerCase().contains(lowerKeyword) ||
                book.getAuthor().toLowerCase().contains(lowerKeyword) ||
                book.getCategory().toLowerCase().contains(lowerKeyword)) {
                result.add(book);
            }
        }
        return result;
    }

    // Member management
    public Member addMember(String name, String email) {
        Member member = new Member(nextMemberId++, name, email);
        members.put(member.getId(), member);
        saveData();
        return member;
    }

    public boolean updateMember(int id, String name, String email) {
        Member member = members.get(id);
        if (member == null) return false;
        // For simplicity, update name and email
        member = new Member(id, name, email);
        members.put(id, member);
        saveData();
        return true;
    }

    public boolean deleteMember(int id) {
        boolean removed = members.remove(id) != null;
        if (removed) saveData();
        return removed;
    }

    public Member getMember(int id) {
        return members.get(id);
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members.values());
    }

    // Issue and return books
    public boolean issueBook(int memberId, int bookId) {
        Member member = members.get(memberId);
        Book book = books.get(bookId);
        if (member == null || book == null || book.isIssued()) {
            return false;
        }
        book.setIssued(true);
        member.issueBook(book);
        saveData();
        // Log the issue event with timestamp, user name, user ID, book ID
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String logEntry = String.format("%s|ISSUE|%d|%s|%d%n", timestamp, member.getId(), member.getName(), book.getId());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LibraryManagementSystem.TRANSACTIONS_FILE, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.out.println("Error logging transaction: " + e.getMessage());
        }
        return true;
    }

    public boolean returnBook(int memberId, int bookId) {
        Member member = members.get(memberId);
        Book book = books.get(bookId);
        if (member == null || book == null || !book.isIssued()) {
            return false;
        }
        book.setIssued(false);
        member.returnBook(book);
        saveData();
        // Log the return event with timestamp, user name, user ID, book ID
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String logEntry = String.format("%s|RETURN|%d|%s|%d%n", timestamp, member.getId(), member.getName(), book.getId());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LibraryManagementSystem.TRANSACTIONS_FILE, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.out.println("Error logging transaction: " + e.getMessage());
        }
        return true;
    }

    // Fine generation (simple fixed fine for demonstration)
    public void generateFine(int memberId, double amount) {
        Member member = members.get(memberId);
        if (member != null) {
            member.addFine(amount);
            saveData();
        }
    }

    // Report generation (simple print)
    public void generateReport() {
        System.out.println("Library Report:");
        System.out.println("Books:");
        for (Book book : books.values()) {
            System.out.println(book);
        }
        System.out.println("Members:");
        for (Member member : members.values()) {
            System.out.println(member);
        }
    }

    // Display raw stored data from books.txt and members.txt
    public void displayStoredData() {
        System.out.println("\nStored Books Data (books.txt):");
        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading books.txt: " + e.getMessage());
        }

        System.out.println("\nStored Members Data (members.txt):");
        try (BufferedReader br = new BufferedReader(new FileReader(MEMBERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading members.txt: " + e.getMessage());
        }
    }

    // Save data to text files
    private void saveData() {
        try (BufferedWriter bookWriter = new BufferedWriter(new FileWriter(BOOKS_FILE));
             BufferedWriter memberWriter = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            for (Book book : books.values()) {
                // Format: id|title|author|category|isIssued
                bookWriter.write(String.format("%d|%s|%s|%s|%b%n",
                        book.getId(), escape(book.getTitle()), escape(book.getAuthor()),
                        escape(book.getCategory()), book.isIssued()));
            }
            for (Member member : members.values()) {
                // Format: id|name|email|fine|issuedBookIds(comma separated)
                StringBuilder issuedBookIds = new StringBuilder();
                for (Book b : member.getIssuedBooks()) {
                    if (issuedBookIds.length() > 0) issuedBookIds.append(",");
                    issuedBookIds.append(b.getId());
                }
                memberWriter.write(String.format("%d|%s|%s|%.2f|%s%n",
                        member.getId(), escape(member.getName()), escape(member.getEmail()),
                        member.getFine(), issuedBookIds.toString()));
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // Load data from text files
    private void loadData() {
        books.clear();
        members.clear();
        nextBookId = 1;
        nextMemberId = 1;
        // Load books
        try (BufferedReader bookReader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = bookReader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0]);
                    String title = unescape(parts[1]);
                    String author = unescape(parts[2]);
                    String category = unescape(parts[3]);
                    boolean isIssued = Boolean.parseBoolean(parts[4]);
                    Book book = new Book(id, title, author, category);
                    book.setIssued(isIssued);
                    books.put(id, book);
                    if (id >= nextBookId) nextBookId = id + 1;
                }
            }
        } catch (FileNotFoundException e) {
            // File not found, start fresh
        } catch (IOException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
        // Load members
        try (BufferedReader memberReader = new BufferedReader(new FileReader(MEMBERS_FILE))) {
            String line;
            while ((line = memberReader.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5) {
                    int id = Integer.parseInt(parts[0]);
                    String name = unescape(parts[1]);
                    String email = unescape(parts[2]);
                    double fine = Double.parseDouble(parts[3]);
                    String issuedBooksStr = parts[4];
                    Member member = new Member(id, name, email);
                    member.addFine(fine);
                    if (!issuedBooksStr.isEmpty()) {
                        String[] issuedBookIds = issuedBooksStr.split(",");
                        for (String bookIdStr : issuedBookIds) {
                            try {
                                int bookId = Integer.parseInt(bookIdStr);
                                Book book = books.get(bookId);
                                if (book != null) {
                                    member.issueBook(book);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    members.put(id, member);
                    if (id >= nextMemberId) nextMemberId = id + 1;
                }
            }
        } catch (FileNotFoundException e) {
            // File not found, start fresh
        } catch (IOException e) {
            System.out.println("Error loading members: " + e.getMessage());
        }
    }

    // Escape pipe and newline characters in strings
    private String escape(String input) {
        return input.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n").replace("\r", "");
    }

    // Unescape pipe and newline characters in strings
    private String unescape(String input) {
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escape) {
                if (c == 'n') sb.append('\n');
                else sb.append(c);
                escape = false;
            } else {
                if (c == '\\') escape = true;
                else sb.append(c);
            }
        }
        return sb.toString();
    }
}

public class LibraryManagementSystem {
    private static Library library = new Library();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the Library Management System");
        while (true) {
            System.out.println("\nSelect mode:");
            System.out.println("1. Admin");
            System.out.println("2. User");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    adminMenu();
                    break;
                case 2:
                    userMenu();
                    break;
                case 3:
                    System.out.println("Exiting... Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void adminMenu() {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. Add Member");
            System.out.println("5. Update Member");
            System.out.println("6. Delete Member");
            System.out.println("7. Generate Report");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    addBook();
                    break;
                case 2:
                    updateBook();
                    break;
                case 3:
                    deleteBook();
                    break;
                case 4:
                    addMember();
                    break;
                case 5:
                    updateMember();
                    break;
                case 6:
                    deleteMember();
                    break;
                case 7:
                    library.generateReport();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void userMenu() {
        while (true) {
            System.out.println("\nUser Menu:");
            System.out.println("1. View All Books");
            System.out.println("2. Browse Books by Category");
            System.out.println("3. Search Book");
            System.out.println("4. Issue Book");
            System.out.println("5. Return Book");
            System.out.println("6. Send Email Query");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter choice: ");
            int choice = readInt();
            switch (choice) {
                case 1:
                    viewAllBooks();
                    break;
                case 2:
                    browseByCategory();
                    break;
                case 3:
                    searchBook();
                    break;
                case 4:
                    issueBook();
                    break;
                case 5:
                    returnBook();
                    break;
                case 6:
                    sendEmailQuery();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addBook() {
        System.out.print("Enter title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author: ");
        String author = scanner.nextLine();
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        Book book = library.addBook(title, author, category);
        System.out.println("Book added: " + book);
    }

    private static void updateBook() {
        System.out.print("Enter book ID to update: ");
        int id = readInt();
        System.out.print("Enter new title: ");
        String title = scanner.nextLine();
        System.out.print("Enter new author: ");
        String author = scanner.nextLine();
        System.out.print("Enter new category: ");
        String category = scanner.nextLine();
        boolean success = library.updateBook(id, title, author, category);
        if (success) {
            System.out.println("Book updated successfully.");
        } else {
            System.out.println("Book not found.");
        }
    }

    private static void deleteBook() {
        System.out.print("Enter book ID to delete: ");
        int id = readInt();
        boolean success = library.deleteBook(id);
        if (success) {
            System.out.println("Book deleted successfully.");
        } else {
            System.out.println("Book not found.");
        }
    }

    private static void addMember() {
        System.out.print("Enter member name: ");
        String name = scanner.nextLine();
        System.out.print("Enter member email: ");
        String email = scanner.nextLine();
        Member member = library.addMember(name, email);
        System.out.println("Member added: " + member);
    }

    private static void updateMember() {
        System.out.print("Enter member ID to update: ");
        int id = readInt();
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();
        System.out.print("Enter new email: ");
        String email = scanner.nextLine();
        boolean success = library.updateMember(id, name, email);
        if (success) {
            System.out.println("Member updated successfully.");
        } else {
            System.out.println("Member not found.");
        }
    }

    private static void deleteMember() {
        System.out.print("Enter member ID to delete: ");
        int id = readInt();
        boolean success = library.deleteMember(id);
        if (success) {
            System.out.println("Member deleted successfully.");
        } else {
            System.out.println("Member not found.");
        }
    }

    private static void viewAllBooks() {
        List<Book> books = library.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("No books available.");
        } else {
            for (Book book : books) {
                System.out.println(book);
            }
        }
    }

    private static void browseByCategory() {
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        List<Book> books = library.getBooksByCategory(category);
        if (books.isEmpty()) {
            System.out.println("No books found in this category.");
        } else {
            for (Book book : books) {
                System.out.println(book);
            }
        }
    }

    private static void searchBook() {
        System.out.print("Enter keyword to search: ");
        String keyword = scanner.nextLine();
        List<Book> books = library.searchBooks(keyword);
        if (books.isEmpty()) {
            System.out.println("No books found matching the keyword.");
        } else {
            for (Book book : books) {
                System.out.println(book);
            }
        }
    }

    private static void issueBook() {
        System.out.print("Enter your member ID: ");
        int memberId = readInt();
        System.out.print("Enter book ID to issue: ");
        int bookId = readInt();
        boolean success = library.issueBook(memberId, bookId);
        if (success) {
            System.out.println("Book issued successfully.");
        } else {
            System.out.println("Issue failed. Check member ID, book ID, or if the book is already issued.");
        }
    }

    private static void returnBook() {
        System.out.print("Enter your member ID: ");
        int memberId = readInt();
        System.out.print("Enter book ID to return: ");
        int bookId = readInt();
        boolean success = library.returnBook(memberId, bookId);
        if (success) {
            System.out.println("Book returned successfully.");
        } else {
            System.out.println("Return failed. Check member ID, book ID, or if the book was issued.");
        }
    }

    public static final String TRANSACTIONS_FILE = "transactions.txt";
    public static final String EMAIL_QUERIES_FILE = "email_queries.txt";

    private static void sendEmailQuery() {
        System.out.print("Enter your member ID: ");
        int memberId = readInt();
        Member member = library.getMember(memberId);
        if (member == null) {
            System.out.println("Member not found. Cannot send query.");
            return;
        }
        System.out.print("Enter your query: ");
        String query = scanner.nextLine();
        // Log the email query with timestamp, user name, and user ID
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
        String logEntry = String.format("%s|%d|%s|%s%n", timestamp, member.getId(), member.getName(), query);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMAIL_QUERIES_FILE, true))) {
            writer.write(logEntry);
        } catch (IOException e) {
            System.out.println("Error saving email query: " + e.getMessage());
        }
        System.out.println("Thank you! Your query has been sent. We will respond to " + member.getEmail() + " soon.");
    }

    private static int readInt() {
        while (true) {
            try {
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}
