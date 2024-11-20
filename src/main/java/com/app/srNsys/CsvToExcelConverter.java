package com.app.srNsys;

import com.MainFrame;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvToExcelConverter extends JFrame {

    private static JTextArea outputTextArea;
    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CsvToExcelConverter::createAndShowGUI);
    }

    public static void createAndShowGUI() {
        frame = new JFrame("金骋软件出勤转换工具");

        //设定图标样式
/*        createIcon();*/

        try {
            JFrame.setDefaultLookAndFeelDecorated(true);
            UIManager.setLookAndFeel(new AcrylLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        outputTextArea = new JTextArea();
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton openButton = new JButton("选择 CSV 文件");
        openButton.addActionListener(new OpenFileAction());

        JButton exportButton = new JButton("导出为 Excel");
        exportButton.addActionListener(new ExportToExcelAction());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(exportButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // 设置拖放功能
        outputTextArea.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent event) {
                event.acceptDrop(event.getDropAction());
                try {
                    List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        File csvFile = files.get(0);
                        readCsvAndDisplay(csvFile);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "读取拖入的 CSV 文件失败", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static class OpenFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                readCsvAndDisplay(selectedFile);
            }
        }
    }

    private static void readCsvAndDisplay(File csvFile) {
        Charset charset = Charset.forName("GB2312");  // 设置字符集为 GB2312
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), charset))) {
            String line;
            List<String[]> csvData = new ArrayList<>();
            outputTextArea.setText("");
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                csvData.add(values);
                outputTextArea.append(String.join(" | ", values) + "\n");
            }
            CsvDataHolder.setData(csvData);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "读取 CSV 文件失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ExportToExcelAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String[]> csvData = new ArrayList<>();
            try {
                csvData = CsvDataHolder.getData();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择 Excel 保存路径");
            int result = fileChooser.showSaveDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File excelFile = fileChooser.getSelectedFile();
//                exportToExcel(excelFile,csvData);
                exportToExcelAndPdf(excelFile,csvData);
            }
        }
    }

    private static void exportToExcelAndPdf(File excelFile, List<String[]> csvData) {
        try (InputStream templateStream = CsvToExcelConverter.class.getResourceAsStream("/出勤模板.xlsx");
             Workbook workbook = new XSSFWorkbook(templateStream)) {

            List<GoldenSoftWork> records = convertCsvDataToList(csvData);

            // 按员工姓名分组记录
//            Map<String, List<GoldenSoftWork>> groupedRecords = records.stream()
//                    .collect(Collectors.groupingBy(GoldenSoftWork::getEmployeeName));
            Map<String, List<GoldenSoftWork>> groupedRecords = records.stream()
                    .collect(Collectors.groupingBy(GoldenSoftWork::getEmployeeName));

            // 获取模板工作表和用于格式化的模板行
            Sheet templateSheet = workbook.getSheetAt(0);
            Row templateRow = templateSheet.getRow(10); // 用于格式化的模板行

            for (Map.Entry<String, List<GoldenSoftWork>> entry : groupedRecords.entrySet()) {
                String employeeName = entry.getKey();
                List<GoldenSoftWork> employeeRecords = entry.getValue();

                // 为每个员工克隆模板工作表
                Sheet newSheet = workbook.cloneSheet(0);
                workbook.setSheetName(workbook.getSheetIndex(newSheet), employeeName);

                Row titleRow = newSheet.getRow(4);
                LocalDate latestDate = employeeRecords.stream()
                        .map(GoldenSoftWork::getAttendanceDate)
                        .max(LocalDate::compareTo)
                        .orElse(null); // 如果列表为空，返回 null
                String formattedMonth = "";
                if (latestDate != null) {
                    // 格式化月份字符串，如果前导位是0则去掉
                    String month = latestDate.format(DateTimeFormatter.ofPattern("MM"));
                    formattedMonth = month.startsWith("0") ? month.substring(1) : month;
                }
                newSheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 4));
                titleRow.getCell(0).setCellValue(formattedMonth+"月考勤记录");

                // 填充员工姓名
                Row nameRow = newSheet.getRow(7);
                nameRow.getCell(2).setCellValue(employeeName); // 员工姓名单元格

                int rowIndex = 10; // 从第11行开始
//                int totalWorkHours = 0;
                double totalWorkHours = 0.0;

                for (GoldenSoftWork record : employeeRecords) {
                    // 为每条记录创建新行
                    Row newRow = newSheet.createRow(rowIndex);

                    // 为新行中的每个单元格克隆模板行的样式
                    for (int col = 0; col <= 4; col++) {
                        Cell templateCell = templateRow.getCell(col);
                        Cell newCell = newRow.createCell(col);

                        if (templateCell != null) {
                            // 从模板单元格复制样式
                            CellStyle newCellStyle = workbook.createCellStyle();
                            newCellStyle.cloneStyleFrom(templateCell.getCellStyle());
                            newCell.setCellStyle(newCellStyle);
                        }
                    }

                    // 填充数据单元格
                    newRow.getCell(0).setCellValue(record.getAttendanceDate().toString()); // 日期
                    newRow.getCell(1).setCellValue(record.getAttendanceDate()); // 天
                    newRow.getCell(2).setCellValue(record.getCheckInTime().isEmpty() ? "" : record.getCheckInTime()); // 签到时间
                    newRow.getCell(3).setCellValue(record.getCheckOutTime().isEmpty() ? "" : record.getCheckOutTime() ); // 签退时间
                    newRow.getCell(4).setCellValue((!record.getAttendanceDuration().isEmpty() && record.getAttendanceDuration().equals("0.0"))
                            ? "" :record.getAttendanceDuration()); // 正常出勤时长

                    // 累加总工作小时数
                    if (record.getAttendanceDuration() != null && !record.getAttendanceDuration().isEmpty()) {
//                        totalWorkHours += Integer.parseInt(record.getAttendanceDuration().replace("H", "").trim());
                        totalWorkHours += Double.parseDouble(record.getAttendanceDuration().replace("H", "").trim());
                    }

                    rowIndex++;
                }

                // 创建并格式化合计行
                Row totalRow = newSheet.createRow(rowIndex);

                // 合并合计标签单元格 (D, E, F 列) 并居中显示
                newSheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 3));
                Cell totalLabelCell = totalRow.createCell(0);
                totalLabelCell.setCellValue("合计");

                // 设置“合计”标签的样式并居中
                CellStyle totalLabelStyle = workbook.createCellStyle();
                totalLabelStyle.cloneStyleFrom(templateRow.getCell(0).getCellStyle());
                totalLabelStyle.setAlignment(HorizontalAlignment.CENTER); // 居中对齐
                totalLabelCell.setCellStyle(totalLabelStyle);

                // 设置字体加粗
                Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                totalLabelStyle.setFont(boldFont);
                totalLabelCell.setCellStyle(totalLabelStyle);

                // 为合并单元格区域的 E 和 F 列设置样式
                for (int col = 1; col <= 4; col++) {
                    Cell cell = totalRow.createCell(col);
                    cell.setCellStyle(totalLabelStyle); // 应用居中和样式
                }

                // 填写总工作小时数
                Cell totalValueCell = totalRow.createCell(4);
