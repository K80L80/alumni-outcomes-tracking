package org.example;

import static org.example.MockRAD.createDatabase;
import java.util.List;

public class BatchJob
{
    private static final String URL = "jdbc:h2:mem:testDB";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    
    public static void main( String[] args )
    {

        TraineeDAO traineeDAO = new TraineeDAO();

        // Insert test data
        traineeDAO.createTestTrainees(); //ERRROR

        // Query and print former trainees
        List<Trainee> formerTrainees = traineeDAO.getFormerTrainees();
        System.out.println("Former Trainees:");
        for (Trainee trainee : formerTrainees) {
            System.out.println(trainee);
        }
        System.out.println( "Hello World!" );
    }
}












