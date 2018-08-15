package view;

import com.intellij.lang.Language;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import config.AppConfigReferences;
import inter.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.MethodMatcher;
import util.PsiElementUtil;

// view 处理类
public class ViewReferences2 implements GotoCompletionLanguageRegistrar {
    private static MethodMatcher.CallToSignature[] VIEWS = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "make"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "exists"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "alias"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "name"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "of"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "renderEach"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "callComposer"),
            new MethodMatcher.CallToSignature("\\Illuminate\\View\\Factory", "callCreator"),
            new MethodMatcher.CallToSignature("\\Illuminate\\Mail\\Mailer", "send"),
            new MethodMatcher.CallToSignature("\\Illuminate\\Mail\\Mailer", "plain"),
            new MethodMatcher.CallToSignature("\\Illuminate\\Mail\\Mailer", "queue"),
    };

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {
                if (psiElement == null) {// || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return null;
                }

                PsiElement parent = psiElement.getParent();
                if (parent != null && (PsiElementUtil.isFunctionReference(parent, "fetch", 0)
                        || MethodMatcher.getMatchedSignatureWithDepth(parent, VIEWS) != null)) {
//                    return new AppConfigReferences.ConfigKeyProvider(parent);
                    return null;
                }
                return null;
            }
        });
    }


    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }
}
