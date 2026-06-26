package view;

import service.MidtransQrisService;
import service.QrisHelper;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.json.*;

public class DialogPayment extends JDialog {

    public interface PaymentCallback {
        void onSuccess(String metode, double jumlahBayar, double kembalian);
        void onCancel();
    }

    private final double totalBayar;
    private final PaymentCallback callback;

    private JPanel panelCash, panelQRIS, panelKartu;
    private JPanel contentArea;

    // Cash
    private JTextField txtUangCash;
    private JLabel lblKembalianCash;

    // QRIS
    private Timer qrisTimer;
    private Timer pollingTimer;
    private JLabel lblQrisCountdown;
    private JLabel lblQrisStatus;
    private JLabel lblOrderId;
    private JButton btnSimulateQris;
    private JButton btnCopyQrUrl;
    private JLabel qrLabel;
    private int countdownSecQris = 0;
    private String currentOrderId;
    private String currentQrImageUrl;

    // Kartu
    private JTextField txtNomorKartu, txtExpiry, txtNama;
    private JPasswordField txtCVV;
    private JLabel lblKartuType;
    private JLabel lblKartuHint;

    // Tab buttons
    private JButton btnCash, btnQRIS, btnKartu;
    private String metodeAktif = "CASH";

    // Midtrans config — samakan dengan MidtransQrisService
    private static final String SERVER_KEY = System.getenv("MIDTRANS_SERVER_KEY");
    private static final String MIDTRANS_CLIENT_KEY = "Mid-client-LFyEFCdYAVsiRHYe"; // ganti dengan client key kamu
    private static final String MIDTRANS_SANDBOX_URL = "https://api.sandbox.midtrans.com/v2/charge";

    // Warna background
    private static Color pageBg()   { return AppTheme.isDarkMode() ? new Color(18, 18, 18)   : new Color(245, 247, 250); }
    private static Color tabBg()    { return AppTheme.isDarkMode() ? new Color(22, 24, 30)   : new Color(240, 245, 255); }
    private static Color footerBg() { return AppTheme.isDarkMode() ? new Color(22, 24, 30)   : new Color(240, 245, 255); }
    private static Color nomBg()    { return AppTheme.isDarkMode() ? new Color(35, 40, 55)   : new Color(227, 242, 253); }
    private static Color summBg()   { return AppTheme.isDarkMode() ? new Color(30, 35, 48)   : new Color(235, 245, 255); }

