package pers.fw.tplugin.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.beans.Setting;
import pers.fw.tplugin.util.Util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                value = Util.formatLog(value);
                textPane1.setText(value);
            }
        });

        VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                //判断文件是否是日志文件
                String fileName = event.getFileName();
                if (fileName.equals(Setting.fileName)) {
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
                    boolean append = true;
                    String[] logPrefix = Setting.config.logPrefix;
                    String[] logRegex = Setting.config.logRegex;
                    List<Pattern> patterns=new ArrayList<Pattern>();
                    for(String item : logRegex){
                        Pattern p = Pattern.compile(item);
                        patterns.add(p);
                    }

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
                                            if (logPrefix != null && logPrefix.length != 0) {   //判断前缀
                                                for (String item : logPrefix) {
                                                    append = false;
                                                    if (line.startsWith(item)) {
                                                        append = true;
                                                        tempList.add("      "+line);
                                                        break;
                                                    }
                                                }
                                            } else if(logRegex.length!=0){  //判断正则
                                                for (Pattern item : patterns) {
                                                    append = false;
                                                    Matcher matcher = item.matcher(line);
                                                    if (matcher.matches()) {
                                                        append = true;
                                                        tempList.add("      "+line);
                                                        break;
                                                    }
                                                }
                                            }else{
                                                tempList.add("      "+line);
                                                append=true;
                                            }
                                        }
                                    } else {
                                        // 追加内容, 多行一条记录
                                        if (!append||tempList.size() == 0) continue;
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