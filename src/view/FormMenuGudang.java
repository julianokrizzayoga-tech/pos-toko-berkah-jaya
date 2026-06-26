package view;

import javax.swing.*;
import java.awt.*;

public class FormMenuGudang extends JFrame {

    private int idUser;
    private String namaUser;

    public FormMenuGudang(int idUser, String namaUser) {
        this.idUser = idUser;
        this.namaUser = namaUser;
        initUI();
    }

    private void initUI() {
        setTitle("Menu Gudang - Toko Berkah Jaya");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Color primary = new Color(78, 52, 46);
        getContentPane().setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(primary);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel lblTitle = new JLabel("GUDANG - TOKO BERKAH JAYA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblUser = new JLabel("👤 " + namaUser);
        lblUser.setForeground(new Color(255, 224, 178));
        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblUser, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        menuPanel.setBackground(new Color(245, 245, 245));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        menuPanel.add(createBtn("Kelola Stok Barang", new Color(78,52,46),
            e -> new FormStok().setVisible(true)));
        menuPanel.add(createBtn("Lihat Data Barang", new Color(46,125,50),
            e -> new FormBarang().setVisible(true)));
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