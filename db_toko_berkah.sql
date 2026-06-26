-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Waktu pembuatan: 26 Jun 2026 pada 17.34
-- Versi server: 10.4.32-MariaDB
-- Versi PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_toko_berkah`
--

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_barang`
--

CREATE TABLE `tb_barang` (
  `id_barang` varchar(10) NOT NULL,
  `id_kategori` int(11) DEFAULT NULL,
  `nama_barang` varchar(100) NOT NULL,
  `satuan` varchar(20) DEFAULT NULL,
  `harga_jual` double NOT NULL,
  `stok` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_barang`
--

INSERT INTO `tb_barang` (`id_barang`, `id_kategori`, `nama_barang`, `satuan`, `harga_jual`, `stok`) VALUES
('B0005', 1, 'MBG', 'unit', 15000, 75),
('B001', 1, 'laptop asus', 'Unit', 8500000, 108),
('B002', 1, 'Mouse Logitech', 'Unit', 350000, 541),
('B003', 2, 'Indomie Goreng', 'Bungkus', 3500, 601),
('B004', 3, 'Air Mineral', 'Botol', 5000, 108),
('B009', 1, 'motor listrik', 'unit', 200000, 83),
('B010', 4, 'mykhonos', 'pcs', 100000, 20);

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_customer`
--

CREATE TABLE `tb_customer` (
  `id_customer` varchar(10) NOT NULL,
  `nama_customer` varchar(100) NOT NULL,
  `alamat` text DEFAULT NULL,
  `telepon` varchar(15) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_customer`
--

INSERT INTO `tb_customer` (`id_customer`, `nama_customer`, `alamat`, `telepon`) VALUES
('C002', 'Siti Rahma', 'Jl. Pahlawan No.5', '082222222222'),
('C003', 'kicaaaa', 'sragen', '0882397986'),
('C004', 'Agoy', 'Tangerang', '0082387482'),
('C005', 'April', 'Pondok Kacang', '0882293793'),
('CUS-001', 'agoyyyy', 'mana aja', '382923232232');

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_detail_penjualan`
--

CREATE TABLE `tb_detail_penjualan` (
  `id_detail` int(11) NOT NULL,
  `id_jual` int(11) DEFAULT NULL,
  `id_barang` varchar(10) DEFAULT NULL,
  `jumlah_beli` int(11) NOT NULL,
  `harga_satuan` double NOT NULL,
  `subtotal` double NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_detail_penjualan`
--

INSERT INTO `tb_detail_penjualan` (`id_detail`, `id_jual`, `id_barang`, `jumlah_beli`, `harga_satuan`, `subtotal`) VALUES
(1, 1, 'B003', 1, 4, 4),
(2, 2, 'B004', 1, 5000, 5000),
(3, 2, 'B003', 1, 3500, 3500),
(4, 2, 'B001', 1, 8500000, 8500000),
(5, 3, 'B009', 1, 200000, 200000),
(6, 4, 'B003', 1, 3500, 3500),
(7, 4, 'B001', 1, 8500000, 8500000),
(8, 5, 'B009', 2, 200000, 400000),
(9, 5, 'B0005', 1, 15, 15),
(10, 5, 'B001', 1, 8500000, 8500000),
(11, 5, 'B003', 1, 3500, 3500),
(12, 6, 'B0005', 1, 15, 15),
(13, 6, 'B001', 1, 8500000, 8500000),
(14, 7, 'B0005', 1, 15, 15),
(15, 7, 'B002', 2, 350000, 700000),
(16, 8, 'B0005', 1, 15, 15),
(17, 8, 'B009', 1, 200000, 200000),
(18, 9, 'B009', 1, 200000, 200000),
(19, 10, 'B003', 2, 3500, 7000),
(20, 11, 'B001', 1, 8500000, 8500000),
(21, 11, 'B003', 1, 3500, 3500),
(22, 12, 'B002', 1, 350000, 350000),
(23, 13, 'B0005', 1, 15, 15),
(24, 13, 'B003', 1, 3500, 3500),
(25, 14, 'B004', 1, 5000, 5000),
(26, 15, 'B003', 1, 3500, 3500),
(27, 16, 'B004', 1, 5000, 5000),
(28, 16, 'B009', 1, 200000, 200000),
(29, 17, 'B009', 1, 200000, 200000),
(30, 17, 'B001', 1, 8500000, 8500000),
(31, 18, 'B009', 1, 200000, 200000),
(32, 19, 'B001', 1, 8500000, 8500000),
(33, 19, 'B0005', 1, 15000, 15000),
(34, 20, 'B004', 1, 5000, 5000),
(35, 20, 'B003', 1, 3500, 3500),
(36, 21, 'B004', 5, 5000, 25000),
(37, 21, 'B003', 1, 3500, 3500),
(38, 22, 'B003', 1, 3500, 3500),
(39, 22, 'B004', 1, 5000, 5000),
(40, 23, 'B0005', 1, 15000, 15000),
(41, 24, 'B003', 1, 3500, 3500),
(42, 24, 'B004', 1, 5000, 5000),
(43, 24, 'B0005', 1, 15000, 15000),
(44, 25, 'B004', 1, 5000, 5000),
(45, 25, 'B003', 1, 3500, 3500),
(46, 26, 'B003', 4, 3500, 14000),
(47, 26, 'B001', 1, 8500000, 8500000),
(48, 27, 'B003', 1, 3500, 3500),
(49, 27, 'B001', 1, 8500000, 8500000),
(50, 28, 'B0005', 1, 15000, 15000),
(51, 29, 'B001', 1, 8500000, 8500000),
(52, 29, 'B0005', 1, 15000, 15000),
(53, 30, 'B003', 1, 3500, 3500),
(54, 30, 'B004', 1, 5000, 5000),
(55, 30, 'B001', 1, 8500000, 8500000),
(56, 31, 'B009', 1, 200000, 200000),
(57, 32, 'B004', 1, 5000, 5000),
(58, 32, 'B003', 1, 3500, 3500),
(59, 32, 'B0005', 1, 15000, 15000),
(60, 33, 'B003', 1, 3500, 3500),
(61, 33, 'B001', 1, 8500000, 8500000),
(62, 34, 'B004', 1, 5000, 5000),
(63, 34, 'B003', 1, 3500, 3500),
(64, 35, 'B009', 1, 200000, 200000),
(65, 35, 'B001', 1, 8500000, 8500000),
(66, 36, 'B0005', 2, 15000, 30000),
(67, 36, 'B009', 1, 200000, 200000),
(68, 36, 'B001', 1, 8500000, 8500000),
(69, 37, 'B003', 1, 3500, 3500),
(70, 38, 'B002', 1, 350000, 350000),
(71, 39, 'B003', 1, 3500, 3500),
(72, 39, 'B004', 1, 5000, 5000),
(73, 40, 'B001', 1, 8500000, 8500000),
(74, 40, 'B004', 1, 5000, 5000),
(75, 41, 'B003', 1, 3500, 3500),
(76, 41, 'B004', 1, 5000, 5000),
(77, 42, 'B009', 1, 200000, 200000),
(78, 42, 'B003', 1, 3500, 3500),
(79, 43, 'B004', 1, 5000, 5000),
(80, 47, 'B003', 1, 3500, 3500),
(81, 47, 'B009', 1, 200000, 200000),
(82, 48, 'B002', 1, 350000, 350000),
(83, 49, 'B003', 1, 3500, 3500),
(84, 50, 'B009', 1, 200000, 200000),
(85, 51, 'B001', 1, 8500000, 8500000),
(86, 52, 'B004', 1, 5000, 5000),
(87, 52, 'B003', 1, 3500, 3500),
(88, 53, 'B003', 1, 3500, 3500),
(89, 53, 'B009', 1, 200000, 200000),
(90, 55, 'B003', 1, 3500, 3500),
(91, 55, 'B0005', 1, 15000, 15000),
(92, 59, 'B003', 1, 3500, 3500),
(93, 59, 'B001', 1, 8500000, 8500000),
(94, 60, 'B0005', 1, 15000, 15000),
(95, 62, 'B004', 1, 5000, 5000),
(96, 62, 'B0005', 100, 15000, 1500000),
(97, 63, 'B0005', 1, 15000, 15000),
(98, 64, 'B0005', 1, 15000, 15000),
(99, 65, 'B004', 1, 5000, 5000),
(100, 65, 'B003', 1, 3500, 3500),
(101, 66, 'B003', 1, 3500, 3500),
(102, 66, 'B0005', 1, 15000, 15000),
(103, 67, 'B004', 1, 5000, 5000),
(104, 67, 'B003', 1, 3500, 3500),
(105, 67, 'B002', 1, 350000, 350000);

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_kategori`
--

CREATE TABLE `tb_kategori` (
  `id_kategori` int(11) NOT NULL,
  `nama_kategori` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_kategori`
--

INSERT INTO `tb_kategori` (`id_kategori`, `nama_kategori`) VALUES
(1, 'Elektronik'),
(2, 'Makanan'),
(3, 'Minuman'),
(4, 'obat'),
(5, 'barang'),
(6, 'aaddds'),
(7, 'adbjjs'),
(8, 'agoy'),
(9, 'awd'),
(10, 'yg'),
(11, 'djhcdji'),
(12, 'asdsd'),
(13, 'asiujsopp'),
(14, 'hg'),
(15, 'ooo');

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_penjualan`
--

CREATE TABLE `tb_penjualan` (
  `id_jual` int(11) NOT NULL,
  `tgl_transaksi` date NOT NULL,
  `id_customer` varchar(10) DEFAULT NULL,
  `total_bayar` double NOT NULL,
  `uang_bayar` double NOT NULL DEFAULT 0,
  `kembalian` double NOT NULL DEFAULT 0,
  `id_user` int(11) DEFAULT NULL,
  `metode_bayar` varchar(30) DEFAULT 'CASH'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_penjualan`
--

INSERT INTO `tb_penjualan` (`id_jual`, `tgl_transaksi`, `id_customer`, `total_bayar`, `uang_bayar`, `kembalian`, `id_user`, `metode_bayar`) VALUES
(1, '2026-05-30', 'C002', 4, 0, 0, 4, 'CASH'),
(2, '2026-05-30', 'C002', 8508500, 0, 0, 4, 'CASH'),
(3, '2026-05-30', 'C004', 200000, 0, 0, 4, 'CASH'),
(4, '2026-05-30', 'C002', 8503500, 0, 0, 4, 'CASH'),
(5, '2026-05-30', 'C002', 8903515, 0, 0, 4, 'CASH'),
(6, '2026-05-30', 'C002', 8500015, 0, 0, 4, 'CASH'),
(7, '2026-05-30', 'C002', 700015, 0, 0, 4, 'CASH'),
(8, '2026-05-30', 'C002', 200015, 0, 0, 4, 'CASH'),
(9, '2026-05-30', 'C002', 200000, 0, 0, 4, 'CASH'),
(10, '2026-05-30', 'C002', 7000, 0, 0, 4, 'CASH'),
(11, '2026-05-31', 'C002', 8503500, 0, 0, 4, 'CASH'),
(12, '2026-05-31', 'C002', 350000, 0, 0, 4, 'CASH'),
(13, '2026-05-31', 'C002', 3515, 0, 0, 4, 'CASH'),
(14, '2026-05-31', 'C002', 5000, 0, 0, 4, 'CASH'),
(15, '2026-05-31', 'C002', 3500, 0, 0, 4, 'CASH'),
(16, '2026-05-31', 'C002', 205000, 0, 0, 5, 'CASH'),
(17, '2026-05-31', 'C002', 8700000, 0, 0, 5, 'CASH'),
(18, '2026-05-31', 'C002', 200000, 0, 0, 5, 'CASH'),
(19, '2026-05-31', 'C002', 8515000, 0, 0, 5, 'CASH'),
(20, '2026-05-31', 'C002', 8500, 0, 0, 5, 'CASH'),
(21, '2026-05-31', 'C002', 28500, 0, 0, 5, 'CASH'),
(22, '2026-06-01', 'C002', 8500, 0, 0, 5, 'CASH'),
(23, '2026-06-01', 'C002', 15000, 0, 0, 5, 'CASH'),
(24, '2026-06-01', 'C002', 23500, 0, 0, 5, 'CASH'),
(25, '2026-06-01', 'C002', 8500, 0, 0, 5, 'CASH'),
(26, '2026-06-01', 'C002', 8514000, 0, 0, 5, 'CASH'),
(27, '2026-06-01', 'C002', 8503500, 0, 0, 5, 'CASH'),
(28, '2026-06-01', 'C002', 15000, 0, 0, 5, 'CASH'),
(29, '2026-06-01', 'C002', 8515000, 0, 0, 5, 'CASH'),
(30, '2026-06-01', 'C002', 8508500, 0, 0, 5, 'CASH'),
(31, '2026-06-01', 'C002', 200000, 0, 0, 5, 'CASH'),
(32, '2026-06-08', 'C002', 23500, 0, 0, 5, 'CASH'),
(33, '2026-06-08', 'C003', 8503500, 0, 0, 5, 'CASH'),
(34, '2026-06-08', 'C002', 8500, 100000, 91500, 5, 'CASH'),
(35, '2026-06-08', 'C005', 8700000, 10000000, 1300000, 5, 'CASH'),
(36, '2026-06-09', 'C002', 8730000, 10000000, 1270000, 5, 'CASH'),
(37, '2026-06-09', 'C002', 3500, 50000, 46500, 5, 'CASH'),
(38, '2026-06-10', 'C002', 350000, 5000000, 4650000, 5, 'CASH'),
(39, '2026-06-12', 'C002', 8500, 10000, 1500, 5, 'CASH'),
(40, '2026-06-12', 'C002', 8505000, 10000000, 1495000, 5, 'CASH'),
(41, '2026-06-12', 'C002', 8500, 8500, 0, 5, 'QRIS'),
(42, '2026-06-12', 'C002', 203500, 203500, 0, 5, 'QRIS'),
(43, '2026-06-12', 'C002', 5000, 5000, 0, 5, 'QRIS'),
(47, '2026-06-12', 'C002', 203500, 1000000, 796500, 5, 'CASH'),
(48, '2026-06-13', 'C002', 350000, 10000000, 9650000, 5, 'CASH'),
(49, '2026-06-13', 'C002', 3500, 3500, 0, 5, 'QRIS'),
(50, '2026-06-13', 'C002', 200000, 200000, 0, 5, 'QRIS'),
(51, '2026-06-13', 'C002', 8500000, 8500000, 0, 5, 'QRIS'),
(52, '2026-06-13', 'C002', 8500, 21000, 12500, 5, 'CASH'),
(53, '2026-06-13', 'C002', 203500, 203500, 0, 5, 'KARTU - Visa'),
(55, '2026-06-13', 'C002', 18500, 18500, 0, 5, 'QRIS'),
(59, '2026-06-13', 'C002', 8503500, 8503500, 0, 5, 'QRIS'),
(60, '2026-06-13', 'C002', 15000, 15000, 0, 5, 'QRIS'),
(62, '2026-06-13', 'C002', 1505000, 1505000, 0, 5, 'QRIS'),
(63, '2026-06-13', 'C002', 15000, 15000, 0, 5, 'QRIS'),
(64, '2026-06-13', 'C002', 15000, 15000, 0, 5, 'QRIS'),
(65, '2026-06-14', 'C002', 8500, 8500, 0, 5, 'QRIS'),
(66, '2026-06-14', 'C002', 18500, 18500, 0, 5, 'KARTU - Visa'),
(67, '2026-06-23', 'C002', 358500, 400024, 41524, 5, 'CASH');

-- --------------------------------------------------------

--
-- Struktur dari tabel `tb_user`
--

CREATE TABLE `tb_user` (
  `id_user` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `nama_lengkap` varchar(100) DEFAULT NULL,
  `level` enum('SuperAdmin','Admin','Kasir','Gudang') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data untuk tabel `tb_user`
--

INSERT INTO `tb_user` (`id_user`, `username`, `password`, `nama_lengkap`, `level`) VALUES
(1, 'admin', '1234', 'Administrator', 'Admin'),
(2, 'kasir1', '1234', 'Budi Kasir', 'Admin'),
(3, 'gudang1', '1234', 'Siti Gudang', 'Gudang'),
(4, 'superadmin', 'super123', 'Super Admin', 'SuperAdmin'),
(5, 'agoy', '123', 'agoy', 'SuperAdmin');

--
-- Indexes for dumped tables
--

--
-- Indeks untuk tabel `tb_barang`
--
ALTER TABLE `tb_barang`
  ADD PRIMARY KEY (`id_barang`),
  ADD KEY `id_kategori` (`id_kategori`);

--
-- Indeks untuk tabel `tb_customer`
--
ALTER TABLE `tb_customer`
  ADD PRIMARY KEY (`id_customer`);

--
-- Indeks untuk tabel `tb_detail_penjualan`
--
ALTER TABLE `tb_detail_penjualan`
  ADD PRIMARY KEY (`id_detail`),
  ADD KEY `id_jual` (`id_jual`),
  ADD KEY `id_barang` (`id_barang`);

--
-- Indeks untuk tabel `tb_kategori`
--
ALTER TABLE `tb_kategori`
  ADD PRIMARY KEY (`id_kategori`);

--
-- Indeks untuk tabel `tb_penjualan`
--
ALTER TABLE `tb_penjualan`
  ADD PRIMARY KEY (`id_jual`),
  ADD KEY `id_customer` (`id_customer`),
  ADD KEY `id_user` (`id_user`);

--
-- Indeks untuk tabel `tb_user`
--
ALTER TABLE `tb_user`
  ADD PRIMARY KEY (`id_user`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT untuk tabel yang dibuang
--

--
-- AUTO_INCREMENT untuk tabel `tb_detail_penjualan`
--
ALTER TABLE `tb_detail_penjualan`
  MODIFY `id_detail` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=106;

--
-- AUTO_INCREMENT untuk tabel `tb_kategori`
--
ALTER TABLE `tb_kategori`
  MODIFY `id_kategori` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT untuk tabel `tb_penjualan`
--
ALTER TABLE `tb_penjualan`
  MODIFY `id_jual` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=68;

--
-- AUTO_INCREMENT untuk tabel `tb_user`
--
ALTER TABLE `tb_user`
  MODIFY `id_user` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Ketidakleluasaan untuk tabel pelimpahan (Dumped Tables)
--

--
-- Ketidakleluasaan untuk tabel `tb_barang`
--
ALTER TABLE `tb_barang`
  ADD CONSTRAINT `tb_barang_ibfk_1` FOREIGN KEY (`id_kategori`) REFERENCES `tb_kategori` (`id_kategori`);

--
-- Ketidakleluasaan untuk tabel `tb_detail_penjualan`
--
ALTER TABLE `tb_detail_penjualan`
  ADD CONSTRAINT `tb_detail_penjualan_ibfk_1` FOREIGN KEY (`id_jual`) REFERENCES `tb_penjualan` (`id_jual`),
  ADD CONSTRAINT `tb_detail_penjualan_ibfk_2` FOREIGN KEY (`id_barang`) REFERENCES `tb_barang` (`id_barang`);

--
-- Ketidakleluasaan untuk tabel `tb_penjualan`
--
ALTER TABLE `tb_penjualan`
  ADD CONSTRAINT `tb_penjualan_ibfk_1` FOREIGN KEY (`id_customer`) REFERENCES `tb_customer` (`id_customer`),
  ADD CONSTRAINT `tb_penjualan_ibfk_2` FOREIGN KEY (`id_user`) REFERENCES `tb_user` (`id_user`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
