package pers.fw.tplugin.db;

import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl;
import pers.fw.tplugin.beans.ArrayMapVisitor;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.JBIterable;
import com.jetbrains.php.lang.psi.elements.Method;
import pers.fw.tplugin.util.*;

import java.util.*;

public class DbTableUtil {

    public static String[] getPossibleTables() {


        return null;
    }

    //根据模型表获取列,
    public static JBIterable<? extends DasColumn> getColumns(Project project, String table, int type) {
        if (table.isEmpty()) return null;
        table = table.replace("'", "").replace("\"", "");
        JBIterable<? extends DasTable> tables = getTables(project);
        if (tables == null) return null;
        if (type == 1) {        //有前缀
            for (DasTable item : tables) {
                String tableName = item.getName();
                int i = item.getName().indexOf("_");
                if (i == -1) {
                    if (tableName.equals(table)) {
                        return DasUtil.getColumns(item);
                    }
                } else {
                    String name = tableName.substring(i + 1, tableName.length());
                    if (name.equals(table)) {
                        return DasUtil.getColumns(item);
                    }
                }
            }
        } else if (type == 2) { //无前缀
            for (DasTable item : tables) {
                if (item.getName().equals(table)) {
                    return DasUtil.getColumns(item);
                }
            }
        }
        return null;
    }

    public static JBIterable<? extends DasColumn> getColumns(Project project, String table) {
        if (table == null) return null;
        try {
            JBIterable<? extends DasTable> tables = getTables(project);
            for (DasTable item : tables) {
                if (table.equals(item.getName())) {
                    return DasUtil.getColumns(item);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    //获取所有表
    public static JBIterable<? extends DasTable> getTables(Project project) {

        JBIterable<DbDataSource> dataSources = DbUtil.getDataSources(project);
        if (dataSources.size() < 1) {
            return null;
        } else {
            DbDataSource work = null;
            for (DbDataSource db : dataSources) {
                if (db.getName().contains("work")) {
                    work = db;
                    break;
                }
            }
            if (work != null) {
                return DasUtil.getTables(work.getDelegate());
            } else {
                if (dataSources.get(0) == null) return null;
                return DasUtil.getTables(dataSources.get(0).getDelegate());
            }
        }
    }

//    public static String getTableName(PsiElement element) {
//        PsiElement parent = element.getParent();
//        if (parent == null) return null;
//        MethodReferenceImpl parent1 = (MethodReferenceImpl) parent.getParent();
//        if (parent1 == null) return null;
//        parent1.getClassReference();
//        return "";
//    }

    //    private static final Pattern aliasPattern = Pattern.compile(".*/application/(\\w+)/controller/(\\w+).php$");
    //从上下文中获取表
    public static void collectionTableByContext(PsiElement element, HashSet<String> tables) {
        Map<String, String> alias = new HashMap<>();
        Method method = Util.getMethod(element);
        if (method == null) return;
        method.acceptChildren(new TablesVisitor(new ArrayMapVisitor() {
            @Override
            public void visit(String key, String value) {
                tables.add(key);
            }
        }, tables));
        return;
    }

    public static void collectionTableByCurFile(PsiElement psiElement, HashSet<String> tables) {

        PhpClassImpl phpClass = Util.getPhpClass(psiElement);
        Project project = psiElement.getProject();

        if (phpClass != null) {
            Collection<Field> fields = phpClass.getFields();
            for (Field item : fields) {
                if ("name".equals(item.getName())) {
                    String name = item.getDefaultValue().getText();
                    if (name != null && !name.isEmpty() && !"$name".equals(name)) {
                        tables.add(DbTableUtil.getTableByName(project, name));
                        break;
                    }
                }
                if ("table".equals(item.getName())) {
                    String name = item.getDefaultValue().getText();//item.getDefaultValuePresentation();
                    if (name != null && !name.isEmpty() && !"$table".equals(name)) {
                        name = name.replace("'", "").replace("\"", "");
                        tables.add(name);
                        break;
                    }
                }
            }
        }
    }

    //从模型变量中获取表
    public static void collectionTableByModel(PsiElement psiElement, HashSet<String> tables) {
        //获取方法对象的类
//            PsiElement resolve = ((MethodReference) methodRef).resolve();
        //Query类
//            PhpClassImpl phpClass = Util.getPhpClass(resolve);
        //获取可能出现的表
//            List<TableBean> tables= new ArrayList<>();
//            Tables tables = new Tables();
        PsiElement paramList = psiElement.getParent();
        if (paramList == null) return;
        PsiElement methodRef = paramList.getParent();
        if (!(methodRef instanceof MethodReference)) return;

        Project project = psiElement.getProject();
        //Model子类
        PhpClass phpClass = Util.getInstanseClass(project, (MethodReference) methodRef);  //获取模型类
        String table = Util.getTableByClass(phpClass, project);
        if (table != null) {
            tables.add(table);
        }
//        if (phpClass != null) {
//            Collection<Field> fields = phpClass.getFields();
//            for (Field item : fields) {
//                if ("name".equals(item.getName())) {
//                    String name = item.getDefaultValue().getText();
//                    if (name != null && !name.isEmpty() && !"$name".equals(name)) {
//                        tables.add(DbTableUtil.getTableByName(project, name));
//                        break;
//                    }
//                }
//                if ("table".equals(item.getName())) {
//                    String name = item.getDefaultValue().getText();//item.getDefaultValuePresentation();
//                    if (name != null && !name.isEmpty() && !"$table".equals(name)) {
//                        name = name.replace("'", "").replace("\"", "");
//                        tables.add(name);
//                        break;
//                    }
//                }
//            }
//        }
    }


    public static String getTableByName(Project project, String name) {
        if (name.isEmpty()) return null;
        name = name.replace("'", "").replace("\"", "");
        JBIterable<? extends DasTable> tables = getTables(project);
        if (tables == null) return null;
        for (DasTable item : tables) {
            String tableName = item.getName();
            if (tableName.contains(name)) {
                String prefix = tableName.replace(name, "");
                if (prefix.equals(tableName.substring(0, prefix.length()))) {
                    if (prefix.indexOf("_") == prefix.length() - 1)
                        return tableName;
                }
            }
        }
        return null;
    }
}