    public DialogPayment(JFrame parent, double totalBayar, PaymentCallback callback) {
        super(parent, "Pembayaran", true);
        this.totalBayar = totalBayar;
        this.callback   = callback;
        buildUI();
        setSize(520, 680);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                stopQrisTimer();
                stopPollingTimer();
                callback.onCancel();
            }
        });
    }

    private static JPanel solidPanel(LayoutManager lm, Color bg) {
        JPanel p = new JPanel(lm) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        p.setOpaque(true);
        p.setBackground(bg);
        return p;
    }

    private void buildUI() {
        JPanel root = solidPanel(new BorderLayout(0, 0), pageBg());
        setContentPane(root);
        getLayeredPane().setBackground(pageBg());
        getLayeredPane().setOpaque(true);

        // ── Header ────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(AppTheme.PRIMARY);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setOpaque(true);
        header.setBackground(AppTheme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        header.setPreferredSize(new Dimension(0, 70));

        JPanel headerLeft = solidPanel(new GridLayout(2, 1, 0, 2), AppTheme.PRIMARY);
        JLabel lblTitle = new JLabel("\uD83D\uDCB3  Pilih Metode Pembayaran");
        lblTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblTotal = new JLabel(String.format("Total:  Rp %,.0f", totalBayar));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(new Color(187, 222, 251));
        headerLeft.add(lblTitle);
        headerLeft.add(lblTotal);
        header.add(headerLeft, BorderLayout.WEST);
        root.add(header, BorderLayout.NORTH);

        // ── Tab panel ─────────────────────────────────────────
        JPanel tabPanel = solidPanel(new GridLayout(1, 3, 0, 0), tabBg());
        tabPanel.setPreferredSize(new Dimension(0, 52));
        tabPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, AppTheme.PRIMARY));

        btnCash  = makeTabBtn("\uD83D\uDCB5  Cash",  true);
        btnQRIS  = makeTabBtn("\uD83D\uDCF1  QRIS",  false);
        btnKartu = makeTabBtn("\uD83D\uDCB3  Kartu", false);

        btnCash.addActionListener(e  -> switchTab("CASH"));
        btnQRIS.addActionListener(e  -> switchTab("QRIS"));
        btnKartu.addActionListener(e -> switchTab("KARTU"));

        tabPanel.add(btnCash);
        tabPanel.add(btnQRIS);
        tabPanel.add(btnKartu);

        // ── Content area ──────────────────────────────────────
        contentArea = solidPanel(new CardLayout(), pageBg());

        panelCash  = buildPanelCash();
        panelQRIS  = buildPanelQRIS();
        panelKartu = buildPanelKartu();

        contentArea.add(panelCash,  "CASH");
        contentArea.add(panelQRIS,  "QRIS");
        contentArea.add(panelKartu, "KARTU");

        JPanel center = solidPanel(new BorderLayout(), pageBg());
        center.add(tabPanel,    BorderLayout.NORTH);
        center.add(contentArea, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // ── Footer ────────────────────────────────────────────
        JPanel footer = solidPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10), footerBg());
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER));

        JButton btnBatal = makeActionBtn("  Batal", AppTheme.DANGER);
        btnBatal.addActionListener(e -> {
            stopQrisTimer();
            stopPollingTimer();
            callback.onCancel();
            dispose();
        });

        JButton btnBayar = makeActionBtn("  Proses Bayar", AppTheme.SUCCESS);
        btnBayar.addActionListener(e -> prosesBayar());

        footer.add(btnBatal);
        footer.add(btnBayar);
        root.add(footer, BorderLayout.SOUTH);
    }

    private JButton makeActionBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        return btn;
    }

    // ── PANEL CASH ────────────────────────────────────────────
    private JPanel buildPanelCash() {
        JPanel p = solidPanel(new GridBagLayout(), pageBg());
        p.setBorder(new EmptyBorder(24, 32, 24, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;

        gbc.gridy = 0;
        p.add(makeSummaryRow("Total Pembayaran", String.format("Rp %,.0f", totalBayar), AppTheme.PRIMARY), gbc);

        gbc.gridy = 1;
        JLabel lbl = new JLabel("Uang yang Dibayar");
        lbl.setFont(AppTheme.FONT_BOLD);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        p.add(lbl, gbc);

        gbc.gridy = 2;
        txtUangCash = makeInput("Masukkan jumlah uang");
        txtUangCash.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtUangCash.setPreferredSize(new Dimension(0, 44));
        txtUangCash.setText("0");
        txtUangCash.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { hitungKembalianCash(); }
        });

        JButton btnReset = new JButton("\uD83D\uDDD1") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? AppTheme.DANGER.darker() : AppTheme.DANGER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnReset.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnReset.setForeground(Color.WHITE);
        btnReset.setContentAreaFilled(false);
        btnReset.setOpaque(false);
        btnReset.setFocusPainted(false);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.setPreferredSize(new Dimension(44, 44));
        btnReset.setToolTipText("Reset ke 0");
        btnReset.addActionListener(e -> {
            txtUangCash.setText("0");
            hitungKembalianCash();
        });

        JPanel inputRow = solidPanel(new BorderLayout(6, 0), pageBg());
        inputRow.add(txtUangCash, BorderLayout.CENTER);
        inputRow.add(btnReset,    BorderLayout.EAST);
        p.add(inputRow, gbc);

        gbc.gridy = 3;
        JPanel nominalPanel = solidPanel(new GridLayout(2, 3, 6, 6), pageBg());
        long[] nominals = {5000, 10000, 20000, 50000, 100000,
                           (long) Math.ceil(totalBayar / 1000) * 1000};
        String[] labels = {
            formatNominal(nominals[0]),
            formatNominal(nominals[1]),
            formatNominal(nominals[2]),
            formatNominal(nominals[3]),
            formatNominal(nominals[4]),
            "Pas"
        };
        for (int i = 0; i < nominals.length; i++) {
            nominalPanel.add(makeNominalBtn(labels[i], nominals[i]));
        }
        p.add(nominalPanel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(12, 0, 0, 0);
        lblKembalianCash = new JLabel("Kembalian:  Rp 0");
        lblKembalianCash.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKembalianCash.setForeground(AppTheme.SUCCESS);
        lblKembalianCash.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(lblKembalianCash, gbc);

        return p;
    }

    private JButton makeNominalBtn(String label, long nominal) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(nomBg());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_BOLD);
        btn.setForeground(AppTheme.PRIMARY);
        btn.setBackground(nomBg());
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 34));
        btn.addActionListener(e -> {
            if ("Pas".equals(label)) {
                txtUangCash.setText(String.valueOf(nominal));
            } else {
                try {
                    double current = Double.parseDouble(
                        txtUangCash.getText().trim().replace(",", "").replace(".", "")
                    );
                    txtUangCash.setText(String.valueOf((long)(current + nominal)));
                } catch (NumberFormatException ex) {
                    txtUangCash.setText(String.valueOf(nominal));
                }
            }
            hitungKembalianCash();
        });
        return btn;
    }

    private void hitungKembalianCash() {
        try {
            double uang = Double.parseDouble(txtUangCash.getText().trim().replace(",", ""));
            double kembalian = uang - totalBayar;
            if (kembalian >= 0) {
                lblKembalianCash.setText("Kembalian:  " + String.format("Rp %,.0f", kembalian));
                lblKembalianCash.setForeground(AppTheme.SUCCESS);
            } else {
                lblKembalianCash.setText("Kurang:  " + String.format("Rp %,.0f", Math.abs(kembalian)));
                lblKembalianCash.setForeground(AppTheme.DANGER);
            }
        } catch (NumberFormatException ex) {
            lblKembalianCash.setText("Kembalian:  Rp 0");
            lblKembalianCash.setForeground(AppTheme.SUCCESS);
        }
    }

    // ── PANEL QRIS ────────────────────────────────────────────
    private JPanel buildPanelQRIS() {
        JPanel p = solidPanel(new BorderLayout(0, 10), pageBg());
        p.setBorder(new EmptyBorder(16, 32, 16, 32));

        JPanel infoPanel = solidPanel(new GridLayout(5, 1, 0, 4), pageBg());

        JLabel lblInstruksi = new JLabel("Scan QR dengan GoPay / OVO / Dana / ShopeePay", SwingConstants.CENTER);
        lblInstruksi.setFont(AppTheme.FONT_BODY);
        lblInstruksi.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel lblNominal = new JLabel(String.format("Nominal:  Rp %,.0f", totalBayar), SwingConstants.CENTER);
        lblNominal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblNominal.setForeground(AppTheme.PRIMARY);

        lblQrisCountdown = new JLabel("Menyiapkan QR...", SwingConstants.CENTER);
        lblQrisCountdown.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblQrisCountdown.setForeground(AppTheme.AMBER);

        lblQrisStatus = new JLabel("⏳  Menghubungi server...", SwingConstants.CENTER);
        lblQrisStatus.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblQrisStatus.setForeground(AppTheme.TEXT_MUTED);

        lblOrderId = new JLabel("Order ID: -", SwingConstants.CENTER);
        lblOrderId.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblOrderId.setForeground(AppTheme.TEXT_MUTED);
        lblOrderId.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblOrderId.setToolTipText("Klik untuk copy Order ID ke clipboard");
        lblOrderId.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (currentOrderId != null && !currentOrderId.isEmpty()) {
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new java.awt.datatransfer.StringSelection(currentOrderId), null);
                    lblOrderId.setText("✅ Tersalin: " + currentOrderId);
                    Timer reset = new Timer(2000, ev ->
                        lblOrderId.setText("Order ID: " + currentOrderId + "  (klik untuk copy)"));
                    reset.setRepeats(false);
                    reset.start();
                }
            }
        });

        infoPanel.add(lblInstruksi);
        infoPanel.add(lblNominal);
        infoPanel.add(lblQrisCountdown);
        infoPanel.add(lblQrisStatus);
        infoPanel.add(lblOrderId);

        qrLabel = new JLabel("⏳ Memuat QR...", SwingConstants.CENTER);
        qrLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        qrLabel.setForeground(AppTheme.TEXT_MUTED);
        qrLabel.setPreferredSize(new Dimension(200, 200));
        qrLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1),
            new EmptyBorder(12, 12, 12, 12)
        ));

        JPanel qrBox = solidPanel(new FlowLayout(FlowLayout.CENTER), pageBg());
        qrBox.add(qrLabel);

        btnSimulateQris = makeRoundedBtn("🔄  Refresh QR", AppTheme.PRIMARY);
        btnSimulateQris.setEnabled(false);
        btnSimulateQris.addActionListener(e -> loadQrisFromAPI());

        btnCopyQrUrl = makeRoundedBtn("📋  Copy QR Image URL", AppTheme.SUCCESS);
        btnCopyQrUrl.setEnabled(false);
        btnCopyQrUrl.addActionListener(e -> {
            if (currentQrImageUrl != null && !currentQrImageUrl.isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(currentQrImageUrl), null);
                lblQrisStatus.setText("✅  QR Image URL tersalin!");
                lblQrisStatus.setForeground(AppTheme.SUCCESS);
            }
        });

        JPanel btnWrap = solidPanel(new FlowLayout(FlowLayout.CENTER, 8, 0), pageBg());
        btnWrap.add(btnSimulateQris);
        btnWrap.add(btnCopyQrUrl);

        p.add(infoPanel, BorderLayout.NORTH);
        p.add(qrBox,     BorderLayout.CENTER);
        p.add(btnWrap,   BorderLayout.SOUTH);
        return p;
    }

    private JButton makeRoundedBtn(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!isEnabled()) {
                    g2.setColor(AppTheme.isDarkMode() ? new Color(50, 55, 65) : new Color(180, 195, 210));
                } else {
                    g2.setColor(getModel().isRollover() ? baseColor.darker() : baseColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void startQrisTimer() {
        stopQrisTimer();
        stopPollingTimer();
        loadQrisFromAPI();
    }

    private void loadQrisFromAPI() {
        qrLabel.setIcon(null);
        qrLabel.setText("⏳ Memuat...");
        lblQrisCountdown.setText("Menghubungi Midtrans...");
        lblQrisCountdown.setForeground(AppTheme.AMBER);
        lblQrisStatus.setText("⏳  Menyiapkan QR Code...");
        lblQrisStatus.setForeground(AppTheme.TEXT_MUTED);
        lblOrderId.setText("Order ID: membuat...");
        btnSimulateQris.setEnabled(false);
        btnCopyQrUrl.setEnabled(false);

        String temporaryId = "TBJ-" + (System.currentTimeMillis() % 100000);

        new Thread(() -> {
            try {
                MidtransQrisService.QrisResult result =
                    MidtransQrisService.createQrisTransaction(temporaryId, (long) totalBayar);

                currentOrderId    = result.orderId;
                currentQrImageUrl = result.qrImageUrl;

                BufferedImage qrImage = QrisHelper.generateQRImage(result.qrString, 200);
                ImageIcon icon = new ImageIcon(qrImage);

                SwingUtilities.invokeLater(() -> {
                    qrLabel.setIcon(icon);
                    qrLabel.setText("");
                    lblQrisStatus.setText("⏳  Menunggu pelanggan scan QR...");
                    lblQrisStatus.setForeground(AppTheme.TEXT_MUTED);
                    lblOrderId.setText("Order ID: " + currentOrderId + "  (klik untuk copy)");
                    btnCopyQrUrl.setEnabled(true);
                    startCountdown(15 * 60);
                    startPollingTimer(currentOrderId);
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    qrLabel.setText("❌ Gagal load QR");
                    lblQrisCountdown.setText("Gagal terhubung");
                    lblQrisCountdown.setForeground(AppTheme.DANGER);
                    lblQrisStatus.setText(ex.getMessage());
                    lblQrisStatus.setForeground(AppTheme.DANGER);
                    lblOrderId.setText("Order ID: gagal dibuat");
                    btnSimulateQris.setEnabled(true);
                });
            }
        }).start();
    }

    private void startCountdown(int totalSeconds) {
        stopQrisTimer();
        countdownSecQris = totalSeconds;
        qrisTimer = new Timer(1000, e -> {
            countdownSecQris--;
            int m = countdownSecQris / 60, s = countdownSecQris % 60;
            lblQrisCountdown.setText(String.format("QR berlaku: %02d:%02d", m, s));
            if (countdownSecQris <= 0) {
                stopQrisTimer();
                stopPollingTimer();
                lblQrisCountdown.setText("QR Kedaluwarsa");
                lblQrisCountdown.setForeground(AppTheme.DANGER);
                lblQrisStatus.setText("❌  QR expire — klik Refresh QR");
                lblQrisStatus.setForeground(AppTheme.DANGER);
                btnSimulateQris.setEnabled(true);
                btnCopyQrUrl.setEnabled(false);
            } else if (countdownSecQris <= 60) {
                lblQrisCountdown.setForeground(AppTheme.DANGER);
            }
        });
        qrisTimer.start();
    }

    private void startPollingTimer(String orderId) {
        stopPollingTimer();
        pollingTimer = new Timer(3000, e -> {
            new Thread(() -> {
                try {
                    String status = MidtransQrisService.checkTransactionStatus(orderId);
                    if ("settlement".equals(status) || "capture".equals(status)) {
                        SwingUtilities.invokeLater(() -> {
                            stopQrisTimer();
                            stopPollingTimer();
                            lblQrisStatus.setText("✅  Pembayaran QRIS berhasil!");
                            lblQrisStatus.setForeground(AppTheme.SUCCESS);
                            Timer delay = new Timer(800, ev -> {
                                callback.onSuccess("QRIS", totalBayar, 0);
                                dispose();
                            });
                            delay.setRepeats(false);
                            delay.start();
                        });
                    } else if ("expire".equals(status) || "cancel".equals(status) || "deny".equals(status)) {
                        SwingUtilities.invokeLater(() -> {
                            stopQrisTimer();
                            stopPollingTimer();
                            lblQrisStatus.setText("❌  Transaksi ditolak/expire");
                            lblQrisStatus.setForeground(AppTheme.DANGER);
                            btnSimulateQris.setEnabled(true);
                        });
                    }
                } catch (Exception ex) {
                    System.out.println("[QRIS] Polling error: " + ex.getMessage());
                }
            }).start();
        });
        pollingTimer.start();
    }

    private void stopQrisTimer() {
        if (qrisTimer != null && qrisTimer.isRunning()) qrisTimer.stop();
    }

    private void stopPollingTimer() {
        if (pollingTimer != null && pollingTimer.isRunning()) pollingTimer.stop();
    }

    // ── PANEL KARTU ───────────────────────────────────────────
    private JPanel buildPanelKartu() {
        JPanel p = solidPanel(new GridBagLayout(), pageBg());
        p.setBorder(new EmptyBorder(20, 32, 20, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(4, 0, 4, 0);

        gbc.gridy = 0;
        p.add(makeSummaryRow("Total Pembayaran", String.format("Rp %,.0f", totalBayar), AppTheme.PRIMARY), gbc);

        // Hint sandbox card
        gbc.gridy = 1;
        JPanel hintBox = solidPanel(new BorderLayout(), new Color(255, 248, 225));
        hintBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 193, 7), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        lblKartuHint = new JLabel(
    "<html><b>🧪 Sandbox Test Card:</b><br>" +
    "No: 4811 1111 1111 1114 &nbsp;|&nbsp; Exp: 12/27 &nbsp;|&nbsp; CVV: 123<br>" +
    "OTP: 112233</html>"
);
        lblKartuHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblKartuHint.setForeground(new Color(102, 77, 3));
        hintBox.add(lblKartuHint);
        p.add(hintBox, gbc);

        gbc.gridy = 2; p.add(makeFieldLabel("Nama Pemegang Kartu"), gbc);
        gbc.gridy = 3;
        txtNama = makeInput("Nama sesuai kartu");
        p.add(txtNama, gbc);

        gbc.gridy = 4;
        JPanel nomorHeader = solidPanel(new BorderLayout(), pageBg());
        nomorHeader.add(makeFieldLabel("Nomor Kartu"), BorderLayout.WEST);
        lblKartuType = new JLabel("");
        lblKartuType.setFont(AppTheme.FONT_BOLD);
        lblKartuType.setForeground(AppTheme.PRIMARY);
        nomorHeader.add(lblKartuType, BorderLayout.EAST);
        p.add(nomorHeader, gbc);

        gbc.gridy = 5;
        txtNomorKartu = makeInput("4811 1111 1111 1114");
        txtNomorKartu.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { formatNomorKartu(); detectCardType(); }
        });
        p.add(txtNomorKartu, gbc);

        gbc.gridy = 6;
        JPanel row = solidPanel(new GridLayout(1, 2, 12, 0), pageBg());

        JPanel expiryCol = solidPanel(new BorderLayout(0, 4), pageBg());
        expiryCol.add(makeFieldLabel("Berlaku Hingga (MM/YY)"), BorderLayout.NORTH);
        txtExpiry = makeInput("12/27");
        txtExpiry.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { formatExpiry(); }
        });
        expiryCol.add(txtExpiry, BorderLayout.CENTER);

        JPanel cvvCol = solidPanel(new BorderLayout(0, 4), pageBg());
        cvvCol.add(makeFieldLabel("CVV"), BorderLayout.NORTH);
        txtCVV = new JPasswordField();
        txtCVV.setFont(AppTheme.FONT_BODY);
        txtCVV.setBackground(AppTheme.INPUT_BG);
        txtCVV.setForeground(AppTheme.TEXT_PRIMARY);
        txtCVV.setCaretColor(AppTheme.TEXT_PRIMARY);
        txtCVV.setOpaque(true);
        txtCVV.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        cvvCol.add(txtCVV, BorderLayout.CENTER);

        row.add(expiryCol);
        row.add(cvvCol);
        p.add(row, gbc);

        return p;
    }

    private void formatNomorKartu() {
        String raw = txtNomorKartu.getText().replaceAll("[^0-9]", "");
        if (raw.length() > 16) raw = raw.substring(0, 16);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(" ");
            sb.append(raw.charAt(i));
        }
        int pos = sb.length();
        txtNomorKartu.setText(sb.toString());
        try { txtNomorKartu.setCaretPosition(pos); } catch (Exception ignored) {}
    }

    private void formatExpiry() {
        String raw = txtExpiry.getText().replaceAll("[^0-9]", "");
        if (raw.length() > 4) raw = raw.substring(0, 4);
        if (raw.length() > 2) raw = raw.substring(0, 2) + "/" + raw.substring(2);
        txtExpiry.setText(raw);
        try { txtExpiry.setCaretPosition(raw.length()); } catch (Exception ignored) {}
    }

    private void detectCardType() { lblKartuType.setText(detectCardTypeStr()); }

    // ── TAB SWITCHING ─────────────────────────────────────────
    private void switchTab(String metode) {
        metodeAktif = metode;
        ((CardLayout) contentArea.getLayout()).show(contentArea, metode);
        setTabActive(btnCash,  "CASH".equals(metode));
        setTabActive(btnQRIS,  "QRIS".equals(metode));
        setTabActive(btnKartu, "KARTU".equals(metode));
        if ("QRIS".equals(metode)) startQrisTimer();
        else { stopQrisTimer(); stopPollingTimer(); }
    }

    private void setTabActive(JButton btn, boolean active) {
        if (active) {
            btn.setBackground(AppTheme.PRIMARY);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, AppTheme.PRIMARY_DARK),
                new EmptyBorder(11, 16, 8, 16)));
        } else {
            btn.setBackground(tabBg());
            btn.setForeground(AppTheme.TEXT_SECONDARY);
            btn.setBorder(new EmptyBorder(11, 16, 11, 16));
        }
        btn.repaint();
    }

    // ── PROSES BAYAR ──────────────────────────────────────────
    private void prosesBayar() {
        switch (metodeAktif) {
            case "CASH":  prosesCash();  break;
            case "QRIS":  prosesQRIS();  break;
            case "KARTU": prosesKartu(); break;
        }
    }

    private void prosesCash() {
        try {
            double uang = Double.parseDouble(txtUangCash.getText().trim().replace(",", ""));
            if (uang < totalBayar) {
                showError("Uang tidak cukup! Masih kurang " + String.format("Rp %,.0f", totalBayar - uang));
                return;
            }
            double kembalian = uang - totalBayar;
            int ok = JOptionPane.showConfirmDialog(this,
                String.format("Total:      Rp %,.0f\nUang:       Rp %,.0f\nKembalian: Rp %,.0f\n\nProses pembayaran Cash?",
                    totalBayar, uang, kembalian),
                "Konfirmasi Cash", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;
            callback.onSuccess("CASH", uang, kembalian);
            dispose();
        } catch (NumberFormatException e) {
            showError("Masukkan jumlah uang yang valid!");
        }
    }

    private void prosesQRIS() {
        showError("Tunggu pelanggan scan dan konfirmasi QR. Sistem otomatis mendeteksi pembayaran.");
    }

    private void prosesKartu() {
        String nama   = txtNama.getText().trim();
        String nomor  = txtNomorKartu.getText().replaceAll("[^0-9]", "");
        String expiry = txtExpiry.getText().trim();
        String cvv    = new String(txtCVV.getPassword()).trim();

        if (nama.isEmpty())                   { showError("Nama pemegang kartu wajib diisi!"); return; }
        if (nomor.length() < 13)              { showError("Nomor kartu tidak valid (minimal 13 digit)!"); return; }
        if (!luhnCheck(nomor))                { showError("Nomor kartu tidak valid!"); return; }
        if (!expiry.matches("\\d{2}/\\d{2}")) { showError("Format tanggal berlaku: MM/YY"); return; }
        if (cvv.length() < 3)                 { showError("CVV minimal 3 digit!"); return; }

        JDialog loading = showLoadingDialog("Memproses kartu via Midtrans Sandbox...");

        new Thread(() -> {
            try {
                String[] expiryParts = expiry.split("/");
                String expMonth = expiryParts[0];
                String expYear  = "20" + expiryParts[1];

                String token = getMidtransCardToken(nomor, expMonth, expYear, cvv);

                String orderId = "TBJ-CARD-" + (System.currentTimeMillis() % 100000);
                JSONObject chargeResult = chargeMidtransCard(token, orderId, (long) totalBayar);

                String txStatus    = chargeResult.optString("transaction_status", "");
                String fraudStatus = chargeResult.optString("fraud_status", "");

                SwingUtilities.invokeLater(() -> {
                    loading.dispose();
                    if (("capture".equals(txStatus) && "accept".equals(fraudStatus))
                            || "settlement".equals(txStatus)) {
                        JOptionPane.showMessageDialog(this,
                            "✅  Pembayaran Kartu Berhasil!\n" +
                            detectCardTypeStr() + "\n" +
                            "No: **** **** **** " + nomor.substring(Math.max(0, nomor.length() - 4)) + "\n" +
                            "Order ID: " + orderId,
                            "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                        callback.onSuccess("KARTU - " + detectCardTypeStr(), totalBayar, 0);
                        dispose();
                    } else if ("deny".equals(txStatus) || "cancel".equals(txStatus)) {
                        showError("Pembayaran ditolak oleh bank.\nStatus: " + txStatus);
                    } else {
                        // challenge / pending → tetap sukses di sandbox
                        JOptionPane.showMessageDialog(this,
                            "✅  Pembayaran diterima (status: " + txStatus + ")\n" +
                            "Order ID: " + orderId,
                            "Berhasil", JOptionPane.INFORMATION_MESSAGE);
                        callback.onSuccess("KARTU - " + detectCardTypeStr(), totalBayar, 0);
                        dispose();
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    loading.dispose();
                    showError("Gagal memproses kartu:\n" + ex.getMessage());
                });
            }
        }).start();
    }

    // ── Midtrans Card Token ───────────────────────────────────
    private String getMidtransCardToken(String cardNumber, String expMonth,
                                         String expYear, String cvv) throws Exception {
        String urlStr = "https://api.sandbox.midtrans.com/v2/token"
            + "?card_number=" + cardNumber
            + "&card_exp_month=" + expMonth
            + "&card_exp_year=" + expYear
            + "&card_cvv=" + cvv
            + "&client_key=" + MIDTRANS_CLIENT_KEY;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = readStream(is);
        System.out.println("[KARTU] Token response: " + response);

        JSONObject json = new JSONObject(response);
        if (json.has("token_id")) return json.getString("token_id");
        throw new Exception("Gagal ambil token: " + response);
    }

    // ── Midtrans Card Charge ──────────────────────────────────
    private JSONObject chargeMidtransCard(String tokenId, String orderId, long amount) throws Exception {
        JSONObject body = new JSONObject();
        body.put("payment_type", "credit_card");

        JSONObject creditCard = new JSONObject();
        creditCard.put("token_id", tokenId);
        creditCard.put("authentication", true);
        body.put("credit_card", creditCard);

        JSONObject txDetails = new JSONObject();
        txDetails.put("order_id", orderId);
        txDetails.put("gross_amount", amount);
        body.put("transaction_details", txDetails);

       String auth = Base64.getEncoder().encodeToString(
    (SERVER_KEY + ":").getBytes(StandardCharsets.UTF_8));

        URL url = new URL(MIDTRANS_SANDBOX_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = readStream(is);
        System.out.println("[KARTU] Charge response: " + response);

        return new JSONObject(response);
    }

    private String readStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    // ── Luhn Algorithm ────────────────────────────────────────
    private boolean luhnCheck(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(number.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private String detectCardTypeStr() {
        String num = txtNomorKartu.getText().replaceAll("[^0-9]", "");
        if (num.startsWith("4")) return "Visa";
        if (num.startsWith("5")) return "Mastercard";
        if (num.startsWith("3")) return "Amex";
        if (num.startsWith("6")) return "GPN/Debit";
        return "Kartu";
    }

    private JDialog showLoadingDialog(String msg) {
        JDialog d = new JDialog(this, "", true);
        d.setUndecorated(true);
        JLabel lbl = new JLabel("⏳  " + msg, SwingConstants.CENTER);
        lbl.setFont(AppTheme.FONT_BOLD);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setBorder(new EmptyBorder(20, 32, 20, 32));
        JPanel panel = solidPanel(new BorderLayout(), AppTheme.BG_CARD);
        panel.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        panel.add(lbl);
        d.setContentPane(panel);
        d.pack();
        d.setLocationRelativeTo(this);
        SwingUtilities.invokeLater(() -> d.setVisible(true));
        return d;
    }

    // ── HELPERS ───────────────────────────────────────────────
    private JPanel makeSummaryRow(String label, String value, Color valueColor) {
        JPanel row = solidPanel(new BorderLayout(), summBg());
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1),
            new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel lblL = new JLabel(label);
        lblL.setFont(AppTheme.FONT_BODY);
        lblL.setForeground(AppTheme.TEXT_SECONDARY);
        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblV.setForeground(valueColor);
        row.add(lblL, BorderLayout.WEST);
        row.add(lblV, BorderLayout.EAST);
        return row;
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BOLD);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        return lbl;
    }

    private JTextField makeInput(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(AppTheme.FONT_BODY);
        tf.setBackground(AppTheme.INPUT_BG);
        tf.setForeground(AppTheme.TEXT_PRIMARY);
        tf.setCaretColor(AppTheme.TEXT_PRIMARY);
        tf.setOpaque(true);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        tf.setToolTipText(placeholder);
        return tf;
    }

    private String formatNominal(long nominal) { return String.format("%,d", nominal); }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Perhatian", JOptionPane.WARNING_MESSAGE);
    }

    private JButton makeTabBtn(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        setTabActive(btn, active);
        return btn;
    }
}