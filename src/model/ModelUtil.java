package model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import router.RouteUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelUtil {
    private static final Pattern modelFilePattern = Pattern.compile(".*/application/(\\w+)/model/(\\w+).php$");

    public static String matchModelFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if (path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }
        Matcher matcher = modelFilePattern.matcher(path);
        if (matcher.matches()) {
            return matcher.group(2);
        } else {
            return null;
        }
    }
}
