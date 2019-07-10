package pers.fw.tplugin.config;

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
import pers.fw.tplugin.inter.GotoCompletionContributor;
import pers.fw.tplugin.inter.GotoCompletionLanguageRegistrar;
import pers.fw.tplugin.inter.GotoCompletionProvider;
import pers.fw.tplugin.inter.GotoCompletionRegistrarParameter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.*;

import java.util.*;

public class AppConfigReferences implements GotoCompletionLanguageRegistrar {

    private static MethodMatcher.CallToSignature[] CONFIG = new MethodMatcher.CallToSignature[]{
//            new MethodMatcher.CallToSignature("\\think\\Config", "get"),
//            new MethodMatcher.CallToSignature("\\think\\Config", "has"),
//            new MethodMatcher.CallToSignature("\\think\\Config", "set"),
//            new MethodMatcher.CallToSignature("\\think\\facade\\Config", "get"),
//            new MethodMatcher.CallToSignature("\\think\\facade\\Config", "set"),
//            new MethodMatcher.CallToSignature("\\think\\facade\\Config", "has"),
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

                PsiElement parent = psiElement.getParent();
                if (parent != null && (PsiElementUtil.isFunctionReference(parent, "C", 0)
                        || Util.isHintMethod(parent, CONFIG, 0, true))) {
                    return new ConfigKeyProvider(parent);
                }

                return null;
            }
        });
    }

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    private static class ConfigKeyProvider extends GotoCompletionProvider {

        public ConfigKeyProvider(PsiElement element) {
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

            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), ConfigKeyStubIndex.KEY);
            //扫描文件获取key, 放入ymlProjectProcessor
            FileBasedIndex.getInstance().processAllKeys(ConfigKeyStubIndex.KEY, ymlProjectProcessor, getProject());
            for (String key : ymlProjectProcessor.getResult()) {
                lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.CONFIG));
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

            FileBasedIndex.getInstance().getFilesWithKey(ConfigKeyStubIndex.KEY, new HashSet<>(Collections.singletonList(contents)), new Processor<VirtualFile>() {
                @Override
                public boolean process(VirtualFile virtualFile) {
                    PsiFile psiFileTarget = PsiManager.getInstance(ConfigKeyProvider.this.getProject()).findFile(virtualFile);
                    if (psiFileTarget == null) {
                        return true;
                    }

                    psiFileTarget.acceptChildren(new ArrayReturnPsiRecursiveVisitor(
                            ConfigFileUtil.matchConfigFile(ConfigKeyProvider.this.getProject(), virtualFile).getKeyPrefix(),
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
