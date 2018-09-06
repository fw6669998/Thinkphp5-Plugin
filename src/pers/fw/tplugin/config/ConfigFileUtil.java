package pers.fw.tplugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 匹配文件
 */
public class ConfigFileUtil {
    private static final Pattern configFilePattern = Pattern.compile(".*/(config|application)/([\\w-/]+).php$");

    public static ConfigFileMatchResult matchConfigFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if (path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }
        //todo 兼容5.1
//        if(path.contains("config")){
//            return new ConfigFileMatchResult(false, "");
//        }
        Matcher m = configFilePattern.matcher(path);

        if (m.matches()) {
            String prefix2 = m.group(2).replace('/', '.');

            if (!prefix2.contains("database"))
                prefix2 = "";
            return new ConfigFileMatchResult(true, prefix2);
        } else {
            return new ConfigFileMatchResult(false, "");
        }
    }

    public static class ConfigFileMatchResult {
        private boolean matches;

        private String keyPrefix;

        ConfigFileMatchResult(boolean matches, @NotNull String keyPrefix) {
            this.matches = matches;
            this.keyPrefix = keyPrefix;
        }

        public boolean matches() {
            return matches;
        }

        @NotNull
        public String getKeyPrefix() {
            return keyPrefix;
        }
    }
}
