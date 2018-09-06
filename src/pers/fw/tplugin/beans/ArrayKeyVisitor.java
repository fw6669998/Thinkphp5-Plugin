package pers.fw.tplugin.beans;

import com.intellij.psi.PsiElement;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public interface ArrayKeyVisitor {
    void visit(String key, PsiElement psiKey, boolean isRootElement);
}
