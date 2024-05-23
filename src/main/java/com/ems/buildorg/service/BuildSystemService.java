package com.ems.buildorg.service;

import com.ems.buildorg.modal.DbConfiguration;
import com.ems.buildorg.modal.RegistrationDetail;
import com.ems.buildorg.util.GetEncryptedPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BuildSystemService {
    @Autowired
    DbConfiguration databaseConfiguration;

    private DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        var catalog = databaseConfiguration.getCatalog();
        if (databaseConfiguration.getCatalog() == null || databaseConfiguration.getCatalog().isEmpty())
            catalog = "";

        dataSource.setDriverClassName(databaseConfiguration.getDriver());
        dataSource.setUrl(databaseConfiguration.getUrl() + catalog);
        dataSource.setUsername(databaseConfiguration.getUsername());
        dataSource.setPassword(databaseConfiguration.getPassword());
        dataSource.setCatalog(databaseConfiguration.getCatalog());
        return dataSource;
    }

    private List<String> getDatabaseScript(String databaseName) throws IOException, URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("backup.sql");
        File file = new File(resource.toURI());
        List<String> queries = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        boolean isProc = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--")) {
                    continue;
                }

                if (line.startsWith("/*") && line.trim().length() > 2) {
                    continue;
                }

                line = line.replaceAll("`", "");
                if (query.length() == 0) {
                    if (line.toUpperCase().startsWith("CREATE DATABASE")) {
                        queries.add(line.replace("__database_name__", databaseName));
                    } else if (line.toUpperCase().startsWith("USE") || line.toUpperCase().startsWith("DROP TABLE")) {
                        continue;
                    } else if (line.toUpperCase().startsWith("CREATE")) {
                        query = new StringBuilder(new StringBuilder(line));
                    } else if (line.toUpperCase().startsWith("DELIMITER ;;")) {
                        line = reader.readLine();
                        if (line != null && line.toUpperCase().contains("PROCEDURE")) {
                            line = line.replaceAll("`", "");
                            query = new StringBuilder();
                            query.append("\n")
                                    .append(line);

                            isProc = true;
                        } else {
                            query = new StringBuilder();
                            query.append(line);
                        }

                        query.append("\n");
                    } else if (line.toUpperCase().startsWith("INSERT INTO")) {
                        queries.add(line);
                    }
                } else {
                    if (line.toUpperCase().contains("ENGINE=INNODB")) {
                        query.append(line).append("\n");
                        queries.add(String.valueOf(query.toString()));
                        query = new StringBuilder();
                    } else if (line.toUpperCase().contains("DELIMITER ;")) {
                        query.append("\n");
                        queries.add(String.valueOf(query.toString()));
                        query = new StringBuilder();
                    } else {
                        query.append(line).append("\n");
                    }
                }
            }
        }

        queries.add(query.toString());
        return queries;
    }

    @Transactional(rollbackFor = Exception.class)
    public String buildNewOrganizationService(RegistrationDetail registrationDetail) throws IOException, URISyntaxException {
        if (registrationDetail.getOrganizationName() == null || registrationDetail.getOrganizationName().isEmpty()) {
            return "Invalid organization name passed";
        }

        if (registrationDetail.getTrailRequestId() == 0)
            return "Invalid request detail";

        String databaseName = getDatabaseName(registrationDetail);
        DataSource dataSource = getDataSource();
        List<String> scripts = getDatabaseScript(databaseName);

        // Create new database
        boolean flag = createDatabase(dataSource, scripts.get(0));
        if (flag) {
            // change connection string and point to new database
            databaseConfiguration.setCatalog(databaseName);
            dataSource = getDataSource();

            executeQuery(dataSource, scripts);
        }

        callStoredProcedureWithParameter(databaseName, registrationDetail);
        updateTrialrequestService(registrationDetail);
        return addNewEntryIntoMasterData(databaseName, registrationDetail.getOrganizationName());
    }

    private String addNewEntryIntoMasterData(String database, String organizationName) {
        databaseConfiguration.setCatalog("ems_master");
        DataSource dataSource = getDataSource();

        String outputParamValue = null;

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call sp_database_connections_insupd(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");

            // Set input parameters
            callableStatement.setInt("_EmsMasterId", 0);
            callableStatement.setString("_OrganizationCode", organizationName.toUpperCase().substring(0, 3));
            callableStatement.setString("_Code", "");
            callableStatement.setString("_Schema", "jdbc");
            callableStatement.setString("_DatabaseName", "mysql");
            callableStatement.setString("_Server", "178.16.138.169");
            callableStatement.setString("_Port", "3308");
            callableStatement.setString("_Database", database);
            callableStatement.setString("_UserId", "root");
            callableStatement.setString("_Password", databaseConfiguration.getPassword());
            callableStatement.setInt("_ConnectionTimeout", 30);
            callableStatement.setInt("_ConnectionLifetime", 0);
            callableStatement.setInt("_MinPoolSize", 0);
            callableStatement.setInt("_MaxPoolSize", 100);
            callableStatement.setBoolean("_Pooling", true);

            // Register output parameter
            callableStatement.registerOutParameter("_ProcessingResult", Types.VARCHAR);

            // Execute the stored procedure
            callableStatement.execute();

            // Retrieve output parameter value
            outputParamValue = callableStatement.getString("_ProcessingResult");

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception as needed
        }

        return outputParamValue;
    }

    private void updateTrialrequestService(RegistrationDetail registrationDetail) {
        databaseConfiguration.setCatalog("ems_master");
        DataSource dataSource = getDataSource();

        String outputParamValue = null;

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call sp_trail_request_insupd(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");

            // Set input parameters
            callableStatement.setLong("_TrailRequestId", registrationDetail.getTrailRequestId());
            callableStatement.setString("_FullName", registrationDetail.getFirstName() + " " + registrationDetail.getLastName());
            callableStatement.setString("_Email", registrationDetail.getEmailId());
            callableStatement.setString("_CompanyName", registrationDetail.getCompanyName());
            callableStatement.setString("_OrganizationName", registrationDetail.getOrganizationName());
            callableStatement.setString("_PhoneNumber", registrationDetail.getMobile());
            callableStatement.setInt("_HeadCount", 0);
            callableStatement.setString("_Country", registrationDetail.getCountry());
            callableStatement.setString("_State", registrationDetail.getState());
            callableStatement.setString("_City", registrationDetail.getCity());
            callableStatement.setString("_FullAddress", registrationDetail.getFirstAddress());
            callableStatement.setBoolean("_IsProcessed", true);
            // Register output parameter
            callableStatement.registerOutParameter("_ProcessingResult", Types.VARCHAR);

            // Execute the stored procedure
            callableStatement.execute();

            // Retrieve output parameter value
            outputParamValue = callableStatement.getString("_ProcessingResult");

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception as needed
        }
    }

    private String getDatabaseName(RegistrationDetail registrationDetail) {
        // Remove all whitespaces
        String organizationName = registrationDetail.getCompanyName().replaceAll("\\s", "");
        return "emstum_" + organizationName.toLowerCase() + "_" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd_MM_yy"));
    }

    public String callStoredProcedureWithParameter(String databaseName, RegistrationDetail registrationDetail) throws IOException {
        String outputParamValue = null;
//        String databaseName = getDatabaseName(registrationDetail);
//        databaseConfiguration.setUrl(databaseConfiguration.getUrl() + databaseName);
//        databaseConfiguration.setCatalog(databaseName);

        DataSource dataSource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        GetEncryptedPassword getEncryptedPassword = GetEncryptedPassword.getEncryptedPassword();
        var encryptedPassword = getEncryptedPassword.generateEncryptedPassword();
        try (Connection connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection()) {
            CallableStatement callableStatement = connection.prepareCall("{call sp_new_registration(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");

            // Set input parameters
            callableStatement.setString("_OrganizationName", registrationDetail.getOrganizationName());
            callableStatement.setString("_CompanyName", registrationDetail.getCompanyName());
            callableStatement.setString("_Mobile", registrationDetail.getMobile());
            callableStatement.setString("_EmailId", registrationDetail.getEmailId());
            callableStatement.setString("_FirstName", registrationDetail.getFirstName());
            callableStatement.setString("_LastName", registrationDetail.getLastName());
            callableStatement.setString("_Password", encryptedPassword);
            callableStatement.setString("_Country", registrationDetail.getCountry());
            callableStatement.setString("_State", registrationDetail.getState());
            callableStatement.setString("_City", registrationDetail.getCity());
            callableStatement.setString("_FirstAddress", registrationDetail.getFirstAddress());
            callableStatement.setString("_SecondAddress", registrationDetail.getSecondAddress());
            callableStatement.setString("_ThirdAddress", registrationDetail.getThirdAddress());
            callableStatement.setString("_ForthAddress", registrationDetail.getForthAddress());
            callableStatement.setString("_GSTNo", registrationDetail.getGSTNo());
            callableStatement.setInt("_DeclarationStartMonth", registrationDetail.getDeclarationStartMonth());
            callableStatement.setInt("_DeclarationEndMonth", registrationDetail.getDeclarationEndMonth());
            callableStatement.setInt("_FinancialYear", registrationDetail.getFinancialYear());
            callableStatement.setInt("_AttendanceSubmissionLimit", registrationDetail.getAttendanceSubmissionLimit());
            callableStatement.setInt("_ProbationPeriodInDays", registrationDetail.getProbationPeriodInDays());
            callableStatement.setInt("_NoticePeriodInDays", registrationDetail.getNoticePeriodInDays());
            callableStatement.setInt("_NoticePeriodInProbation", registrationDetail.getNoticePeriodInProbation());
            callableStatement.setInt("_CreatedBy", 1);
            callableStatement.setString("_TimezoneName", registrationDetail.getTimezoneName());


            // Register output parameter
            callableStatement.registerOutParameter("_ProcessingResult", Types.VARCHAR);

            // Execute the stored procedure
            callableStatement.execute();

            // Retrieve output parameter value
            outputParamValue = callableStatement.getString("_ProcessingResult");

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception as needed
        }

        return outputParamValue;
    }

    private boolean createDatabase(DataSource dataSource, String script) {
        boolean flag = false;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(script)
        ) {
            statement.execute();
            flag = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return flag;
    }

    private String executeQuery(DataSource dataSource, List<String> scripts) {
        String message = "";
        String query = "";
        int index = 1;
        List<String> executedTable = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();

            while (index < scripts.size()) {
                query = scripts.get(index);
                if (query.isEmpty()){
                    index++;
                    continue;
                }

                if (query.startsWith("CREATE TABLE")) {
                    String createTable = findTableName(query);

                    if (ifTableNotCreated(createTable, executedTable)) {
                        if (query.contains("CONSTRAINT")) {
                            getNextTable(query, executedTable, scripts, connection);
                            createTable = findTableName(query);
                            if (ifTableNotCreated(createTable, executedTable)) {
                                execute(connection, query);
                                executedTable.add(createTable);
                            }
                            index++;
                            continue;
                        } else {
                            query = scripts.get(index);
                        }

                        execute(connection, query);
                        executedTable.add(createTable);
                    }
                } else {
                    execute(connection, query);
                }

                index++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            message = e.getMessage() + " Query: " + query;
        } catch (Exception ex) {
            message = ex.getMessage();
        }

        return message;
    }

    private void execute(Connection connection, String query) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        statement.execute();
    }

    private void getNextTable(String query, List<String> executedTable, List<String> scripts, Connection connection) throws Exception {
        List<String> tables = findReferenceTableName(query);

        String newQuery = "";
        if (tables.size() > 0) {
            for (var table : tables) {
                var referencedTable = scripts.stream().filter(x -> x.startsWith("CREATE TABLE " + table)).findFirst();
                if (referencedTable.isPresent()) {
                    newQuery = referencedTable.get();
                    getNextTable(newQuery, executedTable, scripts, connection);
                }
            }

            String createTable = findTableName(query);
            if (ifTableNotCreated(createTable, executedTable)) {
                execute(connection, query);
                executedTable.add(createTable);
            }
        } else {
            String createTable = findTableName(query);
            if (ifTableNotCreated(createTable, executedTable)) {
                execute(connection, query);
                executedTable.add(createTable);
            }
        }
    }

    private boolean ifTableNotCreated(String tableName, List<String> createdTableList) {
        var records = createdTableList.stream().filter(x -> x.equals(tableName)).toList();

        return records.size() == 0;
    }

    private List<String> findReferenceTableName(String query) throws Exception {
        List<String> referencedTables = new ArrayList<>();
        // Regular expression pattern to match the table name
        Pattern pattern = Pattern.compile("REFERENCES\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(query);

        while (matcher.find()) {
            referencedTables.add(matcher.group(1));
        }

        return referencedTables;
    }

    private String findTableName(String query) throws Exception {
        // Regular expression pattern to match the table name
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(query);

        // Find the table name in the CREATE TABLE statement
        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            // If no match is found, return null or throw an exception
            throw new Exception("Table not found. Query: " + query);
            // Alternatively, you can throw an exception or handle the case as per your requirement
        }
    }
}
