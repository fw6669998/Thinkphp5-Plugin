package pers.fw.tplugin.log;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Condition;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.beans.Setting;
import pers.fw.tplugin.util.Util;

public class ShowLog implements Condition {

    @Override
    public boolean value(Object o) {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        if (openProjects.length == 0) {
            return false;
        }
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        String root = project.getBasePath();
        Util.setConfig(root + "/" + Setting.fileName);
        return Setting.config.logEnable;
    }
}
