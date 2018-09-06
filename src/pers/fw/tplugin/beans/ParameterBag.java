package pers.fw.tplugin.beans;

import com.intellij.psi.PsiElement;

public class ParameterBag {

    private int index;
    private PsiElement psiElement;

    public ParameterBag(int index, PsiElement psiElement) {
        this.index = index;
        this.psiElement = psiElement;
    }

    public int getIndex() {
        return index;
    }

//    public String getValue() {
//        return PsiElementUtils.getMethodParameter(psiElement);
//    }

    public PsiElement getElement() {
        return this.psiElement;
    }

}
