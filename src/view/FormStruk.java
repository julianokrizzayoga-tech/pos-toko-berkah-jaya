package view;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import koneksi.Koneksi;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormStruk extends JDialog {

    private final int idJual;
    private final double grandTotal;
    private final double uangBayar;

    public FormStruk(JFrame parent, int idJual, double grandTotal, double uangBayar) {
        super(parent, "Struk Transaksi", true);
        this.idJual    = idJual;
        this.grandTotal = grandTotal;
        this.uangBayar  = uangBayar;
        initUI();
    }

    private void initUI() {
        setSize(420, 680);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // ── HEADER UI SWING ─────────────────────────────────
        JPanel header = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(25, 118, 210),
                    getWidth(), 0, new Color(21, 101, 192));
                g2.setPaint(gp);
                g2.fillRoundRect(20, 14, getWidth()-40, getHeight()-20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(22, 30, 28, 30));
        header.setPreferredSize(new Dimension(0, 115));

        JLabel lblToko = new JLabel("TOKO BERKAH JAYA");
        lblToko.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblToko.setForeground(Color.WHITE);
        lblToko.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Struk Pembelian");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(199, 220, 255));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(lblToko);
        header.add(Box.createVerticalStrut(4));
        header.add(lblSub);
        add(header, BorderLayout.NORTH);

        // ── BODY UI SWING ───────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Color.WHITE);
        body.setBorder(new EmptyBorder(18, 28, 18, 28));

        String queryMaster = "SELECT p.*, c.nama_customer, u.nama_lengkap "
                           + "FROM tb_penjualan p "
                           + "LEFT JOIN tb_customer c ON p.id_customer=c.id_customer "
                           + "LEFT JOIN tb_user u ON p.id_user=u.id_user "
                           + "WHERE p.id_jual=?";

        String queryDetail = "SELECT d.*, b.nama_barang FROM tb_detail_penjualan d "
                           + "LEFT JOIN tb_barang b ON d.id_barang=b.id_barang "
                           + "WHERE d.id_jual=?";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement psM = conn.prepareStatement(queryMaster);
             PreparedStatement psD = conn.prepareStatement(queryDetail)) {

            // Ambil Data Master Penjualan
            psM.setInt(1, idJual);
            String namaCustomer = "-";
            String tglTransaksi = "-";
            
            try (ResultSet rsM = psM.executeQuery()) {
                if (rsM.next()) {
                    namaCustomer = rsM.getString("nama_customer");
                    tglTransaksi = rsM.getString("tgl_transaksi");
                }
            }
            
            if (tglTransaksi == null) {
                tglTransaksi = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }

            // Tampilkan Info Transaksi di UI
            body.add(infoRow("No. Transaksi", "#" + idJual));
            body.add(Box.createVerticalStrut(3));
            body.add(infoRow("Tanggal", tglTransaksi));
            body.add(Box.createVerticalStrut(3));
            body.add(infoRow("Customer", namaCustomer == null ? "-" : namaCustomer));
            body.add(Box.createVerticalStrut(10));
            body.add(dashedLine());
            body.add(Box.createVerticalStrut(10));

            // Header kolom komponen barang
            JLabel lblHdrBarang = new JLabel("Barang");
            lblHdrBarang.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblHdrBarang.setForeground(new Color(100, 116, 139));

            JLabel lblHdrSubtotal = new JLabel("Subtotal");
            lblHdrSubtotal.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblHdrSubtotal.setForeground(new Color(100, 116, 139));
            lblHdrSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel hdrPanel = new JPanel(new BorderLayout());
            hdrPanel.setBackground(Color.WHITE);
            hdrPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
            hdrPanel.add(lblHdrBarang, BorderLayout.WEST);
            hdrPanel.add(lblHdrSubtotal, BorderLayout.EAST);
            body.add(hdrPanel);
            body.add(Box.createVerticalStrut(4));

            // Ambil Data Rincian Item Barang
            psD.setInt(1, idJual);
            try (ResultSet rsD = psD.executeQuery()) {
                while (rsD.next()) {
                    String nama  = rsD.getString("nama_barang");
                    int qty      = rsD.getInt("jumlah_beli");
                    double harga = rsD.getDouble("harga_satuan");
                    double sub   = rsD.getDouble("subtotal");

                    String strQtyHarga = qty + " x Rp " + String.format("%,.0f", harga);
                    String strSubtotal = "Rp " + String.format("%,.0f", sub);

                    JPanel row1 = new JPanel(new BorderLayout());
                    row1.setBackground(Color.WHITE);
                    row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

                    JLabel lNama = new JLabel(nama == null ? "Produk Terhapus" : nama);
                    lNama.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lNama.setForeground(new Color(30, 41, 59));

                    JLabel lSub = new JLabel(strSubtotal);
                    lSub.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lSub.setForeground(new Color(25, 118, 210));

                    row1.add(lNama, BorderLayout.WEST);
                    row1.add(lSub, BorderLayout.EAST);

                    JPanel row2 = new JPanel(new BorderLayout());
                    row2.setBackground(Color.WHITE);
                    row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
                    row2.setBorder(new EmptyBorder(0, 10, 0, 0));

                    JLabel lDetail = new JLabel(strQtyHarga);
                    lDetail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    lDetail.setForeground(new Color(148, 163, 184));
                    row2.add(lDetail, BorderLayout.WEST);

                    body.add(row1);
                    body.add(row2);
                    body.add(Box.createVerticalStrut(8));
                }
            }

            body.add(dashedLine());
            body.add(Box.createVerticalStrut(10));

            // Kalkulasi Total, Bayar, dan Kembalian
            double kembalian = uangBayar - grandTotal;

            body.add(totalRow("TOTAL", "Rp " + String.format("%,.0f", grandTotal), true, false));
            body.add(Box.createVerticalStrut(4));
            body.add(totalRow("Bayar", "Rp " + String.format("%,.0f", uangBayar), false, false));
            body.add(Box.createVerticalStrut(6));
            body.add(dashedLine());
            body.add(Box.createVerticalStrut(6));
            body.add(totalRow("Kembalian", "Rp " + String.format("%,.0f", kembalian), false, true));

            body.add(Box.createVerticalStrut(18));
            body.add(dashedLine());
            body.add(Box.createVerticalStrut(14));

            // Footer Note
            JLabel lblTerima = new JLabel("Terima kasih atas kunjungan Anda!");
            lblTerima.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 12));
            lblTerima.setForeground(new Color(30, 41, 59));
            lblTerima.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblNote = new JLabel("Barang yang sudah dibeli tidak dapat dikembalikan.");
            lblNote.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            lblNote.setForeground(new Color(148, 163, 184));
            lblNote.setAlignmentX(Component.CENTER_ALIGNMENT);

            body.add(lblTerima);
            body.add(Box.createVerticalStrut(4));
            body.add(lblNote);

        } catch (SQLException e) {
            body.add(new JLabel("Gagal memuat rincian transaksi: " + e.getMessage()));
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // ── FOOTER BUTTONS ──────────────────────────────────
        JPanel footer = new JPanel(new GridLayout(1, 2, 10, 0));
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 228, 240)),
            new EmptyBorder(12, 20, 12, 20)
        ));

        JButton btnPDF = makeBtn("📄  Export PDF", new Color(239, 68, 68));
        btnPDF.addActionListener(e -> exportPDF());

        JButton btnTutup = makeBtn("✕  Tutup", new Color(107, 114, 128));
        btnTutup.addActionListener(e -> dispose());

        footer.add(btnPDF);
        footer.add(btnTutup);
        add(footer, BorderLayout.SOUTH);
    }

    // ── Helper UI Methods ────────────────────────────────────
    private JPanel infoRow(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setPreferredSize(new Dimension(100, 20));

        JLabel sep = new JLabel(": ");
        sep.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sep.setForeground(new Color(100, 116, 139));

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 11));
        val.setForeground(new Color(30, 41, 59));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(Color.WHITE);
        left.add(lbl); left.add(sep); left.add(val);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    private JPanel totalRow(String label, String value, boolean bold, boolean highlight) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(highlight ? new Color(235, 245, 255) : Color.WHITE);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        if (highlight) p.setBorder(new EmptyBorder(4, 8, 4, 8));

        int size = (bold || highlight) ? 14 : 12;
        Color fg = highlight ? new Color(25, 118, 210) : bold ? new Color(15, 23, 42) : new Color(71, 85, 105);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", (bold || highlight) ? Font.BOLD : Font.PLAIN, size));
        lbl.setForeground(fg);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", (bold || highlight) ? Font.BOLD : Font.PLAIN, size));
        val.setForeground(fg);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(lbl, BorderLayout.WEST);
        p.add(val, BorderLayout.EAST);
        return p;
    }

    private JSeparator dashedLine() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 215, 230));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── PROSES EKSPOR LANGSUNG PDF KE DIR PILIHAN ───────────────────
    private void exportPDF() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Struk_" + idJual + ".pdf"));
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        String path = fc.getSelectedFile().getAbsolutePath();
        if (!path.endsWith(".pdf")) path += ".pdf";

        Document doc = new Document(new com.itextpdf.text.Rectangle(300, 650), 15, 15, 20, 20);
        
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            com.itextpdf.text.Font fBig = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fBold = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 9, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Font fNormal = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8);
            com.itextpdf.text.Font fSmall = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 7);
            com.itextpdf.text.Font fBlue = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.BOLD, new com.itextpdf.text.BaseColor(25, 118, 210));

            Paragraph pToko = new Paragraph("TOKO BERKAH JAYA", fBig);
            pToko.setAlignment(Element.ALIGN_CENTER);
            doc.add(pToko);

            Paragraph pAlamat = new Paragraph("Jl. Berkah No. 1, Jakarta | 021-12345678", fSmall);
            pAlamat.setAlignment(Element.ALIGN_CENTER);
            pAlamat.setSpacingAfter(6);
            doc.add(pAlamat);
            doc.add(new com.itextpdf.text.Chunk(new com.itextpdf.text.pdf.draw.LineSeparator()));

            String cust = "-", kasir = "-", tgl = "-";

            String queryMaster = "SELECT p.*, c.nama_customer, u.nama_lengkap "
                               + "FROM tb_penjualan p "
                               + "LEFT JOIN tb_customer c ON p.id_customer=c.id_customer "
                               + "LEFT JOIN tb_user u ON p.id_user=u.id_user "
                               + "WHERE p.id_jual=?";

            String queryDetail = "SELECT d.*, b.nama_barang FROM tb_detail_penjualan d "
                               + "LEFT JOIN tb_barang b ON d.id_barang=b.id_barang "
                               + "WHERE d.id_jual=?";

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement psM = conn.prepareStatement(queryMaster);
                 PreparedStatement psD = conn.prepareStatement(queryDetail)) {
                
                psM.setInt(1, idJual);
                try (ResultSet rsM = psM.executeQuery()) {
                    if (rsM.next()) {
                        cust = rsM.getString("nama_customer");
                        kasir = rsM.getString("nama_lengkap");
                        tgl = rsM.getString("tgl_transaksi");
                    }
                }
                
                if (tgl == null) tgl = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                doc.add(new Paragraph("No. Transaksi : #" + idJual, fNormal));
                doc.add(new Paragraph("Tanggal       : " + tgl, fNormal));
                doc.add(new Paragraph("Customer      : " + (cust == null ? "-" : cust), fNormal));
                doc.add(new Paragraph("Kasir         : " + (kasir == null ? "-" : kasir), fNormal));
                doc.add(new Paragraph(" "));
                doc.add(new com.itextpdf.text.Chunk(new com.itextpdf.text.pdf.draw.LineSeparator()));

                PdfPTable tbl = new PdfPTable(new float[]{4.5f, 3f, 2.5f});
                tbl.setWidthPercentage(100);
                tbl.setSpacingBefore(4);

                com.itextpdf.text.BaseColor blueColor = new com.itextpdf.text.BaseColor(25, 118, 210);
                com.itextpdf.text.Font fTblH = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 8, com.itextpdf.text.Font.BOLD, com.itextpdf.text.BaseColor.WHITE);
                
                String[] hCols = {"Barang", "Qty x Harga", "Subtotal"};
                for (String h : hCols) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, fTblH));
                    cell.setBackgroundColor(blueColor);
                    cell.setPadding(5);
                    cell.setBorderColor(com.itextpdf.text.BaseColor.WHITE);
                    if (h.equals("Subtotal")) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    tbl.addCell(cell);
                }

                psD.setInt(1, idJual);
                try (ResultSet rsD = psD.executeQuery()) {
                    com.itextpdf.text.BaseColor altRow = new com.itextpdf.text.BaseColor(245, 250, 255);
                    com.itextpdf.text.BaseColor borderC = new com.itextpdf.text.BaseColor(220, 230, 245);
                    int rowNum = 0;
                    
                    while (rsD.next()) {
                        com.itextpdf.text.BaseColor bg = (rowNum % 2 == 0) ? com.itextpdf.text.BaseColor.WHITE : altRow;
                        rowNum++;

                        PdfPCell c1 = new PdfPCell(new Phrase(rsD.getString("nama_barang"), fNormal));
                        PdfPCell c2 = new PdfPCell(new Phrase(rsD.getInt("jumlah_beli") + " x Rp " + String.format("%,.0f", rsD.getDouble("harga_satuan")), fNormal));
                        PdfPCell c3 = new PdfPCell(new Phrase("Rp " + String.format("%,.0f", rsD.getDouble("subtotal")), fBold));

                        c1.setBackgroundColor(bg); c2.setBackgroundColor(bg); c3.setBackgroundColor(bg);
                        c1.setPadding(4); c2.setPadding(4); c3.setPadding(4);
                        c1.setBorderColor(borderC); c2.setBorderColor(borderC); c3.setBorderColor(borderC);
                        c3.setHorizontalAlignment(Element.ALIGN_RIGHT);

                        tbl.addCell(c1);
                        tbl.addCell(c2);
                        tbl.addCell(c3);
                    }
                }
                doc.add(tbl);
            }

            doc.add(new Paragraph(" "));
            doc.add(new com.itextpdf.text.Chunk(new com.itextpdf.text.pdf.draw.LineSeparator()));

            double kembalian = uangBayar - grandTotal;
            Paragraph pTotal = new Paragraph("TOTAL         : Rp " + String.format("%,.0f", grandTotal), fBold);
            pTotal.setSpacingBefore(4);
            doc.add(pTotal);
            
            doc.add(new Paragraph("Uang Bayar    : Rp " + String.format("%,.0f", uangBayar), fNormal));
            doc.add(new com.itextpdf.text.Chunk(new com.itextpdf.text.pdf.draw.LineSeparator()));

            Paragraph pKembali = new Paragraph("Kembalian     : Rp " + String.format("%,.0f", kembalian), fBlue);
            pKembali.setSpacingBefore(4);
            pKembali.setSpacingAfter(6);
            doc.add(pKembali);
            doc.add(new com.itextpdf.text.Chunk(new com.itextpdf.text.pdf.draw.LineSeparator()));

            Paragraph pTerima = new Paragraph("\nTerima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat dikembalikan.", fSmall);
            pTerima.setAlignment(Element.ALIGN_CENTER);
            doc.add(pTerima);

            doc.close();

            // Selesai cetak murni, muncul informasi sukses simpan
            JOptionPane.showMessageDialog(this, "✅ PDF berhasil disimpan!\nSilakan cek di folder pilihan Anda.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
            
            // FUNGSI OTOMATIS BUKA DIAPUS TOTAL DI SINI BIAR OS WINDOWS ENGGAK BISA NGASIH DIALOG PRINTER LAGI.

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal export PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}