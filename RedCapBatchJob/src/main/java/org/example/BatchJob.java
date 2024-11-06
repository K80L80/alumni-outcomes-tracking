package org.example;

import static org.example.MockRAD.createDatabase;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class BatchJob
{
    private static final String URL = "jdbc:h2:mem:testDB";
    private static final String USER = "user";
    private static final String PASSWORD = "password";
    
    public static void main( String[] args ) throws IOException, URISyntaxException {
        // Creating tester table and adding fake records
        TraineeDAO traineeDAO = new TraineeDAO();
        traineeDAO.createTestTrainees();
        System.out.println( "Done inserting test records into in-memory H2 database" );
        //TODO:


        // Query and print former trainees
        List<Trainee> formerTrainees = traineeDAO.getFormerTrainees();
        System.out.println("Former Trainees:");
        for (Trainee trainee : formerTrainees) {
            System.out.println(JSONUtility.toJson(trainee));
        }

        String apiToken = "FD486BE328013358B9BCBDA6439B2343";
        String projectUrl = "https://hci-redcap.hci.utah.edu/redcap/api/";

        //Uses Redcaps API to import records
        SSLHelper.disableCertificateValidation(); //TODO: Temporarily disable SSL verfication in dev b/c unable to verify redcap's SSL certificate

        RedCapService.importRecordToRedCap(apiToken,projectUrl,formerTrainees);
    }
}













