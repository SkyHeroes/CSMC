package dev.danablend.counterstrike.database;


import dev.danablend.counterstrike.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.sql.*;


public class SQLiteConnection {
    private final File path;
    private String BDName;


    public SQLiteConnection(File path, String BDName) {
        this.path = path;
        this.BDName = BDName;
        initDB();
    }

    /**
     * Connect to the database
     *
     * @return the Connection object
     */
    public Connection connect() {
        File dataFolder = path;
        String url = "jdbc:sqlite:" + dataFolder.getPath() + "/" + BDName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            Utils.debug("Coul not start driver " + e.getMessage());
        }
        return conn;
    }


    public String select(String sql) {

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String result = "";
                try {
                    result = rs.getString(1);
                    result = result + "," + rs.getString(2);
                    result = result + "," + rs.getString(3);
                    result = result + "," + rs.getString(4);
                    result = result + "," + rs.getString(5);
                    result = result + "," + rs.getString(6);
                } catch (Exception e) {
                }

                return result;
            }

        } catch (SQLException e) {
            Utils.debug(sql + "  Error in select: " + e.getMessage());
        }

        return null;
    }


    public Integer checkLock(String sql) {

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            Utils.debug(sql + "  Error in checklock:   " + e.getMessage());
        }

        return -1;
    }


    private void initDB() {
        File saveTo = new File(path, BDName);

        if (!saveTo.exists()) {
            createNewDatabase();
            createNewTable();
        } else {
            maintainDB();
        }
    }


    private void createNewDatabase() {

        try (Connection conn = connect()) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                Utils.debug("The driver name is " + meta.getDriverName());
                Utils.debug("A new database has been created.");
            } else {
                Utils.debug("Failed to create connection");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private void createNewTable() {

        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS mundos (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	nome VARCHAR(255) NOT NULL,\n"
                + "	modoCs TINYINT\n"
                + ");";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);

        } catch (SQLException e) {
            Utils.debug("Error initing DB " + e.getMessage());
        }

        sql = "CREATE TABLE CSMaps (\n"
                + "id	INTEGER NOT NULL,\n"
                + "Descr	varchar(50) NOT NULL,\n"
                + "SpawnLobby	TEXT,\n"
                + "SpawnTerrorists	TEXT,\n"
                + "SpawnCounter	TEXT,\n"
                + "PRIMARY KEY(id AUTOINCREMENT)\n);";

        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        for (World w : Bukkit.getWorlds()) {
            checkLock("insert into mundos (nome,modocs) values ('" + w.getName() + "','false')");
        }

    }

    private void maintainDB() {

       String resultado = select("SELECT COUNT(*) AS CNTREC FROM pragma_table_info('CSMaps') WHERE name='A'");

        if (Integer.parseInt(resultado) ==0) {
            String sql = "ALTER TABLE CSMaps ADD A TEXT;";
            try (Connection conn = this.connect();
                 Statement stmt = conn.createStatement()) {
                // create a new table
                stmt.execute(sql);

            } catch (SQLException e) {
                Utils.debug("Error initing DB " + e.getMessage());
            }

             sql = "ALTER TABLE CSMaps ADD B TEXT;";
            try (Connection conn = this.connect();
                 Statement stmt = conn.createStatement()) {
                // create a new table
                stmt.execute(sql);

            } catch (SQLException e) {
                Utils.debug("Error initing DB " + e.getMessage());
            }
        }

    }
}
