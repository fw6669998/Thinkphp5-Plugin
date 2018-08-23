package db;

import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasTable;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.JBIterable;
import util.MethodMatcher;

import java.util.List;

public class DbTableUtil {

    public static boolean isQueryFun(PsiElement element) {
//        if ()
            return true;
    }


    public static JBIterable<? extends DasColumn> getColumns(Project project, String table) {
        JBIterable<? extends DasTable> tables = getTables(project);
        if (tables == null) return null;
        for (DasTable item : tables) {
            if (item.getName().equals(table)) {
                return DasUtil.getColumns(item);
            }
        }
        return null;
    }

    public static JBIterable<? extends DasTable> getTables(Project project) {
        JBIterable<DbDataSource> dataSources = DbUtil.getDataSources(project);
        DbDataSource dbDataSource = dataSources.get(0);
        if (dbDataSource == null) {
            return null;
        } else {
            JBIterable<? extends DasTable> tables = DasUtil.getTables(dbDataSource);
            return tables;
        }
    }
}
