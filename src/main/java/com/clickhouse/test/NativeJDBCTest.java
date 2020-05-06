package com.clickhouse.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ProjectName: com.clickhouse.test
 * ClassName:   NativeJDBCTest
 * Copyright:
 * Company:     b*d*
 * author:      queyiwen
 * version:     v1.0
 * since:
 * Create at:   2020/5/6
 * Description:
 * <p>
 * <p>
 * Modification History:
 * Date       Author      Version      Description
 * -------------------------------------------------------------
 *
 *
 */
public class NativeJDBCTest {

    private static final Logger logger = LoggerFactory.getLogger(NativeJDBCTest.class);

    private final static String JDBC_URL_FORMAT = "jdbc:clickhouse://%s:%d/%s?user=%s&password=%s";

    private String host;
    private int port;
    private String database;


    private String jdbcUrl;
    private Connection connection;
    private Statement statement;


    public NativeJDBCTest(String host, int port, String database, String user, String password) {
        this.jdbcUrl = String.format(JDBC_URL_FORMAT, host, port, database, user, password);
        logger.debug("jdbc url: " + this.jdbcUrl);
    }

    public void createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.github.housepower.jdbc.ClickHouseDriver");
        this.connection = DriverManager.getConnection(this.jdbcUrl);
        logger.debug("create connection successfully");
    }

    public void createStatement() throws SQLException {
        this.statement = this.connection.createStatement();
        logger.debug("create statement successfully");
    }

    public ResultSet createResultSet(String sql) throws SQLException {
        ResultSet resultSet = this.statement.executeQuery(sql);
        return resultSet;
    }


    public void getResult(ResultSet resultSet) throws SQLException {
        if (resultSet == null) {
            return;
        }

        while (resultSet.next()) {
            String str = resultSet.getString("c2");
            System.out.println(str);
        }
    }

    public void closeStatement() {
        if (this.statement == null) {
            return;
        }

        try {
            this.statement.close();
            logger.debug("close statement successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (this.connection == null) {
            return;
        }

        try {
            this.connection.close();
            logger.debug("close connection successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        NativeJDBCTest test = new NativeJDBCTest("10.227.20.114", 9000, "test", "default", "yiwen");
        ResultSet resultSet = null;
        try {
            test.createConnection();
            test.createStatement();
            resultSet = test.createResultSet("select c1, c2, create_date from test.table1;");
            test.getResult(resultSet);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            test.closeStatement();
            test.closeConnection();
        }
    }

}
