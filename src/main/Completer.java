package main;

import beans.LookupElem;
import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ProcessingContext;
import inter.CompletionContributorParameter;
import inter.GotoCompletionContributor;
import inter.GotoCompletionProviderInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.GotoCompletionUtil;
import util.MethodMatcher;
import util.PsiElementUtil;
import util.Tool;

import java.util.ArrayList;

//代码补全主入口
public class Completer extends CompletionContributor {
//    @Nullable
//    @Override
//    public AutoCompletionDecision handleAutoCompletionPossibility(@NotNull AutoCompletionContext context) {
//
//        return AutoCompletionDecision.SHOW_LOOKUP;
////        return super.handleAutoCompletionPossibility(context);
//    }


//    @Override
//    public void duringCompletion(@NotNull CompletionInitializationContext context) {
//        String dummyIdentifier = CompletionInitializationContext.DUMMY_IDENTIFIER;
//        String test=context.getDummyIdentifier();
//        System.out.println(test);
//        super.duringCompletion(context);
//    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        String test = context.getDummyIdentifier();
        System.out.println(test);

//        context.setDummyIdentifier("ttt");
//        super.beforeCompletion(context);
    }

    @Nullable
    @Override
    public String handleEmptyLookup(@NotNull CompletionParameters parameters, Editor editor) {
        return super.handleEmptyLookup(parameters, editor);
    }

    public Completer() {
//        fillCompletionVariants();
//        handleEmptyLookup();
//        fillCompletionVariants();
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
                        completionResultSet.addAllElements(formReferenceCompletionContributor.getLookupElements());
                        CompletionResultSet completionResultSet1 = completionResultSet.withPrefixMatcher("x.");
                        completionResultSet1.addLookupAdvertisement("s.");
                        if (parameter == null) {
                            parameter = new CompletionContributorParameter(completionParameters, processingContext, completionResultSet1);
                        }
                        formReferenceCompletionContributor.getLookupElements(parameter);
                    }
                }
            }
        });
    }
}

