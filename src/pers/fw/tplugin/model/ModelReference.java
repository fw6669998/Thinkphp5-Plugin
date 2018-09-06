package pers.fw.tplugin.model;

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
import com.jetbrains.php.lang.psi.elements.*;
import pers.fw.tplugin.config.CollectProjectUniqueKeys;
import pers.fw.tplugin.inter.GotoCompletionContributor;
import pers.fw.tplugin.inter.GotoCompletionLanguageRegistrar;
import pers.fw.tplugin.inter.GotoCompletionProvider;
import pers.fw.tplugin.inter.GotoCompletionRegistrarParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.PsiElementUtil;

import java.util.*;

public class ModelReference implements GotoCompletionLanguageRegistrar {
//    private static MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[]{
//            new MethodMatcher.CallToSignature("\\think\\Loader", "pers.fw.tplugin.model")
//    };

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

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

                if (parent != null && PsiElementUtil.isFunctionReference(parent, "model", 0)) {
                    return new ModelReference.ModelProvider(parent);
                }
                return null;
            }
        });
    }

    private static class ModelProvider extends GotoCompletionProvider {
        public ModelProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {


            final Collection<LookupElement> lookupElements = new ArrayList<>();
            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), ModelStubIndex.KEY);
            FileBasedIndex.getInstance().processAllKeys(ModelStubIndex.KEY, ymlProjectProcessor, getProject());
            for (String key : ymlProjectProcessor.getResult()) {    //从ymlProjectProcessor中获取结果
                lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.TEMPLATE_CONTROLLER_LINE_MARKER));
            }
            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression psiElement) {
//            return super.getPsiTargets(psiElement, offset, editor);
            final Set<PsiElement> targets = new HashSet<>();

            final String contents = psiElement.getContents();
            if (StringUtils.isBlank(contents)) {
                return targets;
            }

            FileBasedIndex.getInstance().getFilesWithKey(ModelStubIndex.KEY, new HashSet<>(Collections.singletonList(contents)),
                    new Processor<VirtualFile>() {
                        @Override
                        public boolean process(VirtualFile virtualFile) {
                            if (virtualFile != null) {
                                PsiFile psiFileTarget = PsiManager.getInstance(ModelReference.ModelProvider.this.getProject()).findFile(virtualFile);
                                targets.add(psiFileTarget);
                            }

//                    psiFileTarget.acceptChildren(new PhpControllerVisitor(RouteUtil.matchControllerFile(ModelReference.ModelProvider.this.getProject(), virtualFile).getKeyPrefix(),
//                            new ArrayKeyVisitor() {
//                                @Override
//                                public void visit(String key, PsiElement psiKey, boolean isRootElement) {
//                                    if (!isRootElement && key.equals(contents)) {
//                                        targets.add(psiKey);
//                                    }
//                                }
//                            }));

                            return true;
                        }
                    }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            return targets;
        }
    }
}
