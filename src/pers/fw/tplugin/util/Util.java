package pers.fw.tplugin.util;

import pers.fw.tplugin.beans.Bean;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;

import java.util.Collection;
import java.util.Set;

public class
Util {

    //获取当前引用变量的类
    public static PhpClass getInstanseClass(Project project, MethodReference methodRef) {
        Set<String> types = methodRef.getDeclaredType().getTypes();
        if (types.size() == 0) return null;
        String classType = null;
        for (String type : types) {
            if (type.contains("\\model\\")) {
                classType = type;
                break;
            }
        }
        if (classType == null) return null;
        String classFQN = classType.substring(classType.indexOf("\\"), classType.indexOf("."));
        Collection<PhpClass> classesByFQN = PhpIndex.getInstance(project).getClassesByFQN(classFQN);
        if (classesByFQN.size() == 0) return null;
        else
            return classesByFQN.iterator().next();
    }

    //获取当前引用的方法
    public static Method getRefMethod(MethodReference methodRef) {
        PsiElement resolve = methodRef.resolve();
        if (resolve instanceof Method) {
            return (Method) resolve;
        }
        return null;
    }

    //获取当前编辑的方法
    public static Method getMethod(PsiElement psiElement) {
        if (psiElement == null) return null;
        PsiElement parent = psiElement.getParent();
        if (parent instanceof Method) {
            return (Method) parent;
        } else {
            return getMethod(parent);
        }
    }

    //获取当前编辑文件的类
    public static PhpClassImpl getPhpClass(PsiElement psiElement) {
        if (psiElement == null) return null;
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PhpClassImpl) {
            return (PhpClassImpl) parent;
        } else {
            return getPhpClass(parent);
        }
    }

    //处理前缀
    public static String rePrefix(String prefix) {
        String newPrefix = "";
        int i = prefix.lastIndexOf("=");
        int j = prefix.lastIndexOf(",");
        int k = prefix.lastIndexOf("|");
        int max = Math.max(k, Math.max(i, j));
        if (max != -1) {
            newPrefix = prefix.substring(max + 1, prefix.length());
            return newPrefix;
        } else {
            return prefix;
        }
    }


    /**
     * @param psiElement
     * @return 当前的模型目录; 根据类名获取
     */
    public static String getCurTpModuleName(PsiElement psiElement) {
        String fqn = getPhpClass(psiElement).getFQN();
        String[] split = fqn.split("\\\\");
        if (split.length > 2) {
            return split[2];
        }
        return "xxx";
    }

    /**
     * @return application的目录, 相对目录, 相对于项目的目录
     */
    public static String getApplicationDir(PsiElement psiElement) {
        String application = "application";
        String projectPath = psiElement.getProject().getBasePath();//"D:\\project2\\test";
//        String currentFilePath = psiElement.getContainingFile().getVirtualFile().getPath(); //"D:\\project2\\test\\application\\index\\controller\\Index.php";
        String currentFilePath = psiElement.getContainingFile().getVirtualFile().getPath(); //"D:\\project2\\test\\project\\application\\index\\controller\\Index.php";
        String[] arr = currentFilePath.replace(projectPath, "").split("/"); // project,application,index,xxx
        StringBuilder app = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(application)) {
                for (int j = 0; j < i; j++) {
                    app.append(arr[j]).append("/");
                }
                app.append(application);
            }
        }
        return app.toString();
    }

    /**
     * @param psiElement
     * @return 当前文件名
     */
    public static String getCurFileName(PsiElement psiElement) {
        return psiElement.getContainingFile().getVirtualFile().getName();
    }


    public static String getKeyWithCase(Collection<String> allKeys, String key) {
        for (String item : allKeys) {
            if (item.equals(key)) {
                return item;
            }
        }
        for (String item : allKeys) {
            if (item.toLowerCase().equals(key.toLowerCase())) {
                return item;
            }
        }
        return key;
    }

}
