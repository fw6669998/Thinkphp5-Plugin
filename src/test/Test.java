package test;

import com.intellij.database.DatabaseMessages;
import com.intellij.database.dialects.DatabaseDialect;
import com.intellij.database.model.*;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiManager;
import com.intellij.database.util.DasUtil;
import com.intellij.database.util.DbUtil;
import com.intellij.database.util.JdbcUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.containers.JBIterable;
import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSAUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import util.Tool;

import java.util.List;

public class Test extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        System.out.println("start");
        JBIterable<DbDataSource> dataSources = DbUtil.getDataSources(e.getProject());
        List<DbDataSource> dbDataSources = dataSources.toList();
        for (DbDataSource source : dbDataSources) {
            DatabaseSystem delegate = source.getDelegate();
            JBIterable<? extends DasTable> tables = DasUtil.getTables(delegate);
            List<? extends DasTable> dasTables = tables.toList();
            for (DasTable item : dasTables) {
                JBIterable<? extends DasColumn> columns = DasUtil.getColumns(item);
                for (DasColumn col : columns) {
                    String name = col.getName();
                    System.out.println(name);
                    System.out.println(col.getComment());
                }
            }
        }
    }
}
