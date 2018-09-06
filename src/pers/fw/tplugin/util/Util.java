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

public class Util {

    //获取当前引用变量的类
    public static PhpClass getInstanseClass(Project project, MethodReference methodRef) {
        Set<String> types = methodRef.getDeclaredType().getTypes();
        if (types.size() == 0) return null;
        String classType=null;
        for(String type : types){
            if(type.contains("\\model\\")){
                classType=type;
                break;
            }
        }
        if(classType==null)return null;
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

    public static String getCurTpModuleName(PsiElement psiElement) {
//        String path = element.getContainingFile().getVirtualFile().getPath();
//        int application = path.lastIndexOf("application");
//        if (application != -1) {
//            String substring = path.substring(application, path.length());
//            String[] split = substring.split("/");
//            if (split.length > 1) {
//                return split[1];
//            }
//        }
//        return "xxx";
        String fqn = getPhpClass(psiElement).getFQN();
        String[] split = fqn.split("\\\\");
        if (split.length > 2) {
            return split[2];
        }
        return "xxx";
    }
}
