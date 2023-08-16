package com.app.srNsys;

import com.MainFrame;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.service.DbConnectionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

/**
 * 读取仕样书做成model属性注释
 *
 * @author Sunghalee
 */
public class SrColumnModelReader extends JFrame {
    private static JTextArea outputTextArea;
    private static JTextField columnItemField;
    private static JTextField columnTypeField;
    private static JTextField columnModelNameField;
    private static JTextField sheetField;
    private static JFrame frame;

    // 默认的文件选择路径
    private static String lastSelectedFilePath = "D:\\";

    public static void action() {
        frame = new JFrame("SR-NSYS author Sunghalee");

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
        columnModelNameField = new JTextField();

        inputPanel.add(new JLabel("SHEET名："));
        inputPanel.add(sheetField);
        inputPanel.add(new JLabel("項目名："));
        inputPanel.add(columnItemField);
        inputPanel.add(new JLabel("タイプ："));
        inputPanel.add(columnTypeField);
        inputPanel.add(new JLabel("属性名："));
        inputPanel.add(columnModelNameField);
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
            String columnItem = columnItemField.getText();
            String columnType = columnTypeField.getText();
            String columnModelName = columnModelNameField.getText();
            String sheetName = sheetField.getText();

            //输入校验
            if (sheetName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入Sheet名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (columnItem.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入項目名", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (columnType.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入タイプ", "警告", JOptionPane.WARNING_MESSAGE);
            } else if (columnModelName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "请输入属性名", "警告", JOptionPane.WARNING_MESSAGE);
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
        String columnItem = columnItemField.getText();
        String columnType = columnTypeField.getText();
        String columnModelName = columnModelNameField.getText();

        //获取输入的sheet名称
        String sheetName = sheetField.getText();

        StringBuilder outputText = new StringBuilder();

        try {
            FileInputStream fileInputStream = new FileInputStream(excelFilePath);
            Workbook workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet != null) {
                int columnIndexItem = getColumnIndex(columnItem);
                int columnIndexType = getColumnIndex(columnType);
                int columnIndexModelName = getColumnIndex(columnModelName);

                // 空白行
                boolean previousCommentPrinted = false;

                outputText.append("----------- 输出开始 -----------\n\n");
                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row != null) {
                        Cell cellItem = row.getCell(columnIndexItem);
                        Cell cellType = row.getCell(columnIndexType);
                        Cell cellModelName = row.getCell(columnIndexModelName);

                        if (cellItem != null && cellType != null && cellModelName != null &&
                                StringUtils.isNotBlank(cellItem.toString()) &&
                                StringUtils.isNotBlank(cellType.toString()) &&
                                StringUtils.isNotBlank(cellModelName.toString())) {

                            String valueItem = cellItem.toString();
                            String valueType = cellType.toString();
                            String valueModelName = cellModelName.toString();

                            if ((valueType.equals("TXTS") || valueType.equals("LIST"))) {

                                if (previousCommentPrinted) {
                                    outputText.append("\n");
                                }

                                // 输出内容前添加中文多行注释
                                outputText.append("/**\n");
                                outputText.append(" * ").append(valueItem).append("\n");
                                outputText.append(" */\n");
                                outputText.append("private String ").append(extractAndFormatFieldName(valueModelName)).append(";\n");

                                previousCommentPrinted = true;
                            }
                        }
                    }
                }
                outputText.append("\n----------- 输出结束 -----------\n");

                outputText.append("\n\n");

                // 读取完 Excel 内容后，执行数据库查询
                String dbOutput = executeDbQuery();

                // 更新 outputText，将数据库查询结果添加到末尾
                outputText.append("\n----------- 数据库查询结果 -----------\n");
                outputText.append(dbOutput);
                outputText.append("\n----------- 数据库查询结束 -----------\n");
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
            JOptionPane.showMessageDialog(frame, "不支持的文件类型", "警告", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException | ClassNotFoundException e) {
            if(e instanceof SQLException){
                JOptionPane.showMessageDialog(frame, "SQL执行异常", "警告", JOptionPane.WARNING_MESSAGE);
            }else {
                JOptionPane.showMessageDialog(frame, "JDBC连接错误", "警告", JOptionPane.WARNING_MESSAGE);
            }
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
