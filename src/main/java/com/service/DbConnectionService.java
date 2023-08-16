package com.service;

import java.sql.*;

public class DbConnectionService {
    public static void nsysDdConnection(StringBuilder dbOutput) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        try {
            System.out.println("开始连接数据库");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@10.4.2.179:1521/orcl", "ORATDIKO01", "password");
            System.out.println("数据库连接成功");

            Statement statement = connection.createStatement();

            String sql = "select * from M_CM_SOUKO";

            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String a = resultSet.getString("MS053_SOUKO");
                dbOutput.append(a).append("\n");
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            dbOutput.append("数据库连接错误\n");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    System.out.println("关闭数据库连接");
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static String ccFlowDdConnection( String tableName, String itemName) throws ClassNotFoundException, SQLException {
        Connection connection = null;
        try {
            System.out.println("开始连接数据库");
            connection = DriverManager.getConnection("jdbc:oracle:thin:@10.4.2.179:1521/orcl", "ORATDIKO01", "password");
            System.out.println("数据库连接成功");

            Statement statement = connection.createStatement();

            // 查询与项目名相同注释的字段名
            String query = "SELECT COLUMN_NAME " +
                    "FROM USER_COL_COMMENTS " +
                    "WHERE TABLE_NAME = '" + tableName + "' " +
                    "AND COMMENTS LIKE '" + itemName + "%'";

            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String dbItemName = resultSet.getString("COLUMN_NAME");
                return dbItemName;
            }

            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            System.out.println("数据库连接异常");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    System.out.println("关闭数据库连接");
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
