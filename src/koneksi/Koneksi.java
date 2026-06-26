package koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB   = "db_toko_berkah";
    private static final String USER = "root";
    private static final String PASS = "";

    private static Connection conn = null;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                String url = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB
                           + "?useSSL=false&serverTimezone=Asia/Jakarta";
                conn = DriverManager.getConnection(url, USER, PASS);
                System.out.println("Koneksi MySQL berhasil!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Gagal koneksi: " + e.getMessage());
        }
        return conn;
    }
}