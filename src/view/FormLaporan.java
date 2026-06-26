package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class FormLaporan extends JPanel {

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTotal, lblJmlTransaksi;
    private JTextField txtDari, txtSampai, txtSearch;
    private double totalPendapatan = 0;

    private static final Color PRIMARY      = new Color(25, 118, 210);
    private static final Color PRIMARY_DARK = new Color(21, 101, 192);

    // Warna per metode bayar (light mode)
    private static final Color COLOR_CASH_FG   = new Color(14, 100, 50);
    private static final Color COLOR_QRIS_FG   = new Color(25, 80, 180);
    private static final Color COLOR_KARTU_FG  = new Color(140, 80, 0);
    // Warna per metode bayar (dark mode)
    private static final Color COLOR_CASH_FG_D  = new Color(74, 222, 128);
    private static final Color COLOR_QRIS_FG_D  = new Color(147, 197, 253);
    private static final Color COLOR_KARTU_FG_D = new Color(251, 191, 36);

    private JPanel body, topContainer, summaryCard, filterPanel, searchBar, tableCard, footer;
    private JLabel lblTitle, lblFilter, lblDari, lblSampai, lblSearch;
    private JButton btnClear;
    private JSeparator sep;
    private JScrollPane tableScrollPane;

    public FormLaporan() {
        initUI();
        filterBulanan();
        AppTheme.registerComponent(this);
        addAncestorListener(new javax.swing.event.AncestorListener() {
            @Override public void ancestorRemoved(javax.swing.event.AncestorEvent e) {
                AppTheme.unregisterComponent(FormLaporan.this);
            }
            @Override public void ancestorAdded(javax.swing.event.AncestorEvent e) {}
            @Override public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
        });
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 0));
        setOpaque(true);

        body = new JPanel(new BorderLayout(0, 12)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        body.setOpaque(true);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        topContainer = new JPanel(new BorderLayout(0, 10)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        topContainer.setOpaque(true);

        filterPanel = buildFilterPanel();
        searchBar   = buildSearchBar();
        summaryCard = buildSummaryCard();

        topContainer.add(filterPanel,  BorderLayout.NORTH);
        topContainer.add(searchBar,    BorderLayout.CENTER);
        topContainer.add(summaryCard,  BorderLayout.SOUTH);

        body.add(topContainer, BorderLayout.NORTH);
        tableCard = buildTableCard();
        body.add(tableCard, BorderLayout.CENTER);
        footer = buildFooter();
        body.add(footer, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        setBackground(AppTheme.BG_PAGE);
        super.paintComponent(g);
    }

    // ════════════════════════════════════════════════════════
    //  BUILD PANELS
    // ════════════════════════════════════════════════════════

    private JPanel buildSummaryCard() {
        JPanel card = new JPanel(new GridLayout(1, 2, 20, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(12, 16, 12, 16)
                ));
                super.paintComponent(g);
            }
        };
        card.setOpaque(true);

        JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        leftPart.setOpaque(false);
        lblTitle = new JLabel("📊 Rekap Penjualan Realtime") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        leftPart.add(lblTitle);

        JPanel rightPart = new JPanel(new GridBagLayout());
        rightPart.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        lblTotal = new JLabel("Total Pendapatan: Rp 0", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(PRIMARY);

        lblJmlTransaksi = new JLabel("0 baris data ditemukan", SwingConstants.RIGHT) {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        lblJmlTransaksi.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        rightPart.add(lblTotal, gbc);
        gbc.gridy++;
        rightPart.add(lblJmlTransaksi, gbc);

        card.add(leftPart);
        card.add(rightPart);
        return card;
    }

    private JPanel buildFilterPanel() {
        JPanel card = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
                ));
                super.paintComponent(g);
            }
        };
        card.setOpaque(true);

        lblFilter = new JLabel("Filter Cepat:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(lblFilter);

        String[] filterLabels = {"Hari Ini","Minggu Ini","Bulan Ini","Tahun Ini","Semua"};
        Color[]  filterColors = {
            new Color(25, 118, 210),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(99, 102, 241),
            new Color(107, 114, 128)
        };
        for (int i = 0; i < filterLabels.length; i++) {
            final int idx = i;
            JButton btn = makeFilterBtn(filterLabels[i], filterColors[i]);
            btn.addActionListener(e -> {
                switch (idx) {
                    case 0: filterHarian();   break;
                    case 1: filterMingguan(); break;
                    case 2: filterBulanan();  break;
                    case 3: filterTahunan();  break;
                    case 4: filterSemua();    break;
                }
            });
            card.add(btn);
        }

        sep = new JSeparator(JSeparator.VERTICAL) {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.BORDER);
                super.paintComponent(g);
            }
        };
        sep.setPreferredSize(new Dimension(1, 28));
        card.add(sep);

        lblDari = new JLabel("Dari:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        lblDari.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(lblDari);

        txtDari = makeInputDate();
        card.add(txtDari);

        lblSampai = new JLabel("s/d:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        lblSampai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(lblSampai);

        txtSampai = makeInputDate();
        card.add(txtSampai);

        JButton btnCari = makeBtn("🔍 Cari", PRIMARY);
        btnCari.addActionListener(e -> filterCustom());
        card.add(btnCari);

        return card;
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(2, 8, 2, 8)
                ));
                super.paintComponent(g);
            }
        };
        bar.setOpaque(true);

        lblSearch = new JLabel("🔎  Cari Transaksi:") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bar.add(lblSearch);

        txtSearch = new JTextField() {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.INPUT_BG);
                setForeground(AppTheme.TEXT_PRIMARY);
                setCaretColor(AppTheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.SEARCH_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
                ));
                super.paintComponent(g);
            }
        };
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setPreferredSize(new Dimension(320, 32));
        txtSearch.setToolTipText("Cari berdasarkan nama customer, nama barang, kasir, atau metode bayar");
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { filterTabel(); }
        });
        bar.add(txtSearch);

        btnClear = new JButton("✕  Hapus Filter") {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.isDarkMode()
                    ? new Color(52, 56, 68) : new Color(220, 230, 240));
                setForeground(AppTheme.TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setFocusPainted(false);
        btnClear.setBorderPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnClear.addActionListener(e -> {
            txtSearch.setText("");
            filterTabel();
            txtSearch.requestFocus();
        });
        bar.add(btnClear);

        // ── Legend metode bayar ──
        bar.add(makeLegendBadge("CASH",  AppTheme.isDarkMode() ? COLOR_CASH_FG_D  : COLOR_CASH_FG));
        bar.add(makeLegendBadge("QRIS",  AppTheme.isDarkMode() ? COLOR_QRIS_FG_D  : COLOR_QRIS_FG));
        bar.add(makeLegendBadge("KARTU", AppTheme.isDarkMode() ? COLOR_KARTU_FG_D : COLOR_KARTU_FG));

        return bar;
    }

    private JLabel makeLegendBadge(String text, Color fg) {
        JLabel lbl = new JLabel("● " + text) {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.isDarkMode()
                    ? (text.equals("CASH")  ? COLOR_CASH_FG_D  :
                       text.equals("QRIS")  ? COLOR_QRIS_FG_D  : COLOR_KARTU_FG_D)
                    : (text.equals("CASH")  ? COLOR_CASH_FG  :
                       text.equals("QRIS")  ? COLOR_QRIS_FG  : COLOR_KARTU_FG));
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        return lbl;
    }

    private JPanel buildTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
                super.paintComponent(g);
            }
        };
        card.setOpaque(true);

        // ── 11 kolom: tambah "Metode Bayar" ──
        String[] cols = {"ID Jual","Tanggal","Customer","Barang",
                         "Qty","Harga Satuan","Subtotal","Uang Bayar","Kembalian","Kasir","Metode Bayar"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setForeground(AppTheme.TEXT_PRIMARY);
                setGridColor(AppTheme.isDarkMode()
                    ? new Color(48, 50, 58) : new Color(230, 238, 245));
                setSelectionBackground(AppTheme.PRIMARY_BG);
                setSelectionForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_DARK));
        table.getTableHeader().setReorderingAllowed(false);

        // ── lebar kolom: 11 kolom ──
        int[] widths = {65, 95, 130, 150, 45, 105, 105, 105, 105, 105, 100};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // ── Custom renderer: warna kolom + warna metode bayar ──
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);

                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, AppTheme.BORDER),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));

                if (!sel) {
                    setBackground(row % 2 == 0 ? AppTheme.BG_CARD : AppTheme.ROW_ALT);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));

                    if (col == 6 || col == 7) {
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setForeground(AppTheme.isDarkMode()
                            ? new Color(96, 165, 250) : PRIMARY_DARK);
                        setHorizontalAlignment(RIGHT);

                    } else if (col == 8) {
                        setFont(new Font("Segoe UI", Font.BOLD, 13));
                        setForeground(AppTheme.isDarkMode()
                            ? new Color(74, 222, 128) : new Color(16, 120, 60));
                        setHorizontalAlignment(RIGHT);

                    } else if (col == 5) {
                        setForeground(AppTheme.TEXT_PRIMARY);
                        setHorizontalAlignment(RIGHT);

                    } else if (col == 0 || col == 4) {
                        setForeground(AppTheme.TEXT_PRIMARY);
                        setHorizontalAlignment(CENTER);

                    } else if (col == 10) {
                        // ── Metode Bayar: warna berbeda per metode ──
                        String val = v != null ? v.toString() : "";
                        if (val.startsWith("CASH")) {
                            setForeground(AppTheme.isDarkMode() ? COLOR_CASH_FG_D : COLOR_CASH_FG);
                        } else if (val.startsWith("QRIS")) {
                            setForeground(AppTheme.isDarkMode() ? COLOR_QRIS_FG_D : COLOR_QRIS_FG);
                        } else if (val.startsWith("KARTU")) {
                            setForeground(AppTheme.isDarkMode() ? COLOR_KARTU_FG_D : COLOR_KARTU_FG);
                        } else {
                            setForeground(AppTheme.TEXT_PRIMARY);
                        }
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                        setHorizontalAlignment(CENTER);

                    } else {
                        setForeground(AppTheme.TEXT_PRIMARY);
                        setHorizontalAlignment(LEFT);
                    }
                } else {
                    setBackground(AppTheme.isDarkMode()
                        ? PRIMARY_DARK : new Color(210, 228, 255));
                    setForeground(AppTheme.isDarkMode()
                        ? Color.WHITE : PRIMARY_DARK);
                }
                return this;
            }
        });

        tableScrollPane = new JScrollPane(table) {
            @Override public void paintComponent(Graphics g) {
                getViewport().setBackground(AppTheme.BG_CARD);
                setBorder(null);
                super.paintComponent(g);
            }
        };
        tableScrollPane.getViewport().setBackground(AppTheme.BG_CARD);
        tableScrollPane.setBorder(null);
        card.add(tableScrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        footerPanel.setOpaque(true);

        JButton btnExcel = makeBtn("📊  Export Excel", new Color(33, 115, 70));
        JButton btnPDF   = makeBtn("📄  Export PDF",   new Color(183, 28, 28));
        btnExcel.addActionListener(e -> exportExcel());
        btnPDF.addActionListener(e -> exportPDF());
        footerPanel.add(btnExcel);
        footerPanel.add(btnPDF);
        return footerPanel;
    }

    // ════════════════════════════════════════════════════════
    //  FILTER & LOAD DATA
    // ════════════════════════════════════════════════════════

    private void filterTabel() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) {
            sorter.setRowFilter(null);
            lblJmlTransaksi.setText(tableModel.getRowCount() + " baris data ditemukan");
        } else {
            // col 2=Customer, 3=Barang, 9=Kasir, 10=Metode Bayar
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword, 2, 3, 9, 10));
            lblJmlTransaksi.setText(table.getRowCount() + " baris data ditemukan");
        }
    }

    private void filterHarian() {
        txtSearch.setText(""); sorter.setRowFilter(null);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        txtDari.setText(today); txtSampai.setText(today);
        loadLaporan(today, today);
    }

    private void filterMingguan() {
        txtSearch.setText(""); sorter.setRowFilter(null);
        LocalDate today = LocalDate.now();
        String dari = today.minusDays(today.getDayOfWeek().getValue() - 1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sampai = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        txtDari.setText(dari); txtSampai.setText(sampai);
        loadLaporan(dari, sampai);
    }

    private void filterBulanan() {
        txtSearch.setText(""); sorter.setRowFilter(null);
        LocalDate today = LocalDate.now();
        String dari = today.withDayOfMonth(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sampai = today.withDayOfMonth(today.lengthOfMonth())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        txtDari.setText(dari); txtSampai.setText(sampai);
        loadLaporan(dari, sampai);
    }

    private void filterTahunan() {
        txtSearch.setText(""); sorter.setRowFilter(null);
        LocalDate today = LocalDate.now();
        String dari = today.withDayOfYear(1)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sampai = today.withMonth(12).withDayOfMonth(31)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        txtDari.setText(dari); txtSampai.setText(sampai);
        loadLaporan(dari, sampai);
    }

    private void filterSemua() {
        txtSearch.setText(""); sorter.setRowFilter(null);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        txtDari.setText("2000-01-01"); txtSampai.setText(today);
        loadLaporan("2000-01-01", today);
    }

    private void filterCustom() {
        txtSearch.setText(""); sorter.setRowFilter(null);
        String dari   = txtDari.getText().trim();
        String sampai = txtSampai.getText().trim();
        if (!dari.matches("\\d{4}-\\d{2}-\\d{2}") || !sampai.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this,
                "Format tanggal harus: YYYY-MM-DD\nContoh: 2026-01-15",
                "Format Salah", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loadLaporan(dari, sampai);
    }

    private void loadLaporan(String dari, String sampai) {
        tableModel.setRowCount(0);
        totalPendapatan = 0;
        try {
            // ── FIX: tambah p.metode_bayar di SELECT ──
            String sql = "SELECT p.id_jual, p.tgl_transaksi, c.nama_customer, "
                       + "b.nama_barang, d.jumlah_beli, d.harga_satuan, d.subtotal, "
                       + "p.uang_bayar, p.kembalian, u.nama_lengkap, p.metode_bayar "
                       + "FROM tb_penjualan p "
                       + "LEFT JOIN tb_detail_penjualan d ON p.id_jual = d.id_jual "
                       + "LEFT JOIN tb_customer c ON p.id_customer = c.id_customer "
                       + "LEFT JOIN tb_barang b ON d.id_barang = b.id_barang "
                       + "LEFT JOIN tb_user u ON p.id_user = u.id_user "
                       + "WHERE p.tgl_transaksi BETWEEN ? AND ? "
                       + "ORDER BY p.tgl_transaksi DESC, p.id_jual";
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(sql);
            ps.setString(1, dari);
            ps.setString(2, sampai);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double subtotal  = rs.getDouble("subtotal");
                double uangBayar = rs.getDouble("uang_bayar");
                double kembalian = rs.getDouble("kembalian");
                totalPendapatan += subtotal;

                // ── FIX: tambah rs.getString("metode_bayar") di akhir row ──
                tableModel.addRow(new Object[]{
                    rs.getInt("id_jual"),
                    rs.getDate("tgl_transaksi"),
                    rs.getString("nama_customer"),
                    rs.getString("nama_barang"),
                    rs.getInt("jumlah_beli"),
                    String.format("Rp %,.0f", rs.getDouble("harga_satuan")),
                    String.format("Rp %,.0f", subtotal),
                    String.format("Rp %,.0f", uangBayar),
                    String.format("Rp %,.0f", kembalian),
                    rs.getString("nama_lengkap"),
                    rs.getString("metode_bayar")   // ← kolom ke-11
                });
            }
            lblTotal.setText("Total Pendapatan: " + String.format("Rp %,.0f", totalPendapatan));
            lblJmlTransaksi.setText(tableModel.getRowCount() + " baris data ditemukan");
            filterTabel();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error Load Data: " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════════════════
    //  EXPORT
    // ════════════════════════════════════════════════════════

    private void exportExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diexport!"); return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Laporan_Penjualan.xls"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xls)", "xls"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".xls")) path += ".xls";

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))) {
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            pw.println("<?mso-application progid=\"Excel.Sheet\"?>");
            pw.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\""
                + " xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");
            pw.println("<Styles>");
            pw.println("<Style ss:ID=\"judul\"><Font ss:Bold=\"1\" ss:Size=\"14\"/><Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"header\"><Font ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/><Interior ss:Color=\"#1976D2\" ss:Pattern=\"Solid\"/><Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"data\"><Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" /></Borders></Style>");
            pw.println("<Style ss:ID=\"dataAlt\"><Interior ss:Color=\"#E3F2FD\" ss:Pattern=\"Solid\"/><Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/></Borders></Style>");
            pw.println("<Style ss:ID=\"cash\"><Font ss:Bold=\"1\" ss:Color=\"#0E6432\"/><Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/></Borders><Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"qris\"><Font ss:Bold=\"1\" ss:Color=\"#195094\"/><Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/></Borders><Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"kartu\"><Font ss:Bold=\"1\" ss:Color=\"#8C5000\"/><Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/><Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/></Borders><Alignment ss:Horizontal=\"Center\"/></Style>");
            pw.println("<Style ss:ID=\"total\"><Font ss:Bold=\"1\"/><Interior ss:Color=\"#BBDEFB\" ss:Pattern=\"Solid\"/><Borders><Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\"/></Borders></Style>");
            pw.println("</Styles>");
            pw.println("<Worksheet ss:Name=\"Laporan Penjualan\">");
            pw.println("<Table>");

            // ── 11 kolom ──
            int[] colW = {50, 80, 120, 150, 40, 100, 100, 100, 100, 100, 90};
            for (int w : colW) pw.println("<Column ss:Width=\"" + w + "\"/>");

            pw.println("<Row><Cell ss:MergeAcross=\"10\" ss:StyleID=\"judul\"><Data ss:Type=\"String\">LAPORAN PENJUALAN - TOKO BERKAH JAYA</Data></Cell></Row>");
            pw.println("<Row><Cell ss:MergeAcross=\"10\" ss:StyleID=\"judul\"><Data ss:Type=\"String\">Periode: " + txtDari.getText() + " s/d " + txtSampai.getText() + "</Data></Cell></Row>");
            pw.println("<Row/>");

            String[] cols = {"ID Jual","Tanggal","Customer","Barang","Qty","Harga Satuan","Subtotal","Uang Bayar","Kembalian","Kasir","Metode Bayar"};
            pw.println("<Row>");
            for (String col : cols)
                pw.println("<Cell ss:StyleID=\"header\"><Data ss:Type=\"String\">" + escXml(col) + "</Data></Cell>");
            pw.println("</Row>");

            for (int r = 0; r < table.getRowCount(); r++) {
                String style = (r % 2 == 0) ? "data" : "dataAlt";
                pw.println("<Row>");
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    int modelRow = table.convertRowIndexToModel(r);
                    Object val = tableModel.getValueAt(modelRow, c);
                    String cellStyle = style;
                    // ── kolom Metode Bayar (index 10): pakai style warna ──
                    if (c == 10 && val != null) {
                        String m = val.toString();
                        if (m.startsWith("CASH"))  cellStyle = "cash";
                        else if (m.startsWith("QRIS"))  cellStyle = "qris";
                        else if (m.startsWith("KARTU")) cellStyle = "kartu";
                    }
                    pw.println("<Cell ss:StyleID=\"" + cellStyle + "\"><Data ss:Type=\"String\">"
                        + escXml(val != null ? val.toString() : "") + "</Data></Cell>");
                }
                pw.println("</Row>");
            }

            // ── Baris total: span 7 kolom, sisanya kosong ──
            pw.println("<Row>"
                + "<Cell ss:MergeAcross=\"5\" ss:StyleID=\"total\"><Data ss:Type=\"String\">TOTAL PENDAPATAN</Data></Cell>"
                + "<Cell ss:StyleID=\"total\"><Data ss:Type=\"String\">" + escXml(String.format("Rp %,.0f", totalPendapatan)) + "</Data></Cell>"
                + "<Cell ss:StyleID=\"total\"><Data ss:Type=\"String\"></Data></Cell>"
                + "<Cell ss:StyleID=\"total\"><Data ss:Type=\"String\"></Data></Cell>"
                + "<Cell ss:StyleID=\"total\"><Data ss:Type=\"String\"></Data></Cell>"
                + "<Cell ss:StyleID=\"total\"><Data ss:Type=\"String\"></Data></Cell>"
                + "</Row>");

            pw.println("</Table></Worksheet></Workbook>");
            JOptionPane.showMessageDialog(this, "✅ Excel berhasil disimpan!\n" + path, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().open(new File(path));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal export: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escXml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }

    private void exportPDF() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diexport!"); return;
        }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Laporan_Penjualan.pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files (*.pdf)", "pdf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".pdf")) path += ".pdf";

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            com.itextpdf.text.Font fJudul  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 16, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fSub    = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9);
            com.itextpdf.text.Font fHeader = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
            com.itextpdf.text.Font fData   = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
            com.itextpdf.text.Font fDataBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fTotal  = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD);

            // Warna font per metode
            com.itextpdf.text.BaseColor cashColor  = new com.itextpdf.text.BaseColor(14, 100, 50);
            com.itextpdf.text.BaseColor qrisColor  = new com.itextpdf.text.BaseColor(25, 80, 180);
            com.itextpdf.text.BaseColor kartuColor = new com.itextpdf.text.BaseColor(140, 80, 0);

            Paragraph pJudul = new Paragraph("LAPORAN PENJUALAN - TOKO BERKAH JAYA", fJudul);
            pJudul.setAlignment(Element.ALIGN_CENTER); pJudul.setSpacingAfter(4);
            doc.add(pJudul);

            Paragraph pPeriode = new Paragraph("Periode: " + txtDari.getText() + " s/d " + txtSampai.getText(), fSub);
            pPeriode.setAlignment(Element.ALIGN_CENTER);
            doc.add(pPeriode);

            if (!txtSearch.getText().trim().isEmpty()) {
                Paragraph pSearch = new Paragraph("Filter: \"" + txtSearch.getText().trim() + "\"  (" + table.getRowCount() + " baris)", fSub);
                pSearch.setAlignment(Element.ALIGN_CENTER);
                doc.add(pSearch);
            }

            Paragraph pCetak = new Paragraph("Dicetak: " + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")), fSub);
            pCetak.setAlignment(Element.ALIGN_CENTER); pCetak.setSpacingAfter(12);
            doc.add(pCetak);

            com.itextpdf.text.BaseColor blue        = new com.itextpdf.text.BaseColor(25, 118, 210);
            com.itextpdf.text.BaseColor altRow      = new com.itextpdf.text.BaseColor(227, 242, 253);
            com.itextpdf.text.BaseColor totalColor  = new com.itextpdf.text.BaseColor(187, 222, 251);
            com.itextpdf.text.BaseColor borderColor = new com.itextpdf.text.BaseColor(200, 215, 235);
            com.itextpdf.text.BaseColor greenColor  = new com.itextpdf.text.BaseColor(200, 230, 210);

            // ── 11 kolom ──
            float[] colWidths = {2.2f, 4.5f, 7f, 8f, 2.2f, 5.5f, 5.5f, 5.5f, 5.5f, 5.5f, 4.5f};
            PdfPTable tbl = new PdfPTable(colWidths);
            tbl.setWidthPercentage(100); tbl.setSpacingBefore(4);

            String[] cols = {"ID","Tanggal","Customer","Barang","Qty","Harga Satuan","Subtotal","Uang Bayar","Kembalian","Kasir","Metode"};
            for (String h : cols) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHeader));
                cell.setBackgroundColor(blue);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                cell.setBorderColor(com.itextpdf.text.BaseColor.WHITE);
                tbl.addCell(cell);
            }

            for (int r = 0; r < table.getRowCount(); r++) {
                int modelRow = table.convertRowIndexToModel(r);
                com.itextpdf.text.BaseColor rowBg = (r % 2 == 0)
                    ? com.itextpdf.text.BaseColor.WHITE : altRow;

                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Object val = tableModel.getValueAt(modelRow, c);
                    String valStr = val != null ? val.toString() : "";

                    PdfPCell cell;
                    if (c == 10) {
                        // ── Metode Bayar: font berwarna ──
                        com.itextpdf.text.Font fMetode = new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD);
                        if (valStr.startsWith("CASH"))       fMetode.setColor(cashColor);
                        else if (valStr.startsWith("QRIS"))  fMetode.setColor(qrisColor);
                        else if (valStr.startsWith("KARTU")) fMetode.setColor(kartuColor);
                        cell = new PdfPCell(new Phrase(valStr, fMetode));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    } else {
                        cell = new PdfPCell(new Phrase(valStr, fData));
                        if (c == 0 || c == 4)                           cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        else if (c == 5 || c == 6 || c == 7 || c == 8) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    }

                    cell.setBackgroundColor(c == 8 ? greenColor : rowBg);
                    cell.setPadding(5);
                    cell.setBorderColor(borderColor);
                    tbl.addCell(cell);
                }
            }

            // ── Baris total: span 7 kolom ──
            PdfPCell cellLabel = new PdfPCell(new Phrase("TOTAL PENDAPATAN", fTotal));
            cellLabel.setColspan(6);
            cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellLabel.setPadding(7); cellLabel.setBackgroundColor(totalColor);
            tbl.addCell(cellLabel);

            PdfPCell cellVal = new PdfPCell(new Phrase(String.format("Rp %,.0f", totalPendapatan), fTotal));
            cellVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellVal.setPadding(7); cellVal.setBackgroundColor(totalColor);
            tbl.addCell(cellVal);

            for (int i = 0; i < 4; i++) {
                PdfPCell cellEmpty = new PdfPCell(new Phrase(""));
                cellEmpty.setBackgroundColor(totalColor); cellEmpty.setPadding(7);
                tbl.addCell(cellEmpty);
            }

            doc.add(tbl);
            doc.close();

            JOptionPane.showMessageDialog(this, "✅ PDF berhasil disimpan!\n" + path, "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            Desktop.getDesktop().open(new File(path));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal export PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════

    private JTextField makeInputDate() {
        JTextField tf = new JTextField(10) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.INPUT_BG);
                setForeground(AppTheme.TEXT_PRIMARY);
                setCaretColor(AppTheme.TEXT_PRIMARY);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER, 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
                super.paintComponent(g);
            }
        };
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tf.setPreferredSize(new Dimension(115, 30));
        return tf;
    }

    private JButton makeFilterBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setPreferredSize(new Dimension(90, 30));
        return btn;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false); btn.setContentAreaFilled(false);
        btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }
}