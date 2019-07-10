package pers.fw.tplugin.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import pers.fw.tplugin.beans.Config;
import pers.fw.tplugin.beans.Setting;
import pers.fw.tplugin.db.DbTableUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class
Util {

    //获取当前引用变量的类
    public static PhpClass getInstanseClass(Project project, MethodReference methodRef) {
        Set<String> types = methodRef.getDeclaredType().getTypes();
        if (types.size() == 0) return null;
        String classType = null;
        for (String type : types) {
            if (type.contains("\\model\\")) {
                classType = type;
                break;
            }
        }
        if (classType == null) return null;
        String classFQN = classType.substring(classType.indexOf("\\"), classType.indexOf("."));
        Collection<PhpClass> classesByFQN = PhpIndex.getInstance(project).getClassesByFQN(classFQN);
        if (classesByFQN.size() == 0) return null;
        else
            return classesByFQN.iterator().next();
    }

    //获取当前引用的方法
    public static Method getRefMethod(MethodReference methodRef) {
        PsiElement resolve = methodRef.resolve();
        if (resolve instanceof Method) {
            return (Method) resolve;
        }
        return null;
    }

    //获取当前编辑的方法
    public static Method getMethod(PsiElement psiElement) {
        if (psiElement == null) return null;
        PsiElement parent = psiElement.getParent();
        if (parent instanceof Method) {
            return (Method) parent;
        } else {
            return getMethod(parent);
        }
    }

    //获取当前编辑文件的类
    public static PhpClassImpl getPhpClass(PsiElement psiElement) {
        if (psiElement == null) return null;
        PsiElement parent = psiElement.getParent();
        if (parent instanceof PhpClassImpl) {
            return (PhpClassImpl) parent;
        } else {
            return getPhpClass(parent);
        }
    }

    //处理前缀
    public static String rePrefix(String prefix) {
        String newPrefix = "";
        int i = prefix.lastIndexOf("=");
        int j = prefix.lastIndexOf(",");
        int k = prefix.lastIndexOf("|");
        int d = prefix.lastIndexOf(".");

        int max = Math.max(Math.max(k, Math.max(i, j)), d);
        if (max != -1) {
            newPrefix = prefix.substring(max + 1, prefix.length());
            return newPrefix;
        } else {
            return prefix;
        }
    }


    /**
     * @param psiElement
     * @return 当前的模型目录; 根据类名获取
     */
    public static String getCurTpModuleName(PsiElement psiElement) {
        PhpClassImpl phpClass = getPhpClass(psiElement);
        if(phpClass==null)return "xxx";
        String fqn = phpClass.getFQN();
        String[] split = fqn.split("\\\\");
        if (split.length > 1) {
            return split[1];
        }
        return "xxx";
    }

    public static String getTableByClass(PhpClass phpClass, Project project) {
        if (phpClass != null) {
            Collection<Field> fields = phpClass.getFields();
            for (Field item : fields) {
                if ("name".equals(item.getName())) {
                    PsiElement defaultValue = item.getDefaultValue();
                    if(defaultValue==null)continue;
                    String name = defaultValue.getText();
                    if (name != null && !name.isEmpty() && !"$name".equals(name)) {
                        name = name.replace("'", "").replace("\"", "");
                        return DbTableUtil.getTableByName(project, name);
                    }
                } else if ("table".equals(item.getName())) {
                    PsiElement defaultValue = item.getDefaultValue();
                    if(defaultValue==null)continue;
                    String name = defaultValue.getText();
                    if (name != null && !name.isEmpty() && !"$table".equals(name)) {
                        name = name.replace("'", "").replace("\"", "");
                        return name;
                    }
                }else{
                    String fqn=phpClass.getFQN();
                    if(fqn.contains("\\Model\\")){
                        int start = fqn.lastIndexOf("\\");
                        int end = fqn.lastIndexOf("Model");
                        if(end<start+1)return null;
                        String name=fqn.substring(start+1,end);
                        return DbTableUtil.getTableByName(project, name);
                    }
                }
            }
        }
        return null;
    }

    /**
     * @return application的目录, 相对目录, 相对于项目的目录
     */
    public static String getApplicationDir(PsiElement psiElement) {
        String application = "Application";
        String projectPath = psiElement.getProject().getBasePath();//"D:\\project2\\test";
//        String currentFilePath = psiElement.getContainingFile().getVirtualFile().getPath(); //"D:\\project2\\test\\application\\index\\controller\\Index.php";
        String currentFilePath = psiElement.getContainingFile().getVirtualFile().getPath(); //"D:\\project2\\test\\project\\application\\index\\controller\\Index.php";
        String[] arr = currentFilePath.replace(projectPath, "").split("/"); // project,application,index,xxx
        StringBuilder app = new StringBuilder();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(application)) {
                for (int j = 0; j < i; j++) {
                    app.append(arr[j]).append("/");
                }
                app.append(application);
            }
        }
        return app.toString();
    }

    /**
     * @param psiElement
     * @return 当前文件名
     */
    public static String getCurFileName(PsiElement psiElement) {
        return psiElement.getContainingFile().getVirtualFile().getName();
    }


    public static String getKeyWithCase(Collection<String> allKeys, String key) {
        for (String item : allKeys) {
            if (item.equals(key)) {
                return item;
            }
        }
        for (String item : allKeys) {
            if (item.toLowerCase().equals(key.toLowerCase())) {
                return item;
            }
        }
        return key;
    }

    public static Boolean isHintMethod(PsiElement param, MethodMatcher.CallToSignature[] Signatures, int paramIndex, boolean compareClass) {
        PsiElement methodRef = param.getParent().getParent();
        compareClass = compareClass ? compareClass : false;
        if (!(methodRef instanceof MethodReference)) return false;
        String name = ((MethodReference) methodRef).getName();
        List<MethodMatcher.CallToSignature> list = new ArrayList<>();
        for (MethodMatcher.CallToSignature signature : Signatures) {
            String method = signature.getMethod();
            if (method.equals(name)) {
                if (!compareClass) return PsiElementUtil.isFunctionReference(param, name, paramIndex);
                list.add(signature);
            }
        }
        if (list.size() == 0) return false;
        MethodMatcher.CallToSignature[] newSignatures = new MethodMatcher.CallToSignature[list.size()];
        for (int i = 0; i < list.size(); i++) {
            newSignatures[i] = list.get(i);
        }
        boolean res = MethodMatcher.getMatchedSignatureWithDepth(param, newSignatures, paramIndex) != null;
        return res;
    }

    public static void setConfig(String settingfile) {
        Config config = null;
        try {
            String str = "";
            File file = new File(settingfile);//定义一个file对象，用来初始化FileReader
            FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
            BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
            StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
            String s = "";
            while ((s = bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
                sb.append(s + "\r\n");//将读取的字符串添加换行符后累加存放在缓存中
            }
            bReader.close();
            str = sb.toString();
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            config = mapper.readValue(str, Config.class);
        } catch (UnrecognizedPropertyException e1) {
            System.out.println("配置文件格式不对");
        } catch (Exception e) {
            System.out.println("读取配置失败");
        }
        if (config != null) Setting.config = config;
    }

    /**
     * 是否是配置的提示方法,
     */
    public static boolean isConfigMethod(PsiElement element, int type) {
        String[] methodStrs;
        if (type == 1) {
            methodStrs = Setting.config.dbMethod;
        } else {
            methodStrs = Setting.config.dbArrMethod;
        }
        if (methodStrs == null) return false;
        for (String item : methodStrs) {
            int index = 0;
            String methodStr = item;
            if (item.contains(".")) {
                String[] method = item.split("\\.");
                if (method.length == 2) {
                    try {
                        index = Integer.valueOf(method[1]) - 1;
                    } catch (Exception e) {
                    }
                    if (index < 0) index = 0;
                    methodStr = method[0];
                }
            }
            if (PsiElementUtil.isFunctionReference(element, methodStr, index)) {
                return true;
            }
        }
        return false;
    }

    public static String formatLog(String str){
        if(str==null)return "";
        str=str.replace("[","\r\n[");
        str=str.replace("]","]\r\n");
        str=str.replace("\r\n \r\n","\r\n");
        str=str.replace("',  '","',\r\n'");
        return str;
    }
}
