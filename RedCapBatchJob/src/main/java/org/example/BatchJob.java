package org.example;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class BatchJob
{
    public static void main( String[] args ) throws IOException, URISyntaxException {
        // Creating tester table and adding fake records
        System.out.println( "Done inserting test records into in-memory H2 database" );

        // Query and print former trainees
        TraineeDAO traineeDAO = new TraineeDAO();
        traineeDAO.createMockRadData();

        List<Trainee> formerTrainees = traineeDAO.getFormerTrainees();
        System.out.println("Former Trainees:");
        for (Trainee trainee : formerTrainees) {
            System.out.println(JSONUtility.toJson(trainee));
        }

        String apiToken = "FD486BE328013358B9BCBDA6439B2343";
        String projectUrl = "https://hci-redcap.hci.utah.edu/redcap/api/";

        //Uses Redcaps API to import records
        SSLHelper.disableCertificateValidation(); //TODO: Fix this... had to disable SSL verification in dev b/c unable to verify redcap's SSL certificate

        RedCapService.importRecordToRedCap(apiToken,projectUrl,formerTrainees);
    }
}













