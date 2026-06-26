package view;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DialogScanKamera extends JDialog {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ScheduledExecutorService scanner;

    private JLabel lblStatus;
    private JPanel camWrapper;

    private final AtomicBoolean sedangTutup = new AtomicBoolean(false);

    // Anti double scan
    private String lastBarcode = "";
    private long lastScanTime = 0;

    public interface OnScanResult {
        void onResult(String barcode);
    }

    private final OnScanResult callback;

    public DialogScanKamera(JFrame parent, OnScanResult callback) {
        super(parent, "Scan Barcode via Kamera", true);
        this.callback = callback;
        initUI();
    }

    private void initUI() {

        setSize(520, 460);
        setLocationRelativeTo(getParent());

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                tutup();
            }

            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                startWebcamDanScanner();
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(20, 40, 70));

        // ================= HEADER =================

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 118, 210));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel lblTitle =
                new JLabel("📷 Arahkan kamera ke barcode");

        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        header.add(lblTitle, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // ================= CAMERA =================

        camWrapper = new JPanel(new BorderLayout());
        camWrapper.setBackground(Color.BLACK);
        camWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblLoading =
                new JLabel("Membuka kamera...",
                        SwingConstants.CENTER);

        lblLoading.setForeground(Color.WHITE);
        lblLoading.setFont(new Font("Segoe UI", Font.BOLD, 14));

        camWrapper.add(lblLoading, BorderLayout.CENTER);

        root.add(camWrapper, BorderLayout.CENTER);

        // ================= STATUS =================

        JPanel statusBar = new JPanel(new BorderLayout());

        statusBar.setBackground(new Color(30, 55, 90));
        statusBar.setBorder(new EmptyBorder(10, 16, 10, 16));

        lblStatus = new JLabel("🔍 Menunggu barcode...");
        lblStatus.setForeground(new Color(180, 210, 255));
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));

        statusBar.add(lblStatus, BorderLayout.CENTER);

        JButton btnBatal = new JButton("Batal");

        btnBatal.setBackground(new Color(90, 100, 115));
        btnBatal.setForeground(Color.WHITE);
        btnBatal.setFocusPainted(false);
        btnBatal.setBorderPainted(false);
        btnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBatal.setBorder(new EmptyBorder(6, 16, 6, 16));

        btnBatal.addActionListener(e -> tutup());

        statusBar.add(btnBatal, BorderLayout.EAST);

        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void startWebcamDanScanner() {

        new Thread(() -> {

            try {

                webcam = null;

                for (Webcam w : Webcam.getWebcams()) {

                    if (!w.getName().toLowerCase().contains("virtual")) {
                        webcam = w;
                        break;
                    }
                }

                if (webcam == null) {
                    webcam = Webcam.getDefault();
                }

                if (webcam == null) {

                    SwingUtilities.invokeLater(() -> {

                        lblStatus.setText("❌ Kamera tidak ditemukan");
                        lblStatus.setForeground(Color.RED);

                    });

                    return;
                }

                webcam.setViewSize(WebcamResolution.VGA.getSize());

                webcamPanel = new WebcamPanel(webcam, false);

                webcamPanel.setFPSDisplayed(false);
                webcamPanel.setDisplayDebugInfo(false);
                webcamPanel.setImageSizeDisplayed(false);
                webcamPanel.setMirrored(true);

                SwingUtilities.invokeAndWait(() -> {

                    camWrapper.removeAll();

                    camWrapper.add(
                            webcamPanel,
                            BorderLayout.CENTER
                    );

                    camWrapper.revalidate();
                    camWrapper.repaint();
                });

                webcam.open();

                webcamPanel.start();

                SwingUtilities.invokeLater(() ->
                        lblStatus.setText("🔍 Scan barcode...")
                );

                startScanner();

            } catch (Exception ex) {

                SwingUtilities.invokeLater(() -> {

                    lblStatus.setText(
                            "❌ Gagal membuka kamera : "
                                    + ex.getMessage()
                    );

                    lblStatus.setForeground(Color.RED);

                });
            }

        }, "webcam-init").start();
    }

    private void startScanner() {

        scanner = Executors.newSingleThreadScheduledExecutor();

        scanner.scheduleAtFixedRate(() -> {

            if (webcam == null || !webcam.isOpen()) {
                return;
            }

            try {

                BufferedImage image = webcam.getImage();

                if (image == null) {
                    return;
                }

                LuminanceSource source =
                        new BufferedImageLuminanceSource(image);

                BinaryBitmap bitmap =
                        new BinaryBitmap(
                                new HybridBinarizer(source)
                        );

                Result result =
                        new MultiFormatReader().decode(bitmap);

                String barcode = result.getText();

                long now = System.currentTimeMillis();

                // Anti scan barcode sama berulang
                if (barcode.equals(lastBarcode)
                        && (now - lastScanTime) < 1500) {
                    return;
                }

                lastBarcode = barcode;
                lastScanTime = now;

                Toolkit.getDefaultToolkit().beep();

                SwingUtilities.invokeLater(() -> {

                    lblStatus.setText(
                            "✅ Barcode : " + barcode
                    );

                    lblStatus.setForeground(
                            new Color(100, 255, 150)
                    );

                    if (callback != null) {
                        callback.onResult(barcode);
                    }
                });

            } catch (NotFoundException ex) {

                // tidak ada barcode pada frame

            } catch (Exception ex) {

                ex.printStackTrace();

            }

        }, 300, 300, TimeUnit.MILLISECONDS);
    }

    private void tutup() {

        if (!sedangTutup.compareAndSet(false, true)) {
            return;
        }

        new Thread(() -> {

            try {

                if (scanner != null &&
                        !scanner.isShutdown()) {

                    scanner.shutdownNow();
                }

                if (webcamPanel != null) {
                    webcamPanel.stop();
                }

                if (webcam != null &&
                        webcam.isOpen()) {

                    webcam.close();
                }

            } catch (Exception ignored) {
            }

            SwingUtilities.invokeLater(this::dispose);

        }, "webcam-close").start();
    }
}