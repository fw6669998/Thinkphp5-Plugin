package util;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import config.AppConfigReferences;
import inter.GotoCompletionContributor;
import inter.GotoCompletionLanguageRegistrar;
import inter.GotoCompletionRegistrar;
import inter.GotoCompletionRegistrarParameter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class GotoCompletionUtil {

    private static GotoCompletionRegistrar[] CONTRIBUTORS = new GotoCompletionRegistrar[]{
            new AppConfigReferences(),
//            new ProviderGotoCompletion(),
//            new ViewReferences(),
//            new ControllerReferences(),
//        new BladeDirectiveReferences(),
//        new TranslationReferences(),
//        new RoutingGotoCompletionRegistrar(),
//        new DicCompletionRegistrar(),
//        new AssetGotoCompletionRegistrar(),
    };

    public static Collection<GotoCompletionContributor> getContributors(final PsiElement psiElement) {
        Collection<GotoCompletionContributor> contributors = new ArrayList<>();

        GotoCompletionRegistrarParameter registrar = new GotoCompletionRegistrarParameter() {
            @Override
            public void register(@NotNull ElementPattern<? extends PsiElement> pattern, GotoCompletionContributor contributor) {
                if (pattern.accepts(psiElement)) {
                    contributors.add(contributor);
                }
            }
        };

        for (GotoCompletionRegistrar register : CONTRIBUTORS) {
            // filter on language
            if (register instanceof GotoCompletionLanguageRegistrar) {
                if (((GotoCompletionLanguageRegistrar) register).support(psiElement.getLanguage())) {
                    register.register(registrar);
                }
            } else {
                register.register(registrar);
            }
        }

        return contributors;
    }
}
