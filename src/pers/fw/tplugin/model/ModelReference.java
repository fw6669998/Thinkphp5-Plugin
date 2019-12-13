package pers.fw.tplugin.model;

import com.intellij.util.indexing.ID;
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
import pers.fw.tplugin.util.Tool;
import pers.fw.tplugin.util.Util;

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

                if(parent == null)return null;
                if (PsiElementUtil.isFunctionReference(parent, "model", 0)) {
                    return new ModelReference.ModelProvider(parent);
                }else if(PsiElementUtil.isFunctionReference(parent, "controller", 0)){
                    return new ModelReference.ControllerProvider(parent);
                }
                return null;
            }
        });
    }

    public static class ControllerProvider extends ModelReference.ModelProvider {

        public ControllerProvider(PsiElement element) {
            super(element);
            this.key=ControllerStubIndex.KEY;
        }
    }

    private static class ModelProvider extends GotoCompletionProvider {

        protected ID<String, Void> key=ModelStubIndex.KEY;

        public ModelProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {


            final Collection<LookupElement> lookupElements = new ArrayList<>();
            CollectProjectUniqueKeys ymlProjectProcessor = new CollectProjectUniqueKeys(getProject(), key);
            FileBasedIndex.getInstance().processAllKeys(key, ymlProjectProcessor, getProject());
            String curModule = Util.getCurTpModuleName(getElement()) + "/";
            for (String key : ymlProjectProcessor.getResult()) {    //从ymlProjectProcessor中获取结果
                lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.TEMPLATE_CONTROLLER_LINE_MARKER));
                if (key.startsWith(curModule)) {    //去掉前缀的模型提示
                    key = key.replace(curModule, "");
                    lookupElements.add(LookupElementBuilder.create(key).withIcon(LaravelIcons.TEMPLATE_CONTROLLER_LINE_MARKER));
                }
            }
            return lookupElements;
        }

        @NotNull
        @Override
        public Collection<PsiElement> getPsiTargets(StringLiteralExpression psiElement) {
//            return super.getPsiTargets(psiElement, offset, editor);
            final Set<PsiElement> targets = new HashSet<>();

            String contents = psiElement.getContents();
            if (StringUtils.isBlank(contents)) {
                return targets;
            }

            //判断是否有模块名
            String curTpModuleName = Util.getCurTpModuleName(getElement())+"/";
            String content2="";
            if(!contents.startsWith(curTpModuleName)){
                content2=curTpModuleName+contents;
            }

            //忽略大小写
            Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(key, getElement().getProject());

            contents = Util.getKeyWithCase(allKeys, contents);
            if(!allKeys.contains(contents))
                contents = Util.getKeyWithCase(allKeys, content2);

            FileBasedIndex.getInstance().getFilesWithKey(key, new HashSet<>(Collections.singletonList(contents)),
                    new Processor<VirtualFile>() {
                        @Override
                        public boolean process(VirtualFile virtualFile) {
                            if (virtualFile != null) {
                                PsiFile psiFileTarget = PsiManager.getInstance(ModelReference.ModelProvider.this.getProject()).findFile(virtualFile);
                                targets.add(psiFileTarget);
                            }
                            return true;
                        }
                    }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(getProject()), PhpFileType.INSTANCE));

            return targets;
        }
    }
}
