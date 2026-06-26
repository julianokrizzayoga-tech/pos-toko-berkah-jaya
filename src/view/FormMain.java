package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class FormMain extends JFrame {

    private static final int SIDEBAR_W = 220;

    private final int idUser;
    private final String namaUser;
    private final String level;

    private JPanel contentPanel;
    private JPanel sidebarPanel;
    private JPanel mainArea;
    private JButton activeBtn = null;
    private JButton btnToggle;
    private boolean sidebarVisible = true;

    private static final String[][] MENU_SUPERADMIN = {
        {"Dashboard",     "🏠", "dashboard"},
        {"Data Barang",   "📦", "barang"},
        {"Data Customer", "👥", "customer"},
        {"Kategori",      "🏷", "kategori"},
        {"Transaksi",     "💰", "transaksi"},
        {"Laporan",       "📊", "laporan"},
        {"Kelola Stok",   "📋", "stok"},
        {"Kelola User",   "👤", "user"},
    };
    private static final String[][] MENU_ADMIN = {
        {"Dashboard",     "🏠", "dashboard"},
        {"Data Barang",   "📦", "barang"},
        {"Data Customer", "👥", "customer"},
        {"Kategori",      "🏷", "kategori"},
        {"Transaksi",     "💰", "transaksi"},
        {"Laporan",       "📊", "laporan"},
        {"Kelola Stok",   "📋", "stok"},
    };
    private static final String[][] MENU_KASIR = {
        {"Dashboard",     "🏠", "dashboard"},
        {"Data Customer", "👥", "customer"},
        {"Transaksi",     "💰", "transaksi"},
    };
    private static final String[][] MENU_GUDANG = {
        {"Dashboard",     "🏠", "dashboard"},
        {"Data Barang",   "📦", "barang"},
        {"Kategori",      "🏷", "kategori"},
        {"Kelola Stok",   "📋", "stok"},
    };

    public FormMain(int idUser, String namaUser, String level) {
        this.idUser   = idUser;
        this.namaUser = namaUser;
        this.level    = level;
        initUI();
        showPanel("dashboard");
    }

    private void initUI() {
        setTitle("Toko Berkah Jaya — " + level);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // ── TOPBAR ──────────────────────────────────────────
        JPanel topbar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(AppTheme.PRIMARY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topbar.setOpaque(false);
        topbar.setPreferredSize(new Dimension(0, 70));
        topbar.setBorder(new EmptyBorder(0, 10, 0, 20));

        btnToggle = new JButton("≡");
        btnToggle.setFont(new Font("Dialog", Font.BOLD, 30));
        btnToggle.setForeground(Color.WHITE);
        btnToggle.setOpaque(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setFocusPainted(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setPreferredSize(new Dimension(44, 44));
        btnToggle.addActionListener(e -> toggleSidebar());

        JLabel lblApp = new JLabel("Toko Berkah Jaya");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblApp.setForeground(Color.WHITE);
        lblApp.setBorder(new EmptyBorder(0, 6, 0, 0));

        JPanel topLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topLeft.setOpaque(false);
        topLeft.add(btnToggle);
        topLeft.add(lblApp);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        topRight.setOpaque(false);

        JLabel lblLevel = new JLabel(level);
        lblLevel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblLevel.setForeground(new Color(187, 222, 251));

        JLabel lblUser = new JLabel("👤  " + namaUser);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(Color.WHITE);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnLogout.setBackground(new Color(239, 68, 68));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setBorder(new EmptyBorder(5, 14, 5, 14));
        btnLogout.addActionListener(e -> doLogout());

        topRight.add(lblLevel);
        topRight.add(lblUser);
        topRight.add(btnLogout);
        topbar.add(topLeft, BorderLayout.WEST);
        topbar.add(topRight, BorderLayout.EAST);
        add(topbar, BorderLayout.NORTH);

        // ── MAIN AREA ───────────────────────────────────────
        mainArea = new JPanel(new BorderLayout(0, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };

        sidebarPanel = buildSidebar();
        mainArea.add(sidebarPanel, BorderLayout.WEST);

        contentPanel = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_PAGE);
                super.paintComponent(g);
            }
        };
        mainArea.add(contentPanel, BorderLayout.CENTER);

        add(mainArea, BorderLayout.CENTER);

        AppTheme.registerComponent(mainArea);
    }

    private void doLogout() {
        int opt = JOptionPane.showConfirmDialog(this,
            "Yakin ingin logout?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            dispose();
            new FormLogin().setVisible(true);
        }
    }

    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebarPanel.setVisible(sidebarVisible);
        mainArea.revalidate();
        mainArea.repaint();
    }

    // ── SIDEBAR ─────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, AppTheme.BORDER));
                super.paintComponent(g);
            }
        };
        sidebar.setOpaque(true);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        // ── Brand ────────────────────────────────────────────
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        brandPanel.setOpaque(true);
        brandPanel.setBorder(new EmptyBorder(18, 8, 18, 8));

        JPanel iconBox = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(36, 36));
        JLabel iconLbl = new JLabel("🏪");
        iconLbl.setFont(new Font("Dialog", Font.PLAIN, 18));
        iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        iconBox.add(iconLbl, BorderLayout.CENTER);

        JPanel nameBlock = new JPanel(new GridLayout(2, 1, 0, 1)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        nameBlock.setOpaque(true);

        JLabel lblName = new JLabel("Berkah Jaya") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblRole = new JLabel(level) {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        nameBlock.add(lblName);
        nameBlock.add(lblRole);
        brandPanel.add(iconBox);
        brandPanel.add(nameBlock);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // ── Menu ─────────────────────────────────────────────
        JPanel menuPanel = new JPanel() {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        menuPanel.setOpaque(true);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(new EmptyBorder(4, 10, 4, 10));

        for (String[] item : getMenuByRole()) {
            menuPanel.add(makeNavBtn(item[0], item[1], item[2]));
            menuPanel.add(Box.createVerticalStrut(2));
        }
        sidebar.add(menuPanel, BorderLayout.CENTER);

        // ── Bottom logout ────────────────────────────────────
        JPanel bottomPanel = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER),
                    new EmptyBorder(10, 10, 16, 10)
                ));
                super.paintComponent(g);
            }
        };
        bottomPanel.setOpaque(true);

        JButton btnOut = new JButton() {
            @Override public void paintComponent(Graphics g) {
                if (!getModel().isRollover()) setBackground(AppTheme.BG_CARD);
                super.paintComponent(g);
            }
        };
        btnOut.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnOut.setBackground(AppTheme.BG_CARD);
        btnOut.setOpaque(true);
        btnOut.setBorderPainted(false);
        btnOut.setFocusPainted(false);
        btnOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnOut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnOut.setPreferredSize(new Dimension(200, 40));

        JLabel iOut = new JLabel("⎋") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.DANGER); super.paintComponent(g);
            }
        };
        iOut.setFont(new Font("Dialog", Font.PLAIN, 16));

        JLabel tOut = new JLabel("Logout") {
            @Override public void paintComponent(Graphics g) {
                setForeground(AppTheme.DANGER); super.paintComponent(g);
            }
        };
        tOut.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnOut.add(iOut);
        btnOut.add(tOut);
        btnOut.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnOut.setBackground(AppTheme.isDarkMode()
                    ? new Color(80, 20, 20) : new Color(254, 242, 242));
            }
            public void mouseExited(MouseEvent e) { btnOut.setBackground(AppTheme.BG_CARD); }
        });
        btnOut.addActionListener(e -> doLogout());

        bottomPanel.add(btnOut, BorderLayout.CENTER);
        sidebar.add(bottomPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    // ── Nav Button ───────────────────────────────────────────
    private JButton makeNavBtn(String label, String icon, String key) {
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Dialog", Font.PLAIN, 16));

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // FIX: paintComponent mengatur background DAN foreground setiap repaint
        JButton btn = new JButton() {
            @Override public void paintComponent(Graphics g) {
                if (this == activeBtn) {
                    setBackground(AppTheme.PRIMARY_BG);
                    iconLbl.setForeground(AppTheme.PRIMARY);
                    textLbl.setForeground(AppTheme.PRIMARY);
                } else if (!getModel().isRollover()) {
                    setBackground(AppTheme.BG_CARD);
                    iconLbl.setForeground(AppTheme.TEXT_SECONDARY);
                    textLbl.setForeground(AppTheme.TEXT_SECONDARY);
                }
                super.paintComponent(g);
            }
        };
        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btn.setOpaque(true);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.setBorder(new EmptyBorder(0, 4, 0, 4));

        iconLbl.setForeground(AppTheme.TEXT_SECONDARY);
        textLbl.setForeground(AppTheme.TEXT_SECONDARY);
        btn.add(iconLbl);
        btn.add(textLbl);

        if (key.equals("dashboard")) {
            activeBtn = btn;
            btn.setBackground(AppTheme.PRIMARY_BG);
            iconLbl.setForeground(AppTheme.PRIMARY);
            textLbl.setForeground(AppTheme.PRIMARY);
            textLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        btn.addActionListener(e -> {
            setActiveVisual(btn, iconLbl, textLbl);
            showPanel(key);
        });

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != activeBtn)
                    btn.setBackground(AppTheme.isDarkMode()
                        ? new Color(40, 42, 54) : new Color(248, 250, 252));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != activeBtn) btn.setBackground(AppTheme.BG_CARD);
            }
        });

        return btn;
    }

    private void setActiveVisual(JButton btn, JLabel iconLbl, JLabel textLbl) {
        if (sidebarPanel != null) resetAllNavBtns(sidebarPanel);
        activeBtn = btn;
        btn.setBackground(AppTheme.PRIMARY_BG);
        iconLbl.setForeground(AppTheme.PRIMARY);
        textLbl.setForeground(AppTheme.PRIMARY);
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void resetAllNavBtns(JPanel panel) {
        for (Component c : panel.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                Component[] comps = b.getComponents();
                if (comps.length >= 2 && b != activeBtn
                        && comps[0] instanceof JLabel && comps[1] instanceof JLabel) {
                    JLabel iLbl = (JLabel) comps[0];
                    JLabel tLbl = (JLabel) comps[1];
                    if (!tLbl.getText().equals("Logout")) {
                        b.setBackground(AppTheme.BG_CARD);
                        iLbl.setForeground(AppTheme.TEXT_SECONDARY);
                        tLbl.setForeground(AppTheme.TEXT_SECONDARY);
                        tLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    }
                }
            } else if (c instanceof JPanel) {
                resetAllNavBtns((JPanel) c);
            }
        }
    }

    private void showPanel(String key) {
        contentPanel.removeAll();
        JPanel panel;
        switch (key) {
            case "dashboard": panel = new PanelDashboard(namaUser, level); break;
            case "barang":    panel = new FormBarang();                    break;
            case "stok":      panel = new FormStok();                      break;
            case "customer":  panel = new FormCustomer();                  break;
            case "kategori":  panel = new FormKategori();                  break;
            case "transaksi": panel = new FormTransaksi(idUser);           break;
            case "laporan":   panel = new FormLaporan();                   break;
            // ── FIX: kirim level user yang login ke FormUser,
            // agar SuperAdmin bisa melihat kolom Password & 👁 ──
            case "user":      panel = new FormUser(level);                 break;
            default:          panel = new PanelDashboard(namaUser, level);
        }
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private String[][] getMenuByRole() {
        switch (level) {
            case "SuperAdmin": return MENU_SUPERADMIN;
            case "Admin":      return MENU_ADMIN;
            case "Kasir":      return MENU_KASIR;
            case "Gudang":     return MENU_GUDANG;
            default:           return MENU_KASIR;
        }
    }
}