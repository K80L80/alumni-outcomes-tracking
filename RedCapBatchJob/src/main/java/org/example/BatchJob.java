package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

        //Get username and password from shell
        String dbUsername = System.getenv("DB_USERNAME");
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbUsername == null || dbPassword == null) {
            throw new IllegalStateException("Database credentials are not set in environment variables.");
        }

        //TODO: Switch over API token from mine to Tim's API token
        String redCapApiToken = "FD486BE328013358B9BCBDA6439B2343";
        String redCapUrl = "https://hci-redcap.hci.utah.edu/redcap/api/";

        try {
            //Queries RAD Database for former trainees (full info– research_ids, first_name, last_name, ect.)
            ArrayNode formerTraineeFromRAD = DatabaseService.getFormerTraineeFromRAD(DatabaseService.QUERY,dbUrl,dbUsername,dbPassword);
            System.out.println("Database query for all trainees in training group:\n " + formerTraineeFromRAD.toPrettyString());

            //Queries Redcap system for former trainees in project and returns as a set (just research_ids)
            SSLHelper.disableCertificateValidation(); //TODO: ASK Patrick I had to disable SSL verification in dev b/c unable to verify redcap's SSL certificate how should this be fixed for the production version?
            Set<Integer> redcapTraineeList = RedCapService.fetchResearchIDFromRedcap(redCapApiToken, redCapUrl);
            System.out.println("Redcap query for researcher_ids: \n" + redcapTraineeList);

            //remove trainees from RAD trainee list that are already contained in redcap project (to prevent duplicate record insertion)
            //check what records are contained in Redcap that overlap with this query don't insert those, only the ones that have unqique record_ids
            for(int i = formerTraineeFromRAD.size() - 1; i >= 0; i--) {
                JsonNode radTrainee = formerTraineeFromRAD.get(i);
                if (radTrainee.has("id_researcher") && redcapTraineeList.contains(radTrainee.get("id_researcher").asInt())) {
                    System.out.println("Redcap contains record:  "+ radTrainee.get("id_researcher").asInt() + " so it will be removed from trainee list for inserting into redcap");
                    formerTraineeFromRAD.remove(i);  // Safe way to remove items while iterating
                }
            }
            System.out.println("after trainee matches were removed" + formerTraineeFromRAD.toPrettyString());

            //Imports only new trainees into Redcap System (ie not already contained)
            JsonNode importStatus = RedCapService.importRecordToRedCap(redCapUrl,formerTraineeFromRAD,redCapApiToken);
            System.out.println("Result of import attempt (returns ids of successfully imported records):\n" + importStatus.toPrettyString());

            //TODO: Ask Patrick: Do something if there is a discrpancy between Trainees attempted to insert and trainees who actually got inserted???
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










