package pers.fw.tplugin.config;

import pers.fw.tplugin.beans.ArrayKeyVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ArrayReturnPsiRecursiveVisitor extends PsiRecursiveElementWalkingVisitor {

    private final String fileNameWithoutExtension;
    private final ArrayKeyVisitor arrayKeyVisitor;

    public ArrayReturnPsiRecursiveVisitor(String fileNameWithoutExtension, ArrayKeyVisitor arrayKeyVisitor) {
        this.fileNameWithoutExtension = fileNameWithoutExtension;
        this.arrayKeyVisitor = arrayKeyVisitor;
    }

    @Override
    public void visitElement(PsiElement element) {

//        if (element instanceof PhpReturn) {
//            visitPhpReturn((PhpReturn) element);
//        }

        if (element instanceof ArrayCreationExpression && !(element.getParent().getParent() instanceof ArrayHashElement)) {   //如果是创建数组会进行收录, 如果是数组中的数组不能满足条件
//        if (element instanceof ArrayCreationExpression) {
            collectConfigKeys((ArrayCreationExpression) element, this.arrayKeyVisitor, fileNameWithoutExtension);
//            Tool.printPsiTree(element.getParent().getParent());
//            element = element.getNextSibling();
        }

        super.visitElement(element);
    }

    public void visitPhpReturn(PhpReturn phpReturn) {
        PsiElement arrayCreation = phpReturn.getFirstPsiChild();
        if (arrayCreation instanceof ArrayCreationExpression) {
            collectConfigKeys((ArrayCreationExpression) arrayCreation, this.arrayKeyVisitor, fileNameWithoutExtension);
        }
    }


    public static void collectConfigKeys(ArrayCreationExpression creationExpression, ArrayKeyVisitor arrayKeyVisitor, String configName) {
        collectConfigKeys(creationExpression, arrayKeyVisitor, Collections.singletonList(configName));
    }

    public static void collectConfigKeys(ArrayCreationExpression creationExpression, ArrayKeyVisitor arrayKeyVisitor, List<String> context) {

        List<ArrayHashElement> childrenOfTypeAsList = PsiTreeUtil.getChildrenOfTypeAsList(creationExpression, ArrayHashElement.class);
        for (ArrayHashElement hashElement : childrenOfTypeAsList) {  //遍历文件所有元素

            PsiElement arrayKey = hashElement.getKey();
            PsiElement arrayValue = hashElement.getValue();

            if (arrayKey instanceof StringLiteralExpression) {  //键是数组键

                List<String> myContext = new ArrayList<>(context);

                //fwModify: 协助去掉文件前缀
                if (myContext.get(0).equals(""))
                    myContext.remove(0);

                myContext.add(((StringLiteralExpression) arrayKey).getContents());
                String keyName = StringUtils.join(myContext, ".");

                if (arrayValue instanceof ArrayCreationExpression) {
                    arrayKeyVisitor.visit(keyName, arrayKey, false); //数组键也收录
                    collectConfigKeys((ArrayCreationExpression) arrayValue, arrayKeyVisitor, myContext);
                } else {
                    arrayKeyVisitor.visit(keyName, arrayKey, false);
                }
            }
        }

    }
}
