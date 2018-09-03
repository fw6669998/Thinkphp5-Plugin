package util;

import beans.Bean;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;

import java.util.Collection;
import java.util.Set;

public class Util {
    public static PhpClass getInstanseClass(Project project, MethodReference methodRef) {
        Set<String> types = methodRef.getDeclaredType().getTypes();
        if (types.size() == 0) return null;
        String next = types.iterator().next();
        String classFQN = next.substring(next.indexOf("\\"), next.indexOf("."));
        Collection<PhpClass> classesByFQN = PhpIndex.getInstance(project).getClassesByFQN(classFQN);
        if (classesByFQN.size() == 0) return null;
        else
            return classesByFQN.iterator().next();
    }

    //获取当前方法名,
    public static Method getMethod(PsiElement psiElement) {
        if (psiElement == null) return null;
        PsiElement parent = psiElement.getParent();
        if (parent instanceof Method) {
            return (Method) parent;
        } else {
            return getMethod(parent);
        }
    }

    //获取当前类
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
}
