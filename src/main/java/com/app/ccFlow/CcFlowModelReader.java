package com.app.ccFlow;

import com.MainFrame;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

/**
 * 读取仕样书做成model属性注释
 *
 * @author Sunghalee
 */
public class CcFlowModelReader extends JFrame {
    private static JTextArea outputTextArea;
    private static JTextField columnItemField;
    private static JTextField columnTypeField;
    private static JTextField tableField;
    private static JTextField sheetField;
    private static JFrame frame;

    // 默认的文件选择路径
    private static String lastSelectedFilePath = "D:\\";

    public static void action() {
        frame = new JFrame("金骋软件考勤转换 author Sunghalee");

        //设定图标样式
        createIcon();

        // 设置Nimbus Look and Feel
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(new AcrylLookAndFeel());
        } catch (Exception e) {
            String err = e.toString();
            System.out.println(err);
        }

        //关闭只关闭当前窗口，不结束程序运行
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        //设定滚动条位置 便于输出结束时查看结果
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));

        //实例化4个输入窗口
        sheetField = new JTextField();
        columnItemField = new JTextField();
        columnTypeField = new JTextField();
        tableField = new JTextField();

        inputPanel.add(new JLabel("SHEET名："));
        inputPanel.add(sheetField);
        inputPanel.add(new JLabel("項目名："));
        inputPanel.add(columnItemField);
        inputPanel.add(new JLabel("タイプ："));
        inputPanel.add(columnTypeField);
        inputPanel.add(new JLabel("TABLE："));
        inputPanel.add(tableField);
        frame.add(inputPanel, BorderLayout.NORTH);

        JButton startButton = getjButton();
        frame.add(startButton, BorderLayout.SOUTH);

        // 让窗口在屏幕中央显示
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);

        // 添加窗口关闭监听器
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(frame,
                        "你要关闭整个程序吗", "Confirmation",
                        JOptionPane.YES_NO_OPTION);

                // 完全关闭程序
                if (option == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private static JButton getjButton() {
        JButton startButton = new JButton("选择文件");

        //创建读取按钮的监听事件
        startButton.addActionListener(e -> {
            String sheetName = sheetField.getText();
            String columnItem = columnItemField.getText();
            String columnType = columnTypeField.getText();

            //输入校验
            if (sheetName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入Sheet名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (columnItem.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入項目名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (columnType.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入タイプ", "警告", JOptionPane.WARNING_MESSAGE);
            } else {
                JFileChooser fileChooser = new JFileChooser(lastSelectedFilePath);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    // 记住当前选择的路径
                    lastSelectedFilePath = selectedFile.getParent();
                    //开始读取
                    readExcelAndShowOutput(selectedFile.getAbsolutePath());
                }
            }
        });
        return startButton;
    }

    /**
     * 读取输出方法
     */
    private static void readExcelAndShowOutput(String excelFilePath) {
//        String columnItem = columnItemField.getText();
//        String columnType = columnTypeField.getText();
//
//        //获取输入的sheet名称
//        String sheetName = sheetField.getText();
//
//        StringBuilder outputText = new StringBuilder();
//        outputText.append("----------- 输出开始 -----------\n\n");
//
//        // JDBC连接信息
//        String jdbcUrl = "jdbc:sqlserver://ccflowtest.database.windows.net:1433;database=ccflow8_test";
//        String username = "ccflowtest";
//        String password = "shinseiDX2022";
//
//        try {
//            FileInputStream fileInputStream = new FileInputStream(excelFilePath);
//            Workbook workbook = WorkbookFactory.create(fileInputStream);
//            Sheet sheet = workbook.getSheet(sheetName);
//
//            if (sheet != null) {
//                int columnIndexItem = getColumnIndex(columnItem);
//                int columnIndexType = getColumnIndex(columnType);
//
//                // 空白行
//                boolean previousCommentPrinted = false;
//
//                // 加载SQL Server的JDBC驱动程序
//                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//                System.out.println("开始连接数据库");
//                // 建立连接
//                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
//                System.out.println("数据库连接成功");
//                Statement statement = connection.createStatement();
//                ResultSet resultSet = null;
//
//                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
//                    Row row = sheet.getRow(i);
//                    if (row != null) {
//                        Cell cellItem = row.getCell(columnIndexItem);
//                        Cell cellType = row.getCell(columnIndexType);
//
//                        if (cellItem != null && cellType != null && StringUtils.isNotBlank(cellItem.toString()) && StringUtils.isNotBlank(cellType.toString())) {
//
//                            String valueItem = cellItem.toString();
//                            String valueType = cellType.toString();
//                            String table = tableField.getText();
//
//                            // 查询与项目名相同注释的字段名 (oracle数据库查询列伦理名)
////                            String query = "SELECT COLUMN_NAME " +
////                                    "FROM USER_COL_COMMENTS " +
////                                    "WHERE TABLE_NAME = '" + table + "' " +
////                                    "AND COMMENTS LIKE '" + valueItem + "%'";
//
//
//                            String sqlQuery = "SELECT\n" +
//                                    "  a1.name as columnName,\n" +
//                                    "  b.value as columnComment\n" +
//                                    "FROM\n" +
//                                    "  sysobjects a\n" +
//                                    "  LEFT JOIN sys.columns a1 ON a.id = a1.object_id\n" +
//                                    "  LEFT JOIN sys.extended_properties b ON b.major_id = a.id AND b.minor_id = a1.column_id\n" +
//                                    "WHERE\n" +
//                                    "  a.name = '" + table + "'";
//
//                            resultSet = statement.executeQuery(sqlQuery);
//
//                            String dbItemName = "";
//
//                            while (resultSet.next()) {
//                                dbItemName = resultSet.getString("columnComment");
//                            }
//
//                            if ((valueType.equals("TXTS") || valueType.equals("LIST"))) {
//                                if (previousCommentPrinted) {
//                                    outputText.append("\n");
//                                }
//
//                                // 输出内容前添加中文多行注释
//                                outputText.append("/**\n");
//                                outputText.append(" * ").append(valueItem).append("\n");
//                                outputText.append(" */\n");
//                                outputText.append("private String ").append(dbItemName).append(";\n");
//
//                                previousCommentPrinted = true;
//                            }
//                        }
//                    }
//                }
//                outputText.append("\n----------- 输出结束 -----------\n");
//
//                outputText.append("\n\n");
//
//                assert resultSet != null;
//
//                resultSet.close();
//                statement.close();
//                connection.close();
//
////                // 更新 outputText，将数据库查询结果添加到末尾
////                outputText.append("\n----------- 数据库查询结果 -----------\n");
////                outputText.append(dbOutput);
////                outputText.append("\n----------- 数据库查询结束 -----------\n");
//            } else {
//                JOptionPane.showMessageDialog(frame, "输入的Sheet不存在", "警告", JOptionPane.WARNING_MESSAGE);
//                workbook.close();
//                return; //
//            }
//            workbook.close();
//            fileInputStream.close();
//        } catch (IOException e) {
//            String err = e.toString();
//            System.out.println(err);
//        } catch (SQLException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        outputTextArea.setText(outputText.toString());
//        outputTextArea.setCaretPosition(0);
    }

    /**
     * 创建图标
     */
    private static void createIcon() {
        //设置左上角图标
        String imagePath = "book-day_4219151.png";
        Image imageIcon = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getClassLoader().getResource(imagePath));
        frame.setIconImage(imageIcon);
    }

    /**
     * .
     * 根据列名获取列索引
     */
    private static int getColumnIndex(String columnName) {
        char ch = columnName.charAt(0);
        if (ch < 'A' || ch > 'Z') {
            JOptionPane.showMessageDialog(frame, "输入的【" + columnName + "】是无效的列名", "警告", JOptionPane.WARNING_MESSAGE);
            throw new IllegalArgumentException("无效的列名：" + columnName);
        }
        return ch - 'A';
    }


//    private static String executeDbQuery() throws SQLException, ClassNotFoundException {
//        StringBuilder dbOutput = new StringBuilder();
//        DbConnectionService.ccFlowDdConnection(dbOutput);
//        return dbOutput.toString();
//    }

}
