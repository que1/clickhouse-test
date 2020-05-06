package com.clickhouse.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ProjectName: com.clickhouse.test
 * ClassName:   ConcurrencyTest
 * Copyright:
 * Company:     bytedance
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
public class ConcurrencyTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrencyTest.class);

    private static final int TEST_COUNT = 120;

    public void test() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(TEST_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(TEST_COUNT);
        for (int i = 0; i < TEST_COUNT; i++) {
            executorService.execute(new ClickHouseQueryTask(i, cyclicBarrier));
        }
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        ConcurrencyTest concurrencyTest = new ConcurrencyTest();
        long startTime = System.currentTimeMillis();
        concurrencyTest.test();
        long endTime = System.currentTimeMillis();
        logger.info("cost-time: " + (endTime - startTime) + "ms");
    }


    public class ClickHouseQueryTask implements Runnable {

        private int taskId;
        private CyclicBarrier cyclicBarrier;

        private NativeJDBCTest nativeJDBCTest;

        public ClickHouseQueryTask(int taskId, CyclicBarrier cyclicBarrier) {
            this.taskId = taskId;
            this.cyclicBarrier = cyclicBarrier;
            this.nativeJDBCTest = new NativeJDBCTest("10.227.20.114", 9000, "test", "default", "yiwen");
        }

        @Override
        public void run() {
            try {
                // 先创建连接
                this.prepare();
                // 等待执行
                this.cyclicBarrier.await();
                // 执行查询
                this.testQuery();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }

        private void prepare() {
            long startTime = System.currentTimeMillis();
            try {
                this.nativeJDBCTest.createConnection();
                this.nativeJDBCTest.createStatement();
            } catch (ClassNotFoundException e) {
                logger.error("create connection/statement error, class not found exception", e);
            } catch (SQLException e) {
                logger.error("create connection/statement error, sql exception", e);
            }
            long endTime = System.currentTimeMillis();
            logger.info(String.format("task[%d] init successfully, cost: %dms", this.taskId, (endTime - startTime)));
        }

        private void testQuery() {
            long startTime = System.currentTimeMillis();
            ResultSet resultSet = null;
            int num = -1;
            try {
                resultSet = this.nativeJDBCTest.createResultSet("select count(1) as num from test.ontime where Year = 2001;");
                while (resultSet.next()) {
                    num = resultSet.getInt("num");
                }
            } catch (SQLException e) {
                logger.error("create result set error, sql exception", e);
            } finally {
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        logger.error("close result set error, sql exception", e);
                    }
                }
                this.nativeJDBCTest.closeStatement();
                this.nativeJDBCTest.closeConnection();
            }
            long endTime = System.currentTimeMillis();
            logger.info(String.format("task[%d] test-query result: %d, cost: %dms", this.taskId, num, (endTime - startTime)));
        }

    }


}
