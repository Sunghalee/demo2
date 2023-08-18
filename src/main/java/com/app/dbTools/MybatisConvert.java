package com.app.dbTools;

import com.MainFrame;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.service.DbConnectionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.sql.*;

/**
 * db相关文件生成
 *
 * @author Sunghalee
 */
public class MybatisConvert extends JFrame {
    private static JTextArea outputTextArea;
    private static JTextField tableNameField;
    private static JTextField tableItemField;
    private static JTextField tableItemCommentsField;
    private static JTextField sheetField;
    private static JTextField programNameField;
    private static JFrame frame;

    // 默认的文件选择路径
    private static String lastSelectedFilePath = "D:\\";

    public static void action() {
        frame = new JFrame("mapperConvert author Sunghalee");

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

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));

        //实例化4个输入窗口
        sheetField = new JTextField();
        tableNameField = new JTextField();
        tableItemField = new JTextField();
        tableItemCommentsField = new JTextField();
        programNameField = new JTextField();

        inputPanel.add(new JLabel("SHEET名："));
        inputPanel.add(sheetField);
        inputPanel.add(new JLabel("テーブル名："));
        inputPanel.add(tableNameField);
        inputPanel.add(new JLabel("テーブル項目ID："));
        inputPanel.add(tableItemField);
        inputPanel.add(new JLabel("テーブル項目名："));
        inputPanel.add(tableItemCommentsField);
        inputPanel.add(new JLabel("程序名："));
        inputPanel.add(programNameField);

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
            String tableName = tableNameField.getText();
            String tableItem = tableItemField.getText();
            String tableItemComments = tableItemCommentsField.getText();
            String sheetName = sheetField.getText();
            String programName = programNameField.getText();

            //输入校验
            if (sheetName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入Sheet名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (tableName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "テーブル名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (tableItem.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "テーブル項目ID", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (tableItemComments.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "テーブル項目名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (programName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "程序名", "警告", JOptionPane.WARNING_MESSAGE);
            } else {
                JFileChooser fileChooser = new JFileChooser(lastSelectedFilePath);
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    // 记住当前选择的路径
                    lastSelectedFilePath = selectedFile.getParent();
                    //开始读取
                    readExcelAndShowOutput(selectedFile.getAbsolutePath());

                    String[] fileNames = {programName + "Mapper.xml", programName + "Mapper.java", programName + "Dto.java"};
                    String[] generatedContents = {outputTextArea.getText(), outputTextArea.getText(), outputTextArea.getText()};
                    generateAndSaveFiles(fileNames, generatedContents, programName);
                }
            }
        });
        return startButton;
    }

    /**
     * 生成文件
     */
    private static void generateAndSaveFiles(String[] fileNames, String[] generatedContents, String folderPath) {
        JFileChooser folderChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        folderChooser.setDialogTitle("选择保存文件夹");
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int result = folderChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            String selectedFolderPath = selectedFolder.getAbsolutePath() + File.separator + folderPath;

            File folder = new File(selectedFolderPath);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    JOptionPane.showMessageDialog(frame, "无法创建文件夹：" + folderPath, "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            for (int i = 0; i < fileNames.length; i++) {
                String filePath = selectedFolderPath + File.separator + fileNames[i];
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(generatedContents[i]);
                    writer.flush();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "保存文件失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(frame, "文件已保存至文件夹：" + selectedFolderPath, "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 读取输出方法
     */
    private static void readExcelAndShowOutput(String excelFilePath) {
        String tableName = tableNameField.getText();
        String tableItem = tableItemField.getText();
        String tableItemComments = tableItemCommentsField.getText();

        //获取输入的sheet名称
        String sheetName = sheetField.getText();

        String programName = programNameField.getText();

        StringBuilder outputText = new StringBuilder();

        try {
            FileInputStream fileInputStream = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet != null) {
                int tableNameIndex = getColumnIndex(tableName);
                int tableItemIndex = getColumnIndex(tableItem);
                int tableItemCommentsIndex = getColumnIndex(tableItemComments);

                outputText.append("----------- 输出开始 -----------\n\n");


                // 输出内容前添加中文多行注释
                outputText.append("<?xml version=\"1.0\" encoding=\"Windows-31J\"?>\n");
                outputText.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
                outputText.append("<mapper namespace=\"nis.spro.seisan.common.dao.mapper.\" + programName + \"Mapper\">\n");
                outputText.append("<!--検索-->\n");
                outputText.append("<resultMap id=\"" + programName + "SearchMap\" type=\"nis.spro.seisan.common.dto." + programName + "SearchDto\">\n");

                Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@10.4.2.179:1521/orcl", "ORATDIKO01", "password");
                Statement statement = connection.createStatement();
                ResultSet resultSet = null;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell tableNameCon = row.getCell(tableNameIndex);
                        Cell tableItemCon = row.getCell(tableItemIndex);
                        Cell tableItemCommentsCon = row.getCell(tableItemCommentsIndex);

                        if (tableNameCon != null && tableItemCon != null && tableItemCommentsCon != null &&
                                StringUtils.isNotBlank(tableNameCon.toString()) &&
                                StringUtils.isNotBlank(tableItemCon.toString()) &&
                                StringUtils.isNotBlank(tableItemCommentsCon.toString())) {

                            String conTable = tableNameCon.toString();
                            String conTableItem = tableItemCon.toString();
                            String conTableItemComments = tableItemCommentsCon.toString();

                            //虽然不知道为啥 这么写在程序中报错 但是拼出来的sql在db是好用的
//                            String sql = "SELECT COLUMN_NAME, DATA_TYPE " +
//                                    "FROM ALL_TAB_COLUMNS " +
//                                    "WHERE TABLE_NAME = '" + tableNameCon + "' " +
//                                    "AND COLUMN_NAME = '" + tableItemCon + "';";
                            String sql = "SELECT COLUMN_NAME, DATA_TYPE " +
                                    "FROM ALL_TAB_COLUMNS " +
                                    "WHERE TABLE_NAME = ? " +
                                    "AND COLUMN_NAME = ?";

                            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                                // 设置第一个参数的值为 conTable
                                preparedStatement.setString(1, conTable);
                                // 设置第二个参数的值为 conTableItem
                                preparedStatement.setString(2, conTableItem);

                                resultSet = preparedStatement.executeQuery();

                                // 处理查询结果
                                while (resultSet.next()) {
                                    String columnName = resultSet.getString("COLUMN_NAME");
                                    String dataType = resultSet.getString("DATA_TYPE");
                                    outputText.append("<result column=\"").append(conTableItem)
                                            .append("\" jdbcType=\"")
                                            .append(dataType)
                                            .append("\" property=\"")
                                            .append(extractAndFormatFieldName(conTableItem))
                                            .append("\" /><!--")
                                            .append(conTableItemComments)
                                            .append("-->\n");
                                }
                            } catch (SQLException e) {
                                // 处理异常
                                JOptionPane.showMessageDialog(frame, e, "警告", JOptionPane.WARNING_MESSAGE);
                            }

//                            resultSet = statement.executeQuery(sql);
//
//                            String outType = "";
//                            while (resultSet.next()) {
//                                outType = resultSet.getString("DATA_TYPE");
//                            }
//                            outputText.append("<result column=\""+ conTableItem +"\" jdbcType=\""+ outType +"\" property=\""+ extractAndFormatFieldName(conTableItem)+"\" /><!--"+ conTableItemComments +"-->");
                        }
                    }
                }
                outputText.append("</resultMap>\n");
                outputText.append("</mapper>\n");
                outputText.append("\n----------- 输出结束 -----------\n");

                outputText.append("\n\n");

                assert resultSet != null;
                resultSet.close();
                statement.close();
                connection.close();

//                // 读取完 Excel 内容后，执行数据库查询
//                String dbOutput = executeDbQuery();
//
//                // 更新 outputText，将数据库查询结果添加到末尾
//                outputText.append("\n----------- 数据库查询结果 -----------\n");
//                outputText.append(dbOutput);
//                outputText.append("\n----------- 数据库查询结束 -----------\n");
            } else {
                JOptionPane.showMessageDialog(frame, "输入的Sheet不存在", "警告", JOptionPane.WARNING_MESSAGE);
                workbook.close();
                return; //
            }
            workbook.close();
            fileInputStream.close();
        } catch (IOException e) {
            String err = e.toString();
            System.out.println(err);
            JOptionPane.showMessageDialog(frame, e, "警告", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, e, "警告", JOptionPane.WARNING_MESSAGE);
            throw new RuntimeException(e);
        }
        outputTextArea.setText(outputText.toString());
        outputTextArea.setCaretPosition(0);
    }


    /**
     * .
     * 获取字段名，转换驼峰
     */
    private static String extractAndFormatFieldName(String fullName) {

        if (fullName == null || fullName.isEmpty()) {
            return "";
        }

        // 从 "[表名.字段名]" 格式中提取字段名
        String fieldName = fullName.substring(fullName.lastIndexOf('.') + 1);

        // 去掉下划线并将首字母大写，转换成驼峰格式
        fieldName = fieldName.toLowerCase().replace("_", " ");
        fieldName = WordUtils.capitalizeFully(fieldName);
        fieldName = fieldName.replaceAll(" ", "");

        if (StringUtils.isBlank(fieldName)) {
            return "当前仕样书对应的字段为空";
        }
        // 将首字母变成小写
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);

        return fieldName;
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


    private static String executeDbQuery() throws SQLException, ClassNotFoundException {
        StringBuilder dbOutput = new StringBuilder();
        DbConnectionService.nsysDdConnection(dbOutput);
        return dbOutput.toString();
    }

}
