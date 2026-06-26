package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormBarang extends JPanel {

    private JTextField txtId, txtNama, txtSatuan, txtHarga, txtStok, txtCari;
    private JComboBox<String> cmbKategori;
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Integer> kategoriIds = new ArrayList<>();
    private JSplitPane split;

    private static final int FORM_MIN_W = 290;

    public FormBarang() {
        initUI();
        loadKategori();
        loadTable();
        generateNextId();
        AppTheme.registerComponent(this);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);

        add(AppTheme.makeTopbar("Data Barang", "Kelola stok dan informasi barang"), BorderLayout.NORTH);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFormPanel(), buildTablePanel()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        split.setBorder(new EmptyBorder(14, 14, 14, 14));
        split.setDividerSize(6);
        split.setContinuousLayout(true);
        split.setResizeWeight(0.0);

        split.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            int loc = (int) evt.getNewValue();
            if (loc < FORM_MIN_W) split.setDividerLocation(FORM_MIN_W);
        });

        add(split, BorderLayout.CENTER);

        addHierarchyListener(new HierarchyListener() {
            @Override public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                    SwingUtilities.invokeLater(() -> {
                        split.setDividerLocation(FORM_MIN_W);
                        split.revalidate();
                        split.repaint();
                    });
                }
            }
        });
    }

    private void generateNextId() {
        try {
            Connection conn = Koneksi.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT id_barang FROM tb_barang ORDER BY id_barang DESC LIMIT 1");
            if (rs.next()) {
                String lastId = rs.getString("id_barang");
                String prefix = lastId.replaceAll("[0-9]", "");
                String numStr = lastId.replaceAll("[^0-9]", "");
                int nextNum   = Integer.parseInt(numStr) + 1;
                int padLen    = numStr.length();
                txtId.setText(prefix + String.format("%0" + padLen + "d", nextNum));
            } else {
                txtId.setText("B001");
            }
        } catch (SQLException e) {
            txtId.setText("B001");
        }
        txtId.setEditable(false);
    }

    private JPanel buildFormPanel() {
        JPanel card = AppTheme.makeCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setMinimumSize(new Dimension(FORM_MIN_W, 0));

        JPanel header = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel lblJudul = new JLabel("Tambah / Edit Barang") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblJudul.setFont(AppTheme.FONT_BOLD);
        header.add(lblJudul, BorderLayout.WEST);
        header.add(AppTheme.makeSep(), BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);

        txtId       = AppTheme.makeInput("Auto-generate");
        txtNama     = AppTheme.makeInput("Nama barang (huruf dan spasi saja)");
        cmbKategori = AppTheme.makeCombo();
        txtSatuan   = AppTheme.makeInput("pcs / kg / lusin");
        txtHarga    = AppTheme.makeInput("0");
        txtStok     = AppTheme.makeInput("0");

        JPanel fields = new JPanel(new GridBagLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        fields.setBorder(new EmptyBorder(12, 0, 12, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        String[] labels = {"ID Barang", "Nama Barang", "Kategori", "Satuan", "Harga Jual", "Stok"};
        JComponent[] inputs = {txtId, txtNama, cmbKategori, txtSatuan, txtHarga, txtStok};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i * 2;
            gbc.insets = new Insets(0, 0, 3, 0);
            fields.add(AppTheme.makeFieldLabel(labels[i]), gbc);
            gbc.gridy = i * 2 + 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            fields.add(inputs[i], gbc);
        }
        gbc.gridy = labels.length * 2;
        gbc.weighty = 1.0;
        fields.add(Box.createVerticalGlue(), gbc);

        JScrollPane scrollFields = new JScrollPane(fields,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
            @Override public void paintComponent(Graphics g) {
                getViewport().setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        scrollFields.setBorder(BorderFactory.createEmptyBorder());
        card.add(scrollFields, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
            new EmptyBorder(12, 0, 0, 0)
        ));

        JButton btnSimpan    = AppTheme.makeSuccessBtn("Simpan");
        JButton btnUpdate    = AppTheme.makePrimaryBtn("Update");
        JButton btnHapus     = AppTheme.makeDangerBtn("Hapus");
        JButton btnBersihkan = AppTheme.makeNeutralBtn("Bersihkan");

        btnSimpan.addActionListener(e -> simpan());
        btnUpdate.addActionListener(e -> update());
        btnHapus.addActionListener(e -> hapus());
        btnBersihkan.addActionListener(e -> bersihkan());

        btnPanel.add(btnSimpan);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBersihkan);
        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildTablePanel() {
        JPanel card = AppTheme.makeCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setMinimumSize(new Dimension(400, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setPreferredSize(null);

        JPanel tableHeader = new JPanel(new BorderLayout(8, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.TABLE_HEADER_BG);
                super.paintComponent(g);
            }
        };
        tableHeader.setOpaque(true);
        tableHeader.setBorder(BorderFactory.createCompoundBorder(
            new AbstractBorder() {
                @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                    g.setColor(AppTheme.TABLE_HEADER_BORDER);
                    g.drawRoundRect(x, y, w - 1, h - 1, 4, 4);
                }
                @Override public Insets getBorderInsets(Component c) { return new Insets(1,1,1,1); }
            },
            new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblDaftar = new JLabel("Daftar Barang") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.PRIMARY);
                super.paintComponent(g);
            }
        };
        lblDaftar.setFont(AppTheme.FONT_BOLD);

        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.setOpaque(false);
        txtCari = AppTheme.makeInput("Cari nama barang...");
        txtCari.setPreferredSize(new Dimension(180, 32));
        txtCari.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(); }
        });
        searchWrap.add(txtCari);
        tableHeader.add(lblDaftar, BorderLayout.WEST);
        tableHeader.add(searchWrap, BorderLayout.EAST);
        card.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Barang", "Kategori", "Satuan", "Harga Jual", "Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        AppTheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(75);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(65);

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    try {
                        int stok = Integer.parseInt(v.toString());
                        if (stok <= 5) {
                            setBackground(AppTheme.STOK_LOW_BG);
                            setForeground(AppTheme.STOK_LOW_FG);
                        } else {
                            setBackground(AppTheme.BG_CARD);
                            setForeground(AppTheme.TEXT_PRIMARY);
                        }
                    } catch (Exception ex) {
                        setBackground(AppTheme.BG_CARD);
                        setForeground(AppTheme.TEXT_PRIMARY);
                    }
                }
                return this;
            }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiForm();
        });

        JScrollPane sp = AppTheme.makeScrollPane(table);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        card.add(sp, BorderLayout.CENTER);

        card.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                table.setPreferredScrollableViewportSize(card.getSize());
                table.revalidate();
            }
        });

        JPanel cardFooter = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        cardFooter.setBorder(new EmptyBorder(8, 0, 0, 0));
        JLabel lblCount = new JLabel("0 barang ditemukan") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        lblCount.setFont(AppTheme.FONT_SMALL);
        cardFooter.add(lblCount, BorderLayout.WEST);
        card.add(cardFooter, BorderLayout.SOUTH);

        tableModel.addTableModelListener(e ->
            lblCount.setText(tableModel.getRowCount() + " barang ditemukan"));

        return card;
    }

    private void loadKategori() {
        try {
            Connection conn = Koneksi.getConnection();
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM tb_kategori ORDER BY id_kategori");
            kategoriIds.clear();
            cmbKategori.removeAllItems();
            while (rs.next()) {
                kategoriIds.add(rs.getInt("id_kategori"));
                cmbKategori.addItem(rs.getString("nama_kategori"));
            }
        } catch (SQLException e) {
            showError("Gagal memuat kategori: " + e.getMessage());
        }
    }

    private void loadTable() {
        tableModel.setRowCount(0);
        try {
            Connection conn = Koneksi.getConnection();
            String sql = "SELECT b.*, k.nama_kategori FROM tb_barang b "
                       + "LEFT JOIN tb_kategori k ON b.id_kategori=k.id_kategori "
                       + "ORDER BY b.id_barang";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),
                    rs.getString("satuan"),
                    String.format("Rp %,.0f", rs.getDouble("harga_jual")),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            showError("Gagal memuat data: " + e.getMessage());
        }
    }

    private void filterTable() {
        String keyword = txtCari.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        try {
            Connection conn = Koneksi.getConnection();
            String sql = "SELECT b.*, k.nama_kategori FROM tb_barang b "
                       + "LEFT JOIN tb_kategori k ON b.id_kategori=k.id_kategori "
                       + "WHERE LOWER(b.nama_barang) LIKE ? OR LOWER(b.id_barang) LIKE ? "
                       + "ORDER BY b.id_barang";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),
                    rs.getString("satuan"),
                    String.format("Rp %,.0f", rs.getDouble("harga_jual")),
                    rs.getInt("stok")
                });
            }
        } catch (SQLException e) {
            showError("Gagal mencari: " + e.getMessage());
        }
    }

    private void isiForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        txtId.setText(tableModel.getValueAt(row, 0).toString());
        txtId.setEditable(false);
        txtNama.setText(tableModel.getValueAt(row, 1).toString());
        String kategoriNama = tableModel.getValueAt(row, 2).toString();
        for (int i = 0; i < cmbKategori.getItemCount(); i++) {
            if (cmbKategori.getItemAt(i).equals(kategoriNama)) {
                cmbKategori.setSelectedIndex(i); break;
            }
        }
        txtSatuan.setText(tableModel.getValueAt(row, 3).toString());
        String hargaRaw = tableModel.getValueAt(row, 4).toString()
            .replace("Rp ", "").replace(" ", "").replace(".", "").replace(",", "");
        txtHarga.setText(hargaRaw);
        txtStok.setText(tableModel.getValueAt(row, 5).toString());
    }

    private boolean validasi() {
        String id     = txtId.getText().trim();
        String nama   = txtNama.getText().trim();
        String satuan = txtSatuan.getText().trim();
        String harga  = txtHarga.getText().trim();
        String stok   = txtStok.getText().trim();

        if (id.isEmpty() || nama.isEmpty() || satuan.isEmpty() || harga.isEmpty() || stok.isEmpty()) {
            showWarn("Semua field harus diisi!"); return false;
        }
        // FIX: ganti isBlank() (Java 11+) dengan trim().isEmpty() agar kompatibel Java 8
        if (nama.trim().isEmpty() || !nama.matches("[\\p{L} ]+")) {
            showWarn("Nama Barang hanya boleh huruf dan spasi.\nAngka dan karakter khusus tidak diizinkan."); return false;
        }
        if (!satuan.matches("[A-Za-z\\s]+")) {
            showWarn("Satuan hanya boleh huruf."); return false;
        }
        if (!harga.matches("[0-9]+(\\.[0-9]+)?") || Double.parseDouble(harga) <= 0) {
            showWarn("Harga harus berupa angka lebih dari 0."); return false;
        }
        if (!stok.matches("[0-9]+")) {
            showWarn("Stok hanya boleh angka."); return false;
        }
        return true;
    }

    private void simpan() {
        if (!validasi()) return;
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "INSERT INTO tb_barang VALUES (?,?,?,?,?,?)");
            ps.setString(1, txtId.getText().trim());
            ps.setInt(2, kategoriIds.get(cmbKategori.getSelectedIndex()));
            ps.setString(3, txtNama.getText().trim());
            ps.setString(4, txtSatuan.getText().trim());
            ps.setDouble(5, Double.parseDouble(txtHarga.getText().trim()));
            ps.setInt(6, Integer.parseInt(txtStok.getText().trim()));
            ps.executeUpdate();
            showInfo("Data berhasil disimpan.");
            loadTable(); bersihkan();
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    private void update() {
        if (!validasi()) return;
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "UPDATE tb_barang SET id_kategori=?,nama_barang=?,satuan=?,harga_jual=?,stok=? WHERE id_barang=?");
            ps.setInt(1, kategoriIds.get(cmbKategori.getSelectedIndex()));
            ps.setString(2, txtNama.getText().trim());
            ps.setString(3, txtSatuan.getText().trim());
            ps.setDouble(4, Double.parseDouble(txtHarga.getText().trim()));
            ps.setInt(5, Integer.parseInt(txtStok.getText().trim()));
            ps.setString(6, txtId.getText().trim());
            ps.executeUpdate();
            showInfo("Data berhasil diperbarui.");
            loadTable(); bersihkan();
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    private void hapus() {
        if (txtId.getText().trim().isEmpty()) { showWarn("Pilih barang yang akan dihapus!"); return; }
        int conf = JOptionPane.showConfirmDialog(this,
            "Yakin ingin menghapus barang ini?", "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "DELETE FROM tb_barang WHERE id_barang=?");
            ps.setString(1, txtId.getText().trim());
            ps.executeUpdate();
            showInfo("Data berhasil dihapus.");
            loadTable(); bersihkan();
        } catch (Exception e) { showError("Error: " + e.getMessage()); }
    }

    private void bersihkan() {
        txtNama.setText(""); txtSatuan.setText("");
        txtHarga.setText(""); txtStok.setText("");
        if (cmbKategori.getItemCount() > 0) cmbKategori.setSelectedIndex(0);
        table.clearSelection();
        generateNextId();
        txtNama.requestFocus();
        SwingUtilities.invokeLater(() -> split.setDividerLocation(FORM_MIN_W));
    }

    private void showInfo(String msg)  { JOptionPane.showMessageDialog(this, msg, "Informasi",  JOptionPane.INFORMATION_MESSAGE); }
    private void showWarn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Peringatan", JOptionPane.WARNING_MESSAGE); }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",      JOptionPane.ERROR_MESSAGE); }
}