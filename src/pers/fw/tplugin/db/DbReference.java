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
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "value")
    };

    private static MethodMatcher.CallToSignature[] QUERYARR = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "delete"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "update"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "insert"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "where"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "order"),
            new MethodMatcher.CallToSignature("\\think\\Model", "delete"),
            new MethodMatcher.CallToSignature("\\think\\Model", "update"),
            new MethodMatcher.CallToSignature("\\think\\Model", "insert"),
    };

    private static MethodMatcher.CallToSignature[] QUERYTABLE = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\Db", "table"),
            new MethodMatcher.CallToSignature("\\think\\Db", "name"),
            new MethodMatcher.CallToSignature("\\think\\Db", "join"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "table"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "name"),
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join"),
    };

    private static MethodMatcher.CallToSignature[] join = new MethodMatcher.CallToSignature[]{
            new MethodMatcher.CallToSignature("\\think\\db\\Query", "join"),
    };

    //是否进行类型比较
    public static Boolean compareClass = true;


    public static boolean isHintVar(PsiElement param) {
        String text = param.getParent().getParent().getText();
        return text.startsWith("$field") || text.startsWith("$where") || text.startsWith("$row");
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
//                Tool.printPsiTree(Util.getMethod(param));

                if (!(param instanceof StringLiteralExpression)) return null;
                if (Util.isHintMethod(param, QUERY, 0, compareClass) || Util.isHintMethod(param, join, 1, compareClass) || isHintVar(param)
                ) {
                    //列
                    return new ColumnProvider(param);
                } else if (PsiElementUtil.isFunctionReference(param, "db", 0) || Util.isHintMethod(param, QUERYTABLE, 0, compareClass)) {
                    //表
                    return new TableProvider(param);
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
                    if (Util.isHintMethod(param1, QUERYARR, 0, compareClass)) {
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
        public TableProvider(PsiElement element) {
            super(element);
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
                lookupElements.add(LookupElementBuilder.create(table.getName()).withTailText("   " + comment).withIcon(DatabaseIcons.Table));
            }
            return lookupElements;
        }

        @NotNull
        public Collection<? extends PsiElement> getPsiTargets(@NotNull PsiElement psiElement, int offset, @NotNull Editor editor) {
            return new ArrayList<>();
        }
    }
}
