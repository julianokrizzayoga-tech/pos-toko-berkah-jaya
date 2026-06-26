package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormTransaksi extends JPanel {

    private final int idUser;
    private JComboBox<String> cmbCustomer;
    private JTable tblBarang, tblKeranjang;
    private DefaultTableModel modelBarang, modelKeranjang;
    private JLabel lblTotal;
    private JTextField txtSearch;
    private JTextField txtBarcode;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<String> customerIds = new ArrayList<>();
    private double grandTotal = 0;

    public FormTransaksi(int idUser) {
        this.idUser = idUser;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        add(AppTheme.makeTopbar("Transaksi Penjualan", null), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        loadCustomer();
        loadBarang();
        AppTheme.registerComponent(this);
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };

        // ── Top bar: Customer, Barcode, Search ──
        JPanel topPanel = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.PRIMARY_BG);
                super.paintComponent(g);
            }
        };
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.PRIMARY_BG);
                super.paintComponent(g);
            }
        };

        JLabel lblCust = new JLabel("Customer:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblCust.setFont(AppTheme.FONT_BOLD);

        cmbCustomer = AppTheme.makeCombo();
        cmbCustomer.setPreferredSize(new Dimension(240, 32));

        JLabel sep1 = new JLabel("│");
        sep1.setForeground(AppTheme.BORDER);
        sep1.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        JLabel lblBarcode = new JLabel("🔍 Scan Barcode:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblBarcode.setFont(AppTheme.FONT_BOLD);

        txtBarcode = AppTheme.makeInput("Scan barcode atau ketik ID Barang lalu Enter");
        txtBarcode.setPreferredSize(new Dimension(170, 32));
        txtBarcode.addActionListener(e -> tambahViaBarcode());

        JButton btnKamera = new JButton("📷");
        btnKamera.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnKamera.setBackground(AppTheme.PRIMARY);
        btnKamera.setForeground(Color.WHITE);
        btnKamera.setFocusPainted(false);
        btnKamera.setBorderPainted(false);
        btnKamera.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKamera.setPreferredSize(new Dimension(38, 32));
        btnKamera.setToolTipText("Scan barcode via kamera");
        btnKamera.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            DialogScanKamera dialog = new DialogScanKamera(parentFrame, barcode -> tambahViaBarcodeOtomatis(barcode));
            dialog.setVisible(true);
        });

        JLabel sep2 = new JLabel("│");
        sep2.setForeground(AppTheme.BORDER);
        sep2.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        JLabel lblSearch = new JLabel("🔎 Cari Barang:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblSearch.setFont(AppTheme.FONT_BOLD);

        txtSearch = AppTheme.makeInput("Cari berdasarkan nama atau kategori");
        txtSearch.setPreferredSize(new Dimension(200, 32));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterBarang(); }
        });

        JButton btnClearSearch = new JButton("✕") {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.isDarkMode() ? new Color(55, 60, 70) : new Color(200, 215, 230));
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        btnClearSearch.setFont(AppTheme.FONT_BOLD);
        btnClearSearch.setFocusPainted(false);
        btnClearSearch.setBorderPainted(false);
        btnClearSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearSearch.setPreferredSize(new Dimension(30, 32));
        btnClearSearch.addActionListener(e -> { txtSearch.setText(""); filterBarang(); txtSearch.requestFocus(); });

        row1.add(lblCust); row1.add(cmbCustomer); row1.add(sep1);
        row1.add(lblBarcode); row1.add(txtBarcode); row1.add(btnKamera); row1.add(sep2);
        row1.add(lblSearch); row1.add(txtSearch); row1.add(btnClearSearch);
        topPanel.add(row1, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setDividerSize(4);
        split.setContinuousLayout(true);
        split.setBorder(null);
        split.setResizeWeight(0.55);
        split.setBackground(AppTheme.BG_PAGE);
        split.setLeftComponent(buildBarangPanel());
        split.setRightComponent(buildKeranjangPanel());

        JPanel center = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        center.add(topPanel, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);

        body.add(center, BorderLayout.CENTER);
        body.add(buildBottomPanel(), BorderLayout.SOUTH);
        return body;
    }

    private JPanel buildBarangPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        panel.setMinimumSize(new Dimension(280, 0));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));

        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.PRIMARY);
                super.paintComponent(g);
            }
        };
        hdr.setBorder(new EmptyBorder(10, 14, 10, 14));
        JLabel lbl = new JLabel("🛍  Daftar Barang — klik untuk tambah ke keranjang");
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lbl.setForeground(Color.WHITE);
        hdr.add(lbl, BorderLayout.WEST);
        panel.add(hdr, BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Barang", "Kategori", "Harga", "Stok"};
        modelBarang = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblBarang = new JTable(modelBarang);
        AppTheme.styleTable(tblBarang);
        tblBarang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tblBarang.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblBarang.getColumnModel().getColumn(1).setPreferredWidth(180);
        tblBarang.getColumnModel().getColumn(2).setPreferredWidth(100);
        tblBarang.getColumnModel().getColumn(3).setPreferredWidth(100);
        tblBarang.getColumnModel().getColumn(4).setPreferredWidth(50);
        tblBarang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    tambahKeKeranjang(1);
                } else if (e.getClickCount() == 2) {
                    int viewRow = tblBarang.getSelectedRow();
                    if (viewRow < 0) return;
                    int modelRow = tblBarang.convertRowIndexToModel(viewRow);
                    int stok = toInt(modelBarang.getValueAt(modelRow, 4));
                    String nama = modelBarang.getValueAt(modelRow, 1).toString();
                    String input = JOptionPane.showInputDialog(FormTransaksi.this,
                        "Jumlah beli \"" + nama + "\" (stok: " + stok + "):",
                        "Input Jumlah", JOptionPane.PLAIN_MESSAGE);
                    if (input == null || input.trim().isEmpty()) return;
                    try {
                        int qty = Integer.parseInt(input.trim());
                        if (qty <= 0) { JOptionPane.showMessageDialog(FormTransaksi.this, "Jumlah harus lebih dari 0!"); return; }
                        tambahKeKeranjang(qty);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(FormTransaksi.this, "Masukkan angka yang valid!");
                    }
                }
            }
        });

        sorter = new TableRowSorter<>(modelBarang);
        tblBarang.setRowSorter(sorter);

        JScrollPane sp = AppTheme.makeScrollPane(tblBarang);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildKeranjangPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        panel.setMinimumSize(new Dimension(260, 0));

        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.isDarkMode() ? new Color(20, 45, 25) : new Color(232, 245, 233));
                super.paintComponent(g);
            }
        };
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
            new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel lbl = new JLabel("🛒  Keranjang Belanja") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.isDarkMode() ? new Color(100, 220, 120) : new Color(27, 94, 32));
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        hdr.add(lbl, BorderLayout.WEST);
        panel.add(hdr, BorderLayout.NORTH);

        String[] cols = {"", "ID", "Nama Barang", "Harga Satuan", "Qty", "Subtotal"};
        modelKeranjang = new DefaultTableModel(cols, 0) {
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Boolean.class : Object.class; }
            @Override public boolean isCellEditable(int r, int c) { return c == 0 || c == 4; }
        };

        tblKeranjang = new JTable(modelKeranjang);
        AppTheme.styleTable(tblKeranjang);

        tblKeranjang.getTableHeader().setBackground(
            AppTheme.isDarkMode() ? new Color(20, 45, 25) : new Color(232, 245, 233));
        tblKeranjang.getTableHeader().setForeground(
            AppTheme.isDarkMode() ? new Color(100, 220, 120) : new Color(27, 94, 32));

        modelKeranjang.addTableModelListener(e -> {
            if (e.getColumn() == 4) {
                int row = e.getFirstRow();
                if (row < 0 || row >= modelKeranjang.getRowCount()) return;
                try {
                    Object qtyVal = modelKeranjang.getValueAt(row, 4);
                    int qty = Integer.parseInt(qtyVal.toString().trim());
                    if (qty <= 0) { modelKeranjang.removeRow(row); hitungTotal(); return; }
                    String idBarang = modelKeranjang.getValueAt(row, 1).toString();
                    int stokTersedia = getStokBarang(idBarang);
                    if (qty > stokTersedia) {
                        JOptionPane.showMessageDialog(FormTransaksi.this,
                            "Stok tidak mencukupi! Tersedia: " + stokTersedia, "Stok Kurang", JOptionPane.WARNING_MESSAGE);
                        modelKeranjang.setValueAt(stokTersedia, row, 4);
                        qty = stokTersedia;
                    }
                    double harga = toDouble(modelKeranjang.getValueAt(row, 3));
                    modelKeranjang.setValueAt(harga * qty, row, 5);
                    hitungTotal();
                } catch (NumberFormatException ex) { }
            }
        });

        tblKeranjang.getColumnModel().getColumn(0).setMaxWidth(36);
        tblKeranjang.getColumnModel().getColumn(1).setPreferredWidth(55);
        tblKeranjang.getColumnModel().getColumn(2).setPreferredWidth(150);
        tblKeranjang.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblKeranjang.getColumnModel().getColumn(4).setPreferredWidth(40);
        tblKeranjang.getColumnModel().getColumn(5).setPreferredWidth(110);

        JScrollPane sp = AppTheme.makeScrollPane(tblKeranjang);
        panel.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 1, 8, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
            new EmptyBorder(8, 10, 8, 10)
        ));
        JButton btnHapus = new JButton("🗑  Hapus Item");
        btnHapus.setBackground(AppTheme.DANGER);
        btnHapus.setForeground(Color.WHITE);
        btnHapus.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btnHapus.setFocusPainted(false); btnHapus.setBorderPainted(false);
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapus.addActionListener(e -> hapusDariKeranjang());
        btnPanel.add(btnHapus);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.isDarkMode() ? new Color(15, 18, 25) : new Color(20, 40, 70));
                super.paintComponent(g);
            }
        };
        panel.setOpaque(true);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.isDarkMode() ? new Color(15, 18, 25) : new Color(20, 40, 70));
                super.paintComponent(g);
            }
        };
        left.setOpaque(true);

        lblTotal = new JLabel("TOTAL:  Rp 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(Color.WHITE);
        left.add(lblTotal);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.isDarkMode() ? new Color(15, 18, 25) : new Color(20, 40, 70));
                super.paintComponent(g);
            }
        };
        right.setOpaque(true);

        JButton btnReset = new JButton("Reset Keranjang");
        btnReset.setBackground(new Color(90, 100, 115));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(AppTheme.FONT_BOLD);
        btnReset.setFocusPainted(false); btnReset.setBorderPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(148, 38));
        btnReset.addActionListener(e -> resetKeranjang());

        JButton btnBayar = new JButton("✔  BAYAR SEKARANG");
        btnBayar.setBackground(AppTheme.SUCCESS);
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnBayar.setFocusPainted(false); btnBayar.setBorderPainted(false);
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBayar.setPreferredSize(new Dimension(188, 38));
        btnBayar.addActionListener(e -> prosesBayar());

        right.add(btnReset); right.add(btnBayar);
        panel.add(left, BorderLayout.CENTER);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private void filterBarang() {
        String keyword = txtSearch.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null : RowFilter.regexFilter("(?i)" + keyword, 1, 2));
    }

    private void tambahViaBarcode() {
        String kode = txtBarcode.getText().trim();
        if (kode.isEmpty()) return;
        boolean found = false;
        for (int i = 0; i < modelBarang.getRowCount(); i++) {
            if (modelBarang.getValueAt(i, 0).toString().equalsIgnoreCase(kode)) {
                String id = modelBarang.getValueAt(i, 0).toString();
                String nama = modelBarang.getValueAt(i, 1).toString();
                double harga = toDouble(modelBarang.getValueAt(i, 3));
                int stok = toInt(modelBarang.getValueAt(i, 4));
                int qty = tanyaQty(nama, stok);
                if (qty > 0) tambahItemKeKeranjang(id, nama, harga, stok, qty);
                found = true; break;
            }
        }
        if (!found) {
            try {
                PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                    "SELECT id_barang, nama_barang, harga_jual, stok FROM tb_barang WHERE id_barang = ?");
                ps.setString(1, kode);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int stok = rs.getInt("stok");
                    if (stok <= 0) { JOptionPane.showMessageDialog(this, "Stok habis!"); }
                    else {
                        int qty = tanyaQty(rs.getString("nama_barang"), stok);
                        if (qty > 0) tambahItemKeKeranjang(rs.getString("id_barang"), rs.getString("nama_barang"), rs.getDouble("harga_jual"), stok, qty);
                    }
                } else { JOptionPane.showMessageDialog(this, "Barcode \"" + kode + "\" tidak ditemukan!"); }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
        txtBarcode.setText(""); txtBarcode.requestFocus();
    }

    private void tambahViaBarcodeOtomatis(String kode) {
        if (kode == null || kode.isEmpty()) return;
        for (int i = 0; i < modelBarang.getRowCount(); i++) {
            if (modelBarang.getValueAt(i, 0).toString().equalsIgnoreCase(kode)) {
                String id = modelBarang.getValueAt(i, 0).toString();
                String nama = modelBarang.getValueAt(i, 1).toString();
                double harga = toDouble(modelBarang.getValueAt(i, 3));
                int stok = toInt(modelBarang.getValueAt(i, 4));
                if (stok <= 0) { JOptionPane.showMessageDialog(this, "Stok habis!"); return; }
                tambahItemKeKeranjang(id, nama, harga, stok, 1); return;
            }
        }
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "SELECT id_barang, nama_barang, harga_jual, stok FROM tb_barang WHERE id_barang = ?");
            ps.setString(1, kode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int stok = rs.getInt("stok");
                if (stok <= 0) { JOptionPane.showMessageDialog(this, "Stok habis!"); return; }
                tambahItemKeKeranjang(rs.getString("id_barang"), rs.getString("nama_barang"), rs.getDouble("harga_jual"), stok, 1);
            } else { JOptionPane.showMessageDialog(this, "Barcode \"" + kode + "\" tidak ditemukan!"); }
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private int tanyaQty(String nama, int stok) {
        String input = JOptionPane.showInputDialog(this,
            "Jumlah beli \"" + nama + "\" (stok: " + stok + "):", "Input Jumlah", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return 0;
        try {
            int qty = Integer.parseInt(input.trim());
            if (qty <= 0) { JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0!"); return 0; }
            if (qty > stok) { JOptionPane.showMessageDialog(this, "Melebihi stok! Maksimal: " + stok); return stok; }
            return qty;
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Masukkan angka yang valid!"); return 0; }
    }

    private void tambahItemKeKeranjang(String id, String nama, double harga, int stok, int qtyTambah) {
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
            if (modelKeranjang.getValueAt(i, 1).toString().equals(id)) {
                // FIX: gunakan toInt() agar aman walau nilai qty di tabel sudah berubah jadi String
                // (terjadi setelah cell qty di-edit manual oleh user, JTable menyimpan sebagai String)
                int qtyBaru = toInt(modelKeranjang.getValueAt(i, 4)) + qtyTambah;
                if (qtyBaru > stok) { JOptionPane.showMessageDialog(this, "Stok tidak cukup! Tersedia: " + stok); qtyBaru = stok; }
                modelKeranjang.setValueAt(qtyBaru, i, 4);
                modelKeranjang.setValueAt(harga * qtyBaru, i, 5);
                hitungTotal(); return;
            }
        }
        modelKeranjang.addRow(new Object[]{false, id, nama, harga, qtyTambah, harga * qtyTambah});
        hitungTotal();
    }

    private void loadCustomer() {
        try {
            ResultSet rs = Koneksi.getConnection().createStatement()
                .executeQuery("SELECT * FROM tb_customer ORDER BY id_customer");
            customerIds.clear(); cmbCustomer.removeAllItems();
            while (rs.next()) {
                customerIds.add(rs.getString("id_customer"));
                cmbCustomer.addItem(rs.getString("id_customer") + " — " + rs.getString("nama_customer"));
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
    }

    private void loadBarang() {
        modelBarang.setRowCount(0);
        try {
            ResultSet rs = Koneksi.getConnection().createStatement().executeQuery(
                "SELECT b.id_barang, b.nama_barang, k.nama_kategori, b.harga_jual, b.stok " +
                "FROM tb_barang b LEFT JOIN tb_kategori k ON b.id_kategori=k.id_kategori " +
                "WHERE b.stok > 0 ORDER BY b.nama_barang");
            while (rs.next())
                modelBarang.addRow(new Object[]{rs.getString("id_barang"), rs.getString("nama_barang"),
                    rs.getString("nama_kategori"), rs.getDouble("harga_jual"), rs.getInt("stok")});
        } catch (SQLException e) { JOptionPane.showMessageDialog(this, "Error: " + e.getMessage()); }
        filterBarang();
    }

    private void tambahKeKeranjang(int qtyTambah) {
        int viewRow = tblBarang.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = tblBarang.convertRowIndexToModel(viewRow);
        String id = modelBarang.getValueAt(modelRow, 0).toString();
        String nama = modelBarang.getValueAt(modelRow, 1).toString();
        double harga = toDouble(modelBarang.getValueAt(modelRow, 3));
        int stok = toInt(modelBarang.getValueAt(modelRow, 4));
        if (qtyTambah > stok) { JOptionPane.showMessageDialog(this, "Stok tidak cukup! Tersedia: " + stok); qtyTambah = stok; }
        tambahItemKeKeranjang(id, nama, harga, stok, qtyTambah);
    }

    private int getStokBarang(String idBarang) {
        for (int i = 0; i < modelBarang.getRowCount(); i++)
            if (modelBarang.getValueAt(i, 0).toString().equals(idBarang)) return toInt(modelBarang.getValueAt(i, 4));
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement("SELECT stok FROM tb_barang WHERE id_barang=?");
            ps.setString(1, idBarang);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("stok");
        } catch (SQLException ignored) {}
        return Integer.MAX_VALUE;
    }

    private void hapusDariKeranjang() {
        List<Integer> del = new ArrayList<>();
        for (int i = 0; i < modelKeranjang.getRowCount(); i++)
            if (Boolean.TRUE.equals(modelKeranjang.getValueAt(i, 0))) del.add(i);
        if (del.isEmpty()) { JOptionPane.showMessageDialog(this, "Centang item yang ingin dihapus!"); return; }
        int c = JOptionPane.showConfirmDialog(this, "Hapus " + del.size() + " item?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        for (int i = del.size() - 1; i >= 0; i--) modelKeranjang.removeRow(del.get(i));
        hitungTotal();
    }

    private void hitungTotal() {
        grandTotal = 0;
        for (int i = 0; i < modelKeranjang.getRowCount(); i++) grandTotal += toDouble(modelKeranjang.getValueAt(i, 5));
        lblTotal.setText("TOTAL:  " + String.format("Rp %,.0f", grandTotal));
    }

    // ── PROSES BAYAR: buka DialogPayment ──────────────────────
    private void prosesBayar() {
        if (modelKeranjang.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong!");
            return;
        }
        if (cmbCustomer.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Pilih customer dulu!");
            return;
        }

        // ── FIX: validasi stok terlebih dahulu SEBELUM membuka DialogPayment ──
        // Jika ada item di keranjang yang stoknya tidak cukup (stok terbaru di DB
        // sudah berubah, misalnya terjual di kasir lain), transaksi tidak lanjut
        // ke proses pembayaran sama sekali.
        StringBuilder pesanKurang = new StringBuilder();
        try {
            Connection conn = Koneksi.getConnection();
            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                String idBarang = modelKeranjang.getValueAt(i, 1).toString();
                String namaBarang = modelKeranjang.getValueAt(i, 2).toString();
                int qty = toInt(modelKeranjang.getValueAt(i, 4));

                PreparedStatement psCekStok = conn.prepareStatement(
                    "SELECT stok FROM tb_barang WHERE id_barang = ?");
                psCekStok.setString(1, idBarang);
                ResultSet rsStok = psCekStok.executeQuery();
                int stokSekarang = rsStok.next() ? rsStok.getInt("stok") : 0;

                if (qty > stokSekarang) {
                    pesanKurang.append("- ").append(namaBarang)
                        .append(" (diminta ").append(qty)
                        .append(", tersedia ").append(stokSekarang).append(")\n");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error cek stok: " + ex.getMessage());
            return;
        }

        if (pesanKurang.length() > 0) {
            JOptionPane.showMessageDialog(this,
                "Stok tidak mencukupi untuk item berikut:\n" + pesanKurang +
                "\nSilakan sesuaikan jumlah di keranjang.",
                "Stok Tidak Mencukupi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        DialogPayment dialog = new DialogPayment(parentFrame, grandTotal, new DialogPayment.PaymentCallback() {
            @Override
            public void onSuccess(String metode, double jumlahBayar, double kembalianHasil) {
                // ── FIX: langsung pass nilai sebagai parameter, tidak lewat field instance ──
                bayar(metode, jumlahBayar, kembalianHasil);
            }

            @Override
            public void onCancel() {
                // User membatalkan pembayaran
            }
        });

        dialog.setVisible(true);
    }

    // ── FIX: bayar() menerima parameter langsung ──────────────
    private void bayar(String metodeBayar, double uangBayar, double kembalian) {
        try {
            Connection conn = Koneksi.getConnection();
            conn.setAutoCommit(false);
            String idCustomer = customerIds.get(cmbCustomer.getSelectedIndex());

            PreparedStatement psJual = conn.prepareStatement(
                "INSERT INTO tb_penjualan (tgl_transaksi, id_customer, total_bayar, uang_bayar, kembalian, id_user, metode_bayar) " +
                "VALUES (CURDATE(), ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
            psJual.setString(1, idCustomer);
            psJual.setDouble(2, grandTotal);
            psJual.setDouble(3, uangBayar);    // ← nilai asli dari DialogPayment
            psJual.setDouble(4, kembalian);    // ← nilai asli dari DialogPayment
            psJual.setInt(5, idUser);
            psJual.setString(6, metodeBayar);  // ← "CASH" / "QRIS" / "KARTU - Visa" dll
            psJual.executeUpdate();

            ResultSet rsKey = psJual.getGeneratedKeys(); rsKey.next();
            int idJual = rsKey.getInt(1);

            for (int i = 0; i < modelKeranjang.getRowCount(); i++) {
                String idBarang = modelKeranjang.getValueAt(i, 1).toString();
                double harga    = toDouble(modelKeranjang.getValueAt(i, 3));
                // FIX: jangan cast langsung (Integer) — kolom Qty bisa berisi String
                // hasil edit manual user di tabel keranjang, sehingga cast langsung
                // memicu ClassCastException: java.lang.String cannot be cast to java.lang.Integer
                int qty         = toInt(modelKeranjang.getValueAt(i, 4));
                double subtotal = toDouble(modelKeranjang.getValueAt(i, 5));

                PreparedStatement psDetail = conn.prepareStatement(
                    "INSERT INTO tb_detail_penjualan (id_jual, id_barang, jumlah_beli, harga_satuan, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?)");
                psDetail.setInt(1, idJual);
                psDetail.setString(2, idBarang);
                psDetail.setInt(3, qty);
                psDetail.setDouble(4, harga);
                psDetail.setDouble(5, subtotal);
                psDetail.executeUpdate();

                PreparedStatement psStok = conn.prepareStatement(
                    "UPDATE tb_barang SET stok = stok - ? WHERE id_barang = ?");
                psStok.setInt(1, qty);
                psStok.setString(2, idBarang);
                psStok.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            final double totalFinal = grandTotal;
            final double bayarFinal = uangBayar;
            final int    idJualFinal = idJual;

            resetKeranjang();
            loadBarang();

            int cetak = JOptionPane.showConfirmDialog(this,
                "Transaksi berhasil!\n\nExport Struk Ke PDF sekarang?",
                "Transaksi Berhasil", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (cetak == JOptionPane.YES_OPTION) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
                new FormStruk(parentFrame, idJualFinal, totalFinal, bayarFinal).setVisible(true);
            }

        } catch (SQLException e) {
            try { Koneksi.getConnection().rollback(); } catch (SQLException ex) { }
            JOptionPane.showMessageDialog(this, "Error Transaksi: " + e.getMessage());
        }
    }

    private void resetKeranjang() {
        // FIX: hentikan cell editor yang masih aktif sebelum mengosongkan tabel,
        // untuk mencegah ArrayIndexOutOfBoundsException (0 >= 0) saat
        // JTable mencoba commit edit ke baris yang sudah tidak ada.
        if (tblKeranjang.isEditing()) {
            tblKeranjang.getCellEditor().stopCellEditing();
        }
        modelKeranjang.setRowCount(0);
        lblTotal.setText("TOTAL:  Rp 0");
        grandTotal = 0;
    }

    private double toDouble(Object val) {
        if (val instanceof Double) return (Double) val;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }
    private int toInt(Object val) {
        if (val instanceof Integer) return (Integer) val;
        try { return Integer.parseInt(val.toString().trim()); } catch (Exception e) { return 0; }
    }
}