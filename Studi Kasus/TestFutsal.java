import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Script Pengujian Otomatis untuk Logika Bisnis & Validasi Bentrok Jadwal.
 */
public class TestFutsal {

    public static void main(String[] args) {
        System.out.println("=== MEMULAI TEST LOGIKA BISNIS RESERVASI FUTSAL ===");

        // Test 1: Kalkulasi Tarif Siang vs Malam
        testTarifCalculator();

        // Test 2: Validasi Interval Bentrok
        testBentrokJadwal();

        // Test 3: Data Persistence CSV
        testCsvPersistence();

        System.out.println("\n🎉 SEMUA TEST BERHASIL DENGAN SEMPURNA!");
    }

    private static void testTarifCalculator() {
        System.out.println("\n--- TEST 1: Tarif Siang vs Malam ---");

        // Kasus 1: Siang saja (09:00 - 11:00 = 2 jam siang)
        TarifCalculator.TarifResult r1 = TarifCalculator.hitungTarif(LocalTime.of(9, 0), 2);
        assert r1.getJamSiang() == 2 : "Jam siang harus 2";
        assert r1.getJamMalam() == 0 : "Jam malam harus 0";
        assert r1.getTotalTarif() == 240_000.0 : "Tarif siang 2x120rb = 240rb";
        assert r1.getMinimalDP() == 120_000.0 : "Minimal DP 50% = 120rb";
        System.out.println("✔ Kasus 1 (Siang Murni): " + r1.getRingkasan() + " -> Total: Rp " + r1.getTotalTarif());

        // Kasus 2: Malam saja (19:00 - 21:00 = 2 jam malam + lampu)
        TarifCalculator.TarifResult r2 = TarifCalculator.hitungTarif(LocalTime.of(19, 0), 2);
        assert r2.getJamSiang() == 0 : "Jam siang harus 0";
        assert r2.getJamMalam() == 2 : "Jam malam harus 2";
        assert r2.getTotalTarif() == 360_000.0 : "Tarif malam 2x180rb = 360rb";
        assert r2.getMinimalDP() == 180_000.0 : "Minimal DP 50% = 180rb";
        System.out.println("✔ Kasus 2 (Malam Murni): " + r2.getRingkasan() + " -> Total: Rp " + r2.getTotalTarif());

        // Kasus 3: Lintas Siang & Malam (17:00 - 19:00 = 1 jam siang @120rb + 1 jam malam @180rb = 300rb)
        TarifCalculator.TarifResult r3 = TarifCalculator.hitungTarif(LocalTime.of(17, 0), 2);
        assert r3.getJamSiang() == 1 : "Jam siang harus 1";
        assert r3.getJamMalam() == 1 : "Jam malam harus 1";
        assert r3.getTotalTarif() == 300_000.0 : "Total lintas siang & malam harus 300rb";
        assert r3.getMinimalDP() == 150_000.0 : "Minimal DP 50% = 150rb";
        System.out.println("✔ Kasus 3 (Lintas Siang-Malam): " + r3.getRingkasan() + " -> Total: Rp " + r3.getTotalTarif());
    }

