import java.sql.*;
import java.util.Scanner;

public class HospitalManagementSystem {

    private static final String URL = "jdbc:mysql://localhost:3306/hospital_db";
    private static final String USER = "root";
    private static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println("       HOSPITAL PATIENT RECORD SYSTEM");
        System.out.println("==============================================");

        while (true) {
            String role = login();

            if (role == null) {
                System.out.println("\nInvalid username or password.");
                continue;
            }

            System.out.println("\nLogin successful!");
            System.out.println("Logged in as: " + role);

            showMenu(role);

            System.out.println("\nThank you for using the Hospital Patient Record System.");
            break;
        }

        scanner.close();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String login() {

        System.out.println("\n--------------- LOGIN ---------------");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                }
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase connection error.");
            System.out.println("Check MySQL and database configuration.");
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }

    private static void showMenu(String role) {

        while (true) {

            System.out.println("\n==============================================");
            System.out.println("                  MAIN MENU");
            System.out.println("==============================================");

            System.out.println("1. Add New Patient");
            System.out.println("2. View Patient Details");
            System.out.println("3. View All Patients");
            System.out.println("4. Update Patient Details");
            System.out.println("5. Update Medical History");

            if (role.equals("ADMIN")) {
                System.out.println("6. Delete Patient");
            }

            System.out.println("0. Logout");

            int choice = readInt("\nEnter your choice: ");

            switch (choice) {

                case 1:
                    if (role.equals("ADMIN") || role.equals("RECEPTIONIST")) {
                        addPatient();
                    } else {
                        accessDenied();
                    }
                    break;

                case 2:
                    viewPatient();
                    break;

                case 3:
                    viewAllPatients();
                    break;

                case 4:
                    if (role.equals("ADMIN") || role.equals("RECEPTIONIST")) {
                        updatePatient();
                    } else {
                        accessDenied();
                    }
                    break;

                case 5:
                    if (role.equals("ADMIN") || role.equals("DOCTOR")) {
                        updateMedicalHistory();
                    } else {
                        accessDenied();
                    }
                    break;

                case 6:
                    if (role.equals("ADMIN")) {
                        deletePatient();
                    } else {
                        accessDenied();
                    }
                    break;

                case 0:
                    System.out.println("\nLogging out...");
                    return;

                default:
                    System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private static void addPatient() {

        System.out.println("\n--------------- ADD NEW PATIENT ---------------");

        String name = readRequired("Patient Name: ");

        int age = readInt("Age: ");

        if (age <= 0 || age > 120) {
            System.out.println("Invalid age.");
            return;
        }

        String gender = readRequired("Gender: ");
        String phone = readRequired("Phone Number: ");

        if (!phone.matches("\\d{10}")) {
            System.out.println("Phone number must contain exactly 10 digits.");
            return;
        }

        String bloodGroup = readRequired("Blood Group: ");
        String address = readRequired("Address: ");
        String medicalHistory = readRequired("Medical History: ");

        String sql = "INSERT INTO patients " +
                "(name, age, gender, phone, blood_group, address, medical_history) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, age);
            statement.setString(3, gender);
            statement.setString(4, phone);
            statement.setString(5, bloodGroup);
            statement.setString(6, address);
            statement.setString(7, medicalHistory);

            if (statement.executeUpdate() > 0) {
                System.out.println("\nPatient added successfully!");
            } else {
                System.out.println("\nFailed to add patient.");
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }
    }

    private static void viewPatient() {

        System.out.println("\n--------------- VIEW PATIENT ---------------");

        int patientId = readInt("Enter Patient ID: ");

        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    printPatient(resultSet);
                } else {
                    System.out.println("\nPatient not found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }
    }

    private static void viewAllPatients() {

        System.out.println("\n--------------- ALL PATIENTS ---------------");

        String sql = "SELECT patient_id, name, age, gender, phone, blood_group " +
                "FROM patients ORDER BY patient_id";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            boolean found = false;

            System.out.println("\nID | Name | Age | Gender | Phone | Blood Group");
            System.out.println("--------------------------------------------------------");

            while (resultSet.next()) {

                found = true;

                System.out.printf(
                        "%d | %s | %d | %s | %s | %s%n",
                        resultSet.getInt("patient_id"),
                        resultSet.getString("name"),
                        resultSet.getInt("age"),
                        resultSet.getString("gender"),
                        resultSet.getString("phone"),
                        resultSet.getString("blood_group")
                );
            }

            if (!found) {
                System.out.println("No patient records found.");
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }
    }

    private static void updatePatient() {

        System.out.println("\n--------------- UPDATE PATIENT ---------------");

        int patientId = readInt("Enter Patient ID: ");

        if (!patientExists(patientId)) {
            System.out.println("\nPatient not found.");
            return;
        }

        String name = readRequired("New Name: ");

        int age = readInt("New Age: ");

        if (age <= 0 || age > 120) {
            System.out.println("Invalid age.");
            return;
        }

        String gender = readRequired("New Gender: ");
        String phone = readRequired("New Phone Number: ");

        if (!phone.matches("\\d{10}")) {
            System.out.println("Phone number must contain exactly 10 digits.");
            return;
        }

        String bloodGroup = readRequired("New Blood Group: ");
        String address = readRequired("New Address: ");

        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, " +
                "phone = ?, blood_group = ?, address = ? WHERE patient_id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, age);
            statement.setString(3, gender);
            statement.setString(4, phone);
            statement.setString(5, bloodGroup);
            statement.setString(6, address);
            statement.setInt(7, patientId);

            if (statement.executeUpdate() > 0) {
                System.out.println("\nPatient details updated successfully!");
            } else {
                System.out.println("\nFailed to update patient.");
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }
    }

    private static void updateMedicalHistory() {

        System.out.println("\n--------------- UPDATE MEDICAL HISTORY ---------------");

        int patientId = readInt("Enter Patient ID: ");

        if (!patientExists(patientId)) {
            System.out.println("\nPatient not found.");
            return;
        }

        String history = readRequired("Enter New Medical History: ");

        String sql = "UPDATE patients SET medical_history = ? WHERE patient_id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, history);
            statement.setInt(2, patientId);

            if (statement.executeUpdate() > 0) {
                System.out.println("\nMedical history updated successfully!");
            } else {
                System.out.println("\nFailed to update medical history.");
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }
    }

    private static void deletePatient() {

        System.out.println("\n--------------- DELETE PATIENT ---------------");

        int patientId = readInt("Enter Patient ID: ");

        if (!patientExists(patientId)) {
            System.out.println("\nPatient not found.");
            return;
        }

        System.out.print(
                "Are you sure you want to delete this patient? (yes/no): ");

        String confirmation = scanner.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("\nDelete operation cancelled.");
            return;
        }

        String sql = "DELETE FROM patients WHERE patient_id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            if (statement.executeUpdate() > 0) {
                System.out.println("\nPatient deleted successfully!");
            } else {
                System.out.println("\nFailed to delete patient.");
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
        }
    }

    private static boolean patientExists(int patientId) {

        String sql = "SELECT patient_id FROM patients WHERE patient_id = ?";

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, patientId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.out.println("\nDatabase error: " + e.getMessage());
            return false;
        }
    }

    private static void printPatient(ResultSet resultSet)
            throws SQLException {

        System.out.println("\n----------------------------------------");
        System.out.println("Patient ID      : " + resultSet.getInt("patient_id"));
        System.out.println("Name            : " + resultSet.getString("name"));
        System.out.println("Age             : " + resultSet.getInt("age"));
        System.out.println("Gender          : " + resultSet.getString("gender"));
        System.out.println("Phone           : " + resultSet.getString("phone"));
        System.out.println("Blood Group     : " + resultSet.getString("blood_group"));
        System.out.println("Address         : " + resultSet.getString("address"));
        System.out.println("Medical History : " + resultSet.getString("medical_history"));
        System.out.println("----------------------------------------");
    }

    private static int readInt(String message) {

        while (true) {

            try {
                System.out.print(message);
                return Integer.parseInt(scanner.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readRequired(String message) {

        while (true) {

            System.out.print(message);

            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("This field cannot be empty.");
        }
    }

    private static void accessDenied() {

        System.out.println("\nACCESS DENIED!");
        System.out.println(
                "You do not have permission to perform this operation.");
    }
}