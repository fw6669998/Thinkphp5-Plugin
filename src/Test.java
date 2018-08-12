import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import java.util.Objects;

public class Test extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("yes");
        VirtualFile data = e.getData(PlatformDataKeys.VIRTUAL_FILE);    //vfs
        Document document = Objects.requireNonNull(e.getData(PlatformDataKeys.EDITOR)).getDocument();   //document
        PsiFile data1 = e.getData(LangDataKeys.PSI_FILE);
//        PsiElement elementAt = data1.findElementAt(0);
//        FileASTNode node = data1.getNode();
//        System.out.println(data);
//        String text = document.getText();
//        System.out.println(text);
        Messages.showInfoMessage("消息内容", "标题");
    }
}
