package pers.fw.tplugin.router;

import pers.fw.tplugin.beans.ArrayKeyVisitor;
import pers.fw.tplugin.beans.LaravelIcons;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import pers.fw.tplugin.config.CollectProjectUniqueKeys;
import pers.fw.tplugin.inter.GotoCompletionContributor;
import pers.fw.tplugin.inter.GotoCompletionLanguageRegistrar;
import pers.fw.tplugin.inter.GotoCompletionProvider;
import pers.fw.tplugin.inter.GotoCompletionRegistrarParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.model.ModelStubIndex;
import pers.fw.tplugin.util.MethodMatcher;

import java.util.*;

public class RouterReference implements GotoCompletionLanguageRegistrar {

    private static MethodMatcher.CallToSignature[] Router = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\Route", "get"),
            new MethodMatcher.CallToSignature("\\think\\Route", "any"),
            new MethodMatcher.CallToSignature("\\think\\Route", "post"),
            new MethodMatcher.CallToSignature("\\think\\Route", "put"),
            new MethodMatcher.CallToSignature("\\think\\Route", "delete"),
            new MethodMatcher.CallToSignature("\\think\\facade\\Route", "get"),
            new MethodMatcher.CallToSignature("\\think\\facade\\Route", "any"),
            new MethodMatcher.CallToSignature("\\think\\facade\\Route", "post"),
            new MethodMatcher.CallToSignature("\\think\\facade\\Route", "put"),
            new MethodMatcher.CallToSignature("\\think\\facade\\Route", "delete"),
//            new MethodMatcher.CallToSignature("\\Illuminate\\Config\\Repository", "setParsedKey"),
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
                //string
                PsiElement parent = psiElement.getParent();
                PsiFile containingFile = psiElement.getContainingFile();
                containingFile.getName();
                if (parent != null && (
                        MethodMatcher.getMatchedSignatureWithDepth(parent, Router, 1) != null
                                || (RouteUtil.isRouteFile(containingFile) && RouteUtil.isRoutePosition(parent)))) {
                    return new RouteProvider(parent);
                }

                return null;
            }
        });
    }

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    private static class RouteProvider extends GotoCompletionProvider {

        public RouteProvider(PsiElement element) {
            super(element);
        }

        /**
         * 获取提示信息
         *
         * @return 返回提示集合
         */
        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {

            final Collection<LookupElement> lookupElements = new ArrayList<>();

            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), RouteValStubIndex.KEY);
            //扫描文件获取key, 放入ymlProjectProcessor
            FileBasedIndex.getInstance().processAllKeys(RouteValStubIndex.KEY, ymlProjectProcessor, getProject());
            for (String key : ymlProjectProcessor.getResult()) {    //从ymlProjectProcessor中获取结果
                lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.ROUTE));
            }

            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression element) {

            final Set<PsiElement> targets = new HashSet<>();

            String contents = element.getContents();
            if (StringUtils.isBlank(contents)) {
                return targets;
            }

            //忽略大小写
            Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(RouteValStubIndex.KEY, getElement().getProject());
            for (String item : allKeys) {
                if (item.toLowerCase().equals(contents.toLowerCase())) {
                    contents = item;
                    break;
                }
            }
            final String contents1 = contents;

            FileBasedIndex.getInstance().getFilesWithKey(RouteValStubIndex.KEY, new HashSet<>(Collections.singletonList(contents1)), new Processor<VirtualFile>() {
                @Override
                public boolean process(VirtualFile virtualFile) {
                    PsiFile psiFileTarget = PsiManager.getInstance(RouteProvider.this.getProject()).findFile(virtualFile);
                    if (psiFileTarget == null) {
                        return true;
                    }

                    psiFileTarget.acceptChildren(new PhpControllerVisitor(RouteUtil.matchControllerFile(RouteProvider.this.getProject(), virtualFile).getKeyPrefix(),
                            new ArrayKeyVisitor() {
                                @Override
                                public void visit(String key, PsiElement psiKey, boolean isRootElement) {
                                    if (!isRootElement && key.equals(contents1)) {
                                        targets.add(psiKey);
                                    }
                                }
                            }));

                    return true;
                }
            }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            return targets;
        }
    }
}
