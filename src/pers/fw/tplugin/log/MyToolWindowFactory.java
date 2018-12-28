package pers.fw.tplugin.log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.*;
import com.intellij.util.ui.Table;
import org.apache.velocity.runtime.directive.Foreach;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.beans.Bean;
import pers.fw.tplugin.beans.Setting;
import pers.fw.tplugin.util.Util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
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
    private JScrollPane scroll1;
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
//        return;
        init();
    }

    // Create the tool window content.
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        myToolWindow = toolWindow;
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void init() {
        String date = new SimpleDateFormat("[ yyyy-MM-dd").format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());

        oldTime = date + "T" + time;

        Vector records = new Vector();
        list1.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String value = (String) list1.getSelectedValue();
                textPane1.setText(value);
            }
        });

        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                //判断文件是否是日志文件
                String fileName = event.getFileName();
                if(fileName.equals(Setting.fileName)){
                    Util.setConfig(event.getFile().getPath());
                    return;
                }
                if (!LogUtil.isLogFile(fileName))
                    return;
                VirtualFile file = event.getFile();
                try {
                    InputStream inputStream = file.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CharsetToolkit.UTF8));
                    String line = "";
                    List<String> tempList = new ArrayList<String>();
                    boolean append=true;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("[ 2")) {
                            if (line.compareTo(oldTime) > 0) {
                                oldTime = line;
                                tempList.add(line);   //开始记录的时间行

                                while ((line = reader.readLine()) != null) {
                                    if (line.startsWith("-")) continue;     //忽略
                                    if (line.startsWith("[")) {
                                        if (line.startsWith("[ 2")) {
                                            // 记录,更新时间
                                            tempList.add(line);
                                            oldTime = line;
                                        } else {
                                            // 记录首行
                                            for (String item : Setting.config.logPrefix) {
                                                if (line.startsWith(item)) {
                                                    append=true;
                                                    tempList.add(line);
                                                } else {
                                                    append=false;
                                                }
                                            }
                                        }
                                    } else {
                                        // 追加内容, 多行一条记录
                                        if (!append) continue;
                                        if(tempList.size()==0)continue;
                                        String str = (String) tempList.get(tempList.size() - 1);
                                        str = str + line;
                                        tempList.set(tempList.size() - 1, str);
                                    }
                                }
                            }
                        }
                    }
                    records.addAll(tempList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                list1.setListData(records);
//                list1.updateUI();
                int height = list1.getHeight();
                scroll1.getViewport().setViewPosition(new Point(0, height));
            }
        });
    }
}