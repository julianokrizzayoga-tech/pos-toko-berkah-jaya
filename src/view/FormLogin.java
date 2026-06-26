package view;

import koneksi.Koneksi;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class FormLogin extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public FormLogin() {
        initUI();
    }

    private void initUI() {
        setTitle("Login - Toko Berkah Jaya");
        setSize(420, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Color primary = new Color(79, 70, 229);
        getContentPane().setBackground(new Color(248, 250, 252));
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setPreferredSize(new Dimension(420, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        JLabel lblTitle = new JLabel("TOKO BERKAH JAYA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("Sistem Manajemen Toko");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(196, 181, 253));
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(primary);
        titlePanel.add(lblTitle);
        titlePanel.add(lblSub);
        header.add(titlePanel, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(248, 250, 252));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblU = new JLabel("Username:");
        lblU.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(lblU, gbc);

        gbc.gridy = 1;
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(txtUsername, gbc);

        gbc.gridy = 2;
        JLabel lblP = new JLabel("Password:");
        lblP.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(lblP, gbc);

        gbc.gridy = 3;
        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(txtPassword, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(16, 0, 0, 0);
        JButton btnLogin = new JButton("MASUK");
        btnLogin.setBackground(primary);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> doLogin());
        form.add(btnLogin, gbc);
        getRootPane().setDefaultButton(btnLogin);
        add(form, BorderLayout.CENTER);
    }

    private void doLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi!");
            return;
        }

        try {
            Connection conn = Koneksi.getConnection();
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM tb_user WHERE username=? AND password=?");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int idUser   = rs.getInt("id_user");
                String nama  = rs.getString("nama_lengkap");
                String level = rs.getString("level");
                dispose();
                new FormMain(idUser, nama, level).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Username atau password salah!", "Login Gagal",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new FormLogin().setVisible(true));
    }
}