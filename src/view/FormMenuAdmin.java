package view;

import javax.swing.*;
import java.awt.*;

public class FormMenuAdmin extends JFrame {

    private int idUser;
    private String namaUser;

    public FormMenuAdmin(int idUser, String namaUser) {
        this.idUser = idUser;
        this.namaUser = namaUser;
        initUI();
    }

    private void initUI() {
        setTitle("Menu Admin - Toko Berkah Jaya");
        setSize(600, 480);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color primary = new Color(25, 118, 210);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel lblTitle = new JLabel("ADMIN - TOKO BERKAH JAYA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblUser = new JLabel("👤 " + namaUser);
        lblUser.setForeground(new Color(200, 230, 255));
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        menuPanel.setBackground(new Color(245, 245, 245));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        menuPanel.add(createBtn("Data Barang", new Color(46, 125, 50),
            e -> new FormBarang().setVisible(true)));
        menuPanel.add(createBtn("Data Customer", new Color(0, 105, 92),
            e -> new FormCustomer().setVisible(true)));
        menuPanel.add(createBtn("Kelola Kategori", new Color(234, 88, 12),
            e -> new FormKategori().setVisible(true)));
        menuPanel.add(createBtn("Laporan Penjualan", new Color(230, 81, 0),
            e -> new FormLaporan().setVisible(true)));
        menuPanel.add(createBtn("Kelola Stok", new Color(100, 100, 100),
            e -> new FormStok().setVisible(true)));
        menuPanel.add(createBtn("Dashboard", new Color(99, 102, 241),
            e -> new PanelDashboard(namaUser, "SuperAdmin").setVisible(true)));
        menuPanel.add(new JLabel()); // placeholder kosong

        add(menuPanel, BorderLayout.CENTER);
        add(buatFooter(), BorderLayout.SOUTH);
    }

    private JButton createBtn(String text, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton("<html><center>" + text + "</center></html>");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private JPanel buatFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(230, 230, 230));
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> { new FormLogin().setVisible(true); dispose(); });
        footer.add(btnLogout);
        return footer;
    }
}