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

import java.util.ArrayList;
//代码补全主入口
public class Completer extends CompletionContributor {
    public Completer() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

//                System.out.println("1");
//                testProvide(completionResultSet);
//                //todo 测试提供内容, 提供config内容
                PsiElement psiElement = completionParameters.getOriginalPosition();
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

    //    检查方法名(帮助方法)
    public void testCheckFunction(CompletionParameters completionParameters) {
        PsiElement originalPosition = completionParameters.getOriginalPosition();
        PsiElement parent = originalPosition.getParent();
        boolean config1 = PsiElementUtil.isFunctionReference(parent, "config", 0);
        System.out.println("psi2 is config" + config1);
    }

    //检查方法名(类方法)
    public MethodMatcher.MethodMatchParameter testCheckMethod(CompletionParameters completionParameters) {
        MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[]{
                new MethodMatcher.CallToSignature("\\think\\Config", "get"),
                new MethodMatcher.CallToSignature("\\think\\Config", "has"),
                new MethodMatcher.CallToSignature("\\think\\Config", "set"),
//                new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "setParsedKey"),
        };
        PsiElement originalPosition = completionParameters.getOriginalPosition();
        PsiElement parent = originalPosition.getParent();
        MethodMatcher.MethodMatchParameter matchedSignatureWithDepth = MethodMatcher.getMatchedSignatureWithDepth(parent, CONFIG);
        System.out.println(matchedSignatureWithDepth);
        return matchedSignatureWithDepth;
    }

    //    提供提示
    public void testProvide(CompletionResultSet completionResultSet) {
        ArrayList<LookupElem> elems = new ArrayList<>();
        elems.add(new LookupElem("aaaaa"));
        elems.add(new LookupElem("bbbbb"));
        elems.add(new LookupElem("ccccc"));
        completionResultSet.addAllElements(elems);
//        System.out.println(elems);
    }

    //提供提示数据
    public void testGetProvideData(CompletionParameters completionParameters, CompletionResultSet completionResultSet) {
        PsiElement psiElement = completionParameters.getOriginalPosition();
        CompletionContributorParameter parameter = null;
        for (GotoCompletionContributor contributor : GotoCompletionUtil.getContributors(psiElement)) {
            GotoCompletionProviderInterface formReferenceCompletionContributor = contributor.getProvider(psiElement);
            if (formReferenceCompletionContributor != null) {
                completionResultSet.addAllElements(
                        formReferenceCompletionContributor.getLookupElements()
                );

                if (parameter == null) {
//                    parameter = new CompletionContributorParameter(completionParameters, processingContext, completionResultSet);
                }

                formReferenceCompletionContributor.getLookupElements(parameter);
            }
        }
    }
}

