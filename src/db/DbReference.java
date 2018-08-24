package db;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpClassHierarchyUtils;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.dataFlow.reachingDefinition.PhpDFAUtil;
import com.jetbrains.php.config.phpInfo.PhpInfoUtil;
import com.jetbrains.php.injection.PhpInjectionUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ArrayHashElementImpl;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import com.jetbrains.php.run.PhpExecutionUtil;
import inter.GotoCompletionContributor;
import inter.GotoCompletionLanguageRegistrar;
import inter.GotoCompletionProvider;
import inter.GotoCompletionRegistrarParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.MethodMatcher;
import util.PhpElementsUtil;
import util.PsiElementUtil;
import util.Symfony2InterfacesUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
                PsiElement parent = psiElement.getParent();
                if (parent == null) return null;
                if ((parent instanceof StringLiteralExpression && MethodMatcher.getMatchedSignatureWithDepth(parent, QUERY, 0) != null)) {
                    //列
                    return new ColumnProvider(psiElement);
                } else if (MethodMatcher.getMatchedSignatureWithDepth(parent, QUERYTABLE, 0) != null) {
                    //表
                    return new TableProvider(psiElement);
                } else {
                    //列, 数组里的列
                    try {
                        parent = parent.getParent().getParent();
                        if (!(parent instanceof ArrayHashElementImpl)) return null;
                    } catch (Exception e) {
                        return null;
                    }
                    if (MethodMatcher.getMatchedSignatureWithDepth(parent.getParent(), QUERYARR, 0) != null) {
                        return new ColumnProvider(parent.getParent());
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
            PsiElement parent = getElement().getParent();
            if (parent==null)return lookupElements;
            MethodReference parent1 = (MethodReference) parent.getParent();
            if(!(parent1 instanceof MethodReference))return lookupElements;

            Symfony2InterfacesUtil.getMultiResolvedMethod(parent1);

            //获取到列
            String table = "";
            PhpClassImpl phpClass = PsiElementUtil.getPhpClass(getElement());
            if (phpClass != null) {
                Collection<Field> fields = phpClass.getFields();
                for (Field item : fields) {
                    if ("name".equals(item.getName()) || "table".equals(item.getName())) {
                        table = item.getDefaultValuePresentation();
                    }
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
