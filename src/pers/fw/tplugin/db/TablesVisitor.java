package pers.fw.tplugin.db;

import com.jetbrains.php.lang.psi.elements.FunctionReference;
import pers.fw.tplugin.beans.ArrayMapVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.jetbrains.php.lang.psi.elements.impl.ParameterListImpl;
import pers.fw.tplugin.util.MethodMatcher;
import pers.fw.tplugin.util.PsiElementUtil;
import pers.fw.tplugin.util.Tool;

import java.util.HashSet;

public class TablesVisitor extends PsiRecursiveElementWalkingVisitor {
    private final ArrayMapVisitor visitor;
    private HashSet<String> tables;

    public TablesVisitor(ArrayMapVisitor visitor, HashSet<String> tables) {
        this.visitor = visitor;
        this.tables = tables;
    }

    private static MethodMatcher.CallToSignature[] alias = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "alias")};

    private static MethodMatcher.CallToSignature[] join = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join")};

    private static MethodMatcher.CallToSignature[] table = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "table"),
            new MethodMatcher.CallToSignature("\\think\\Db", "table"),
    };

    private void addTable(PsiElement param, int type) {
        String text = param.getText().replace("'", "").replace("\"", "");
        if (type == 1) {
            text = DbTableUtil.getTableByName(param.getProject(), text);
        }
        this.visitor.visit(text, null);
    }

    @Override
    public void visitElement(PsiElement element) {
        if (element instanceof FunctionReference) {
            PsiElement[] childrens = element.getChildren();
            for (PsiElement paramList : childrens) {
                if (paramList instanceof ParameterListImpl) {
                    if (paramList.getChildren().length > 0) {
                        PsiElement param = paramList.getChildren()[0];
//                        String methodName = ((FunctionReference) element).getName();
                        if (PsiElementUtil.isFunctionReference(param, "join", 0)) {
                            String text = param.getText().replace("'", "").replace("\"", "");
                            String[] s = text.split(" ");
                            this.visitor.visit(s[0], null);
                        } else if (PsiElementUtil.isFunctionReference(param, "table", 0)) {
                            addTable(param, 0);
                        } else if (PsiElementUtil.isFunctionReference(param, "db", 0)||PsiElementUtil.isFunctionReference(param, "name", 0)) {
                            addTable(param, 1);
                        }
                    }
                } else if (paramList instanceof FunctionReference) {  //链式调用方法
                    super.visitElement(element);
                }
            }
        } else {
            super.visitElement(element);
        }
    }


}
