package pers.fw.tplugin.db;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Tables {


    public Map<String, HashSet<String>> getTables() {
        return tables;
    }

    private Map<String, HashSet<String>> tables;

    public Tables() {
        this.tables = new HashMap<>();
    }

    public void put(String table) {
        this.put(table, null);
    }

    public void put(String table, String alias) {
        if (tables.containsKey(table)) {
            HashSet<String> aliases = tables.get(table);
            if (alias != null)
                aliases.add(alias);
        } else {
            HashSet<String> aliases = new HashSet<>();
            if (alias != null)
                aliases.add(alias);
            tables.put(table, aliases);
        }
    }

}
