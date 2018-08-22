package db;

import com.intellij.lang.Language;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import inter.GotoCompletionContributor;
import inter.GotoCompletionLanguageRegistrar;
import inter.GotoCompletionProvider;
import inter.GotoCompletionRegistrarParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.MethodMatcher;

public class DbReference implements GotoCompletionLanguageRegistrar {
    private static MethodMatcher.CallToSignature[] QUERY = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\Config", "get"),
            new MethodMatcher.CallToSignature("\\think\\Config", "has"),
            new MethodMatcher.CallToSignature("\\think\\Config", "set"),
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
                if(parent!=null&&MethodMatcher.getMatchedSignatureWithDepth(parent,))
                return null;
            }
        });
    }
}
