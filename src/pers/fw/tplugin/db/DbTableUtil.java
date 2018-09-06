package pers.fw.tplugin.db;

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

    public static List<Column> getColumns(Project project, String table) {
        JBIterable<? extends DasTable> tables = getTables(project);

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
                return DasUtil.getTables(work);
            } else {
                return DasUtil.getTables(dataSources.get(0));
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
    public static Map<String, String> getAlias(PsiElement element, String contextTable) {
        Map<String, String> alias = new HashMap<>();
        Method method = Util.getMethod(element);
        if (method == null) return alias;
        method.acceptChildren(new MethodRefVisitor(new ArrayMapVisitor() {
            @Override
            public void visit(String key, String value) {
                alias.put(key, value);
            }
        }, contextTable));
        return alias;
    }
}
