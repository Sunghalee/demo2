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

                        // 处理拖拽的文件，例如读取Excel文件内容
                        for (File file : fileList) {
                            if (file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx")) {
                                // 在这里处理读取Excel文件的逻辑
                                System.out.println("Reading Excel file: " + file.getAbsolutePath());

                                // 设置文件路径
                                droppedFilePath = file.getAbsolutePath();

                                // 触发文件拖放事件监听器
                                if (fileDropListener != null) {
                                    fileDropListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "fileDropped"));
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

    public void addFileDropListener(ActionListener listener) {
        fileDropListener = listener;
    }

    public String getDroppedFilePath() {
        return droppedFilePath;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.drawString("将Excel文件拖放到此处", 180, 120);
        g.drawString("或点击选择文件", 200, 140);
    }
}
