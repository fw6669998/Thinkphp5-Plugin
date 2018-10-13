package pers.fw.tplugin.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.beans.LaravelIcons;

import javax.swing.*;

public class LogView implements ToolWindowFactory {

    private JPanel myToolWindowContent;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        toolWindow.setToHideOnEmptyContent(true);
//        toolWindow.setStripeTitle("RN Console");
//        toolWindow.setIcon(LaravelIcons.log);
//        Content content = createConsoleTabContent(toolWindow, true, "Welcome", null);
//        toolWindow.setShowStripeButton(true);
        ContentFactory instance = ContentFactory.SERVICE.getInstance();
        Content test = instance.createContent(myToolWindowContent, "test", false);
        toolWindow.getContentManager().addContent(test);
//        toolWindow.show(null);
    }
}
