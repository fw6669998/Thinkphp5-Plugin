package util;

import beans.Bean;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;

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
}
