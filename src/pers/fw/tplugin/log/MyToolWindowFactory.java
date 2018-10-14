package pers.fw.tplugin.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.*;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.util.Util;

import javax.swing.*;
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
//        list1.add("test", new Component() {
//            @Override
//            public String getName() {
//                return "test name";
//            }
//        });
//        ArrayList<String> strings = new ArrayList<>();
        Vector records = new Vector();
        records.addElement("one");
        records.addElement("two");
        records.addElement("three22222222222223weqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
        records.addElement("three22222222222223weqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");

        textPane1.setText("test haha");
//        VirtualFileSystem.addVirtualFileListener(VirtualFileListener);
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
//            @Override
//            public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
//                System.out.println("chage:");
//            }

            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                //判断文件是否是日志文件
                if (!Util.isLogFile(event.getFileName()))
                    return;
                //todo 查看新增内容
                VirtualFile file = event.getFile();
                try {
                    InputStream inputStream = file.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String str = "";
                    while ((str = reader.readLine()) != null) {
                        boolean b = str.startsWith("-");
                        if (b) {
                            String timeStr = Util.getTimeStr(reader.readLine());
                            if (oldTime == null || timeStr.compareTo(oldTime) > 0) {

                                String addStr;
                                while ((addStr = reader.readLine()) != null) {
                                    if (addStr.startsWith("-")) {
                                        timeStr = Util.getTimeStr(reader.readLine());
                                        records.addElement("时间: " + timeStr);
                                    } else {
                                        records.addElement(addStr);
                                    }
                                }
                                oldTime = timeStr;
                            }
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