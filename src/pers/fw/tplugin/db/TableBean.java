package pers.fw.tplugin.db;

public class TableBean {

    public String table;
    public String alias;

    public TableBean(String table, String alias) {
        this.table = table;
        this.alias = alias;
    }

    public TableBean(String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
