package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.store.Data;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.*;

public class BatchJob {

    public static void main( String[] args ){
        //Kerberos Authentication + Integrated Security //command line tool works: sqlcmd -S hci-dbdev,1433 -d ResearchAdmin -U 'HCI\u0543505'
        String dbUrl = "jdbc:sqlserver://hci-dbdev:1433;databaseName=ResearchAdmin;integratedSecurity=true;authenticationScheme=JavaKerberos;encrypt=true;trustServerCertificate=true;";

        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");

        if (dbUsername == null || dbPassword == null) {
            throw new IllegalStateException("Database credentials are not set in environment variables.");
        }

        String redCapApiToken = "FD486BE328013358B9BCBDA6439B2343";
        String redCapUrl = "https://hci-redcap.hci.utah.edu/redcap/api/";

        try {
            //Queries Redcap system for former trainees contaied in project (just research_ids)
            SSLHelper.disableCertificateValidation(); //TODO: Fix this... had to disable SSL verification in dev b/c unable to verify redcap's SSL certificate
            Set<Integer> researcherIdsSet = RedCapService.fetchResearchIDFromRedcap(redCapApiToken, redCapUrl);
            System.out.println("Redcap query for researcher_ids: \n" + researcherIdsSet);

            //Queries RAD Database for former trainees (full info– research_ids, first_name, last_name, ect.)
            JsonNode fullTraineeRecords = DatabaseService.getFormerTraineeFromRAD(DatabaseService.QUERY,dbUrl,dbUsername,dbPassword);
            System.out.println("Database query for all trainees in training group:\n " + fullTraineeRecords.toPrettyString());

            //TODO: check what records are contained in Redcap that overlap with this query don't insert those, only the ones that have unqique record_ids

            //Imports only new trainees into Redcap System (ie not already contained)
            JsonNode importStatus = RedCapService.importRecordToRedCap(redCapUrl,fullTraineeRecords,redCapApiToken);
            System.out.println("Result of import attempt (returns ids of successfully imported records):\n" + importStatus.toPrettyString());

            //TODO: Do something if there is a discrpancy between Trainees attempted to insert and trainees who actually got inserted
        }
        catch (IOException e){
            System.err.println("Network or API error: " + e.getMessage());
            e.printStackTrace();  // Log full error details for debugging
        }
        catch (URISyntaxException e) {
            System.err.println("Invalid API URL: " + e.getMessage());
        }
        catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}










