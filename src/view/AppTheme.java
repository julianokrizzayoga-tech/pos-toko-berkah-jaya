package view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class AppTheme {

    private static boolean darkMode = false;

    private static final List<Component> registeredComponents = new ArrayList<>();

    // ── Warna Utama (tidak berubah saat dark mode) ───────────
    public static Color PRIMARY      = new Color(25, 118, 210);
    public static Color PRIMARY_DARK = new Color(21, 101, 192);
    public static Color PRIMARY_BG   = new Color(227, 242, 253);
    public static Color SUCCESS      = new Color(46, 125, 50);
    public static Color SUCCESS_BG   = new Color(232, 245, 233);
    public static Color DANGER       = new Color(198, 40, 40);
    public static Color DANGER_BG    = new Color(255, 235, 238);
    public static Color AMBER        = new Color(245, 158, 11);
    public static Color AMBER_BG     = new Color(255, 247, 237);
    public static Color NEUTRAL      = new Color(100, 116, 139);

    // ── Warna Adaptif Light/Dark ─────────────────────────────
    public static Color BG_PAGE        = new Color(245, 247, 250);
    public static Color BG_CARD        = Color.WHITE;
    public static Color BORDER         = new Color(220, 230, 240);
    public static Color TEXT_PRIMARY   = new Color(15, 30, 50);
    public static Color TEXT_SECONDARY = new Color(60, 80, 100);
    public static Color TEXT_MUTED     = new Color(120, 140, 160);

    // ── Warna Komponen Spesifik ──────────────────────────────
    public static Color INPUT_BG          = Color.WHITE;
    public static Color INPUT_BG_DISABLED = new Color(248, 250, 252);
    public static Color INPUT_FG_DISABLED = new Color(80, 100, 130);
    public static Color TABLE_HEADER_BG     = new Color(239, 246, 255);
    public static Color TABLE_HEADER_BORDER = new Color(147, 197, 253);
    public static Color STOK_LOW_BG       = new Color(254, 226, 226);
    public static Color STOK_LOW_FG       = new Color(153, 27, 27);
    public static Color ROW_ALT           = new Color(245, 249, 253);
    public static Color FILTER_BAR_BG     = Color.WHITE;
    public static Color SUMMARY_CARD_BG   = Color.WHITE;
    public static Color SEARCH_BORDER     = new Color(25, 118, 210);

    // ── Font ─────────────────────────────────────────────────
    public static final Font FONT_BOLD  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    public static boolean isDarkMode() { return darkMode; }

    // ── Toggle Tema Global ───────────────────────────────────
    public static void setDarkMode(boolean enable) {
        darkMode = enable;
        if (darkMode) {
            BG_PAGE        = new Color(18, 18, 18);
            BG_CARD        = new Color(28, 28, 32);
            BORDER         = new Color(50, 52, 60);
            TEXT_PRIMARY   = new Color(220, 222, 228);
            TEXT_SECONDARY = new Color(150, 165, 185);
            TEXT_MUTED     = new Color(95, 108, 125);
            PRIMARY_BG     = new Color(25, 45, 75);

            INPUT_BG          = new Color(38, 38, 44);
            INPUT_BG_DISABLED = new Color(32, 34, 40);
            INPUT_FG_DISABLED = new Color(110, 125, 148);

            TABLE_HEADER_BG     = new Color(22, 36, 56);
            TABLE_HEADER_BORDER = new Color(40, 65, 105);
            STOK_LOW_BG         = new Color(75, 25, 25);
            STOK_LOW_FG         = new Color(255, 145, 145);
            ROW_ALT             = new Color(22, 24, 30);
            FILTER_BAR_BG       = new Color(28, 28, 32);
            SUMMARY_CARD_BG     = new Color(28, 28, 32);
            SEARCH_BORDER       = new Color(60, 100, 160);
        } else {
            BG_PAGE        = new Color(245, 247, 250);
            BG_CARD        = Color.WHITE;
            BORDER         = new Color(220, 230, 240);
            TEXT_PRIMARY   = new Color(15, 30, 50);
            TEXT_SECONDARY = new Color(60, 80, 100);
            TEXT_MUTED     = new Color(120, 140, 160);
            PRIMARY_BG     = new Color(227, 242, 253);

            INPUT_BG          = Color.WHITE;
            INPUT_BG_DISABLED = new Color(248, 250, 252);
            INPUT_FG_DISABLED = new Color(80, 100, 130);

            TABLE_HEADER_BG     = new Color(239, 246, 255);
            TABLE_HEADER_BORDER = new Color(147, 197, 253);
            STOK_LOW_BG         = new Color(254, 226, 226);
            STOK_LOW_FG         = new Color(153, 27, 27);
            ROW_ALT             = new Color(245, 249, 253);
            FILTER_BAR_BG       = Color.WHITE;
            SUMMARY_CARD_BG     = Color.WHITE;
            SEARCH_BORDER       = new Color(25, 118, 210);
        }
        refreshAllNow();
    }

    // ── Registrasi Komponen ──────────────────────────────────
    public static void registerComponent(Component c) {
        if (!registeredComponents.contains(c))
            registeredComponents.add(c);
    }

    public static void unregisterComponent(Component c) {
        registeredComponents.remove(c);
    }

    /**
     * Repaint semua registered component DAN semua Window aktif
     * dalam satu siklus EDT.
     */
    private static void refreshAllNow() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(AppTheme::refreshAllNow);
            return;
        }

        // Repaint rekursif semua komponen yang terdaftar
        for (Component root : new ArrayList<>(registeredComponents)) {
            if (root != null) {
                repaintTree(root);
                if (root instanceof JComponent) {
                    ((JComponent) root).revalidate();
                }
                root.repaint();
            }
        }

        // Paksa semua JFrame/JWindow yang sedang tampil repaint sekaligus
        for (Window w : Window.getWindows()) {
            if (w.isShowing()) {
                w.revalidate();
                w.repaint();
            }
        }
    }

    /**
     * FIX UTAMA: Walk SELURUH component tree tanpa stop di registered component.
     * Dulu ada early-return "if registered, stop" yang menyebabkan
     * child component dari panel yang ter-register tidak ikut di-repaint.
     */
    private static void repaintTree(Component c) {
        if (c == null) return;

        // ── Update eksplisit komponen Swing standard ──
        if (c instanceof JTable) {
            JTable tbl = (JTable) c;
            tbl.setBackground(BG_CARD);
            tbl.setForeground(TEXT_PRIMARY);
            tbl.setGridColor(darkMode ? new Color(48, 50, 58) : new Color(230, 238, 245));
            tbl.setSelectionBackground(PRIMARY_BG);
            tbl.setSelectionForeground(TEXT_PRIMARY);
            if (tbl.getTableHeader() != null) {
                tbl.getTableHeader().setBackground(PRIMARY);
                tbl.getTableHeader().setForeground(Color.WHITE);
                tbl.getTableHeader().repaint();
            }
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.getViewport().setBackground(BG_CARD);
            sp.setBorder(new LineBorder(BORDER, 1, true));
        } else if (c instanceof JTextComponent) {
            c.setBackground(INPUT_BG);
            c.setForeground(TEXT_PRIMARY);
            ((JTextComponent) c).setCaretColor(TEXT_PRIMARY);
        } else if (c instanceof JComboBox) {
            c.setBackground(INPUT_BG);
            c.setForeground(TEXT_PRIMARY);
        }

        // ── Rekursif ke semua child tanpa terkecuali ──
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                repaintTree(child);
            }
        }

        c.repaint();
    }

    // ════════════════════════════════════════════════════════
    //  FACTORY METHODS
    // ════════════════════════════════════════════════════════

    public static JPanel makeTopbar(String title, String subtitle) {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(PRIMARY);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(14, 22, 14, 22));
        bar.setPreferredSize(new Dimension(0, 56));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lbl.setForeground(Color.WHITE);
        bar.add(lbl, BorderLayout.WEST);

        if (subtitle != null) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(FONT_LABEL);
            sub.setForeground(new Color(187, 222, 251));
            bar.add(sub, BorderLayout.EAST);
        }
        return bar;
    }

    public static JPanel makeCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override public void paintComponent(Graphics g) {
                setBackground(BG_CARD);
                super.paintComponent(g);
            }
            @Override public void setBorder(Border border) {
                super.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(BORDER, 12, 1),
                    new EmptyBorder(16, 16, 16, 16)
                ));
            }
        };
        card.setBackground(BG_CARD);
        card.setBorder(null);
        return card;
    }

    public static JSeparator makeSep() {
        JSeparator sep = new JSeparator() {
            @Override protected void paintComponent(Graphics g) {
                setForeground(BORDER);
                super.paintComponent(g);
            }
        };
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    public static JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text) {
            @Override public void paintComponent(Graphics g) {
                setForeground(TEXT_SECONDARY);
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_LABEL);
        return lbl;
    }

    public static JLabel makeSectionHeader(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override public void paintComponent(Graphics g) {
                setForeground(TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_BOLD);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return lbl;
    }

    public static JTextField makeInput(String placeholder) {
        JTextField tf = new JTextField() {
            @Override public void paintComponent(Graphics g) {
                setBackground(INPUT_BG);
                setForeground(TEXT_PRIMARY);
                setCaretColor(TEXT_PRIMARY);
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(5, 10, 5, 10)
                ));
                super.paintComponent(g);
            }
        };
        tf.setFont(FONT_BODY);
        tf.setToolTipText(placeholder);
        return tf;
    }

    public static JComboBox<String> makeCombo() {
        JComboBox<String> cmb = new JComboBox<String>() {
            @Override public void paintComponent(Graphics g) {
                setBackground(INPUT_BG);
                setForeground(TEXT_PRIMARY);
                super.paintComponent(g);
            }
        };
        cmb.setFont(FONT_BODY);
        return cmb;
    }

    // ── Buttons ──────────────────────────────────────────────
    public static JButton makePrimaryBtn(String text) { return makeBtn(text, PRIMARY,  Color.WHITE); }
    public static JButton makeSuccessBtn(String text) { return makeBtn(text, SUCCESS,  Color.WHITE); }
    public static JButton makeDangerBtn(String text)  { return makeBtn(text, DANGER,   Color.WHITE); }
    public static JButton makeNeutralBtn(String text) {
        return makeBtn(text,
            darkMode ? new Color(52, 56, 68) : new Color(220, 230, 240),
            TEXT_SECONDARY);
    }

    private static JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 14, 7, 14));
        return btn;
    }

    public static JButton makeFilterBtn(String text, Color bg) {
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
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btn.setPreferredSize(new Dimension(90, 30));
        return btn;
    }

    public static JButton makeRoundBtn(String text, Color bg) {
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
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        btn.setPreferredSize(new Dimension(160, 34));
        return btn;
    }

    public static JTextField makeDateInput() {
        JTextField tf = new JTextField(10) {
            @Override public void paintComponent(Graphics g) {
                setBackground(INPUT_BG);
                setForeground(TEXT_PRIMARY);
                setCaretColor(TEXT_PRIMARY);
                setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER, 1),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
                ));
                super.paintComponent(g);
            }
        };
        tf.setFont(FONT_BODY);
        tf.setPreferredSize(new Dimension(115, 30));
        return tf;
    }

    public static JPanel makeFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(FILTER_BAR_BG);
                super.paintComponent(g);
            }
        };
        bar.setOpaque(true);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return bar;
    }

    public static JPanel makeSummaryCard() {
        JPanel card = new JPanel(new GridLayout(1, 2, 20, 0)) {
            @Override public void paintComponent(Graphics g) {
                setBackground(SUMMARY_CARD_BG);
                super.paintComponent(g);
            }
        };
        card.setOpaque(true);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        return card;
    }

    // ── Table styling ────────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(FONT_BODY);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(darkMode ? new Color(48, 50, 58) : new Color(230, 238, 245));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                t.setSelectionBackground(PRIMARY_BG);
                t.setSelectionForeground(TEXT_PRIMARY);
                if (!sel) {
                    setBackground(row % 2 == 0 ? BG_CARD : ROW_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });
    }

    public static JScrollPane makeScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table) {
            @Override public void paintComponent(Graphics g) {
                setBorder(new LineBorder(BORDER, 1, true));
                getViewport().setBackground(BG_CARD);
                super.paintComponent(g);
            }
        };
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(new LineBorder(BORDER, 1, true));
        return sp;
    }

    // ── RoundBorder ─────────────────────────────────────────
    public static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int radius, thickness;
        public RoundBorder(Color color, int radius, int thickness) {
            this.color = color; this.radius = radius; this.thickness = thickness;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppTheme.BORDER);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
        }
    }
}