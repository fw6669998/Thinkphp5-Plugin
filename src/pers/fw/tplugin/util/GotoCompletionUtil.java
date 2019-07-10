package pers.fw.tplugin.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.config.AppConfigReferences;
import pers.fw.tplugin.db.DbReference;
import pers.fw.tplugin.inter.GotoCompletionContributor;
import pers.fw.tplugin.inter.GotoCompletionLanguageRegistrar;
import pers.fw.tplugin.inter.GotoCompletionRegistrar;
import pers.fw.tplugin.inter.GotoCompletionRegistrarParameter;
import pers.fw.tplugin.model.ModelReference;
import pers.fw.tplugin.router.RouterReference;
import pers.fw.tplugin.view.ViewReferences2;

import java.util.ArrayList;
import java.util.Collection;

public class GotoCompletionUtil {

    private static GotoCompletionRegistrar[] CONTRIBUTORS = new GotoCompletionRegistrar[]{
            new AppConfigReferences(),
            new ViewReferences2(),
            new RouterReference(),
            new DbReference(),
            new ModelReference(),
    };

//    private static GotoCompletionRegistrar[]  = new GotoCompletionRegistrar[]{
//            new AppConfigReferences(),
//            new ViewReferences2(),
//            new RouterReference(),
//            new ModelReference(),
//    };

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
