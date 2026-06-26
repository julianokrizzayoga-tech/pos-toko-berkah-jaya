package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.concurrent.ExecutionException;

public class FormKategori extends JPanel {

    private JTextField txtId, txtNama;
    private JTable table;
    private DefaultTableModel tableModel;
    private boolean isEditMode = false;

    public FormKategori() {
        initUI();
        loadTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.BG_PAGE);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(AppTheme.BG_PAGE);
        outer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setBackground(AppTheme.BG_PAGE);

        // ── Form panel kiri ──────────────────────────────────────────
        JPanel formCard = AppTheme.makeCard();
        formCard.setLayout(new BorderLayout(0, 0));
        formCard.setPreferredSize(new Dimension(300, 0));

        txtId = AppTheme.makeInput("Otomatis dari database");
        txtId.setEditable(false);
        txtId.setBackground(new Color(248, 250, 252));
        txtNama = AppTheme.makeInput("Contoh: Elektronik");

        JButton btnSimpan    = AppTheme.makeSuccessBtn("Simpan");
        JButton btnHapus     = AppTheme.makeDangerBtn("Hapus");
        JButton btnBersihkan = AppTheme.makeNeutralBtn("Bersihkan");

        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());

        // Header section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(AppTheme.BG_CARD);
        topSection.add(AppTheme.makeSectionHeader("Form Kategori"));
        topSection.add(Box.createVerticalStrut(6));
        topSection.add(AppTheme.makeSep());
        topSection.add(Box.createVerticalStrut(16));

        // Inner panel pakai GridBagLayout untuk center simetri
        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(AppTheme.BG_CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Label ID
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        inner.add(AppTheme.makeFieldLabel("ID Kategori (Auto)"), gbc);
        // Input ID
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 12, 0);
        txtId.setPreferredSize(new Dimension(0, 34));
        inner.add(txtId, gbc);

        // Label Nama
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        inner.add(AppTheme.makeFieldLabel("Nama Kategori"), gbc);
        // Input Nama
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        txtNama.setPreferredSize(new Dimension(0, 34));
        inner.add(txtNama, gbc);

        // Tombol
        Dimension btnSize = new Dimension(0, 36);
        for (JButton btn : new JButton[]{btnSimpan, btnHapus, btnBersihkan}) {
            btn.setPreferredSize(btnSize);
        }
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        inner.add(btnSimpan, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 8, 0);
        inner.add(btnHapus, gbc);
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        inner.add(btnBersihkan, gbc);

        // Wrapper padding kiri-kanan agar simetri
        JPanel padded = new JPanel(new BorderLayout());
        padded.setBackground(AppTheme.BG_CARD);
        padded.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        padded.add(inner, BorderLayout.NORTH);

        formCard.add(topSection, BorderLayout.NORTH);
        formCard.add(padded, BorderLayout.CENTER);

        body.add(formCard, BorderLayout.WEST);

        // ── Tabel kanan ──────────────────────────────────────────────
        JPanel tableCard = AppTheme.makeCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(AppTheme.makeSectionHeader("Daftar Kategori"), BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Kategori"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        AppTheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForm();
        });
        tableCard.add(AppTheme.makeScrollPane(table), BorderLayout.CENTER);

        body.add(tableCard, BorderLayout.CENTER);

        outer.add(body, BorderLayout.CENTER);
        add(outer, BorderLayout.CENTER);
    }

    // ── loadTable pakai SwingWorker ───────────────────────────────────
    private void loadTable() {
        new SwingWorker<Object[][], Void>() {
            @Override
            protected Object[][] doInBackground() throws Exception {
                ResultSet rs = Koneksi.getConnection().createStatement()
                    .executeQuery("SELECT * FROM tb_kategori ORDER BY id_kategori");
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("id_kategori"),
                        rs.getString("nama_kategori")
                    });
                }
                return rows.toArray(new Object[0][]);
            }

            @Override
            protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Object[] row : get()) {
                        tableModel.addRow(row);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FormKategori.this,
                        "Error load tabel: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void isiForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        isEditMode = true;
        txtId.setText(tableModel.getValueAt(row, 0).toString());
        txtNama.setText(tableModel.getValueAt(row, 1).toString());
        txtNama.requestFocus();
    }

    // ── simpan pakai SwingWorker ──────────────────────────────────────
    private void simpan() {
        String nama = txtNama.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama kategori harus diisi!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // FIX: Hanya huruf (A-Z, a-z), spasi, dan karakter khusus relevan
        // Angka (0-9) tidak diizinkan untuk nama kategori
        if (!nama.matches("[A-Za-z\\s&/.,;()'-]+")) {
            JOptionPane.showMessageDialog(this,
                "Nama kategori hanya boleh berisi huruf!\nAngka dan karakter simbol tidak diizinkan.",
                "Input Tidak Valid", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final boolean editMode = isEditMode;
        final String idText    = txtId.getText();

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (editMode && !idText.isEmpty()) {
                    PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                        "UPDATE tb_kategori SET nama_kategori=? WHERE id_kategori=?");
                    ps.setString(1, nama);
                    ps.setInt(2, Integer.parseInt(idText));
                    return ps.executeUpdate() > 0 ? "UPDATE_OK" : "UPDATE_FAIL";
                } else {
                    PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                        "INSERT INTO tb_kategori (nama_kategori) VALUES (?)");
                    ps.setString(1, nama);
                    return ps.executeUpdate() > 0 ? "INSERT_OK" : "INSERT_FAIL";
                }
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    switch (result) {
                        case "UPDATE_OK":
                            JOptionPane.showMessageDialog(FormKategori.this, "Kategori berhasil diupdate!");
                            break;
                        case "UPDATE_FAIL":
                            JOptionPane.showMessageDialog(FormKategori.this, "Data tidak ditemukan!", "Gagal", JOptionPane.ERROR_MESSAGE);
                            return;
                        case "INSERT_OK":
                            JOptionPane.showMessageDialog(FormKategori.this, "Kategori berhasil ditambahkan!");
                            break;
                        case "INSERT_FAIL":
                            JOptionPane.showMessageDialog(FormKategori.this, "Gagal menambahkan kategori.", "Gagal", JOptionPane.ERROR_MESSAGE);
                            return;
                    }
                    loadTable();
                    bersihkan();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FormKategori.this, "Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    // ── hapus pakai SwingWorker ───────────────────────────────────────
    private void hapus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Pilih kategori yang akan dihapus!",
                "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
            "Yakin hapus kategori ini?\nBarang yang terkait kategori ini akan terpengaruh!",
            "Konfirmasi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        final int id = Integer.parseInt(txtId.getText());

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                    "DELETE FROM tb_kategori WHERE id_kategori=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                return true;
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(FormKategori.this, "Kategori berhasil dihapus!");
                    loadTable();
                    bersihkan();
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(FormKategori.this,
                        "Gagal menghapus!\nPastikan tidak ada barang yang memakai kategori ini.\n\nDetail: "
                        + e.getCause().getMessage());
                }
            }
        }.execute();
    }

    private void bersihkan() {
        isEditMode = false;
        txtId.setText("");
        txtNama.setText("");
        table.clearSelection();
        txtNama.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            JFrame frame = new JFrame("Test Kelola Kategori");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(860, 520);
            frame.add(new FormKategori());
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}