package org.example;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BatchJob
{
    public static void main( String[] args ) throws IOException, URISyntaxException {

            //command line tool works: sqlcmd -S hci-dbdev,1433 -d ResearchAdmin -U 'HCI\u0543505'

            //Kerberos Authentication + Integrated Security
            String url = "jdbc:sqlserver://hci-dbdev:1433;databaseName=ResearchAdmin;integratedSecurity=true;authenticationScheme=JavaKerberos;encrypt=true;trustServerCertificate=true;";

            String username = "HCI\\uXXXXXXX"; // Windows domain-style username
            String password = "XXXXXXX"; // Replace with your actual password

        try {
                // Step 1: Establish a connection
                System.out.println("about to open connection connected!");
               // String query = "SELECT TOP 15 * FROM TraineeProgram;";
            String query = """
                SELECT
                    e.idTraineeExperience,
                    e.idTrainee,
                    e.traineeProgramOtherText,
                    e.startDate,
                    e.endDate,
                    l.levelName,
                    p.programName
                FROM
                    TraineeExperience e
                INNER JOIN
                    TraineeLevel l
                ON
                    e.idTraineeLevel = l.idTraineeLevel
                INNER JOIN
                    TraineeProgram p
                ON
                     e.idTraineeProgram = p.idTraineeProgram
                WHERE
                     e.endDate < GETDATE() AND e.endDate IS NOT NULL;
                """;
                Connection connection = DriverManager.getConnection(url, username, password);
                System.out.println("Database connected!"); //
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = statement.executeQuery(query);

            // Getting metadata to determine column count and names
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            System.out.println("column count"+columnCount);

            // Array to track max lengths for each column
            int[] maxWidth = new int[columnCount];
            ArrayList<String> colNamesArr = new ArrayList<>();

            //Initialize max width as the column name length (ie 'isFaculty len = 9)
            for(int i =1; i <= columnCount; i++){//1,2,3,4,5
                String columnName = metaData.getColumnName(i); // Get the column name
                colNamesArr.add(i - 1, columnName);
                maxWidth[i-1] = columnName.length(); //0,1,2,3,4
//                System.out.println(columnName +" "+ maxWidth[i-1]);
            }

            //Change max width as needed if the values in the column are larger than the column name
            while (resultSet.next()) { //each next corresponds to a row
                //then once on a specific row you can retrieve the data by column
                for (int i = 1; i <= columnCount; i++) { // Columns are 1-indexed
                    Object value = resultSet.getObject(i); // Dynamically fetch column value
                    if(value == null ){
                        value = "blank";
                    }
                    if(maxWidth[i-1] < value.toString().length()){
                        maxWidth[i-1] = value.toString().length();
                    }
                }
            }
            resultSet.beforeFirst();

            //print column names with the appropriate width
            StringBuilder colNames = new StringBuilder();

            resultSet.beforeFirst(); // Move back to the start
            //space column names according to max width
            for (int i = 1; i <= columnCount; i++) {
                int spacesToAdd = maxWidth[i-1] - metaData.getColumnName(i).length(); //at least one space
                colNames.append(metaData.getColumnName(i)).append(" ".repeat(spacesToAdd)).append(" | ");
            }

            //Build table with proper spacing
            StringBuilder table = new StringBuilder();
            table.append(colNames.append("\n")); // add column names
            //get the row
            while (resultSet.next()){
                StringBuilder row = new StringBuilder();
                for (int i = 1; i <= columnCount; i++) { //iterate over the cells in that row
                    Object value = resultSet.getObject(i); //cell value
                    if (value == null) {
                        value = "blank";
                    }
                    Integer contentLen = value.toString().length();
                    Integer spacesToAdd = maxWidth[i-1] -contentLen;
                    row.append(value.toString()).append(" ".repeat(spacesToAdd)).append(" | "); //always add at least one space maybe more
                }
                //Add the row to the table
                table.append(row.append("\n"));
            }

            System.out.println(table);

            } catch (Exception e) {
                e.printStackTrace();
            }

           //TODO: REMOVE ALL STUFF ABOVE THIS LINE AND UNCOMMENT EVERYTHING UNDERNEATH IT

//        // Creating tester table and adding fake records
//        System.out.println( "Done inserting test records into in-memory H2 database" );
//
//        // Query and print former trainees
//        MockRAD traineeDAO = new MockRAD();
//        traineeDAO.createMockRadData();
//
//        List<Trainee> formerTrainees = traineeDAO.getFormerTrainees();
//        System.out.println("Former Trainees:");
//        for (Trainee trainee : formerTrainees) {
//            System.out.println(JSONUtility.toJson(trainee));
//        }
//
//        String apiToken = "FD486BE328013358B9BCBDA6439B2343";
//        String projectUrl = "https://hci-redcap.hci.utah.edu/redcap/api/";
//
//        //Uses Redcaps API to import records
//        SSLHelper.disableCertificateValidation(); //TODO: Fix this... had to disable SSL verification in dev b/c unable to verify redcap's SSL certificate
//
//        RedCapService.importRecordToRedCap(apiToken,projectUrl,formerTrainees);
//    } catch (SQLException e) {
//            throw new RuntimeException(e);
        }
    }














