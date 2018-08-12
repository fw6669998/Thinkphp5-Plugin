package stub;

import beans.ArrayKeyVisitor;
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
import util.ArrayReturnPsiRecursiveVisitor;
import config.ConfigFileUtil;
import util.Tool;

import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ConfigKeyStubIndex extends FileBasedIndexExtension<String, Void> {

    public static final ID<String, Void> KEY = ID.create("de.espend.idea.laravel.config_keys");
    private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return KEY;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return new DataIndexer<String, Void, FileContent>() {
            @NotNull
            @Override
            public Map<String, Void> map(@NotNull FileContent fileContent) {
                final Map<String, Void> map = new THashMap<>();

                PsiFile psiFile = fileContent.getPsiFile();
                if (!(psiFile instanceof PhpFile)) {
                    return map;
                }

                //匹配文件
                ConfigFileUtil.ConfigFileMatchResult result = ConfigFileUtil.matchConfigFile(fileContent.getProject(), fileContent.getFile());

                // config/app.php
                // config/testing/app.php
                if (result.matches()) {
                    //去掉前缀
//                    psiFile.acceptChildren(new ArrayReturnPsiRecursiveVisitor(result.getKeyPrefix(), new ArrayKeyVisitor() {
                    psiFile.acceptChildren(new ArrayReturnPsiRecursiveVisitor(null, new ArrayKeyVisitor() {
                        @Override
                        public void visit(String key, PsiElement psiKey, boolean isRootElement) {
                            if (!isRootElement) {
                                Tool.log("key:" + key);
                                map.put(key, null);
                            }
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
        return this.myKeyDescriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
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

    @Override
    public int getVersion() {
        return 1;
    }


}