//                totalValueCell.setCellValue(totalWorkHours+"H");
                totalValueCell.setCellValue(totalWorkHours);

                // 设置合计值的样式并加粗
                CellStyle totalValueStyle = workbook.createCellStyle();
                totalValueStyle.cloneStyleFrom(templateRow.getCell(4).getCellStyle());
                totalValueStyle.setFont(boldFont); // 使用加粗字体
                totalValueCell.setCellStyle(totalValueStyle);
            }

            // 删除原始模板工作表
            workbook.removeSheetAt(0);

            // 保存为 Excel 文件
            try (FileOutputStream fos = new FileOutputStream(excelFile + ".xlsx")) {
                workbook.write(fos);
                JOptionPane.showMessageDialog(frame, "Excel 导出成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            }

//            // 导出为 PDF 文件 TODO
//            String pdfFilePath = excelFile + ".pdf"; // 设置 PDF 文件路径
//            exportToPdf(excelFile + ".xlsx", pdfFilePath);
//
//            com.spire.xls.Workbook workbook1 = new com.spire.xls.Workbook();
//            workbook1.loadFromFile(excelFile+ ".xlsx");
//
//            //设置转换后的PDF页面高宽适应工作表的内容大小
//            workbook1.getConverterSetting().setSheetFitToPage(true);
//
//            //将生成的文档保存到指定路径
//            workbook1.saveToFile("output/ExcelToPdf", FileFormat.PDF);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "导出失败", "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void exportToPdf(String excelFilePath, String pdfFilePath) {
        try {
            printExcelToPDF(excelFilePath, pdfFilePath);
            System.out.println("PDF 文件已成功生成：" + pdfFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printExcelToPDF(String excelFilePath, String pdfFilePath) throws IOException {
        // 使用完整路径来指定 Excel 可执行文件
        String excelExecutablePath = "\"C:\\Program Files\\Microsoft Office\\root\\Office16\\EXCEL.EXE\"";

        // 构建打印命令
        String command = excelExecutablePath + " /p \"" + excelFilePath + "\"";

        // 使用 ProcessBuilder 来执行命令
        ProcessBuilder builder = new ProcessBuilder("cmd", "/c", command);
        builder.environment().put("OutputFile", pdfFilePath);  // 不一定适用于 Microsoft Print to PDF，但可以尝试

        // 启动进程
        Process process = builder.start();

        // 等待进程完成
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public static void exportToPdf(String excelFilePath, String pdfFilePath) {
//        // 使用 JODConverter 的 LocalConverter 进行转换
//        LocalConverter.convert(new File(excelFilePath))
//                .to(new File(pdfFilePath))
//                .execute();
//        System.out.println("PDF 导出成功");
//    }
//    private static void exportToPdf(String excelFilePath, String pdfFilePath) {
//        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(excelFilePath));
//             PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFilePath));
//             PdfDocument pdfDocument = new PdfDocument(writer);
//             Document document = new Document(pdfDocument)) {
//
//            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
//                Sheet sheet = workbook.getSheetAt(i);
//                document.add(new Paragraph("Sheet: " + sheet.getSheetName()));
//                document.add(new Paragraph(" ")); // 添加空行
//
//                // 获取列数
//                int numberOfColumns = sheet.getRow(3).getPhysicalNumberOfCells();
//                Table table = new Table(numberOfColumns);
//
//                // 添加行并保持样式
//                for (int j = 0; j <= sheet.getLastRowNum(); j++) {
//                    Row row = sheet.getRow(j);
//                    if (row != null) {
//                        for (int k = 0; k < numberOfColumns; k++) {
//                            Cell cell = row.getCell(k);
//                            if (cell != null) {
//                                String cellValue = cell.toString();
//                                // 添加单元格到表格
//                                table.addCell(new Paragraph(cellValue));
//                            }
//                        }
//                    }
//                }
//                document.add(table);
//                document.add(new Paragraph(" ")); // 添加空行
//            }
//
//            document.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }


    //TODO 原始代码 不带模板导出
//    private static void exportToExcel(File excelFile,List<String[]> csvData) {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            List<GoldenSoftWork> records = convertCsvDataToList(csvData);
//
//            // 按员工姓名分组
//            Map<String, List<GoldenSoftWork>> groupedRecords = records.stream()
//                    .collect(Collectors.groupingBy(GoldenSoftWork::getEmployeeName));
//
//            // 遍历每个分组，创建独立的 sheet
//            for (Map.Entry<String, List<GoldenSoftWork>> entry : groupedRecords.entrySet()) {
//                String employeeName = entry.getKey();
//                List<GoldenSoftWork> employeeRecords = entry.getValue();
//
//                // 创建新 sheet
//                Sheet sheet = workbook.createSheet(employeeName);
//
//                // 设置模板标题
//                Row titleRow = sheet.createRow(1);
//                titleRow.createCell(1).setCellValue("公司名称");
//                titleRow.createCell(2).setCellValue("大连金骋软件有限公司");
//                titleRow.createCell(3).setCellValue("姓名");
//                titleRow.createCell(4).setCellValue(employeeName);
//                titleRow.createCell(5).setCellValue("所属部门");
//                titleRow.createCell(6).setCellValue("");
//
//                // 设置表头
//                Row headerRow = sheet.createRow(3);
//                headerRow.createCell(1).setCellValue("日期");
//                headerRow.createCell(2).setCellValue("出勤时间");
//                headerRow.createCell(3).setCellValue("退勤时间");
//                headerRow.createCell(4).setCellValue("作业时间");
//
//                sheet.setColumnWidth(0, 15 * 256); // 日期列
//                sheet.setColumnWidth(1, 20 * 256); // 出勤时间列
//                sheet.setColumnWidth(2, 20 * 256); // 退勤时间列
//                sheet.setColumnWidth(3, 15 * 256); // 作业时间列
//
//                CellStyle rightAlignStyle = workbook.createCellStyle();
//                rightAlignStyle.setAlignment(HorizontalAlignment.RIGHT);
//                sheet.setDefaultColumnStyle(4,rightAlignStyle);
//
//                // 创建红色字体样式
//                Font redFont = workbook.createFont();
//                redFont.setColor(IndexedColors.RED.getIndex());
//
//                int rowIndex = 4; // 从第5行开始填充数据
//                float totalWorkHours = 0.0f; // 用于记录总作业时间
//
//                // 填充数据
//                for (GoldenSoftWork record : employeeRecords) {
//                    Row row = sheet.createRow(rowIndex++);
//                    row.createCell(1).setCellValue(record.getAttendanceDate().toString());
//                    row.createCell(2).setCellValue(record.getCheckInTime() != null ? record.getCheckInTime().toString() : "未签到");
//                    row.createCell(3).setCellValue(record.getCheckOutTime() != null ? record.getCheckOutTime().toString() : "未签退");
//                    row.createCell(4).setCellValue(record.getAttendanceDuration());
//                    if(!record.getAttendanceDuration().isEmpty()){
//                        totalWorkHours += Float.parseFloat(record.getAttendanceDuration().replace("H", "").trim());
//                    }
//                }
//
//                // 添加合计行
//                Row totalRow = sheet.createRow(rowIndex+1);
//                totalRow.createCell(3).setCellValue("合计");
//                totalRow.createCell(4).setCellValue(String.format("%.1f", totalWorkHours)+"H"); // 设置总作业时间
//            }
//
//            // 写入文件
//            try (FileOutputStream fos = new FileOutputStream(excelFile + ".xlsx")) {
//                workbook.write(fos);
//                JOptionPane.showMessageDialog(frame, "Excel 导出成功", "成功", JOptionPane.INFORMATION_MESSAGE);
//            }
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(frame, "导出 Excel 失败", "错误", JOptionPane.ERROR_MESSAGE);
//        }
//
//    }

/*    private static void createIcon() {
        //设置左上角图标
        String imagePath = "goldenSoft.PNG";
        Image imageIcon = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getClassLoader().getResource(imagePath));
        frame.setIconImage(imageIcon);
    }*/

    private static List<GoldenSoftWork> convertCsvDataToList(List<String[]> csvData) {
        Integer countRow = 0;
        String[] message = new String[0];
        try{
            List<GoldenSoftWork> records = new ArrayList<>();

            // 跳过第一行（标题行）
            for (int i = 1; i < csvData.size(); i++) {
                countRow +=1;
                message = new String[0];
                message = csvData.get(i);
                String[] values = csvData.get(i);

                // 检查是否是有效行，必须包含必要字段
                if (values.length < 10 || values[0].trim().isEmpty() || values[1].trim().isEmpty()) {
                    continue; // 跳过不完整的行
                }

                GoldenSoftWork record = new GoldenSoftWork();
                record.setEmployeeId(values[0].trim().replace("'", ""));
                record.setEmployeeName(values[2].trim());
                record.setDepartment(values[3].trim());
                record.setAttendanceDate(LocalDate.parse(values[4].trim()));

                if(values[6].trim().equals("缺勤")){

                    String checkInTime = "";

                    String checkOutTime = "";

                    if(values.length == 11 ){
                        checkInTime = values[7].trim().equals("-") ? "" : values[7].trim();
                        checkOutTime = values[8].trim().equals("-") ? "" : values[8].trim();
                    } else {
                        checkInTime = values[11].replace("签到时间：", "").trim().isEmpty() ? "" : (values[11].substring(values[11].length() - 8));
                        checkOutTime = values[12].replace("签退时间：", "").trim().isEmpty() ? "" : (values[12].substring(values[12].length() - 8));
                    }

                    record.setCheckInTime(checkInTime);

                    record.setCheckOutTime(checkOutTime);

                    record.setLunchBreakTime(values[10].trim());
                    if(values[9]!=null){
                        String attendanceDuration = values[9].trim().replace(" 分钟", "").trim();
                        int minutes = Integer.parseInt(attendanceDuration);
                        double hours = (double) minutes / 60;
                        double roundedHours = Math.ceil(hours * 10) / 10.0;
                        record.setAttendanceDuration(String.format("%.1f", roundedHours));
                    }else{
                        record.setAttendanceDuration(null);
                    }
                }else{
                    record.setCheckInTime(values[7].trim());
                    record.setCheckOutTime((values[8].trim()));
                    record.setLunchBreakTime(values[10].trim());
//                    String attendanceDuration = values[9].trim().replace(" 分钟", "").trim();
//                    int minutes = Integer.parseInt(attendanceDuration);
//                    float hours = minutes / 60.0f; // 不进行四舍五入
//                    String formattedHours = String.format("%.1f", hours); // 保留一位小数
//                    record.setAttendanceDuration(formattedHours + "H");

//                    String attendanceDuration = values[9].trim().replace(" 分钟", "").trim();
//                    int minutes = Integer.parseInt(attendanceDuration);
//                    int hours = minutes / 60; // 直接取整数部分，不进行四舍五入
//                    record.setAttendanceDuration(hours + "H");

//                    String attendanceDuration = values[9].trim().replace(" 分钟", "").trim();
//                    int minutes = Integer.parseInt(attendanceDuration);
//                    double hours = (double) minutes / 60;
//                    double roundedHours = Math.ceil(hours * 10) / 10.0;
//                    record.setAttendanceDuration(String.format("%.1f", roundedHours));

                    String attendanceDuration = values[13].trim();
                    String minutesStr = attendanceDuration.replaceAll("[^0-9]", "").trim();
                    int minutes = Integer.parseInt(minutesStr);

                    minutes -= 60;
                    double hours = (double) minutes / 60;
                    double roundedHours = Math.ceil(hours * 10) / 10.0;
                    record.setAttendanceDuration(String.format("%.1f", roundedHours));

                }
                records.add(record);
            }
            return records;
        }catch (Exception e){
            System.out.println("错误信息 ：" + Arrays.toString(message) +"-----------"+"错误行号 ："+String.valueOf(countRow) +"-----------"+ e);
            throw new RuntimeException(e);
        }
    }
}

class CsvDataHolder {
    private static List<String[]> data;

    public static void setData(List<String[]> data) {
        CsvDataHolder.data = data;
    }

    public static List<String[]> getData() throws Exception{
        List<String[]> processedData = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        String[] lastCompleteLine = null;

        for (String[] line : data) {
            // 将当前行数组转为字符串，以便检测是否为折行
            String lineStr = String.join(",", line).trim();

            // 检查是否是折行（例如以 "签到时间" 开头或以特定标点开头）
            if (lineStr.startsWith("签到时间") || lineStr.startsWith(",") || lineStr.startsWith(" ") || lineStr.startsWith("、")) {
                // 如果是折行数据，追加到上一行的内容
                if (lastCompleteLine != null) {
                    currentLine.append(",").append(lineStr);
                }
            } else {
                // 如果是新的一行且之前有未保存的数据，则添加到结果列表
                if (currentLine.length() > 0 && lastCompleteLine != null) {
                    processedData.add((lastCompleteLine[0] + currentLine.toString()).split(","));
                    currentLine = new StringBuilder();
                }
                // 更新 lastCompleteLine 和 currentLine
                lastCompleteLine = line;
                currentLine.append(",").append(lineStr);
            }
        }

        // 添加最后一行数据
        if (currentLine.length() > 0 && lastCompleteLine != null) {
            processedData.add((lastCompleteLine[0] + currentLine.toString()).split(","));
        }

        return processedData;
    }
}
