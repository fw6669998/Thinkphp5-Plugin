package db;

import beans.ArrayMapVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.ParameterListImpl;
import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.ParameterList;
import util.MethodMatcher;

public class MethodRefVisitor extends PsiRecursiveElementWalkingVisitor {
    private final ArrayMapVisitor visitor;
    private String contextTable;

    public MethodRefVisitor(ArrayMapVisitor visitor, String contextTable) {
        this.visitor = visitor;
        this.contextTable = contextTable;
    }

    private static MethodMatcher.CallToSignature[] alias = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "alias")};

    private static MethodMatcher.CallToSignature[] join = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join")};

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof MethodReference) {
            PsiElement[] childrens = element.getChildren();
            for (PsiElement paramList : childrens) {
                if (paramList instanceof ParameterListImpl) {
                    PsiElement param = paramList.getChildren()[0];
                    if (param != null) {
                        if ("alias".equals(((MethodReference) element).getName()) && MethodMatcher.getMatchedSignatureWithDepth(param, alias, 0) != null) {
                            String text = param.getText().replace("'", "");
                            this.visitor.visit(text, contextTable);
                        } else if ("join".equals(((MethodReference) element).getName()) && MethodMatcher.getMatchedSignatureWithDepth(param, join, 0) != null) {
                            String text = param.getText().replace("'", "");
                            String[] s = text.split(" ");
                            if (s.length == 2) {    //有别名
                                this.visitor.visit(s[1], s[0]);
                            } else if (s.length == 1) { //无别名
                                this.visitor.visit(text, text);
                            }
                        }
                    }
                } else if (paramList instanceof MethodReference) {  //链式调用方法
                    super.visitElement(element);
                }
            }
        } else {
            super.visitElement(element);
        }
    }
}
