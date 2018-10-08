package pers.fw.tplugin.inter;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

public interface GotoCompletionProviderInterface {
    void getLookupElements(CompletionContributorParameter parameter);

    @NotNull
    Collection<LookupElement> getLookupElements();

    @NotNull
    default Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {
        return Collections.emptyList();
    }

    @NotNull
    Collection<PsiElement> getPsiTargets(PsiElement element);

    @NotNull
    Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor);
}
