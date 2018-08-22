package router;

import beans.ArrayKeyVisitor;
import beans.LaravelIcons;
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
import config.ArrayReturnPsiRecursiveVisitor;
import config.CollectProjectUniqueKeys;
import config.ConfigFileUtil;
import config.ConfigKeyStubIndex;
import inter.GotoCompletionContributor;
import inter.GotoCompletionLanguageRegistrar;
import inter.GotoCompletionProvider;
import inter.GotoCompletionRegistrarParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.MethodMatcher;
import util.PsiElementUtil;

import java.util.*;

public class RouterReference implements GotoCompletionLanguageRegistrar {

    private static MethodMatcher.CallToSignature[] Router = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\Route", "get"),
            new MethodMatcher.CallToSignature("\\think\\Route", "any"),
            new MethodMatcher.CallToSignature("\\think\\Route", "post"),
            new MethodMatcher.CallToSignature("\\think\\Route", "put"),
            new MethodMatcher.CallToSignature("\\think\\Route", "delete"),
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

            final String contents = element.getContents();
            if (StringUtils.isBlank(contents)) {
                return targets;
            }

            FileBasedIndex.getInstance().getFilesWithKey(RouteValStubIndex.KEY, new HashSet<>(Collections.singletonList(contents)), new Processor<VirtualFile>() {
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
                            if (!isRootElement && key.equals(contents)) {
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
