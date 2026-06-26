package view;

import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class PanelDashboard extends JPanel {

    private JLabel lblPenjualanHariIni, lblTotalTransaksi, lblTotalCustomer, lblTotalBarang;
    private JTable tblTransaksiBaru, tblStokMenipis;
    private DefaultTableModel modelTransaksi, modelStok;
    private JPanel chartContainer;
    private JPanel bodyPanel;
    private JPanel metricPanel;
    private JPanel cardStok, cardTransaksi, cardChart;
    private boolean showHarian = true;
    private final String namaUser;
    private final String level;

    private JToggleButton btnDarkMode;

    public PanelDashboard(String namaUser, String level) {
        this.namaUser = namaUser;
        this.level    = level;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        add(buildTopbar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        loadData();
        AppTheme.registerComponent(this);
    }

    // ── Topbar ────────────────────────────────────────────────
    private JPanel buildTopbar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));
        bar.setPreferredSize(new Dimension(0, 64));

        // ── Kiri: judul + sambutan ──
        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel t = new JLabel("Dashboard");
        t.setFont(new Font("Segoe UI", Font.BOLD, 17));
        t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Selamat datang, " + namaUser + "  ·  " + level);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(new Color(187, 222, 251));
        left.add(t); left.add(s);

        // ── Kanan: jam + tombol dark mode ──
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        // ── FIX: clockPanel lebih lebar, alignment kanan, tinggi cukup ──
        JPanel clockPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        clockPanel.setOpaque(false);
        clockPanel.setPreferredSize(new Dimension(220, 44));

        JLabel lblTgl = new JLabel("", SwingConstants.RIGHT);
        lblTgl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTgl.setForeground(new Color(187, 222, 251));

        // ── FIX: jam pakai font lebih besar dan bold ──
        JLabel lblJam = new JLabel("", SwingConstants.RIGHT);
        lblJam.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblJam.setForeground(Color.WHITE);

        clockPanel.add(lblTgl);
        clockPanel.add(lblJam);

        // ── FIX: timer 500ms agar jam tidak terlambat update ──
        Timer clock = new Timer(500, e -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            lblTgl.setText(now.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", new Locale("id", "ID"))));
            lblJam.setText(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        clock.setInitialDelay(0);
        clock.start();

        // ── Tombol Dark Mode ──
        btnDarkMode = new JToggleButton("🌙 Dark") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected()
                    ? new Color(255, 213, 79)
                    : new Color(255, 255, 255, 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDarkMode.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDarkMode.setForeground(Color.WHITE);
        btnDarkMode.setSelected(AppTheme.isDarkMode());
        btnDarkMode.setText(AppTheme.isDarkMode() ? "☀️ Light" : "🌙 Dark");
        btnDarkMode.setFocusPainted(false);
        btnDarkMode.setBorderPainted(false);
        btnDarkMode.setContentAreaFilled(false);
        btnDarkMode.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDarkMode.setBorder(new EmptyBorder(6, 14, 6, 14));
        // Ganti blok addActionListener btnDarkMode yang lama dengan ini:

btnDarkMode.addActionListener(e -> {
    boolean dark = btnDarkMode.isSelected();
    AppTheme.setDarkMode(dark);
    btnDarkMode.setText(dark ? "☀️ Light" : "🌙 Dark");
    btnDarkMode.setForeground(dark ? new Color(30, 30, 30) : Color.WHITE);
    updateChart();

    // ── FIX DELAY: paksa seluruh window aktif repaint serentak ──
    Window w = SwingUtilities.getWindowAncestor(PanelDashboard.this);
    if (w != null) {
        w.revalidate();
        w.repaint();
    }
});

        // ── FIX: vertikal center right panel ──
        right.setAlignmentY(Component.CENTER_ALIGNMENT);
        right.add(clockPanel);
        right.add(btnDarkMode);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Body ──────────────────────────────────────────────────
    private JPanel buildBody() {
        bodyPanel = new JPanel(new BorderLayout(0, 14)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        bodyPanel.setBorder(new EmptyBorder(18, 18, 18, 18));

        metricPanel = buildMetricCards();
        bodyPanel.add(metricPanel, BorderLayout.NORTH);

        JPanel middle = new JPanel(new GridLayout(1, 2, 14, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        cardChart = buildChartCard();
        cardStok  = buildStokCard();
        middle.add(cardChart);
        middle.add(cardStok);

        JPanel center = new JPanel(new BorderLayout(0, 14)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        cardTransaksi = buildTransaksiCard();
        center.add(middle, BorderLayout.CENTER);
        center.add(cardTransaksi, BorderLayout.SOUTH);
        bodyPanel.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        JButton btnRefresh = new JButton("↻  Refresh Data") {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.PRIMARY);
                setForeground(Color.WHITE);
                super.paintComponent(g);
            }
        };
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setBorder(new EmptyBorder(8, 18, 8, 18));
        btnRefresh.addActionListener(e -> loadData());
        footer.add(btnRefresh);
        bodyPanel.add(footer, BorderLayout.SOUTH);

        return bodyPanel;
    }

    // ── Metric Cards ──────────────────────────────────────────
    private JPanel buildMetricCards() {
        JPanel p = new JPanel(new GridLayout(1, 4, 12, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        p.setPreferredSize(new Dimension(0, 95));

        lblPenjualanHariIni = new JLabel("Rp 0");
        lblTotalTransaksi   = new JLabel("0");
        lblTotalCustomer    = new JLabel("0");
        lblTotalBarang      = new JLabel("0");

        p.add(metricCard("💰  Penjualan Hari Ini", lblPenjualanHariIni, AppTheme.PRIMARY_BG,  AppTheme.PRIMARY,  "Total pendapatan"));
        p.add(metricCard("🧾  Transaksi Hari Ini", lblTotalTransaksi,   AppTheme.SUCCESS_BG, AppTheme.SUCCESS, "Jumlah transaksi"));
        p.add(metricCard("👥  Total Customer",      lblTotalCustomer,    AppTheme.AMBER_BG,  AppTheme.AMBER,   "Terdaftar"));
        p.add(metricCard("📦  Total Barang",        lblTotalBarang,      AppTheme.DANGER_BG, AppTheme.DANGER,  "Jenis produk"));
        return p;
    }

    private JPanel metricCard(String label, JLabel valLbl, Color bg, Color accent, String desc) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.isDarkMode() ? AppTheme.BG_CARD : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel lbl = new JLabel(label) {
            @Override public void paintComponent(Graphics g) {
                setForeground(accent);
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(accent);

        JLabel d = new JLabel(desc) {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_MUTED);
                super.paintComponent(g);
            }
        };
        d.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        card.add(lbl,    BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.add(d,      BorderLayout.SOUTH);
        return card;
    }

    // ── Chart Card ────────────────────────────────────────────
    private JPanel buildChartCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        card.setBorder(BorderFactory.createCompoundBorder(
            new AppTheme.RoundBorder(AppTheme.BORDER, 12, 1),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };

        JLabel title = new JLabel("📈  Grafik Penjualan") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };

        JButton btnH = makeToggleBtn("7 Hari",  true);
        JButton btnB = makeToggleBtn("Bulanan", false);

        btnH.addActionListener(e -> {
            showHarian = true;
            btnH.setBackground(AppTheme.PRIMARY); btnH.setForeground(Color.WHITE);
            btnB.setBackground(AppTheme.PRIMARY_BG); btnB.setForeground(AppTheme.PRIMARY);
            updateChart();
        });
        btnB.addActionListener(e -> {
            showHarian = false;
            btnB.setBackground(AppTheme.PRIMARY); btnB.setForeground(Color.WHITE);
            btnH.setBackground(AppTheme.PRIMARY_BG); btnH.setForeground(AppTheme.PRIMARY);
            updateChart();
        });

        togglePanel.add(btnH); togglePanel.add(btnB);
        hdr.add(title, BorderLayout.WEST);
        hdr.add(togglePanel, BorderLayout.EAST);
        card.add(hdr, BorderLayout.NORTH);

        chartContainer = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        card.add(chartContainer, BorderLayout.CENTER);
        return card;
    }

    private JButton makeToggleBtn(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setBackground(active ? AppTheme.PRIMARY : AppTheme.PRIMARY_BG);
        btn.setForeground(active ? Color.WHITE : AppTheme.PRIMARY);
        return btn;
    }

    private void updateChart() {
        chartContainer.removeAll();
        chartContainer.add(showHarian ? buildChartHarian() : buildChartBulanan(), BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private ChartPanel buildChartHarian() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "SELECT DATE(tgl_transaksi) as tgl, COALESCE(SUM(total_bayar),0) as total " +
                "FROM tb_penjualan WHERE tgl_transaksi >= DATE_SUB(?, INTERVAL 6 DAY) " +
                "GROUP BY DATE(tgl_transaksi) ORDER BY tgl ASC");
            ps.setString(1, today);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocalDate d = LocalDate.parse(rs.getString("tgl"));
                ds.addValue(rs.getDouble("total"), "Penjualan",
                    d.format(DateTimeFormatter.ofPattern("EEE dd", new Locale("id"))));
            }
        } catch (SQLException e) { }
        return makeBarChart(ds);
    }

    private ChartPanel buildChartBulanan() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        try {
            PreparedStatement ps = Koneksi.getConnection().prepareStatement(
                "SELECT DATE_FORMAT(tgl_transaksi,'%b %Y') as bln, " +
                "MONTH(tgl_transaksi) as m, YEAR(tgl_transaksi) as y, " +
                "COALESCE(SUM(total_bayar),0) as total " +
                "FROM tb_penjualan WHERE tgl_transaksi >= DATE_SUB(NOW(), INTERVAL 11 MONTH) " +
                "GROUP BY YEAR(tgl_transaksi), MONTH(tgl_transaksi) ORDER BY y ASC, m ASC");
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                ds.addValue(rs.getDouble("total"), "Penjualan", rs.getString("bln"));
        } catch (SQLException e) { }
        return makeLineChart(ds);
    }

    private ChartPanel makeBarChart(DefaultCategoryDataset ds) {
        JFreeChart chart = ChartFactory.createBarChart(null, null, null, ds, PlotOrientation.VERTICAL, false, true, false);
        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer r = (BarRenderer) plot.getRenderer();
        r.setSeriesPaint(0, AppTheme.PRIMARY);
        r.setMaximumBarWidth(0.4);
        r.setShadowVisible(false);
        r.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(new java.text.DecimalFormat("#,###"));
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(AppTheme.BG_CARD);
        cp.setBorder(BorderFactory.createEmptyBorder());
        return cp;
    }

    private ChartPanel makeLineChart(DefaultCategoryDataset ds) {
        JFreeChart chart = ChartFactory.createLineChart(null, null, null, ds, PlotOrientation.VERTICAL, false, true, false);
        styleChart(chart);
        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer r = (LineAndShapeRenderer) plot.getRenderer();
        r.setSeriesPaint(0, AppTheme.PRIMARY);
        r.setSeriesStroke(0, new BasicStroke(2.5f));
        r.setSeriesShapesVisible(0, true);
        r.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
        ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(new java.text.DecimalFormat("#,###"));
        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(AppTheme.BG_CARD);
        cp.setBorder(BorderFactory.createEmptyBorder());
        return cp;
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(AppTheme.BG_CARD);
        chart.setBorderVisible(false);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(AppTheme.BG_CARD);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(AppTheme.BORDER);
        plot.setRangeGridlineStroke(new BasicStroke(0.8f));
        plot.setDomainGridlinesVisible(false);
        CategoryAxis da = plot.getDomainAxis();
        da.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        da.setTickLabelPaint(AppTheme.TEXT_MUTED);
        da.setAxisLineVisible(false);
        da.setTickMarksVisible(false);
        NumberAxis ra = (NumberAxis) plot.getRangeAxis();
        ra.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        ra.setTickLabelPaint(AppTheme.TEXT_MUTED);
        ra.setAxisLineVisible(false);
        ra.setTickMarksVisible(false);
    }

    // ── Stok Card ─────────────────────────────────────────────
    private JPanel buildStokCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        card.setBorder(BorderFactory.createCompoundBorder(
            new AppTheme.RoundBorder(AppTheme.BORDER, 12, 1),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lbl = new JLabel("⚠️  Barang Stok Menipis (≤ 5)") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lbl, BorderLayout.NORTH);

        String[] cols = {"ID", "Nama Barang", "Stok", "Status"};
        modelStok = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStokMenipis = buatTabel(modelStok);
        // ── FIX: pakai makeScrollPane dari AppTheme ──
        card.add(AppTheme.makeScrollPane(tblStokMenipis), BorderLayout.CENTER);
        return card;
    }

    // ── Transaksi Card ────────────────────────────────────────
    private JPanel buildTransaksiCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        card.setPreferredSize(new Dimension(0, 200));
        card.setBorder(BorderFactory.createCompoundBorder(
            new AppTheme.RoundBorder(AppTheme.BORDER, 12, 1),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel lbl = new JLabel("🕐  Transaksi Terbaru Hari Ini") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(lbl, BorderLayout.NORTH);

        String[] cols = {"#", "Customer", "Total", "Kasir"};
        modelTransaksi = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblTransaksiBaru = buatTabel(modelTransaksi);
        // ── FIX: pakai makeScrollPane dari AppTheme ──
        card.add(AppTheme.makeScrollPane(tblTransaksiBaru), BorderLayout.CENTER);
        return card;
    }

    // ── Tabel helper ──────────────────────────────────────────
    private JTable buatTabel(DefaultTableModel model) {
        JTable tbl = new JTable(model);
        // ── FIX: pakai styleTable dari AppTheme ──
        AppTheme.styleTable(tbl);
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 34));
        return tbl;
    }

    // ── Load Data ─────────────────────────────────────────────
    public void loadData() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        try {
            Connection conn = Koneksi.getConnection();

            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT COALESCE(SUM(total_bayar),0) as total, COUNT(*) as jml " +
                "FROM tb_penjualan WHERE tgl_transaksi = ?");
            ps1.setString(1, today);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                lblPenjualanHariIni.setText(String.format("Rp %,.0f", rs1.getDouble("total")));
                lblTotalTransaksi.setText(String.valueOf(rs1.getInt("jml")));
            }

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) as jml FROM tb_customer");
            if (rs2.next()) lblTotalCustomer.setText(String.valueOf(rs2.getInt("jml")));

            ResultSet rs3 = conn.createStatement().executeQuery("SELECT COUNT(*) as jml FROM tb_barang");
            if (rs3.next()) lblTotalBarang.setText(String.valueOf(rs3.getInt("jml")));

            modelTransaksi.setRowCount(0);
            PreparedStatement ps4 = conn.prepareStatement(
                "SELECT p.id_jual, c.nama_customer, p.total_bayar, u.nama_lengkap " +
                "FROM tb_penjualan p " +
                "LEFT JOIN tb_customer c ON p.id_customer=c.id_customer " +
                "LEFT JOIN tb_user u ON p.id_user=u.id_user " +
                "WHERE p.tgl_transaksi=? ORDER BY p.id_jual DESC LIMIT 10");
            ps4.setString(1, today);
            ResultSet rs4 = ps4.executeQuery();
            int no = 1;
            while (rs4.next()) {
                modelTransaksi.addRow(new Object[]{
                    no++, rs4.getString("nama_customer"),
                    String.format("Rp %,.0f", rs4.getDouble("total_bayar")),
                    rs4.getString("nama_lengkap")
                });
            }

            modelStok.setRowCount(0);
            ResultSet rs5 = conn.createStatement().executeQuery(
                "SELECT id_barang, nama_barang, stok FROM tb_barang WHERE stok <= 5 ORDER BY stok ASC");
            while (rs5.next()) {
                int stok = rs5.getInt("stok");
                modelStok.addRow(new Object[]{
                    rs5.getString("id_barang"), rs5.getString("nama_barang"),
                    stok, stok == 0 ? "HABIS" : "MENIPIS"
                });
            }

            // ── FIX: renderer stok — bg/fg pakai AppTheme, fallback row alt benar ──
            tblStokMenipis.setDefaultRenderer(Object.class,
                new javax.swing.table.DefaultTableCellRenderer() {
                    @Override public Component getTableCellRendererComponent(
                            JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                        super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                        setBorder(new EmptyBorder(0, 10, 0, 10));
                        if (!sel) {
                            int stok = 0;
                            try { stok = Integer.parseInt(modelStok.getValueAt(row, 2).toString()); } catch (Exception ignored) {}
                            if (stok == 0) {
                                setBackground(AppTheme.STOK_LOW_BG);
                                setForeground(AppTheme.STOK_LOW_FG);
                            } else {
                                setBackground(AppTheme.isDarkMode()
                                    ? new Color(60, 40, 15) : new Color(255, 247, 237));
                                setForeground(AppTheme.isDarkMode()
                                    ? new Color(255, 190, 90) : new Color(146, 64, 14));
                            }
                            setFont(col == 3 ? AppTheme.FONT_BOLD : AppTheme.FONT_BODY);
                        } else {
                            setBackground(AppTheme.PRIMARY_BG);
                            setForeground(AppTheme.TEXT_PRIMARY);
                        }
                        return this;
                    }
                });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error load data: " + e.getMessage());
        }
        updateChart();
    }
}