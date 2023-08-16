package com.app.B2;

import javax.swing.*;
import java.sql.*;

/**
 * 读取仕样书做成model属性注释
 *
 * @author Sunghalee
 */
public class B2lReader extends JFrame {
//    public static void main() {
//        // JDBC连接信息
//        String jdbcUrl = "jdbc:sqlserver://ccflowtest.database.windows.net:1433;database=ccflow8_test";
//        String username = "ccflowtest";
//        String password = "shinseiDX2022";
//
//        // 尝试建立数据库连接
//        try {
//            // 加载SQL Server的JDBC驱动程序
//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//
//            // 建立连接
//            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
//
//            // 连接成功后可以进行数据库操作
//            Statement statement = connection.createStatement();
//
//            String table = "WF_ShiftWork";
//
//            //查询与项目名相同注释的字段名 (sqlserver数据库查询列伦理名)
//            String sqlQuery = "SELECT\n" +
//                    "  a1.name as columnName,\n" +
//                    "  b.value as columnComment\n" +
//                    "FROM\n" +
//                    "  sysobjects a\n" +
//                    "  LEFT JOIN sys.columns a1 ON a.id = a1.object_id\n" +
//                    "  LEFT JOIN sys.extended_properties b ON b.major_id = a.id AND b.minor_id = a1.column_id\n" +
//                    "WHERE\n" +
//                    "  a.name = '" + table + "'";
//
//            ResultSet resultSet = statement.executeQuery(sqlQuery);
//
//            while (resultSet.next()) {
//                String dbItemName = resultSet.getString("columnComment");
//                System.out.println(dbItemName);
//            }
//
//            resultSet.close();
//            statement.close();
//            connection.close();
//        } catch (ClassNotFoundException e) {
//            System.out.println("找不到SQL Server的JDBC驱动程序");
//            e.printStackTrace();
//        } catch (SQLException e) {
//            System.out.println("数据库连接出错");
//            e.printStackTrace();
//        }
//    }
}
