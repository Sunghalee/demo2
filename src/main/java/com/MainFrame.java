package com;

import com.app.ccFlow.CcFlowModelReader;
import com.app.dbTools.MybatisConvert;
import com.app.srNsys.SrColumnGetSetReader;
import com.app.srNsys.SrColumnModelReader;
import com.jtattoo.plaf.acryl.AcrylLookAndFeel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.app.dbTools.DbEnum.shutdownDataSource;

/**
 * 转换工具
 *
 * @author Sunghalee
 */
public class MainFrame {
    private static JFrame mainFrame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            mainFrame = new JFrame("Conversion Tool V7.0.1 By Sunghalee");

            //设定图标样式
            createIcon();

            //设定NimbusLookAndFeel样式
            try {
//                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

                JFrame.setDefaultLookAndFeelDecorated(true);
                UIManager.setLookAndFeel(new AcrylLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //关闭结束程序运行
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            mainFrame.setSize(600, 500);
            // 2行3列的布局
            mainFrame.setLayout(new GridLayout(3, 2));
//            mainFrame.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

            addButton(mainFrame, "SR-NSYS", e -> openFrameWithButtons("SR-NSYS"));
            addButton(mainFrame, "CC-FLOW", e -> openFrameWithButtons("CC-FLOW"));
            addButton(mainFrame, "B2", e -> openFrameWithButtons("B2"));
            addButton(mainFrame, "mybatis", e -> MybatisConvert.action());
            addButton(mainFrame, "D4", e -> openFrameWithButtons("D4"));
            addButton(mainFrame, "E5", e -> openFrameWithButtons("E5"));

            //设定屏幕中央显示
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);

            //默认关闭啥也不做
            mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            // 添加窗口关闭监听器
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    int option = JOptionPane.showConfirmDialog(mainFrame,
                            "你要关闭整个程序吗", "Confirmation",
                            JOptionPane.YES_NO_OPTION);

                    // 完全关闭程序
                    if (option == JOptionPane.YES_OPTION) {
                        shutdownDataSource();
                        System.exit(0);
                    }
                }
            });
        });
    }

    /**
     * 创建图标
     */
    private static void createIcon() {
        // 创建渐变色的图标
/*        BufferedImage iconImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = iconImage.createGraphics();
        GradientPaint gradient = new GradientPaint(0, 0, Color.BLUE, 16, 16, Color.GREEN);
        graphics2D.setPaint(gradient);
        graphics2D.fillRect(0, 0, 16, 16);
        graphics2D.dispose();*/

        //设置左上角图标
        String imagePath = "wave_616545.png";

        //这种方式也好用 但是导出为jar包无法正确找到图片
//        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(imagePath)).getPath();
//        Toolkit tk = Toolkit.getDefaultToolkit();
//        Image image = tk.createImage(path);
//        mainFrame.setIconImage(image);

        Image imageIcon = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getClassLoader().getResource(imagePath));
        mainFrame.setIconImage(imageIcon);

    }

    /**
     * 追加不同项目的按钮
     */
    private static void addButton(Container container, String buttonText, ActionListener listener) {
        JButton button = new JButton(buttonText);
        button.addActionListener(listener);
        container.add(button);
    }

    /**
     * 打开带有两个小按钮的窗口
     */
    private static void openFrameWithButtons(String projectName) {
        JFrame frame = new JFrame(projectName);
        frame.setSize(300, 300);
        frame.setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
        JButton modelButton = new JButton("model生成");
        JButton getSetButton = new JButton("getSet生成");
        buttonsPanel.add(modelButton);
        buttonsPanel.add(getSetButton);
        frame.add(buttonsPanel, BorderLayout.CENTER);

        // 设置按钮点击事件
        modelButton.addActionListener(e -> openModelGenerationFrame(projectName));
        getSetButton.addActionListener(e -> openGetSetGenerationFrame(projectName));

        //设置左上角图标
        String imagePath = "teamwork_4859784.png";
        Image imageIcon = Toolkit.getDefaultToolkit().getImage(MainFrame.class.getClassLoader().getResource(imagePath));
        frame.setIconImage(imageIcon);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * 打开model生成画面
     */
    private static void openModelGenerationFrame(String projectName) {
        switch (projectName) {
            case "SR-NSYS":
                // 打开SR-NSYS的model生成画面
                SrColumnModelReader.action();
                break;
            case "CC-FLOW":
                // 打开CC-FLOW的model生成画面
                CcFlowModelReader.action();
                break;
            case "B2":
                // 打开B2的model生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
            case "mybatis":
                // 打开mybatis生成画面
                MybatisConvert.action();
                break;
            case "D4":
                // 打开D4的model生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
            case "E5":
                // 打开E5的model生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
        }
    }


    /**
     * 打开getSet生成画面
     */
    private static void openGetSetGenerationFrame(String projectName) {
        switch (projectName) {
            case "SR-NSYS":
                // 打开SR-NSYS的getSet生成画面
                SrColumnGetSetReader.action();
                break;
            case "CC-FLOW":
                // 打开CC-FLOW的getSet生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
            case "B2":
                // 打开B2的getSet生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
            case "C3":
                // 打开C3的getSet生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
            case "D4":
                // 打开D4的getSet生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
            case "E5":
                // 打开E5的getSet生成画面
                JOptionPane.showMessageDialog(mainFrame, "暂未开发", "警告", JOptionPane.WARNING_MESSAGE);
                break;
        }
    }
}