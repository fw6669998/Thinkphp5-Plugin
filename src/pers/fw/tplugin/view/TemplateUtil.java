package pers.fw.tplugin.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.view.dict.TemplatePath;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateUtil {
    public static Set<String> RENDER_METHODS = new HashSet<String>() {{
        add("make");
        add("of");
    }};

    @NotNull
    public static Set<VirtualFile> resolveTemplateName(@NotNull Project project, @NotNull String templateName) {
        Set<String> templateNames = new HashSet<>();

        int i = templateName.indexOf("::");
        String ns = null;
        if (i > 0) {
            ns = templateName.substring(0, i);
            templateName = templateName.substring(i + 2, templateName.length());
        }

        String pointName = templateName.replace(".", "/");

        // find template files by extensions
//        templateNames.add(pointName.concat(".blade.php"));

        templateNames.add(ViewCollector.curModule + "/" + pointName.concat(".html"));
        templateNames.add(pointName.concat(".html"));

        Set<VirtualFile> templateFiles = new HashSet<>();
        for (TemplatePath templatePath : ViewCollector.getPaths(project)) {
            // we have a namespace given; ignore all other paths
            String namespace = templatePath.getNamespace();
            if ((ns == null && namespace != null) || ns != null && !ns.equals(namespace)) {
                continue;
            }

            VirtualFile viewDir = templatePath.getRelativePath(project);
            if (viewDir == null) {
                continue;
            }
            for (String templateRelative : templateNames) {
                VirtualFile viewsDir = VfsUtil.findRelativeFile(templateRelative, viewDir);
                if (viewsDir != null) {
                    templateFiles.add(viewsDir);
                }
            }
        }
        return templateFiles;
    }

    @NotNull
    public static Collection<String> resolveTemplateName(@NotNull PsiFile psiFile) {
        return resolveTemplateName(psiFile.getProject(), psiFile.getVirtualFile());
    }

    @NotNull
    public static Collection<String> resolveTemplateName(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        Set<String> templateNames = new HashSet<>();

        for (TemplatePath templatePath : ViewCollector.getPaths(project)) {
            VirtualFile viewDir = templatePath.getRelativePath(project);
            if (viewDir == null) {
                continue;
            }

            String relativePath = VfsUtil.getRelativePath(virtualFile, viewDir);
            if (relativePath != null) {
                relativePath = stripTemplateExtensions(relativePath);

                if (templatePath.getNamespace() != null && StringUtils.isNotBlank(templatePath.getNamespace())) {
                    templateNames.add(templatePath.getNamespace() + "::" + relativePath.replace("/", "."));
                } else {
                    templateNames.add(relativePath.replace("/", "."));
                }
            }
        }

        return templateNames;
    }

    @NotNull
    public static Set<VirtualFile> resolveTemplateDirectory(@NotNull Project project, @NotNull String directory) {
        int i = directory.indexOf("::");
        String ns = null;
        if (i > 0) {
            ns = directory.substring(0, i);
            directory = directory.substring(i + 2, directory.length());
        }

        directory = directory.replace(".", "/");

        Set<VirtualFile> templateFiles = new HashSet<>();
        for (TemplatePath templatePath : ViewCollector.getPaths(project)) {
            // we have a namespace given; ignore all other paths
            String namespace = templatePath.getNamespace();
            if ((ns == null && namespace != null) || ns != null && !ns.equals(namespace)) {
                continue;
            }

            VirtualFile viewDir = templatePath.getRelativePath(project);
            if (viewDir == null) {
                continue;
            }

            VirtualFile viewsDir = VfsUtil.findRelativeFile(directory, viewDir);
            if (viewsDir != null) {
                templateFiles.add(viewsDir);
            }
        }

        return templateFiles;
    }


    /**
     * Try to find directory or file navigation for template name
     * <p>
     * "foo.bar" => "foo", "bar"
     */
    @NotNull
    public static Collection<VirtualFile> resolveTemplate(@NotNull Project project, @NotNull String templateName, int offset) {
        Set<VirtualFile> files = new HashSet<>();

        // try to find a path pattern on current offset after path normalization
        if (offset > 0 && offset < templateName.length()) {
            String templateNameWithCaret = normalizeTemplate(new StringBuilder(templateName).insert(offset, '\u0182').toString()).replace("/", ".");
            offset = templateNameWithCaret.indexOf('\u0182');

            int i = StringUtils.strip(templateNameWithCaret.replace(String.valueOf('\u0182'), ""), "/").indexOf(".", offset);
            if (i > 0) {
                files.addAll(resolveTemplateDirectory(project, templateName.substring(0, i)));
            }
        }

        // full filepath fallback: "foo/foo<caret>.blade.php"
        if (files.size() == 0) {
            files.addAll(resolveTemplateName(project, templateName));
        }

        return files;
    }

    /**
     * Normalize template path
     */
    @NotNull
    public static String normalizeTemplate(@NotNull String templateName) {
        return templateName
                .replace("\\", "/")
                .replaceAll("/+", "/");
    }


    /**
     * "'foobar'"
     * "'foobar', []"
     */
    @Nullable
    public static String getParameterFromParameterDirective(@NotNull String content) {
        Matcher matcher = Pattern.compile("^\\s*['|\"]([^'\"]+)['|\"]").matcher(content);

        if (matcher.find()) {
            return StringUtil.trim(matcher.group(1));
        }

        return null;
    }


    /**
     * Strip template extension for given template name
     * <p>
     * "foo_blade.blade.php" => "foo_blade"
     * "foo_blade.html.twig" => "foo_blade"
     * "foo_blade.php" => "foo_blade"
     */
    @NotNull
    public static String stripTemplateExtensions(@NotNull String filename) {
        if (filename.endsWith(".blade.php")) {
            filename = filename.substring(0, filename.length() - ".blade.php".length());
        } else if (filename.endsWith(".html.twig")) {
            filename = filename.substring(0, filename.length() - ".html.twig".length());
        } else if (filename.endsWith(".php")) {
            filename = filename.substring(0, filename.length() - ".php".length());
        }

        return filename;
    }

    private static class MyViewRecursiveElementWalkingVisitor extends PsiRecursiveElementWalkingVisitor {
        private final Collection<Pair<String, PsiElement>> views;

        private MyViewRecursiveElementWalkingVisitor(Collection<Pair<String, PsiElement>> views) {
            this.views = views;
        }

        @Override
        public void visitElement(PsiElement element) {

            if (element instanceof MethodReference) {
                visitMethodReference((MethodReference) element);
            }

            if (element instanceof FunctionReference) {
                visitFunctionReference((FunctionReference) element);
            }

            super.visitElement(element);
        }

        private void visitFunctionReference(FunctionReference functionReference) {

            if (!"view".equals(functionReference.getName())) {
                return;
            }

            PsiElement[] parameters = functionReference.getParameters();

            if (parameters.length < 1 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) parameters[0]).getContents();
            if (StringUtils.isBlank(contents)) {
                return;
            }

            views.add(Pair.create(contents, parameters[0]));
        }

        private void visitMethodReference(MethodReference methodReference) {

            String methodName = methodReference.getName();
            if (!RENDER_METHODS.contains(methodName)) {
                return;
            }

            PsiElement classReference = methodReference.getFirstChild();
            if (!(classReference instanceof ClassReference)) {
                return;
            }

            if (!"View".equals(((ClassReference) classReference).getName())) {
                return;
            }

            PsiElement[] parameters = methodReference.getParameters();
            if (parameters.length == 0 || !(parameters[0] instanceof StringLiteralExpression)) {
                return;
            }

            String contents = ((StringLiteralExpression) parameters[0]).getContents();
            if (StringUtils.isBlank(contents)) {
                return;
            }

            views.add(Pair.create(contents, parameters[0]));
        }
    }

    public static File recursionMatch(File root, String file) {
        if (root == null) return null;
        File[] files = root.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isFile()) {
                    String curPath = f.getAbsolutePath().toLowerCase();
                    curPath = curPath.replace("\\", "/");
                    curPath=curPath.substring(0,curPath.lastIndexOf("."));
                    if (curPath.equals(file.toLowerCase())) {
                        return f;
                    }
                } else if (f.isDirectory()) {
                    File res=recursionMatch(f, file);
                    if(res!=null)
                        return res;
                }
            }
        return null;
    }
}
