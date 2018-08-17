package util;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

public class Tool {
    public static void log(Object obj) {
        System.out.println(obj);
    }

    public static void printPsiTree(PsiElement element) {
        if (element == null) {
            return;
        }
        level++;
        int i=0;
        PsiElement[] elements = element.getChildren();
        for (PsiElement item : elements) {
            i++;
            printeIndent("   ", level);
            String s = item.getClass().getSimpleName();
            //level+"."+i+":"+
            System.out.println(level+" "+s);
            printPsiTree(item);
        }
        level--;
    }

    private static int level = 0;

    private static void printeIndent(String str, int cnt) {
        for (int i = 0; i < cnt; i++) {
            System.out.print(str);
        }
    }

}
