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
import java.awt.event.*;
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
    private static StringBuilder xmlBuilder;
    private static StringBuilder mapperBuilder;
    private static StringBuilder searchDtoBuilder;

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
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);

        //设定滚动条位置 便于输出结束时查看结果
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5, 1));

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

        // 创建一个面板作为可拖拽区域
        FileDropPanel dropPanel = new FileDropPanel();
        frame.add(dropPanel, BorderLayout.CENTER);

        // 添加文件拖放事件监听器
        dropPanel.addFileDropListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 处理文件拖放读取
                System.out.println("1!");

                // 获取文件路径
                String filePath = dropPanel.getDroppedFilePath();
                fileDrop(filePath);
            }
        });

        // 让窗口在屏幕中央显示
        frame.setLocationRelativeTo(null);

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

        // 在窗口上绑定回车键
        frame.getRootPane().setDefaultButton(startButton);

        // 回车键事件
        Action openFileChooserAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileChooser();
            }
        };

        // 为整个窗口添加回车键绑定
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openFileChooserAction"
        );
        frame.getRootPane().getActionMap().put("openFileChooserAction", openFileChooserAction);

        // 添加ActionListener到具体输入框，实现点击回车也能触发选择问文件的事件
//        tableNameField.addActionListener(e -> openFileChooser());
//        tableItemField.addActionListener(e -> openFileChooser());
//        tableItemCommentsField.addActionListener(e -> openFileChooser());
//        sheetField.addActionListener(e -> openFileChooser());
//        programNameField.addActionListener(e -> openFileChooser());

        frame.setVisible(true);
    }

    /**
     * 选择文件按钮
     */
    private static JButton getjButton() {
        JButton startButton = new JButton("选择文件");

        //创建读取按钮的监听事件
        startButton.addActionListener(e -> {
            openFileChooser();
        });
        return startButton;
    }

    /**
     * 打开文件选择对话框并读取
     */
    private static void openFileChooser() {
        // 获取输入的内容
        String tableName = tableNameField.getText();
        String tableItem = tableItemField.getText();
        String tableItemComments = tableItemCommentsField.getText();
        String sheetName = sheetField.getText();
        String programName = programNameField.getText();

        // 输入校验
        if (sheetName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入Sheet名", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (tableName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入テーブル名", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (tableItem.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入テーブル項目ID", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (tableItemComments.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入テーブル項目名", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (programName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入程序名", "警告", JOptionPane.WARNING_MESSAGE);
        } else {
            JFileChooser fileChooser = new JFileChooser(lastSelectedFilePath);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // 记住当前选择的路径
                lastSelectedFilePath = selectedFile.getParent();
                // 开始读取
                readExcelAndShowOutput(selectedFile.getAbsolutePath());

                String programId = convertToUpperCase(programName);
                String[] fileNames = {programId + "Mapper.xml", programId + "Mapper.java", programId + "Dto.java"};
                String[] generatedContents = {xmlBuilder.toString(), mapperBuilder.toString(), outputTextArea.getText()};
                generateAndSaveFiles(fileNames, generatedContents, programId);
            }
        }
    }


    /**
     * 拖拽打开文件并读取
     */
    private static void fileDrop(String filePath) {
        // 获取输入的内容
        String tableName = tableNameField.getText();
        String tableItem = tableItemField.getText();
        String tableItemComments = tableItemCommentsField.getText();
        String sheetName = sheetField.getText();
        String programName = programNameField.getText();

        // 输入校验
        if (sheetName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入Sheet名", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (tableName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入テーブル名", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (tableItem.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入テーブル項目ID", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (tableItemComments.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入テーブル項目名", "警告", JOptionPane.WARNING_MESSAGE);
        } else if (programName.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "请输入程序名", "警告", JOptionPane.WARNING_MESSAGE);
        } else {
            // 开始读取
            readExcelAndShowOutput(filePath);
            String programId = convertToUpperCase(programName);
            String[] fileNames = {programId + "Mapper.xml", programId + "Mapper.java", programId + "Dto.java"};
            String[] generatedContents = {xmlBuilder.toString(), mapperBuilder.toString(), outputTextArea.getText()};
            generateAndSaveFiles(fileNames, generatedContents, programId);
        }
    }

    /**
     * 保存文件
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

            if (folder.exists() && folder.isDirectory()) {
                // 如果文件夹已存在，询问用户是否覆盖
                int choice = JOptionPane.showConfirmDialog(frame,
                        "目标文件夹已存在，是否要覆盖？",
                        "确认覆盖",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    // 用户同意覆盖，删除现有文件夹及其内容
                    deleteFolder(folder);
                } else {
                    // 用户选择不覆盖，要求用户输入新的文件夹名称
                    String newFolderPath = askForNewFolderName(selectedFolderPath);
                    if (newFolderPath == null) {
                        return; // 用户取消了操作
                    }
                    folder = new File(newFolderPath);
                }
            }

            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    JOptionPane.showMessageDialog(frame, "无法创建文件夹：" + folderPath, "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            for (int i = 0; i < fileNames.length; i++) {
                String filePath = folder.getAbsolutePath() + File.separator + fileNames[i];
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    writer.write(generatedContents[i]);
                    writer.flush();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "保存文件失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }

            JOptionPane.showMessageDialog(frame, "文件已保存至文件夹：" + folder.getAbsolutePath(), "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 删除文件夹及其内容
     */
    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * 询问用户输入新的文件夹名称
     */
    private static String askForNewFolderName(String defaultName) {
        String newFolderName = JOptionPane.showInputDialog(frame,
                "输入新的文件夹名称：",
                "重命名文件夹",
                JOptionPane.PLAIN_MESSAGE);

        if (newFolderName != null && !newFolderName.isEmpty()) {
            return defaultName.substring(0, defaultName.lastIndexOf(File.separator)) + File.separator + newFolderName;
        }

        return null; // 用户取消了输入
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

                // 输出内容前添加中文多行注释
                outputText.append("<?xml version=\"1.0\" encoding=\"Windows-31J\"?>\n");
                outputText.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");
                outputText.append("<mapper namespace=\"nis.spro.seisan.common.dao.mapper.").append(convertToUpperCase(programName)).append("Mapper\">\n");
                outputText.append("<!--検索-->\n");
                outputText.append("<resultMap id=\"").append(convertToLowerCase(programName))
                        .append("SearchMap\" type=\"nis.spro.seisan.common.dto.")
                        .append(convertToUpperCase(programName)).append("SearchDto\">\n");

                //创建DB连接
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

                outputText.append("\n\n");

                //.xml对象
                xmlBuilder = new StringBuilder();
                xmlBuilder.append(outputText);

                //mapper对象
                mapperBuilder = new StringBuilder();
                getMapperInfo(programName);

                //.xml对象
                searchDtoBuilder = new StringBuilder();
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


    /**
     * .
     * DB查询
     */
    private static String executeDbQuery() throws SQLException, ClassNotFoundException {
        StringBuilder dbOutput = new StringBuilder();
        DbConnectionService.nsysDdConnection(dbOutput);
        return dbOutput.toString();
    }

    /**
     * .
     * 首字母大写转换
     */
    private static String convertToUpperCase(String inputString) {
        // 检查输入字符串是否为空
        if (inputString == null || inputString.isEmpty()) {
            return inputString;
        }

        // 将字符串的第一个字符大写，其余字符保持小写
        String firstLetter = inputString.substring(0, 1).toUpperCase();
        String restOfString = inputString.substring(1).toLowerCase();

        return firstLetter + restOfString;
    }

    /**
     * .
     * 全部小写转换
     */
    public static String convertToLowerCase(String inputString) {
        // 检查输入字符串是否为空
        if (inputString == null) {
            return null;
        }

        return inputString.toLowerCase();
    }

    /**
     * .
     * mapper Java对象转换
     */
    private static void getMapperInfo(String programName) {
        StringBuilder mapper = new StringBuilder();
        mapper.append("package nis.spro.seisan.common.dao.mapper;");
        mapper.append("\n");
        mapper.append(System.lineSeparator());
        mapper.append("import java.util.List;");
        mapper.append(System.lineSeparator());
        mapper.append("import java.util.Map;");
        mapper.append("\n");
        mapper.append(System.lineSeparator());
        mapper.append("import org.apache.ibatis.annotations.Param;");
        mapper.append("\n");
        mapper.append(System.lineSeparator());
        mapper.append("import nis.spro.seisan.common.dto.").append(convertToUpperCase(programName)).append("SearchDto;");
        mapper.append("\n");
        mapper.append(System.lineSeparator());
        mapper.append("public interface ").append(convertToUpperCase(programName)).append("Mapper {");
        mapper.append("\n");
        mapper.append(System.lineSeparator());
        mapper.append("   /**");
        mapper.append(System.lineSeparator());
        mapper.append("    * 検索");
        mapper.append(System.lineSeparator());
        mapper.append("    *");
        mapper.append(System.lineSeparator());
        mapper.append("    */");
        mapper.append(System.lineSeparator());
        mapper.append("   List<").append(convertToUpperCase(programName))
                .append("SearchDto> select").append(convertToUpperCase(programName)).append("List(@Param(\"params\") Map<String, Object> map);");
        mapper.append("\n");
        mapper.append(System.lineSeparator());
        mapper.append("}");

        mapperBuilder = mapper;
    }

}
