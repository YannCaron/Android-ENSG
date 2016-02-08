package eu.ensg.spatialite;

import android.util.Log;

import jsqlite.*;

/**
 * Created by cyann on 06/02/16.
 */
public class SpatialiteDatabase extends Database {

    public String dumpQuery(String query) throws jsqlite.Exception {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Execute query: ").append(query).append("\n\n");

        Stmt stmt = prepare(query);

        int maxColumns = stmt.column_count();

        for (int i = 0; i < maxColumns; i++) {
            if (i != 0) stringBuilder.append(" | ");
            stringBuilder.append(stmt.column_name(i));
        }
        stringBuilder.append("\n--------------------------------------------\n");

        int rowIndex = 0;
        while (stmt.step()) {
            for (int i = 0; i < maxColumns; i++) {
                if (i != 0) stringBuilder.append(" | ");
                stringBuilder.append(stmt.column_string(i));
            }
            stringBuilder.append("\n");

            if (rowIndex++ > 100) break;
        }
        stringBuilder.append("\n--------------------------------------------\n");
        stmt.close();

        stringBuilder.append("\ndump done\n");

        return stringBuilder.toString();
    }

    public void exec(String sql) throws jsqlite.Exception {
        Log.i(this.getClass().getName(), "Execute query: " + sql);
        exec(sql, new Callback() {
            @Override
            public void columns(String[] coldata) {
            }

            @Override
            public void types(String[] types) {
            }

            @Override
            public boolean newrow(String[] rowdata) {
                return false;
            }
        });
        Log.i(this.getClass().getName(), "Query executed successfully !");
    }

    @Override
    public Stmt prepare(String sql) throws jsqlite.Exception {
        Log.i(this.getClass().getName(), "Prepare query: " + sql);
        Stmt stmt = super.prepare(sql);
        Log.i(this.getClass().getName(), "Query prepared successfully !");
        return stmt;
    }
}
