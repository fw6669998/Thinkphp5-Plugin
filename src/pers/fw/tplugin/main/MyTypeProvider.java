package pers.fw.tplugin.main;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.Util;
import java.util.Collection;
import java.util.Set;

public class MyTypeProvider implements PhpTypeProvider4 {
//    private static final Key<CachedValue<Map<String, Map<String, String>>>> MODEL_TYPE_MAP =
//            new Key<CachedValue<Map<String, Map<String, String>>>>("MODEL_TYPE_MAP");

    @Override
    public char getKey() {
        return 'F';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (psiElement instanceof FunctionReference && "D".equals(((FunctionReference) psiElement).getName())) {
            FunctionReference fun = (FunctionReference) psiElement;
            ParameterList parameterList = fun.getParameterList();
            if (parameterList != null) {
                PsiElement[] parameters = parameterList.getParameters();
                if (parameters.length > 0) {
                    String moduleName = "";
                    String text = parameters[0].getText().replace("'", "").replace("\"", "");
                    if (text.contains("/")) {   //跨模块的model
                        String[] split = text.split("/");
                        moduleName = split[0];
                        text = split[1];
                    } else {
                        moduleName = Util.getCurTpModuleName(psiElement);
                    }
                    String clsRef = moduleName + "\\Model\\" + text+"Model";
                    PhpType type = PhpType.builder().add(clsRef).build();
                    return type;
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable PhpType complete(String s, Project project) {
        return null;
    }

//    @Override
//    public @Nullable PhpType complete(String s, Project project) {
//        return null;
//    }

//    public Collection<PsiElement> getPsiTargets(StringLiteralExpression psiElement) {
////            return super.getPsiTargets(psiElement, offset, editor);
//        final Set<PsiElement> targets = new HashSet<>();
//
//        String contents = psiElement.getContents();
//        if (StringUtils.isBlank(contents)) {
//            return targets;
//        }
//        if (!contents.contains("/")) contents = Util.getCurTpModuleName(getElement()) + "/" + contents;
//        FileBasedIndex.getInstance().getFilesWithKey(ModelStubIndex.KEY, new HashSet<>(Collections.singletonList(contents)),
//                new Processor<VirtualFile>() {
//                    @Override
//                    public boolean process(VirtualFile virtualFile) {
//                        if (virtualFile != null) {
//                            PsiFile psiFileTarget = PsiManager.getInstance(psiElement.getProject()).findFile(virtualFile);
//                            targets.add(psiFileTarget);
//                        }
//                        return true;
//                    }
//                }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(psiElement.getProject()), PhpFileType.INSTANCE));
//
//        return targets;
//    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
