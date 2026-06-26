package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FormCustomer extends JPanel {

    private JTextField txtId, txtNama, txtAlamat, txtTelepon, txtCari;
    private JTable table;
    private DefaultTableModel tableModel;

    public FormCustomer() {
        initUI();
        loadTable();
        // Daftarkan ke AppTheme agar ikut refresh saat dark mode toggle
        AppTheme.registerComponent(this);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);

        JPanel body = new JPanel(new BorderLayout(14, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        body.setBackground(AppTheme.BG_PAGE);
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        body.add(buildFormPanel(), BorderLayout.WEST);
        body.add(buildTablePanel(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel card = AppTheme.makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(300, 0));

        card.add(AppTheme.makeSectionHeader("Tambah / Edit Customer"));
        card.add(AppTheme.makeSep());
        card.add(Box.createVerticalStrut(14));

        txtId      = AppTheme.makeInput("Otomatis dari sistem");
        txtNama    = AppTheme.makeInput("Nama lengkap customer");
        txtAlamat  = AppTheme.makeInput("Alamat lengkap");
        txtTelepon = AppTheme.makeInput("Contoh: 08123456789");

        txtId.setEditable(false);
        txtId.setToolTipText("ID otomatis dari sistem");

        card.add(fieldRow("ID Customer (Auto)", txtId));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldRow("Nama Customer", txtNama));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldRow("Alamat",        txtAlamat));
        card.add(Box.createVerticalStrut(10));
        card.add(fieldRow("No. Telepon",   txtTelepon));
        card.add(Box.createVerticalStrut(18));
        card.add(AppTheme.makeSep());
        card.add(Box.createVerticalStrut(14));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        JButton btnSimpan = AppTheme.makeSuccessBtn("Simpan");
        JButton btnUpdate = AppTheme.makePrimaryBtn("Update");
        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        row1.add(btnSimpan);
        row1.add(btnUpdate);

        JPanel row2 = new JPanel(new GridLayout(1, 2, 8, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row2.setBorder(new EmptyBorder(8, 0, 0, 0));
        JButton btnHapus     = AppTheme.makeDangerBtn("Hapus");
        JButton btnBersihkan = AppTheme.makeNeutralBtn("Bersihkan");
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());
        row2.add(btnHapus);
        row2.add(btnBersihkan);

        card.add(row1);
        card.add(row2);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildTablePanel() {
        JPanel card = AppTheme.makeCard();
        card.setLayout(new BorderLayout(0, 12));

        JPanel tableHeader = new JPanel(new BorderLayout(8, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.TABLE_HEADER_BG);
                super.paintComponent(g);
            }
        };
        tableHeader.setOpaque(true);
        tableHeader.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(AppTheme.TABLE_HEADER_BORDER, 1, true) {
                @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                    ((Graphics2D) g).setColor(AppTheme.TABLE_HEADER_BORDER);
                    super.paintBorder(c, g, x, y, w, h);
                }
            },
            new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblDaftar = new JLabel("Daftar Customer") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.PRIMARY);
                super.paintComponent(g);
            }
        };
        lblDaftar.setFont(AppTheme.FONT_BOLD);

        txtCari = AppTheme.makeInput("Cari nama / ID customer...");
        txtCari.setPreferredSize(new Dimension(220, 32));
        txtCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { filterTable(); }
        });

        tableHeader.add(lblDaftar, BorderLayout.WEST);
        tableHeader.add(txtCari, BorderLayout.EAST);
        card.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"ID Customer", "Nama", "Alamat", "No. Telepon"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        AppTheme.styleTable(table);

        int[] widths = {100, 200, 300, 130};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForm();
        });
        card.add(AppTheme.makeScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        footer.setBorder(new EmptyBorder(8, 0, 0, 0));
        JLabel lblCount = new JLabel("0 customer ditemukan") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        lblCount.setFont(AppTheme.FONT_SMALL);
        footer.add(lblCount, BorderLayout.WEST);
        card.add(footer, BorderLayout.SOUTH);

        tableModel.addTableModelListener(e ->
            lblCount.setText(tableModel.getRowCount() + " customer ditemukan"));

        return card;
    }

    private JPanel fieldRow(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 4)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        p.add(AppTheme.makeFieldLabel(label), BorderLayout.NORTH);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private String generateIdBaru() {
        String sql = "SELECT id_customer FROM tb_customer "
                   + "WHERE id_customer LIKE 'CUS-%' "
                   + "ORDER BY CAST(SUBSTRING(id_customer, 5) AS UNSIGNED) DESC LIMIT 1";
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString("id_customer");
                int lastNum = Integer.parseInt(lastId.substring(4));
                return String.format("CUS-%03d", lastNum + 1);
            }
        } catch (SQLException e) {
            showError("Gagal generate ID: " + e.getMessage());
        }
        return "CUS-001";
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT * FROM tb_customer ORDER BY id_customer")) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("id_customer"),
                    rs.getString("nama_customer"),
                    rs.getString("alamat"),
                    rs.getString("telepon")
                });
            }
        } catch (SQLException e) {
            showError("Gagal memuat data: " + e.getMessage());
        }
        txtId.setText(generateIdBaru());
    }

    private void filterTable() {
        String keyword = txtCari.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM tb_customer WHERE LOWER(nama_customer) LIKE ? "
                   + "OR LOWER(id_customer) LIKE ? ORDER BY id_customer";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                        rs.getString("id_customer"),
                        rs.getString("nama_customer"),
                        rs.getString("alamat"),
                        rs.getString("telepon")
                    });
                }
            }
        } catch (SQLException e) {
            showError("Gagal mencari: " + e.getMessage());
        }
    }

    private void isiForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        txtId.setText(tableModel.getValueAt(row, 0).toString());
        txtNama.setText(tableModel.getValueAt(row, 1).toString());
        txtAlamat.setText(tableModel.getValueAt(row, 2).toString());
        txtTelepon.setText(tableModel.getValueAt(row, 3).toString());
        txtId.setEditable(false);
    }

    private boolean validasi() {
        String nama    = txtNama.getText().trim();
        String alamat  = txtAlamat.getText().trim();
        String telepon = txtTelepon.getText().trim();
        if (nama.isEmpty() || alamat.isEmpty() || telepon.isEmpty()) {
            showWarn("Semua field harus diisi!"); return false;
        }
        if (!nama.matches("[A-Za-z\\s]+")) {
            showError("Nama Customer hanya boleh huruf dan spasi."); return false;
        }
        if (!alamat.matches("[A-Za-z0-9\\s.,/\\-]+")) {
            showError("Alamat hanya boleh huruf, angka, spasi, dan tanda baca umum ( . , / - )."); return false;
        }
        if (!telepon.matches("[0-9]+")) {
            showError("No. Telepon hanya boleh angka."); return false;
        }
        if (telepon.length() < 10 || telepon.length() > 15) {
            showError("No. Telepon harus 10–15 digit."); return false;
        }
        return true;
    }

    private void simpan() {
        if (!validasi()) return;
        String sql = "INSERT INTO tb_customer VALUES (?,?,?,?)";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtId.getText().trim());
            ps.setString(2, txtNama.getText().trim());
            ps.setString(3, txtAlamat.getText().trim());
            ps.setString(4, txtTelepon.getText().trim());
            ps.executeUpdate();
            showInfo("Customer berhasil disimpan dengan ID: " + txtId.getText().trim());
            loadTable(); bersihkan();
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    private void update() {
        int row = table.getSelectedRow();
        if (row < 0) { showWarn("Pilih baris data pada tabel terlebih dahulu sebelum diubah!"); return; }
        if (!validasi()) return;
        String sql = "UPDATE tb_customer SET nama_customer=?, alamat=?, telepon=? WHERE id_customer=?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtNama.getText().trim());
            ps.setString(2, txtAlamat.getText().trim());
            ps.setString(3, txtTelepon.getText().trim());
            ps.setString(4, txtId.getText().trim());
            ps.executeUpdate();
            showInfo("Data berhasil diperbarui.");
            loadTable(); bersihkan();
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    private void hapus() {
        if (txtId.getText().trim().isEmpty()) { showWarn("Pilih customer yang akan dihapus!"); return; }
        int conf = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus customer ini?", "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;
        String sql = "DELETE FROM tb_customer WHERE id_customer=?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, txtId.getText().trim());
            ps.executeUpdate();
            showInfo("Data berhasil dihapus.");
            loadTable(); bersihkan();
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    private void bersihkan() {
        txtId.setText(generateIdBaru());
        txtNama.setText(""); txtAlamat.setText(""); txtTelepon.setText("");
        txtId.setEditable(false);
        table.clearSelection();
        txtNama.requestFocus();
    }

    private void showInfo(String msg)  { JOptionPane.showMessageDialog(this, msg, "Informasi",  JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",      JOptionPane.ERROR_MESSAGE); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            JFrame frame = new JFrame("Test Data Customer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 600);
            frame.add(new FormCustomer());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}