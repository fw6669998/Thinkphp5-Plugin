package pers.fw.tplugin.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

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
        Vector strings = new Vector();
        strings.addElement("one");
        strings.addElement("two");
        strings.addElement("three22222222222223weqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
        strings.addElement("three22222222222223weqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");

        textPane1.setText("test haha");
//        VirtualFileSystem.addVirtualFileListener(VirtualFileListener);
        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
//            @Override
//            public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
//                System.out.println("chage:");
//            }

            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                //todo 判断文件是否是日志文件
                System.out.println(event.getFileName());
                //todo 查看新增内容
                VirtualFile file = event.getFile();
                String extension = file.getExtension();
                try {
                    InputStream inputStream = file.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String str="";
                    while ((str=reader.readLine())!=null){
                        boolean b = str.startsWith("-");
                        if(b){
                            str = reader.readLine();
                            if(str.startsWith("[ 2")){
                                //处理时间行:
                                
                            }
                        }
                    }

                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("chage:"+event.getFile());
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