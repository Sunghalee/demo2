package com.app.dbTools;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;


/**
 * 文件拖拽窗口
 */
public class FileDropPanel extends JPanel {
    private ActionListener fileDropListener;
    private String droppedFilePath;
    private boolean fileNo;

    public FileDropPanel() {
        setPreferredSize(new Dimension(300, 200));

        // 设置面接受拖拽操作
        DropTarget dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent event) {
                try {
                    Transferable transferable = event.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        event.acceptDrop(DnDConstants.ACTION_COPY);
                        java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                        if(fileList.size() > 1){
                            fileNo = false;
                            System.out.println("3!");
                        } else if (fileList.size() == 1) {
                            fileNo = true;
                        }

                        // 处理拖拽的文件，例如读取Excel文件内容
                        for (File file : fileList) {
                            if (file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx")) {
                                // 获取拖拽文件的绝对路径
                                System.out.println("Reading Excel file: " + file.getAbsolutePath());

                                // 设置文件路径
                                droppedFilePath = file.getAbsolutePath();

                                // 触发文件拖放事件监听器
                                if (fileDropListener != null) {
                                    fileDropListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "fileDropped"));
                                    if(!fileNo){
                                        return;
                                    }
                                }
                            }
                        }

                        event.dropComplete(true);
                    } else {
                        event.rejectDrop();
                    }
                } catch (IOException | UnsupportedFlavorException e) {
                    e.printStackTrace();
                    event.rejectDrop();
                }
            }
        });

        setDropTarget(dropTarget);
    }

    /**
     * 获取监听对象
     */
    public void addFileDropListener(ActionListener listener) {
        fileDropListener = listener;
    }

    /**
     * 获取文件绝对路径
     */
    public String getDroppedFilePath() {
        return droppedFilePath;
    }

    /**
     * 文件数量
     */
    public boolean getFileSize() {
        return fileNo;
    }

    /**
     * 窗口UI
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(Color.lightGray);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(Color.BLACK);
        graphics.drawString("将Excel文件拖放到此处", 180, 120);
        graphics.drawString("或点击选择文件", 200, 140);
    }
}
