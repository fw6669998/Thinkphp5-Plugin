package pers.fw.tplugin.view;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.VfsExUtil;
import pers.fw.tplugin.view.dict.JsonTemplatePaths;
import pers.fw.tplugin.view.dict.TemplatePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ViewCollector {
    /**
     * Default "view" path based on laravel versions
     */
    public static String curModule = "";
    public static TemplatePath[] DEFAULT_TEMPLATE_PATH;

    @NotNull
    public static Collection<TemplatePath> getPaths(@NotNull Project project) {
        return getPaths(project, false, false); //fowModify: true,true->false.false
    }

    @NotNull
    public static Collection<TemplatePath> getPaths(@NotNull Project project, boolean includeSettings, boolean includeJson) {
        Collection<TemplatePath> templatePaths = new ArrayList<>(Arrays.asList(DEFAULT_TEMPLATE_PATH));
        return templatePaths;
    }

    private static void collectIdeJsonBladePaths(@NotNull Project project, @NotNull Collection<TemplatePath> templatePaths) {
        for (PsiFile psiFile : FilenameIndex.getFilesByName(project, "ide-blade.json", GlobalSearchScope.allScope(project))) {
            Collection<TemplatePath> cachedValue = CachedValuesManager.getCachedValue(psiFile, new MyJsonCachedValueProvider(psiFile));
            if (cachedValue != null) {
                templatePaths.addAll(cachedValue);
            }
        }
    }

    /**
     * Visit all templates in project path configuration
     */
    public static void visitFile(@NotNull Project project, @NotNull ViewVisitor visitor) {
        //fowModify
//        for(TemplatePath templatePath : getPaths(project)) {
        Collection<TemplatePath> paths = getPaths(project, false, false);
        for (TemplatePath templatePath : paths) {
            visitTemplatePath(project, templatePath, visitor);
        }
    }

    /**
     * Visit all templates in given path
     */
    private static void visitTemplatePath(@NotNull Project project, @NotNull TemplatePath templatePath, @NotNull ViewVisitor visitor) {
        final VirtualFile templateDir = VfsUtil.findRelativeFile(templatePath.getPath(), project.getBaseDir());
        if (templateDir == null) {
            return;
        }

        VfsUtil.visitChildrenRecursively(templateDir, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile virtualFile) {
                if (virtualFile.isDirectory() || !isTemplateFile(virtualFile)) {
                    return true;
                }

                String filename = VfsUtil.getRelativePath(virtualFile, templateDir, '/');
                if (filename == null||!filename.contains("/")) {
                    return true;
                }
                //fowModify:
//                filename = BladeTemplateUtil.stripTemplateExtensions(filename);
                filename = clearStr(filename);

                String namespace = templatePath.getNamespace();
                if (namespace != null && StringUtils.isNotBlank(namespace)) {
                    visitor.visit(virtualFile, namespace + "::" + filename);
                } else {
                    visitor.visit(virtualFile, filename);
                }

                return true;
            }


            /**
             * 清理多余字符,
             */
            private String clearStr(String filename) {
                filename = filename.substring(0, filename.length() - ".html".length());
                //如果没有module
                String prefix = filename.substring(0, curModule.length());
                if (curModule.equals(prefix)) {
                    int len = filename.length();
                    filename = filename.substring(curModule.length() + 1, len);
                }
                return filename;
            }

            private boolean isTemplateFile(VirtualFile virtualFile) {
//                if(virtualFile.getFileType() ==  BladeFileType.INSTANCE || virtualFile.getFileType() == PhpFileType.INSTANCE) {
                if (virtualFile.getFileType() == HtmlFileType.INSTANCE) {
                    return true;
                }

                String extension = virtualFile.getExtension();
//                if (extension != null && (extension.equalsIgnoreCase("php") || extension.equalsIgnoreCase("twig"))) {
                if (extension != null && (extension.equalsIgnoreCase("html"))) {
                    return true;
                }

                return false;
            }
        });
    }

    public interface ViewVisitor {
        void visit(@NotNull VirtualFile virtualFile, @NotNull String name);
    }

    private static class MyJsonCachedValueProvider implements CachedValueProvider<Collection<TemplatePath>> {
        private final PsiFile psiFile;

        public MyJsonCachedValueProvider(PsiFile psiFile) {
            this.psiFile = psiFile;
        }

        @Nullable
        @Override
        public Result<Collection<TemplatePath>> compute() {

            Collection<TemplatePath> twigPaths = new ArrayList<>();

            String text = psiFile.getText();
            JsonTemplatePaths configJson = null;
            try {
                configJson = new Gson().fromJson(text, JsonTemplatePaths.class);
            } catch (JsonSyntaxException | JsonIOException | IllegalStateException ignored) {
            }

            if (configJson == null) {
                return Result.create(twigPaths, psiFile, psiFile.getVirtualFile());
            }

            Collection<JsonTemplatePaths.Path> namespaces = configJson.getNamespaces();
            if (namespaces == null || namespaces.size() == 0) {
                return Result.create(twigPaths, psiFile, psiFile.getVirtualFile());
            }

            for (JsonTemplatePaths.Path jsonPath : namespaces) {
                String path = jsonPath.getPath();
                if (path == null) {
                    path = "";
                }

                path = StringUtils.stripStart(path.replace("\\", "/"), "/");
                PsiDirectory parent = psiFile.getParent();
                if (parent == null) {
                    continue;
                }

                // current directory check and subfolder
                VirtualFile twigRoot;
                if (path.length() > 0) {
                    twigRoot = VfsUtil.findRelativeFile(parent.getVirtualFile(), path.split("/"));
                } else {
                    twigRoot = psiFile.getParent().getVirtualFile();
                }

                if (twigRoot == null) {
                    continue;
                }

                String relativePath = VfsExUtil.getRelativeProjectPath(psiFile.getProject(), twigRoot);
                if (relativePath == null) {
                    continue;
                }

                String namespace = jsonPath.getNamespace();

                String namespacePath = StringUtils.stripStart(relativePath, "/");

                if (StringUtils.isNotBlank(namespace)) {
                    twigPaths.add(new TemplatePath(namespacePath, namespace, true));
                } else {
                    twigPaths.add(new TemplatePath(namespacePath, true));
                }
            }

            return Result.create(twigPaths, psiFile, psiFile.getVirtualFile());
        }
    }
}
