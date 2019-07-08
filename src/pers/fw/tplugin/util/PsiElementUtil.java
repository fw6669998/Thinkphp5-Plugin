package pers.fw.tplugin.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.ParameterListOwner;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.beans.ParameterBag;

public class PsiElementUtil {

    public static boolean isFunctionReference(@NotNull PsiElement psiElement, @NotNull String functionName, int parameterIndex) {


        PsiElement parameterList = psiElement.getParent();
        PsiElement functionCall = parameterList.getParent();
        if (parameterIndex != -1) {
            if (!(parameterList instanceof ParameterList)) {
                return false;
            }
            ParameterBag index = getCurrentParameterIndex(psiElement);
            if (index == null || index.getIndex() != parameterIndex) {
                return false;
            }
        } else {
            functionCall = psiElement;
        }

        if (!(functionCall instanceof FunctionReference)) {
            return false;
        }
        return functionName.equals(((FunctionReference) functionCall).getName());
    }

//    public static boolean isFunctionReference(@NotNull PsiElement psiElement, @NotNull String functionName, int parameterIndex) {
//
//        PsiElement parameterList = psiElement.getParent();
//        if (!(parameterList instanceof ParameterList)) {
//            return false;
//        }
//        ParameterBag index = getCurrentParameterIndex(psiElement);
//        if (index == null || index.getIndex() != parameterIndex) {
//            return false;
//        }
//        PsiElement functionCall = parameterList.getParent();
//        if (!(functionCall instanceof FunctionReference)) {
//            return false;
//        }
//        return functionName.equals(((FunctionReference) functionCall).getName());
//    }

    @Nullable
    public static ParameterBag getCurrentParameterIndex(PsiElement psiElement) {

        if (!(psiElement.getContext() instanceof ParameterList)) {
            return null;
        }

        ParameterList parameterList = (ParameterList) psiElement.getContext();
        if (!(parameterList.getContext() instanceof ParameterListOwner)) {
            return null;
        }

        return getCurrentParameterIndex(parameterList.getParameters(), psiElement);
    }

    @Nullable
    public static ParameterBag getCurrentParameterIndex(PsiElement[] parameters, PsiElement parameter) {
        int i;
        for (i = 0; i < parameters.length; i = i + 1) {
            if (parameters[i].equals(parameter)) {
                return new ParameterBag(i, parameters[i]);
            }
        }

        return null;
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

    public static PhpClassImpl getPhpClass(PsiElement psiElement) {
        if (psiElement == null) return null;
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PhpClassImpl) {
            return (PhpClassImpl) parent;
        } else {
            return getPhpClass(parent);
        }
    }
}

