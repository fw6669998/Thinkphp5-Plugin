package pers.fw.tplugin.main;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.Util;

import java.util.Collection;
import java.util.Set;

public class MyTypeProvider implements PhpTypeProvider3 {
//    private static final Key<CachedValue<Map<String, Map<String, String>>>> MODEL_TYPE_MAP =
//            new Key<CachedValue<Map<String, Map<String, String>>>>("MODEL_TYPE_MAP");

    @Override
    public char getKey() {
        return 'F';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (psiElement instanceof FunctionReference && "model".equals(((FunctionReference) psiElement).getName())) {
            FunctionReference fun = (FunctionReference) psiElement;
            ParameterList parameterList = fun.getParameterList();
            if (parameterList != null) {
                PsiElement[] parameters = parameterList.getParameters();
                if (parameters.length > 0) {
                    String moduleName = Util.getCurTpModuleName(psiElement);
                    String text = parameters[0].getText().replace("'", "").replace("\"", "");
                    String clsRef = "\\app\\" + moduleName + "\\model\\" + text;
                    PhpType type = PhpType.builder().add(clsRef).build();
                    return type;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
