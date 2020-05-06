package com.clickhouse.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.ResultSet;
import java.sql.SQLException;

@SpringBootApplication
public class Boot {

    private static final Logger logger = LoggerFactory.getLogger(Boot.class);

    static {
        System.setProperty("Log4jContextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
    }


    public static void main(String[] args) {
        SpringApplication.run(Boot.class, args);
        logger.info("start clickhouse-test successfully");

        ClickHouseJDBCTest test = new ClickHouseJDBCTest("10.227.20.114", 9000, "test");
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
