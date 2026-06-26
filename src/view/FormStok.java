package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class FormStok extends JPanel {

    private JTextField txtIdBarang, txtNamaBarang, txtStokSekarang, txtTambahStok;
    private JTextField txtSearch;
    private JTable tableStok;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public FormStok() {
        initUI();
        loadTable();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.BG_PAGE);

        JPanel body = new JPanel(new BorderLayout(14, 0));
        body.setBackground(AppTheme.BG_PAGE);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Panel Kiri: Form
        JPanel formCard = AppTheme.makeCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(BorderFactory.createCompoundBorder(
            new AppTheme.RoundBorder(AppTheme.BORDER, 12, 1),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));
        formCard.setPreferredSize(new Dimension(280, 0));

        JLabel header = AppTheme.makeSectionHeader("Form Kelola Stok");
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(header);
        formCard.add(Box.createVerticalStrut(6));

        JSeparator sep = AppTheme.makeSep();
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        formCard.add(sep);
        formCard.add(Box.createVerticalStrut(16));

        txtIdBarang     = AppTheme.makeInput("Pilih dari tabel");
        txtNamaBarang   = AppTheme.makeInput("");
        txtStokSekarang = AppTheme.makeInput("0");
        txtTambahStok   = AppTheme.makeInput("Contoh: 50");

        txtIdBarang.setEditable(false);
        txtNamaBarang.setEditable(false);
        txtStokSekarang.setEditable(false);

        formCard.add(fieldRow("ID Barang",        txtIdBarang));
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(fieldRow("Nama Barang",       txtNamaBarang));
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(fieldRow("Stok Saat Ini",     txtStokSekarang));
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(fieldRow("Jumlah Stok Masuk", txtTambahStok));
        formCard.add(Box.createVerticalStrut(20));

        JButton btnSimpan    = AppTheme.makeSuccessBtn("Update Stok");
        JButton btnBersihkan = AppTheme.makeNeutralBtn("Bersihkan");

        btnSimpan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnBersihkan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnSimpan.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBersihkan.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnSimpan.addActionListener(e -> simpanStok());
        btnBersihkan.addActionListener(e -> bersihkan());

        formCard.add(btnSimpan);
        formCard.add(Box.createVerticalStrut(8));
        formCard.add(btnBersihkan);
        formCard.add(Box.createVerticalGlue());

        body.add(formCard, BorderLayout.WEST);

        // Panel Kanan: Tabel
        JPanel tableCard = AppTheme.makeCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        // ── Header tabel: judul + search ──
        JPanel tableHeader = new JPanel(new BorderLayout(8, 0));
        tableHeader.setBackground(AppTheme.BG_CARD);

        JLabel lblJudul = AppTheme.makeSectionHeader("Data Stok Barang");
        lblJudul.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tableHeader.add(lblJudul, BorderLayout.WEST);

        // Search field + tombol clear
        JPanel searchPanel = new JPanel(new BorderLayout(4, 0));
        searchPanel.setBackground(AppTheme.BG_CARD);

        txtSearch = AppTheme.makeInput("Cari nama / kategori...");
        txtSearch.setPreferredSize(new Dimension(220, 32));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTabel(); }
        });

        JButton btnClear = new JButton("✕");
        btnClear.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));
        btnClear.setBackground(new Color(220, 230, 240));
        btnClear.setForeground(AppTheme.TEXT_SECONDARY);
        btnClear.setFocusPainted(false);
        btnClear.setBorderPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setPreferredSize(new Dimension(30, 32));
        btnClear.setToolTipText("Hapus pencarian");
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            filterTabel();
            txtSearch.requestFocus();
        });

        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnClear, BorderLayout.EAST);
        tableHeader.add(searchPanel, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        // Tabel
        String[] cols = {"ID", "Nama Barang", "Kategori", "Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableStok = new JTable(tableModel);
        AppTheme.styleTable(tableStok);

        // Pasang sorter untuk fitur search/filter
        sorter = new TableRowSorter<>(tableModel);
        tableStok.setRowSorter(sorter);

        tableStok.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForm();
        });

        tableCard.add(AppTheme.makeScrollPane(tableStok), BorderLayout.CENTER);
        body.add(tableCard, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Filter tabel berdasarkan kolom Nama Barang (1) dan Kategori (2) ──
    private void filterTabel() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 1, 2));
        }
    }

    private JPanel fieldRow(String label, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setBackground(AppTheme.BG_CARD);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(AppTheme.makeFieldLabel(label), BorderLayout.NORTH);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        try {
            String sql = "SELECT b.id_barang, b.nama_barang, "
                       + "COALESCE(k.nama_kategori, '-') AS kategori, b.stok "
                       + "FROM tb_barang b "
                       + "LEFT JOIN tb_kategori k ON b.id_kategori = k.id_kategori "
                       + "ORDER BY b.id_barang DESC";
            ResultSet rs = Koneksi.getConnection().createStatement().executeQuery(sql);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("kategori"),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error load stok: " + e.getMessage());
        }
        // Terapkan ulang filter jika ada keyword aktif
        filterTabel();
    }

    private void isiForm() {
        int viewRow = tableStok.getSelectedRow();
        if (viewRow < 0) return;
        // Konversi view row ke model row (penting karena ada sorter/filter)
        int modelRow = tableStok.convertRowIndexToModel(viewRow);
        txtIdBarang.setText(tableModel.getValueAt(modelRow, 0).toString());
        txtNamaBarang.setText(tableModel.getValueAt(modelRow, 1).toString());
        txtStokSekarang.setText(tableModel.getValueAt(modelRow, 3).toString());
        txtTambahStok.setText("");
    }

    private void simpanStok() {
        if (txtIdBarang.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Pilih barang terlebih dahulu!", "Peringatan",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tambahan = txtTambahStok.getText().trim().replaceAll("[^0-9]", "");
        if (tambahan.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Masukkan jumlah stok yang valid!", "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int jmlTambah = Integer.parseInt(tambahan);
            int stokBaru  = Integer.parseInt(txtStokSekarang.getText()) + jmlTambah;

            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "UPDATE tb_barang SET stok=? WHERE id_barang=?");
            ps.setInt(1, stokBaru);
            ps.setString(2, txtIdBarang.getText());
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Stok berhasil diperbarui!");
            loadTable();
            bersihkan();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void bersihkan() {
        txtIdBarang.setText("");
        txtNamaBarang.setText("");
        txtStokSekarang.setText("0");
        txtTambahStok.setText("");
        tableStok.clearSelection();
    }
}