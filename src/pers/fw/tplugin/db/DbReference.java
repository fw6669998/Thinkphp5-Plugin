package pers.fw.tplugin.db;

import com.intellij.codeInsight.lookup.*;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasTable;
import com.intellij.database.view.ui.DbTableDialog;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.containers.JBIterable;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.*;
import icons.DatabaseIcons;
import pers.fw.tplugin.beans.Setting;
import pers.fw.tplugin.inter.GotoCompletionContributor;
import pers.fw.tplugin.inter.GotoCompletionLanguageRegistrar;
import pers.fw.tplugin.inter.GotoCompletionProvider;
import pers.fw.tplugin.inter.GotoCompletionRegistrarParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pers.fw.tplugin.util.*;

import java.util.*;

public class DbReference implements GotoCompletionLanguageRegistrar {
    private static MethodMatcher.CallToSignature[] QUERY = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "where"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "group"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "avg"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "count"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "sum"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "max"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "min"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "field"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "order"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "value"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereOr"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereXor"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereNull"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereNotNull"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereExists"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereNotExists"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereIn"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereNotIn"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereLike"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereNotLike"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereBetween"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereNotBetween"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereExp"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "whereTime"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "withField"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "group"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "having"),
//            new MethodMatcher.CallToSignature("\\think\\db\\Query", "value"),
    };

    private static MethodMatcher.CallToSignature[] QUERYARR = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "delete"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "update"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "insert"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "where"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "order"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "data"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "insertGetId"),
            new MethodMatcher.CallToSignature("\\think\\Model", "delete"),
            new MethodMatcher.CallToSignature("\\think\\Model", "update"),
            new MethodMatcher.CallToSignature("\\think\\Model", "insert"),
            new MethodMatcher.CallToSignature("\\think\\Model", "data"),
            new MethodMatcher.CallToSignature("\\think\\Model", "save"),
    };

    private static MethodMatcher.CallToSignature[] QUERYTABLE = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\Db", "table"),
            new MethodMatcher.CallToSignature("\\think\\Db", "join"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "table"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "getTableInfo"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "getPk"),
    };

    private static MethodMatcher.CallToSignature[] QUERYNAME = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\Db", "name"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "name"),
    };

    private static MethodMatcher.CallToSignature[] join = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join"),
    };

    //是否进行类型比较
    public static Boolean compareClass = true;


    public static boolean isHintVar(PsiElement param) {
        String text = param.getParent().getParent().getText();
        if (text.startsWith("$field") || text.startsWith("$where") || text.startsWith("$row")) {
            return true;
        } else {
            String[] dbVar = Setting.config.dbVar;
            if (dbVar == null) {
                return false;
            }
            for (String item : dbVar) {
                if (text.startsWith("$" + item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean support(@NotNull Language language) {
        return PhpLanguage.INSTANCE == language;
    }

    @Override
    public void register(GotoCompletionRegistrarParameter registrar) {
        registrar.register(PlatformPatterns.psiElement(), new GotoCompletionContributor() {
            @Nullable
            @Override
            public GotoCompletionProvider getProvider(@Nullable PsiElement psiElement) {
                if (psiElement == null) {
                    return null;
                }

                PsiElement param = psiElement.getParent();

                if (!(param instanceof StringLiteralExpression)) return null;
                if (Util.isHintMethod(param, QUERY, 0, compareClass) || Util.isHintMethod(param, join, 1, compareClass)
                        || isHintVar(param) || Util.isConfigMethod(param, 1)
                ) {
                    //列
                    return new ColumnProvider(param);
                } else if (Util.isHintMethod(param, QUERYTABLE, 0, compareClass)) {
                    //表
                    return new TableProvider(param, 1);
                } else if (PsiElementUtil.isFunctionReference(param, "db", 0) || Util.isHintMethod(param, QUERYNAME, 0, compareClass)) {
                    //表, name
                    return new TableProvider(param, 2);
                } else {
                    //列, 数组里的列
                    PsiElement param1 = null;
                    try {
                        param1 = param.getParent().getParent();
                        if (!(param1 instanceof ArrayCreationExpression))
                            param1 = param1.getParent();
                        if (!(param1 instanceof ArrayCreationExpression))
                            return null;
                    } catch (Exception e) {
                        return null;
                    }
                    if (Util.isHintMethod(param1, QUERYARR, 0, compareClass) || Util.isConfigMethod(param1, 2)) {
                        return new ColumnProvider(param1);
                    }
                }

                return null;
            }
        });
    }

    public static class ColumnProvider extends GotoCompletionProvider {
        public ColumnProvider(PsiElement element) {
            super(element);
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<>();

            HashSet<String> tables = new HashSet<>();

            //收集表
            DbTableUtil.collectionTableByCurFile(getElement(), tables);
            DbTableUtil.collectionTableByModel(getElement(), tables);
            DbTableUtil.collectionTableByContext(getElement(), tables);

            //获取列
            HashMap<String, Column> cols = new HashMap<String, Column>();
            for (String table : tables) {
                JBIterable<? extends DasColumn> columns = DbTableUtil.getColumns(getElement().getProject(), table);
                if (columns != null)
                    for (DasColumn column : columns) {
                        String comment = column.getComment();
                        if (comment == null) comment = "";
                        Column column1 = cols.get(column.getName());
                        if (column1 == null) {
                            cols.put(column.getName(), new Column(comment, table));
                        } else {
                            column1.comment = column1.comment + " | " + comment;
                            column1.table = column1.table + "|" + table;
                        }
                    }
            }
            for (String item : cols.keySet()) {
                Column column = cols.get(item);
                lookupElements.add(LookupElementBuilder.create(item)
                        .withTailText("   " + column.comment).withTypeText(column.table)
                        .withBoldness(true).withIcon(DatabaseIcons.Col));
            }
            return lookupElements;
        }

        @NotNull
        public Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor) {
            return new ArrayList<>();
        }
    }

    private static class TableProvider extends GotoCompletionProvider {
        int type = 1;

        public TableProvider(PsiElement element, int type) {
            super(element);
            this.type = type;
        }

        @NotNull
        @Override
        public Collection<LookupElement> getLookupElements() {
            final Collection<LookupElement> lookupElements = new ArrayList<>();
            JBIterable<? extends DasTable> tables = DbTableUtil.getTables(getElement().getProject());
            for (DasTable table : tables) {
                String comment = "";
                if (table.getComment() != null)
                    comment = table.getComment();
                String tableStr = table.getName();
                lookupElements.add(LookupElementBuilder.create(tableStr).withTailText("   " + comment).withIcon(DatabaseIcons.Table));
                if (this.type == 2) {
                    int index = tableStr.indexOf("_");
                    if (index != -1) {
                        tableStr = tableStr.substring(index+1);
                        lookupElements.add(LookupElementBuilder.create(tableStr).withTailText("   " + comment).withIcon(DatabaseIcons.Table));
                    }
                }
            }
            return lookupElements;
        }

        @NotNull
        public Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor) {
            return new ArrayList<>();
        }
    }
}
