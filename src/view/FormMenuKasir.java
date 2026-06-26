package view;

import javax.swing.*;
import java.awt.*;

public class FormMenuKasir extends JFrame {

    private int idUser;
    private String namaUser;

    public FormMenuKasir(int idUser, String namaUser) {
        this.idUser = idUser;
        this.namaUser = namaUser;
        initUI();
    }

    private void initUI() {
        setTitle("Menu Kasir - Toko Berkah Jaya");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color primary = new Color(0, 150, 136);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel lblTitle = new JLabel("KASIR - TOKO BERKAH JAYA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblUser = new JLabel("👤 " + namaUser);
        lblUser.setForeground(new Color(200, 255, 245));
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        menuPanel.setBackground(new Color(245, 245, 245));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        menuPanel.add(createBtn("Transaksi Penjualan", new Color(0,150,136),
            e -> new FormTransaksi(idUser).setVisible(true)));
        menuPanel.add(createBtn("Riwayat Transaksi", new Color(230,81,0),
            e -> new FormLaporan().setVisible(true)));
        menuPanel.add(createBtn("Dashboard", new Color(99, 102, 241),
            e -> new PanelDashboard(namaUser, "SuperAdmin").setVisible(true)));

        add(menuPanel, BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);
    }

    private JButton createBtn(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton("<html><center>" + text + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private JPanel buatFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(230,230,230));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> { new FormLogin().setVisible(true); dispose(); });
        footer.add(btnLogout);
        return footer;
    }
}