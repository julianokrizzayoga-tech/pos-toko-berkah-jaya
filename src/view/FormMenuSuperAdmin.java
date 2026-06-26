package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class FormMenuSuperAdmin extends JFrame {

    private int idUser;
    private String namaUser;

    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel mainAreaRef;
    private JButton btnToggle;
    private boolean sidebarVisible = false;

    private static final int SIDEBAR_W       = 220;
    private static final Color C_SIDEBAR_BG  = new Color(15, 23, 42);
    private static final Color C_SIDEBAR_ITEM= new Color(30, 41, 63);
    private static final Color C_SIDEBAR_ACT = new Color(25, 118, 210);
    private static final Color C_SIDEBAR_TEXT= new Color(203, 213, 225);
    private static final Color C_SIDEBAR_MUT = new Color(100, 116, 139);
    private static final Color C_TOPBAR      = new Color(25, 118, 210);
    private static final Color C_BG          = new Color(245, 247, 250);

    private JButton activeBtn = null;

    public FormMenuSuperAdmin(int idUser, String namaUser) {
        this.idUser   = idUser;
        this.namaUser = namaUser;
        initUI();
    }

    private void initUI() {
        setTitle("Toko Berkah Jaya — SuperAdmin");
        setSize(1200, 750);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        // TOPBAR
        JPanel topbar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(C_TOPBAR);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topbar.setOpaque(false);
        topbar.setPreferredSize(new Dimension(0, 52));
        topbar.setBorder(new EmptyBorder(0, 12, 0, 20));

        btnToggle = new JButton("☰");
        btnToggle.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setBackground(C_TOPBAR);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setPreferredSize(new Dimension(44, 44));
        btnToggle.addActionListener(e -> toggleSidebar());

        JLabel lblApp = new JLabel("Toko Berkah Jaya");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblApp.setForeground(Color.WHITE);
        lblApp.setBorder(new EmptyBorder(0, 8, 0, 0));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topLeft.setOpaque(false);
        topLeft.add(btnToggle);
        topLeft.add(lblApp);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        topRight.setOpaque(false);

        JLabel lblRole = new JLabel("SuperAdmin");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRole.setForeground(new Color(187, 222, 251));

        JLabel lblUser = new JLabel("  " + namaUser);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(6, 14, 6, 14));
        btnLogout.addActionListener(e -> {
            new FormLogin().setVisible(true);
            dispose();
        });

        topRight.add(lblRole);
        topRight.add(lblUser);
        topRight.add(btnLogout);
        topbar.add(topLeft, BorderLayout.WEST);
        topbar.add(topRight, BorderLayout.EAST);
        add(topbar, BorderLayout.NORTH);

        // MAIN AREA
        JPanel mainArea = new JPanel(new BorderLayout(0, 0));
        mainArea.setBackground(C_BG);
        this.mainAreaRef = mainArea;

        // SIDEBAR
        sidebarPanel = buildSidebar();

        // CONTENT
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(C_BG);
        mainArea.add(contentPanel, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);

        // Tampilkan dashboard awal
        showPanel("dashboard");
    }

    // ── SIDEBAR BUILD ────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(C_SIDEBAR_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setOpaque(false);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        // Profile box
        JPanel profileBox = new JPanel();
        profileBox.setOpaque(false);
        profileBox.setLayout(new BoxLayout(profileBox, BoxLayout.Y_AXIS));
        profileBox.setBorder(new EmptyBorder(20, 16, 16, 16));
        profileBox.setMaximumSize(new Dimension(SIDEBAR_W, 90));

        JPanel avatar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_SIDEBAR_ACT);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                String init = namaUser.length() > 0
                    ? String.valueOf(namaUser.charAt(0)).toUpperCase() : "A";
                g2.drawString(init,
                    (40 - fm.stringWidth(init)) / 2,
                    (40 - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setMaximumSize(new Dimension(40, 40));
        avatar.setPreferredSize(new Dimension(40, 40));

        JLabel lblNama = new JLabel(namaUser);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNama.setForeground(Color.WHITE);
        lblNama.setBorder(new EmptyBorder(8, 0, 2, 0));

        JLabel lblLvl = new JLabel("SuperAdmin");
        lblLvl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLvl.setForeground(C_SIDEBAR_MUT);

        profileBox.add(avatar);
        profileBox.add(lblNama);
        profileBox.add(lblLvl);
        sidebar.add(profileBox);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(30, 41, 63));
        sep.setMaximumSize(new Dimension(SIDEBAR_W, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        // Menu buttons
        String[][] menus = {
            {"Dashboard",    "dashboard"},
            {"Data Barang",  "barang"},
            {"Data Customer","customer"},
            {"Kategori",     "kategori"},
            {"Transaksi",    "transaksi"},
            {"Laporan",      "laporan"},
            {"Kelola Stok",  "stok"},
            {"Kelola User",  "user"},
        };

        for (String[] m : menus) {
            JButton btn = makeSidebarBtn(m[0], m[1]);
            if (m[1].equals("dashboard")) {
                activeBtn = btn;
                btn.setBackground(C_SIDEBAR_ACT);
                for (Component c : btn.getComponents())
                    if (c instanceof JLabel) ((JLabel)c).setForeground(Color.WHITE);
            }
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton makeSidebarBtn(String label, String key) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getBackground().equals(C_SIDEBAR_ACT)) {
                    g2.setColor(C_SIDEBAR_ACT);
                    g2.fill(new RoundRectangle2D.Float(8, 4, getWidth()-16, getHeight()-8, 10, 10));
                } else if (getModel().isRollover()) {
                    g2.setColor(C_SIDEBAR_ITEM);
                    g2.fill(new RoundRectangle2D.Float(8, 4, getWidth()-16, getHeight()-8, 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 0));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(SIDEBAR_W, 44));
        btn.setPreferredSize(new Dimension(SIDEBAR_W, 44));
        btn.setBackground(C_SIDEBAR_BG);

        JLabel txtLbl = new JLabel(label);
        txtLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLbl.setForeground(C_SIDEBAR_TEXT);
        btn.add(txtLbl);

        btn.addActionListener(e -> {
            setActiveBtn(btn);
            showPanel(key);
            if (sidebarVisible) toggleSidebar();
        });

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.repaint(); }
        });

        return btn;
    }

    private void setActiveBtn(JButton btn) {
        if (activeBtn != null) {
            activeBtn.setBackground(C_SIDEBAR_BG);
            for (Component c : activeBtn.getComponents())
                if (c instanceof JLabel) ((JLabel)c).setForeground(C_SIDEBAR_TEXT);
            activeBtn.repaint();
        }
        activeBtn = btn;
        btn.setBackground(C_SIDEBAR_ACT);
        for (Component c : btn.getComponents())
            if (c instanceof JLabel) ((JLabel)c).setForeground(Color.WHITE);
        btn.repaint();
    }

    // ── TOGGLE SIDEBAR ───────────────────────────────────────
    private void toggleSidebar() {
        if (sidebarVisible) {
            hideSidebar();
        } else {
            showSidebar();
        }
    }

    private void showSidebar() {
        sidebarVisible = true;
        btnToggle.setText("✕");
        mainAreaRef.add(sidebarPanel, BorderLayout.WEST);
        mainAreaRef.revalidate();

        Timer t = new Timer(8, null);
        final int[] w = {0};
        t.addActionListener(e -> {
            w[0] = Math.min(w[0] + 22, SIDEBAR_W);
            sidebarPanel.setPreferredSize(new Dimension(w[0], 0));
            mainAreaRef.revalidate();
            if (w[0] >= SIDEBAR_W) ((Timer)e.getSource()).stop();
        });
        t.start();
    }

    private void hideSidebar() {
        Timer t = new Timer(8, null);
        final int[] w = {SIDEBAR_W};
        t.addActionListener(e -> {
            w[0] = Math.max(w[0] - 22, 0);
            sidebarPanel.setPreferredSize(new Dimension(w[0], 0));
            mainAreaRef.revalidate();
            if (w[0] <= 0) {
                ((Timer)e.getSource()).stop();
                mainAreaRef.remove(sidebarPanel);
                mainAreaRef.revalidate();
                mainAreaRef.repaint();
                sidebarVisible = false;
                btnToggle.setText("☰");
            }
        });
        t.start();
    }

    // ── SHOW PANEL ───────────────────────────────────────────
    private void showPanel(String key) {
        contentPanel.removeAll();

        if (key.equals("dashboard")) {
            contentPanel.add(new PanelDashboard(namaUser, "SuperAdmin"), BorderLayout.CENTER);
        } else if (key.equals("barang")) {
            contentPanel.add(new FormBarang(), BorderLayout.CENTER);
        } else if (key.equals("customer")) {
            contentPanel.add(new FormCustomer(), BorderLayout.CENTER);
        } else if (key.equals("kategori")) {
            contentPanel.add(new FormKategori(), BorderLayout.CENTER);
        } else if (key.equals("transaksi")) {
           contentPanel.add(new FormTransaksi(idUser), BorderLayout.CENTER);
        } else if (key.equals("laporan")) {
            contentPanel.add(new FormLaporan(), BorderLayout.CENTER);
        } else if (key.equals("stok")) {
            contentPanel.add(new FormStok(), BorderLayout.CENTER);
        } else if (key.equals("user")) {
            contentPanel.add(new FormUser(), BorderLayout.CENTER);
        } else {
            contentPanel.add(new PanelDashboard(namaUser, "SuperAdmin"), BorderLayout.CENTER);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}