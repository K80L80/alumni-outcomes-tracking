package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DatabaseService {

    //TODO: Clarify with Melanie Ward if those from training group history table should only be included or if those from active trainees (ie in the Researcher Group should be included in the case they had a program specific participation which ended while they are still active members)

    //For former trainee members who have left it looks at their oldest end date
    static String QUERY2 =
            """
                SELECT
                    r.idResearcher AS id_researcher,
                    r.firstName AS first_name,
                    Min(rgh.endDate) AS [end] -- Gets the oldest training experience end date
                FROM Researcher r
                JOIN ResearcherGroupHistory rgh -- inactive CCSG members have no end date
                ON r.idResearcher = rgh.idResearcher AND rgh.idGroup = 119
                GROUP BY r.idResearcher, r.firstName;
            """;

    //active trainees (with training experience in the past)
    static String QUERY =
            """
                SELECT
                    ROW_NUMBER() OVER (ORDER BY r.idResearcher) AS record_id,
                    r.idResearcher AS id_researcher,
                    COALESCE(r.preferredName, r.firstName) AS first_name, -- pick preferred name the oldest training experience end date
                    r.lastName AS last_name,
                    'katieannestokes@gmail.com' AS personal_email,
                    MIN(te.endDate) AS end_date, -- Gets the oldest training experience end date
                    FORMAT(GETDATE(), 'yyyy-MM-dd HH:mm:ss') AS [end],
                    '1' AS email_okay,
                    te.idTraineeProgram AS trainee_program,
                    FORMAT(DATEADD(MINUTE, 1, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS intial_send_out,
                    FORMAT(DATEADD(MINUTE, 5, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year2,
                    FORMAT(DATEADD(MINUTE, 6, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year3,
                    FORMAT(DATEADD(MINUTE, 7, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year4,
                    FORMAT(DATEADD(MINUTE, 8, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year5,
                    FORMAT(DATEADD(MINUTE, 9, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year6,
                    FORMAT(DATEADD(MINUTE, 9, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year7,
                    FORMAT(DATEADD(MINUTE, 10, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year8,
                    FORMAT(DATEADD(MINUTE, 11, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year9,
                    FORMAT(DATEADD(MINUTE, 12, GETDATE()), 'yyyy-MM-dd HH:mm:ss') AS offset_year10
                FROM Researcher r
                JOIN ResearcherGroup rg -- Active CCSG members have no end date
                ON r.idResearcher = rg.idResearcher AND rg.idGroup = 119
                JOIN TraineeExperience te
                ON te.idResearcher = r.idResearcher AND te.idTraineeProgram != 14
                GROUP BY r.idResearcher, COALESCE(r.preferredName, r.firstName), r.lastName, te.idTraineeProgram
                FOR JSON PATH
            """;

    //For Java Doc –   //For active ccsg trainees it looks for any of them that have training experience that are in the past and will use that end date
    public static JsonNode getFormerTraineeFromRAD(String query,String dbURL,String username, String password) {
        ObjectMapper mapper = new ObjectMapper();
        try (
            Connection connection = DriverManager.getConnection(dbURL, username, password);
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
        ){
            System.out.println("Database connected!"); //
            String json = "";
            while (rs.next()) {
                json = rs.getString(1);  // JSON is returned as a single column
                // Pretty print JSON
            }
            return mapper.readTree(json);
            //TODO: deal with empty json?
        }
        catch (SQLException e){
            //TODO:  throw exception? or e.printStackTrace(); // Log properly in production
            return mapper.createObjectNode().put("error", "Database connection failed");
        }
        catch (com.fasterxml.jackson.core.JsonProcessingException e){
            //TODO:  throw exception? or e.printStackTrace(); // Log properly in production
            return mapper.createObjectNode().put("error", "JSON processing error");
        }
    }

    //Method just for testing, it prints the table in a tabular form rather than the JSON Array of JSON objects (ie Json Node)
    public static void printTable(String query, String dbURL, String username, String password) throws SQLException {

        Connection connection = DriverManager.getConnection(dbURL, username, password);
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = statement.executeQuery(query);

        // Getting metadata to determine column count and names
        ResultSetMetaData metaData = rs.getMetaData();
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
        while (rs.next()) { //each next corresponds to a row
            //then once on a specific row you can retrieve the data by column
            for (int i = 1; i <= columnCount; i++) { // Columns are 1-indexed
                Object value = rs.getObject(i); // Dynamically fetch column value
                if(value == null ){
                    value = "blank";
                }
                if(maxWidth[i-1] < value.toString().length()){
                    maxWidth[i-1] = value.toString().length();
                }
            }
        }
        rs.beforeFirst();

        //print column names with the appropriate width
        StringBuilder colNames = new StringBuilder();

        rs.beforeFirst(); // Move back to the start
        //space column names according to max width
        for (int i = 1; i <= columnCount; i++) {
            int spacesToAdd = maxWidth[i-1] - metaData.getColumnName(i).length(); //at least one space
            colNames.append(metaData.getColumnName(i)).append(" ".repeat(spacesToAdd)).append(" | ");
        }

        //Build table with proper spacing
        StringBuilder table = new StringBuilder();
        table.append(colNames.append("\n")); // add column names
        //get the row
        while (rs.next()){
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) { //iterate over the cells in that row
                Object value = rs.getObject(i); //cell value
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
    }
}
