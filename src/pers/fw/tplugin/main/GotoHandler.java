package pers.fw.tplugin.main;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import pers.fw.tplugin.db.DbReference;
import pers.fw.tplugin.inter.GotoCompletionContributor;
import pers.fw.tplugin.inter.GotoCompletionProviderInterface;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.GotoCompletionUtil;

import java.util.ArrayList;
import java.util.Collection;
//跳转主入口
public class GotoHandler implements GotoDeclarationHandler {
    @Nullable
    @Override
    public PsiElement[] getGotoDeclarationTargets(PsiElement psiElement, int i, Editor editor) {

        //fwmodify:关闭开关
//        if (!LaravelProjectComponent.isEnabled(psiElement)) {
//            return new PsiElement[0];}
        Collection<PsiElement> psiTargets = new ArrayList<PsiElement>();
        try {
            if (psiElement == null) return psiTargets.toArray(new PsiElement[psiTargets.size()]); //报错解决

            PsiElement parent = psiElement.getParent();
            for (GotoCompletionContributor contributor : GotoCompletionUtil.getContributors(psiElement)) {
                GotoCompletionProviderInterface gotoCompletionContributorProvider = contributor.getProvider(psiElement);
                if (gotoCompletionContributorProvider != null) {
                    if (parent instanceof StringLiteralExpression) {
                        psiTargets.addAll(gotoCompletionContributorProvider.getPsiTargets((StringLiteralExpression) parent));
                    } else {
                        psiTargets.addAll(gotoCompletionContributorProvider.getPsiTargets(psiElement));
                    }

                    psiTargets.addAll(gotoCompletionContributorProvider.getPsiTargets(psiElement, i, editor));
                }
            }
        }catch (Exception e){
            //
        }
        return psiTargets.toArray(new PsiElement[psiTargets.size()]);
    }

    @Nullable
    @Override
    public String getActionText(DataContext dataContext) {
        return null;
    }
}
