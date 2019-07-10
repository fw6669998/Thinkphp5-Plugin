package pers.fw.tplugin.router;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import pers.fw.tplugin.beans.ArrayKeyVisitor;

import java.util.Map;


public class RouteValStubIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("fw.router.index");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    //收集数据 索引进程, 数据文件被修改不会立即触发该方法, 只有当动作(例如代码补全)发生才会触发该方法
    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {   //fileContent, 被修改的文件,controller文件
                final Map<String, Void> map = new THashMap<>(); //创建结果容器
                PsiFile psiFile = fileContent.getPsiFile();     //获取Psi
                if (!(psiFile instanceof PhpFile)) {    //过滤非php文件
                    return map;
                }
                //匹配文件
                RouteUtil.ControllerFileMatchResult controllerFileMatchResult = RouteUtil.matchControllerFile(fileContent.getProject(), fileContent.getFile());
                if (controllerFileMatchResult.matches()) {    //是controller文件
                    String keyPrefix = controllerFileMatchResult.getKeyPrefix();
                    if (keyPrefix != null)
                        map.put(keyPrefix, null);
                    psiFile.acceptChildren(new PhpControllerVisitor(keyPrefix, new ArrayKeyVisitor() {
                        @Override
                        public void visit(String key, PsiElement psiKey, boolean isRootElement) {
                            map.put(key, null);
                        }
                    }));
                }
                return map;
            }
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
