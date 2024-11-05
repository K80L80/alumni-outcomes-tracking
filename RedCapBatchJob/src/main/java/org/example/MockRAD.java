package org.example;


import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MockRAD {

    private final Connection connection;

    public MockRAD(Connection connection) {
        this.connection = connection;
    }

    //A Method To create a Trainee Table
    public static void createDatabase(String URL, String USER, String Password) {
        try {
            // H2 Database (in-memory based database)
            Connection connection = DriverManager.getConnection(URL, USER, Password);
            Statement statement = connection.createStatement();

            // Create a sample table
            statement.execute("CREATE TABLE IF NOT EXISTS Trainees " +
                    "(id INT PRIMARY KEY, " +
                    "firstName VARCHAR(255), " +
                    "lastName VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "endDate DATE)");

            // Add a mock record for testing, insert if they do not already exist
            statement.execute("MERGE INTO Trainees (id, firstName, lastName, email, endDate) KEY(id) VALUES " +
                    "(1, 'John', 'Doe', 'john.doe@example.com', '2023-11-01')," +
                    "(2, 'Jane', 'Smith', 'jane.smith@example.com', '2022-05-15')," +
                    "(3, 'Alice', 'Johnson', 'alice.johnson@example.com', '2024-01-10')," +
                    "(4, 'Bob', 'Brown', 'bob.brown@example.com', '2020-12-25')");


            // Query the database
            ResultSet rs = statement.executeQuery("SELECT * FROM Trainees WHERE endDate < CURRENT_DATE");
            while (rs.next()) {
                System.out.println("Eligible for Redcap: " + rs.getString("firstName") + " " + rs.getString("lastName"));
                // Here you could call the Redcap API to insert the data
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addingSampleTrainees(){


    }
    public static void printFormerTrainees() {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:~/testDB", "user", "password");
             Statement statement = connection.createStatement()) {

            // Query to select trainees whose endDate is in the past
            String query = "SELECT firstName, lastName, email, endDate FROM Trainees WHERE endDate < CURRENT_DATE";
            ResultSet rs = statement.executeQuery(query);

            System.out.println("Former trainees:");
            while (rs.next()) {
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                String email = rs.getString("email");
                LocalDate endDate = rs.getDate("endDate").toLocalDate();

                System.out.println("Name: " + firstName + " " + lastName + ", Email: " + email + ", End Date: " + endDate);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}