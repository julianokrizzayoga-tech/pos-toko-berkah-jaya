# Sistem Informasi Manajemen Toko Berkah Jaya

Aplikasi Point of Sale (POS) desktop berbasis Java Swing untuk manajemen toko retail, dikembangkan menggunakan NetBeans IDE dengan database MySQL.

## Fitur Utama

- Multi-role login (Super Admin, Admin, Kasir, Gudang)
- Dashboard dengan informasi real-time
- Manajemen barang, kategori, stok, dan user
- Transaksi penjualan dengan keranjang belanja
- Pembayaran QRIS via Midtrans Payment Gateway (Sandbox)
- Scan barcode menggunakan kamera (ZXing)
- Cetak struk / receipt otomatis
- Laporan penjualan dalam format PDF dan Excel
- Dark mode / Light mode

## Teknologi yang Digunakan

| Teknologi | Keterangan |
|---|---|
| Java Swing | Framework UI desktop |
| NetBeans IDE | IDE pengembangan |
| MySQL | Database |
| XAMPP | Local server database |
| Midtrans | Payment Gateway (Sandbox) |
| iText | Generate PDF |
| Apache POI | Generate Excel |
| ZXing | Barcode Scanner |

## Cara Menjalankan

1. Install XAMPP dan jalankan Apache + MySQL
2. Import database: buka phpMyAdmin → import file SQL dari folder `sql/`
3. Buka project di NetBeans
4. Sesuaikan konfigurasi di `src/koneksi/Koneksi.java`:
```java
   String url = "jdbc:mysql://localhost:3306/nama_database";
   String user = "root";
   String password = "";
```
5. Clean & Build → Run project

## Struktur Project

TokoBerkahJaya/

├── src/

│   ├── koneksi/        # Koneksi database

│   ├── service/        # Midtrans & QRIS service

│   └── view/           # Semua form UI

├── nbproject/          # Konfigurasi NetBeans

├── 

└── 
