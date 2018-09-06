package pers.fw.tplugin.inter;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface GotoCompletionContributor {
    @Nullable
    GotoCompletionProvider getProvider(@Nullable PsiElement psiElement);
}
