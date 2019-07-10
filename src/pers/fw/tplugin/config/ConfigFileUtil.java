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
    private static final Pattern configFilePattern = Pattern.compile(".*/(config/([\\w-./]+)|application/([\\w-.]+)).php$");
    private static final String[] configFiles = new String[]{"app", "cache", "cookie", "database", "log", "session", "template", "trace"};

    public static ConfigFileMatchResult matchConfigFile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath();
        String projectPath = project.getBaseDir().getPath();

        if (path.startsWith(projectPath)) {
            path = path.substring(projectPath.length());
        }
        Matcher m = configFilePattern.matcher(path);

        if (m.matches()) {
//            String temp0=m.group(0);
//            String temp1=m.group(1);
//            String temp2=m.group(2);
//            String temp3=m.group(3);
            String prefix2 = m.group(2);
            String prefix3 = m.group(3);
            if ((prefix2 != null && prefix2.contains("route")) || (prefix3 != null && prefix3.contains("route")))
                return new ConfigFileMatchResult(false, ""); //忽略路由文件
            if ("database".equals(prefix3))
                return new ConfigFileMatchResult(true, "database");//适配app目录下的配置文件
            if (m.group(0).startsWith("/config/")) {      //适配5.1配置格式
                for (String configFile : configFiles) {
                    if (configFile.equals(prefix2)) {
                        return new ConfigFileMatchResult(true, configFile);
                    }
                }
            }
            return new ConfigFileMatchResult(true, "");
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
