package pers.fw.tplugin.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelUtil {
    private static final Pattern modelFilePattern = Pattern.compile(".*/Application/(\\w+)/Model/(\\w+).php$");

    public static String matchModelFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if (path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }
        Matcher matcher = modelFilePattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(1)+"/"+matcher.group(2).replace(".class","");
        } else {
            return null;
        }
    }
}
