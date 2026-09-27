# ⚽ Futsal & Mini Soccer Arena Malang — Sistem Reservasi Lapangan Olahraga

Aplikasi desktop GUI berbasis **Java (Swing & Java Time API)** untuk manajemen reservasi jadwal sewa lapangan futsal & mini soccer di Malang. Sistem ini dirancang untuk menyelesaikan permasalahan **bentrok jadwal** antar fakultas / angkatan akibat pencatatan manual di buku tulis.

---

## 🎯 Fitur Utama & Kepatuhan Studi Kasus

1. **Rancangan OOP (Object-Oriented Programming):**
   - Class `JadwalSewa` dengan atribut lengkap:
     - `idBooking` (String, auto-generated unik seperti `BKG-101`)
     - `namaTim` (String)
     - `noHp` (String)
     - `tanggalMain` (LocalDate)
     - `jamMulai` (LocalTime)
     - `durasiJam` (int)
     - `statusDP` (String)
     - `totalTarif` (double)
     - Serta atribut & metode pendukung: `uangDP`, `getJamSelesai()`, `getSisaBayar()`, `toCsvLine()`, `fromCsvLine()`.

2. **Antarmuka Grafis (GUI):**
   - **Pemilihan Tanggal Kalender:** Dialog interaktif kalender visual bulanan (`CalendarDialog`) dan tombol cepat (*Hari Ini*, *Besok*).
   - **Slot Jam Terstruktur:** Pilihan jam mulai 08.00 s.d. 22.00 WIB (operasional s.d. 23.00 WIB).
   - **Perbedaan Tarif Otomatis (Siang vs Malam + Lampu):**
     - Siang (08.00 - 18.00 WIB): **Rp 120.000 / jam** (pencahayaan alami)
     - Malam (18.00 - 23.00 WIB): **Rp 180.000 / jam** (dengan lampu sorot penerangan arena)
     - *Kalkulasi proporsional real-time jika durasi sewa mencakup lintas siang dan malam*.
   - **Input Uang Muka (DP Minimal 50%):**
     - Validasi otomatis secara live: jika DP < 50%, sistem menolak dan menampilkan indikator peringatan merah.
     - Tombol shortcut praktis: `[DP 50%]` dan `[Lunas 100%]`.
     - Tampilan informasi sisa pembayaran yang harus dilunasi di kasir arena.

3. **Data Persistence & Validasi Anti-Bentrok (`jadwal_futsal.csv`):**
   - Data tersimpan persisten ke dalam berkas `jadwal_futsal.csv`.
   - **Pencegahan Bentrok Jadwal (Conflict Detection):**
     Sistem memvalidasi relasi interval waktu `[jamMulai, jamSelesai)` pada tanggal yang sama. Jika ada irisan waktu dengan tim lain, penyimpanan ditolak dan sistem menampilkan informasi tim yang telah memesan sebelumnya.

4. **Fitur Tambahan:**
   - **Visual Timeline Slot Lapangan:** Panel visual 15 blok jam (08:00 - 23:00) untuk tanggal yang dipilih dengan kode warna (Hijau = Tersedia, Merah = Terisi nama tim, Kuning = Slot malam).
   - **Pencetakan Bukti Struk:** Dialog bukti pemesanan resmi (`StrukDialog`) yang dapat diekspor/disimpan ke file teks `.txt`.
   - **Filter Pencarian & Pembatalan:** Kemampuan mencari pemesanan berdasarkan nama tim/tanggal dan membatalkan jadwal.

---

## 📂 Struktur Berkas

```
c:\Users\Dell\Documents\Studi Kasus\
│
├── JadwalSewa.java         # Model Entitas OOP (Data, formatting & serialisasi CSV)
├── TarifCalculator.java    # Service kalkulasi tarif siang vs malam & syarat DP 50%
├── JadwalRepository.java   # Data persistence & logika validasi anti-bentrok CSV
├── CalendarDialog.java     # Komponen GUI kalender interaktif Swing
├── StrukDialog.java        # Dialog bukti cetak/simpan struk pemesanan resmi
├── FutsalApp.java          # Window GUI utama (Form, visualizer timeline & tabel)
├── Main.java               # Titik masuk program (Look and Feel & startup)
├── TestFutsal.java         # Unit test otomatis pengujian logika bisnis
├── jadwal_futsal.csv       # Berkas database persisten CSV
└── run.bat                 # Script batch sekali klik untuk Windows
```

---

## 🚀 Cara Menjalankan Aplikasi

### Opsi 1: Menggunakan Script `run.bat`
Klik ganda pada berkas **`run.bat`** di File Explorer.

### Opsi 2: Menggunakan Terminal / Command Prompt
Buka terminal di direktori proyek, lalu jalankan:

```bash
# Kompilasi semua berkas Java
javac *.java

# Jalankan aplikasi
java Main
```

### Opsi 3: Menjalankan Unit Test Logika Bisnis
Untuk menguji validasi kalkulasi tarif dan algoritma bentrok interval:

```bash
java -ea TestFutsal
```
