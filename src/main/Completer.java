package main;

import beans.LookupElem;
import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import inter.CompletionContributorParameter;
import inter.GotoCompletionContributor;
import inter.GotoCompletionProviderInterface;
import org.jetbrains.annotations.NotNull;
import util.GotoCompletionUtil;
import util.MethodMatcher;
import util.PsiElementUtil;
import util.Tool;

import java.util.ArrayList;

//代码补全主入口
public class Completer extends CompletionContributor {
    public Completer() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

                PsiElement psiElement = completionParameters.getOriginalPosition();

//                Tool.printPsiTree(psiElement.getContainingFile());

                if (psiElement == null) {   // || !LaravelProjectComponent.isEnabled(psiElement)) {
                    return;
                }

                CompletionContributorParameter parameter = null;

                for (GotoCompletionContributor contributor : GotoCompletionUtil.getContributors(psiElement)) {
                    GotoCompletionProviderInterface formReferenceCompletionContributor = contributor.getProvider(psiElement);
                    if (formReferenceCompletionContributor != null) {
                        completionResultSet.addAllElements(
                                formReferenceCompletionContributor.getLookupElements()
                        );
                        if (parameter == null) {
                            parameter = new CompletionContributorParameter(completionParameters, processingContext, completionResultSet);
                        }
                        formReferenceCompletionContributor.getLookupElements(parameter);
                    }
                }
            }
        });
    }
}

