package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormUser extends JPanel {

    private JTextField txtNama, txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbLevel;
    private JTable table;
    private DefaultTableModel tableModel;
    private int selectedUserId = -1;

    // ── FIX: flag apakah user yang login adalah SuperAdmin ──
    private final boolean isSuperAdmin;

    // ── FIX: indeks kolom Password & tombol mata (hanya valid jika isSuperAdmin) ──
    private static final int COL_PASSWORD = 4;
    private static final int COL_EYE = 5;

    // ── FIX: status tampil/sembunyi password per baris ──
    private final List<Boolean> passwordVisible = new ArrayList<>();

    public FormUser() {
        this("");
    }

    // ── FIX: constructor menerima level user yang sedang login ──
    public FormUser(String levelUser) {
        this.isSuperAdmin = "SuperAdmin".equalsIgnoreCase(levelUser);
        initUI();
        loadTable();
        AppTheme.registerComponent(this);
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);

        JPanel mainBody = new JPanel(new BorderLayout(0, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        add(mainBody, BorderLayout.CENTER);

        // ── Panel Form Kiri ──────────────────────────────────
        JPanel formOuter = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        formOuter.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));
        formOuter.setPreferredSize(new Dimension(320, 0));

        JPanel formCard = new JPanel(new GridBagLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0; gbc.gridy = 0;

        JLabel lblForm = new JLabel("Tambah / Kelola User") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.PRIMARY);
                super.paintComponent(g);
            }
        };
        lblForm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        formCard.add(lblForm, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 16, 0);
        JSeparator sep = new JSeparator() {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.BORDER);
                super.paintComponent(g);
            }
        };
        formCard.add(sep, gbc);

        txtNama     = AppTheme.makeInput("Nama Lengkap");
        txtUsername = AppTheme.makeInput("Username");

        // ── FIX: txtPassword pakai paintComponent agar ikut dark mode ──
        txtPassword = new JPasswordField() {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.INPUT_BG);
                setForeground(AppTheme.TEXT_PRIMARY);
                setCaretColor(AppTheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
                super.paintComponent(g);
            }
        };
        txtPassword.setFont(AppTheme.FONT_BODY);
        txtPassword.setToolTipText("Password (Isi jika ingin ganti)");

        cmbLevel = AppTheme.makeCombo();
        for (String lvl : new String[]{"SuperAdmin", "Admin", "Kasir", "Gudang"}) cmbLevel.addItem(lvl);
        cmbLevel.setPreferredSize(new Dimension(0, 36));

        gbc.insets = new Insets(0, 0, 12, 0);
        gbc.gridy++; formCard.add(fieldRow("Nama Lengkap", txtNama), gbc);
        gbc.gridy++; formCard.add(fieldRow("Username", txtUsername), gbc);
        gbc.gridy++; formCard.add(fieldRow("Password (Isi jika ingin ganti)", txtPassword), gbc);

        gbc.gridy++;
        JPanel pLevel = new JPanel(new BorderLayout(0, 5)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        JLabel lblLevel = new JLabel("Level / Role") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        lblLevel.setFont(AppTheme.FONT_LABEL);
        pLevel.add(lblLevel, BorderLayout.NORTH);
        pLevel.add(cmbLevel, BorderLayout.CENTER);
        formCard.add(pLevel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(8, 0, 0, 0);
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 8, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };

        JButton btnSimpan    = AppTheme.makeSuccessBtn("✔ Simpan");
        JButton btnUpdate    = AppTheme.makePrimaryBtn("✏ Update");
        JButton btnHapus     = AppTheme.makeDangerBtn("🗑 Hapus");
        JButton btnBersihkan = AppTheme.makeNeutralBtn("↺ Reset");

        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());

        btnGrid.add(btnSimpan); btnGrid.add(btnUpdate);
        btnGrid.add(btnHapus);  btnGrid.add(btnBersihkan);
        formCard.add(btnGrid, gbc);

        gbc.gridy++; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formCard.add(Box.createVerticalGlue(), gbc);

        formOuter.add(formCard, BorderLayout.CENTER);
        mainBody.add(formOuter, BorderLayout.WEST);

        // ── Panel Tabel Kanan ────────────────────────────────
        JPanel tableOuter = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        tableOuter.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));

        JPanel tableCard = new JPanel(new BorderLayout(0, 10)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JPanel tblHeader = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        tblHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel lblDaftar = new JLabel("Daftar User Terdaftar") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblDaftar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tblHeader.add(lblDaftar, BorderLayout.WEST);
        tableCard.add(tblHeader, BorderLayout.NORTH);

        // ── FIX: tambahkan kolom Password & 👁 hanya untuk SuperAdmin ──
        String[] cols = isSuperAdmin
            ? new String[]{"ID", "Nama Lengkap", "Username", "Level", "Password", "👁"}
            : new String[]{"ID", "Nama Lengkap", "Username", "Level"};

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        // ── FIX: pakai styleTable dari AppTheme ──
        AppTheme.styleTable(table);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(250);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);

        // Warna level — tetap cerah biar mudah dibedakan di dark mode
        final Color C_SUPERADMIN = new Color(109, 40, 217);
        final Color C_ADMIN      = new Color(25, 118, 210);
        final Color C_KASIR      = new Color(16, 185, 129);
        final Color C_GUDANG     = new Color(180, 83, 9);

        // ── FIX: renderer tetap pakai AppTheme untuk bg/fg ──
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                setFont(AppTheme.FONT_BODY);
                if (!sel) {
                    setBackground(row % 2 == 0 ? AppTheme.BG_CARD : AppTheme.ROW_ALT);
                    setForeground(AppTheme.TEXT_PRIMARY);
                    if (col == 3 && v != null) {
                        switch (v.toString()) {
                            case "SuperAdmin": setForeground(C_SUPERADMIN); setFont(AppTheme.FONT_BOLD); break;
                            case "Admin":      setForeground(C_ADMIN);      setFont(AppTheme.FONT_BOLD); break;
                            case "Kasir":      setForeground(C_KASIR);      setFont(AppTheme.FONT_BOLD); break;
                            case "Gudang":     setForeground(C_GUDANG);     setFont(AppTheme.FONT_BOLD); break;
                        }
                    }
                } else {
                    setBackground(AppTheme.PRIMARY_BG);
                    setForeground(AppTheme.TEXT_PRIMARY);
                }
                return this;
            }
        });

        // ── FIX: kolom khusus SuperAdmin: Password (tersensor) & tombol mata ──
        if (isSuperAdmin) {
            table.getColumnModel().getColumn(COL_PASSWORD).setPreferredWidth(140);
            table.getColumnModel().getColumn(COL_EYE).setPreferredWidth(40);
            table.getColumnModel().getColumn(COL_EYE).setMaxWidth(40);

            // Renderer kolom Password: tampilkan asli jika "dibuka", selain itu tersensor
            table.getColumnModel().getColumn(COL_PASSWORD).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                        BorderFactory.createEmptyBorder(0, 10, 0, 10)
                    ));
                    setFont(AppTheme.FONT_BODY);
                    setHorizontalAlignment(LEFT);
                    boolean visible = row < passwordVisible.size() && passwordVisible.get(row);
                    setText(visible ? (v == null ? "" : v.toString()) : "••••••••");
                    if (!sel) {
                        setBackground(row % 2 == 0 ? AppTheme.BG_CARD : AppTheme.ROW_ALT);
                        setForeground(AppTheme.TEXT_PRIMARY);
                    } else {
                        setBackground(AppTheme.PRIMARY_BG);
                        setForeground(AppTheme.TEXT_PRIMARY);
                    }
                    return this;
                }
            });

            // Renderer kolom 👁: ikon mata berubah sesuai status tampil/sembunyi
            table.getColumnModel().getColumn(COL_EYE).setCellRenderer(new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)
                    ));
                    setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                    setHorizontalAlignment(CENTER);
                    boolean visible = row < passwordVisible.size() && passwordVisible.get(row);
                    setText(visible ? "🙈" : "👁");
                    setToolTipText(visible ? "Sembunyikan password" : "Lihat password");
                    if (!sel) {
                        setBackground(row % 2 == 0 ? AppTheme.BG_CARD : AppTheme.ROW_ALT);
                        setForeground(AppTheme.TEXT_PRIMARY);
                    } else {
                        setBackground(AppTheme.PRIMARY_BG);
                        setForeground(AppTheme.TEXT_PRIMARY);
                    }
                    return this;
                }
            });

            // Klik pada kolom 👁 → toggle tampil/sembunyi password baris tersebut
            table.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    int viewCol = table.columnAtPoint(e.getPoint());
                    if (viewRow < 0 || viewCol != COL_EYE) return;
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    while (passwordVisible.size() <= modelRow) passwordVisible.add(false);
                    passwordVisible.set(modelRow, !passwordVisible.get(modelRow));
                    table.repaint();
                }
            });
        }

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForm();
        });

        // ── FIX: pakai makeScrollPane dari AppTheme ──
        JScrollPane scrollPane = AppTheme.makeScrollPane(table);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // ── FIX: legend panel background ikut dark mode ──
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        legend.setOpaque(true);
        legend.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));
        legend.add(legendItem("● SuperAdmin", C_SUPERADMIN));
        legend.add(legendItem("● Admin",      C_ADMIN));
        legend.add(legendItem("● Kasir",      C_KASIR));
        legend.add(legendItem("● Gudang",     C_GUDANG));
        tableCard.add(legend, BorderLayout.SOUTH);

        tableOuter.add(tableCard, BorderLayout.CENTER);
        mainBody.add(tableOuter, BorderLayout.CENTER);
    }

    private JPanel fieldRow(String labelText, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        JLabel lbl = new JLabel(labelText) {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        lbl.setFont(AppTheme.FONT_LABEL);
        p.add(lbl, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BOLD);
        lbl.setForeground(color);
        return lbl;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        passwordVisible.clear();

        // ── FIX: SuperAdmin ikut ambil kolom password dari DB ──
        String sql = isSuperAdmin
            ? "SELECT id_user, nama_lengkap, username, level, password FROM tb_user ORDER BY id_user"
            : "SELECT id_user, nama_lengkap, username, level FROM tb_user ORDER BY id_user";

        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                if (isSuperAdmin) {
                    tableModel.addRow(new Object[]{
                        rs.getInt("id_user"), rs.getString("nama_lengkap"),
                        rs.getString("username"), rs.getString("level"),
                        rs.getString("password"), ""
                    });
                    passwordVisible.add(false);
                } else {
                    tableModel.addRow(new Object[]{
                        rs.getInt("id_user"), rs.getString("nama_lengkap"),
                        rs.getString("username"), rs.getString("level")
                    });
                }
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error load data: " + e.getMessage()); }
    }

    private void isiForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedUserId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        txtNama.setText(tableModel.getValueAt(row, 1).toString());
        txtUsername.setText(tableModel.getValueAt(row, 2).toString());
        txtPassword.setText("");
        cmbLevel.setSelectedItem(tableModel.getValueAt(row, 3).toString());
    }

    private boolean isUsernameDuplicated(String username, int id) {
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM tb_user WHERE username=? AND id_user <> ?")) {
            ps.setString(1, username); ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Validasi Gagal: " + e.getMessage()); }
        return false;
    }

    private void simpan() {
        String nama = txtNama.getText().trim(), username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) { JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }
        if (!nama.matches("[A-Za-z\\s]+")) { JOptionPane.showMessageDialog(this, "Nama hanya boleh huruf dan spasi.", "Validasi", JOptionPane.ERROR_MESSAGE); return; }
        if (!username.matches("[A-Za-z0-9._]+")) { JOptionPane.showMessageDialog(this, "Username hanya boleh huruf, angka, titik, dan underscore.", "Validasi", JOptionPane.ERROR_MESSAGE); return; }
        if (password.length() < 4) { JOptionPane.showMessageDialog(this, "Password minimal 4 karakter.", "Validasi", JOptionPane.ERROR_MESSAGE); return; }
        if (isUsernameDuplicated(username, -1)) { JOptionPane.showMessageDialog(this, "Username sudah terpakai!", "Gagal", JOptionPane.WARNING_MESSAGE); return; }
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO tb_user (username, password, nama_lengkap, level) VALUES (?,?,?,?)")) {
            ps.setString(1, username); ps.setString(2, password); ps.setString(3, nama); ps.setString(4, cmbLevel.getSelectedItem().toString());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil disimpan.");
            loadTable(); bersihkan();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void update() {
        if (selectedUserId == -1) { JOptionPane.showMessageDialog(this, "Pilih data di tabel dulu!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }
        String nama = txtNama.getText().trim(), username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        if (nama.isEmpty() || username.isEmpty()) { JOptionPane.showMessageDialog(this, "Nama & Username wajib diisi!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }
        if (!nama.matches("[A-Za-z\\s]+")) { JOptionPane.showMessageDialog(this, "Nama hanya boleh huruf dan spasi.", "Validasi", JOptionPane.ERROR_MESSAGE); return; }
        if (!username.matches("[A-Za-z0-9._]+")) { JOptionPane.showMessageDialog(this, "Username hanya boleh huruf, angka, titik, dan underscore.", "Validasi", JOptionPane.ERROR_MESSAGE); return; }
        if (!password.isEmpty() && password.length() < 4) { JOptionPane.showMessageDialog(this, "Password minimal 4 karakter.", "Validasi", JOptionPane.ERROR_MESSAGE); return; }
        if (isUsernameDuplicated(username, selectedUserId)) { JOptionPane.showMessageDialog(this, "Username sudah digunakan user lain!", "Gagal", JOptionPane.WARNING_MESSAGE); return; }
        String sql = password.isEmpty() ?
            "UPDATE tb_user SET nama_lengkap=?, username=?, level=? WHERE id_user=?" :
            "UPDATE tb_user SET nama_lengkap=?, username=?, level=?, password=? WHERE id_user=?";
        try (Connection conn = Koneksi.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama); ps.setString(2, username); ps.setString(3, cmbLevel.getSelectedItem().toString());
            if (password.isEmpty()) ps.setInt(4, selectedUserId); else { ps.setString(4, password); ps.setInt(5, selectedUserId); }
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil diperbarui.");
            loadTable(); bersihkan();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void hapus() {
        if (selectedUserId == -1) { JOptionPane.showMessageDialog(this, "Pilih user yang akan dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE); return; }
        if (JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus user ini?", "Hapus", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tb_user WHERE id_user=?")) {
            ps.setInt(1, selectedUserId); ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
            loadTable(); bersihkan();
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void bersihkan() {
        txtNama.setText(""); txtUsername.setText(""); txtPassword.setText("");
        cmbLevel.setSelectedIndex(0); selectedUserId = -1;
        table.clearSelection(); txtNama.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            JFrame frame = new JFrame("Test Kelola User");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 650);
            // ── FIX: contoh testing sebagai SuperAdmin ──
            frame.add(new FormUser("SuperAdmin"));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}