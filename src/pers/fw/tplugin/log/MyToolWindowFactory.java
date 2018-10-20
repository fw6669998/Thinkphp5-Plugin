package pers.fw.tplugin.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.*;
import com.intellij.util.ui.Table;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.util.Util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey.Chursin
 * Date: Aug 25, 2010
 * Time: 2:09:00 PM
 */
public class MyToolWindowFactory implements ToolWindowFactory {

    private JButton refreshToolWindowButton;
    private JButton hideToolWindowButton;
    private JLabel currentDate;
    private JLabel currentTime;
    private JLabel timeZone;
    private JPanel myToolWindowContent;
    private JList list1;
    //    private JList list2;
    private JTextPane textPane1;
    private ToolWindow myToolWindow;
    private static String oldTime = "1000";

    public MyToolWindowFactory() {
//        hideToolWindowButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                myToolWindow.hide(null);
//            }
//        });
//        refreshToolWindowButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                MyToolWindowFactory.this.currentDateTime();
//            }
//        });
        init();
    }

    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow;
//        this.currentDateTime();
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);

    }

    public void init() {
        String[] columnNames = {"类型1", "类型2", "内容"};
        Vector records = new Vector();
//        for (String col : columnNames) {
////            String[] split = col.split(".");
//            TableColumn tableColumn = new TableColumn(1, 200);
//            tableColumn.setHeaderValue(col);
//            table1.addColumn(tableColumn);
//        }

        textPane1.setText("test haha");
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                //判断文件是否是日志文件
                if (!LogUtil.isLogFile(event.getFileName()))
                    return;
                //todo 查看新增内容
                VirtualFile file = event.getFile();
                try {
                    InputStream inputStream = file.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = "";
                    while ((line = reader.readLine()) != null) {

                        if (line.startsWith("-")) continue;     //忽略
                        if (line.startsWith("[")) {
                            if (line.startsWith("[ 2")) {
                                //todo 时间行: 记录,更新时间
                                records.addElement(line);
                                oldTime = line;
                            } else {
                                //todo 内容头行: 记录
                                records.addElement(line);
                            }
                        } else {
                            //todo 内容行: 追加记录
                            String preStr = (String) records.get(records.size() - 1);
                            records.addElement(preStr + line);
                        }

                        String timeStr = LogUtil.getTimeStr(reader.readLine());
                        if (timeStr != null)   //这是新时间行

                            if (oldTime == null || timeStr.compareTo(oldTime) > 0) {
                                String addStr;
                                while ((addStr = reader.readLine()) != null) {
                                    if (addStr.startsWith("-")) {
                                        timeStr = LogUtil.getTimeStr(reader.readLine());
                                        records.addElement("时间: " + timeStr);

                                    } else {
//                                        String[] content = LogUtil.getContent(addStr);
//                                        DefaultTableModel model = new DefaultTableModel();
//                                        model.addRow(content);
//                                        table1.setModel(model);
                                        records.addElement(oldTime + " : " + addStr);
                                    }
                                }
                                oldTime = timeStr;
                            }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("chage:" + event.getFile());
                list1.setListData(records);
            }
        });
//        while (true) {
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            list1.setListData(strings);
//        }
    }

    public void currentDateTime() {
        // Get current date and time
        Calendar instance = Calendar.getInstance();
        currentDate.setText(String.valueOf(instance.get(Calendar.DAY_OF_MONTH)) + "/"
                + String.valueOf(instance.get(Calendar.MONTH) + 1) + "/" +
                String.valueOf(instance.get(Calendar.YEAR)));
        currentDate.setIcon(new ImageIcon(getClass().getResource("/icons/1.png")));
        int min = instance.get(Calendar.MINUTE);
        String strMin;
        if (min < 10) {
            strMin = "0" + String.valueOf(min);
        } else {
            strMin = String.valueOf(min);
        }
        currentTime.setText(instance.get(Calendar.HOUR_OF_DAY) + ":" + strMin);
        currentTime.setIcon(new ImageIcon(getClass().getResource("/icons/1.png")));
        // Get time zone
        long gmt_Offset = instance.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
        String str_gmt_Offset = String.valueOf(gmt_Offset / 3600000);
        str_gmt_Offset = (gmt_Offset > 0) ? "GMT + " + str_gmt_Offset : "GMT - " + str_gmt_Offset;
        timeZone.setText(str_gmt_Offset);
        timeZone.setIcon(new ImageIcon(getClass().getResource("/icons/1.png")));


    }

}