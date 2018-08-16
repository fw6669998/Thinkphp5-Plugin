package util;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import config.AppConfigReferences;
import inter.GotoCompletionContributor;
import inter.GotoCompletionLanguageRegistrar;
import inter.GotoCompletionRegistrar;
import inter.GotoCompletionRegistrarParameter;
import org.jetbrains.annotations.NotNull;
import view.ViewReferences2;

import java.util.ArrayList;
import java.util.Collection;

public class GotoCompletionUtil {

    private static GotoCompletionRegistrar[] CONTRIBUTORS = new GotoCompletionRegistrar[]{
            new AppConfigReferences(),
            new ViewReferences2(),
//            new ProviderGotoCompletion(),
//            new ControllerReferences(),
//        new BladeDirectiveReferences(),
//        new TranslationReferences(),
//        new RoutingGotoCompletionRegistrar(),
//        new DicCompletionRegistrar(),
//        new AssetGotoCompletionRegistrar(),
    };

    public static Collection<GotoCompletionContributor> getContributors(final PsiElement psiElement) {
        Collection<GotoCompletionContributor> contributors = new ArrayList<>();

        GotoCompletionRegistrarParameter registerParam = new GotoCompletionRegistrarParameter() {
            @Override
            public void register(@NotNull ElementPattern<? extends PsiElement> pattern, GotoCompletionContributor contributor) {
                if (pattern.accepts(psiElement)) {
                    contributors.add(contributor);
                }
            }
        };

        for (GotoCompletionRegistrar register : CONTRIBUTORS) {
            // filter on language, 根据当前文件语言过滤处理对象
            if (register instanceof GotoCompletionLanguageRegistrar) {
                if (((GotoCompletionLanguageRegistrar) register).support(psiElement.getLanguage())) {
                    register.register(registerParam);
//                    ((ArrayList<GotoCompletionContributor>) contributors).add(((AppConfigReferences)register).);
                }
            } else {
                register.register(registerParam);
            }
        }

        return contributors;
    }
}
