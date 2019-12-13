package pers.fw.tplugin.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelUtil {
    private static final Pattern modelFilePattern = Pattern.compile(".*/application/(\\w+)/(model|logic|service)/(.+).php$");
    private static final Pattern controllerFilePattern = Pattern.compile(".*/application/(\\w+)/controller/(\\w+).php$");

    public static String matchModelFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if (path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }
        Matcher matcher = modelFilePattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1)+"/"+matcher.group(3);
        } else {
            return null;
        }
    }
    public static String matchControllerFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if (path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }
        Matcher matcher = controllerFilePattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1)+"/"+matcher.group(2);
        } else {
            return null;
        }
    }
}