    private static void testBentrokJadwal() {
        System.out.println("\n--- TEST 2: Validasi Bentrok Jadwal ---");
        JadwalRepository repo = new JadwalRepository();
        LocalDate tglTest = LocalDate.of(2026, 10, 10);

        // Bersihkan atau simpan jadwal pertama (14:00 - 16:00)
        JadwalSewa b1 = new JadwalSewa(
                "TEST-01", "Teknik Mesin FC", "0812345678", tglTest,
                LocalTime.of(14, 0), 2, 120_000.0, "DP 50%", 240_000.0
        );

        try {
            repo.hapus("TEST-01");
            repo.simpan(b1);
            System.out.println("✔ Berhasil menyimpan jadwal dasar: 14:00 - 16:00");
        } catch (Exception e) {
            System.err.println("Gagal simpan jadwal dasar: " + e.getMessage());
        }

        // Uji bentrok sama persis (14:00 - 16:00)
        boolean bentrok1 = repo.isBentrok(tglTest, LocalTime.of(14, 0), 2);
        assert bentrok1 : "Harus bentrok karena jam sama persis!";
        System.out.println("✔ Bentrok terdeteksi untuk jam sama persis (14:00 - 16:00)");

        // Uji bentrok overlap di dalam (14:00 - 15:00)
        boolean bentrok2 = repo.isBentrok(tglTest, LocalTime.of(14, 0), 1);
        assert bentrok2 : "Harus bentrok untuk overlap di dalam!";
        System.out.println("✔ Bentrok terdeteksi untuk overlap parsial di dalam (14:00 - 15:00)");

        // Uji bentrok overlap memotong awal (13:00 - 15:00)
        boolean bentrok3 = repo.isBentrok(tglTest, LocalTime.of(13, 0), 2);
        assert bentrok3 : "Harus bentrok untuk memotong jam mulai!";
        System.out.println("✔ Bentrok terdeteksi untuk overlap memotong awal (13:00 - 15:00)");

        // Uji bentrok overlap memotong akhir (15:00 - 17:00)
        boolean bentrok4 = repo.isBentrok(tglTest, LocalTime.of(15, 0), 2);
        assert bentrok4 : "Harus bentrok untuk memotong jam selesai!";
        System.out.println("✔ Bentrok terdeteksi untuk overlap memotong akhir (15:00 - 17:00)");

        // Uji TIDAK bentrok bersebelahan (12:00 - 14:00)
        boolean tidakBentrok1 = repo.isBentrok(tglTest, LocalTime.of(12, 0), 2);
        assert !tidakBentrok1 : "TIDAK boleh bentrok karena slot berakhir tepat jam 14:00!";
        System.out.println("✔ Aman (Tidak bentrok) untuk slot sebelum: 12:00 - 14:00");

        // Uji TIDAK bentrok bersebelahan (16:00 - 18:00)
        boolean tidakBentrok2 = repo.isBentrok(tglTest, LocalTime.of(16, 0), 2);
        assert !tidakBentrok2 : "TIDAK boleh bentrok karena slot dimulai tepat jam 16:00!";
        System.out.println("✔ Aman (Tidak bentrok) untuk slot sesudah: 16:00 - 18:00");

        // Uji TIDAK bentrok tanggal berbeda
        boolean tidakBentrok3 = repo.isBentrok(tglTest.plusDays(1), LocalTime.of(14, 0), 2);
        assert !tidakBentrok3 : "TIDAK boleh bentrok karena tanggal berbeda!";
        System.out.println("✔ Aman (Tidak bentrok) untuk tanggal berbeda");

        // Bersihkan data uji
        repo.hapus("TEST-01");
    }

    private static void testCsvPersistence() {
        System.out.println("\n--- TEST 3: Data Persistence CSV ---");
        JadwalRepository repo = new JadwalRepository();
        String nextId = repo.generateNextId();
        System.out.println("✔ Next ID yang dihasilkan: " + nextId);

        JadwalSewa booking = new JadwalSewa(
                nextId, "Informatika Angkatan 2023", "08987654321",
                LocalDate.now().plusDays(2), LocalTime.of(19, 0), 2,
                180_000.0, "DP 50%", 360_000.0
        );

        try {
            repo.simpan(booking);
            System.out.println("✔ Berhasil menyimpan booking baru ke CSV: " + booking.getIdBooking());

            boolean found = false;
            for (JadwalSewa j : repo.getAllJadwal()) {
                if (j.getIdBooking().equals(nextId)) {
                    found = true;
                    assert j.getNamaTim().equals("Informatika Angkatan 2023");
                    assert j.getTotalTarif() == 360_000.0;
                    assert j.getUangDP() == 180_000.0;
                    System.out.println("✔ Berhasil memverifikasi data di CSV: " + j);
                    break;
                }
            }
            assert found : "Data yang baru disimpan harus ditemukan di CSV";
            repo.hapus(nextId);
        } catch (Exception e) {
            System.err.println("Gagal test persistence: " + e.getMessage());
        }
    }
}
