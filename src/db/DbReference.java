package db;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.database.model.DasColumn;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.containers.JBIterable;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import inter.GotoCompletionContributor;
import inter.GotoCompletionLanguageRegistrar;
import inter.GotoCompletionProvider;
import inter.GotoCompletionRegistrarParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class DbReference implements GotoCompletionLanguageRegistrar {
    private static MethodMatcher.CallToSignature[] QUERY = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "where"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "group"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "avg"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "count"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "sum"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "max"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "min"),
    };

    private static MethodMatcher.CallToSignature[] QUERYARR = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "delete"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "update"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "insert"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "where"),
    };

    private static MethodMatcher.CallToSignature[] QUERYTABLE = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "table"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "name"),
    };

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {
                if (psiElement == null) {
                    return null;
                }
                PsiElement param = psiElement.getParent();
                if (param == null) return null;
                if ((param instanceof StringLiteralExpression && MethodMatcher.getMatchedSignatureWithDepth(param, QUERY, 0) != null)) {
                    //列
                    return new ColumnProvider(param);
                } else if (MethodMatcher.getMatchedSignatureWithDepth(param, QUERYTABLE, 0) != null) {
                    //表
                    return new TableProvider(param);
                } else {
                    //列, 数组里的列
                    try {
                        param = param.getParent().getParent();
                        if (!(param instanceof ArrayHashElementImpl)) return null;
                    } catch (Exception e) {
                        return null;
                    }
                    if (MethodMatcher.getMatchedSignatureWithDepth(param.getParent(), QUERYARR, 0) != null) {
                        return new ColumnProvider(param);
                    }
                }

                return null;
            }
        });
    }

    private static class ColumnProvider extends GotoCompletionProvider {
        public ColumnProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<>();

            PsiElement paramList = getElement().getParent();
            if (paramList == null) return lookupElements;
            PsiElement methodRef = paramList.getParent();
            if (!(methodRef instanceof MethodReference)) return lookupElements;

            //获取方法对象的类
            int type = 1;
            String tableName = "";
            PhpClass phpClass = Util.getInstanseClass(getElement().getProject(), (MethodReference) methodRef);
            if (phpClass != null) {
                Collection<Field> fields = phpClass.getFields();
                for (Field item : fields) {
                    Tool.log(item.getName() + ": " + item.getDefaultValuePresentation() + ":" + item.getDefaultValue().getText());
                    if ("name".equals(item.getName())) {
                        PsiElement defaultValue = item.getDefaultValue();
                        Tool.printPsiTree(defaultValue.getParent().getParent());
                        String name = item.getDefaultValue().getText();
                        if (name != null && !name.isEmpty()) {
                            tableName = name;
                            break;
                        }
                    } else if ("table".equals(item.getName())) {
                        String name = item.getDefaultValuePresentation();
                        if (name != null && !name.isEmpty()) {
                            type = 2;
                            tableName = name;
                            break;
                        }
                    }
                }
            }
            JBIterable<? extends DasColumn> columns = DbTableUtil.getColumns(getElement().getProject(), tableName, type);
            if (columns != null) {
                for (DasColumn item : columns) {
                    lookupElements.add(LookupElementBuilder.create(item.getName()).withTypeText(item.getComment()));
                }
            }
            return lookupElements;
        }

        @NotNull
        public Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor) {

            return null;
        }
    }

    private static class TableProvider extends GotoCompletionProvider {
        public TableProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
//
            return null;
        }


        @NotNull
        public Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor) {

            return null;
        }
    }
}
