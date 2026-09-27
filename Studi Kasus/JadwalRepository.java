import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Repository data persistence untuk jadwal_futsal.csv.
 * Bertanggung jawab membaca, menyimpan, menghapus, serta
 * memvalidasi bentrok waktu sewa antar tim.
 */
public class JadwalRepository {

    private static final String FILE_NAME = "jadwal_futsal.csv";
    private static final String HEADER = "idBooking,namaTim,noHp,tanggalMain,jamMulai,durasiJam,uangDP,statusDP,totalTarif";

    public JadwalRepository() {
        initFile();
    }

    /**
     * Inisialisasi berkas CSV jika belum ada.
     */
    public synchronized void initFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println(HEADER);
            } catch (IOException e) {
                System.err.println("Gagal inisialisasi file CSV: " + e.getMessage());
            }
        }
    }

    /**
     * Mengambil seluruh data jadwal dari file CSV, diurutkan berdasarkan tanggal & jam mulai.
     */
    public synchronized List<JadwalSewa> getAllJadwal() {
        List<JadwalSewa> list = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Lewati header CSV
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                JadwalSewa jadwal = JadwalSewa.fromCsvLine(line);
                if (jadwal != null) {
                    list.add(jadwal);
                }
            }
        } catch (IOException e) {
            System.err.println("Error saat membaca file CSV: " + e.getMessage());
        }

        list.sort(Comparator.comparing(JadwalSewa::getTanggalMain)
                .thenComparing(JadwalSewa::getJamMulai));

        return list;
    }

    /**
     * Mengambil jadwal sewa pada tanggal tertentu.
     */
    public synchronized List<JadwalSewa> getJadwalByTanggal(LocalDate tanggal) {
        List<JadwalSewa> hasil = new ArrayList<>();
        for (JadwalSewa item : getAllJadwal()) {
            if (item.getTanggalMain().equals(tanggal)) {
                hasil.add(item);
            }
        }
        return hasil;
    }

    /**
     * Memeriksa apakah slot yang diinginkan bentrok dengan jadwal yang sudah ada.
     * Mengembalikan objek JadwalSewa yang bentrok jika ada, atau null jika slot aman.
     *
     * Logika tumpang tindih interval [mulai1, selesai1) dan [mulai2, selesai2):
     * overlap = mulai1 < selesai2 && selesai1 > mulai2
     */
    public synchronized JadwalSewa findBentrok(LocalDate tgl, LocalTime jamMulai, int durasi, String excludeIdBooking) {
        LocalTime jamSelesai = jamMulai.plusHours(durasi);
        List<JadwalSewa> list = getAllJadwal();

        for (JadwalSewa item : list) {
            if (excludeIdBooking != null && excludeIdBooking.equalsIgnoreCase(item.getIdBooking())) {
                continue;
            }

            if (item.getTanggalMain().equals(tgl)) {
                LocalTime exMulai = item.getJamMulai();
                LocalTime exSelesai = item.getJamSelesai();

                boolean isOverlap = jamMulai.isBefore(exSelesai) && jamSelesai.isAfter(exMulai);
                if (isOverlap) {
                    return item; // Ditemukan jadwal yang bertabrakan
                }
            }
        }
        return null;
    }

    public synchronized boolean isBentrok(LocalDate tgl, LocalTime jamMulai, int durasi) {
        return findBentrok(tgl, jamMulai, durasi, null) != null;
    }

    /**
     * Menyimpan jadwal sewa baru ke CSV dengan validasi bentrok.
     */
    public synchronized void simpan(JadwalSewa jadwal) throws IOException, IllegalStateException {
        JadwalSewa bentrok = findBentrok(jadwal.getTanggalMain(), jadwal.getJamMulai(), jadwal.getDurasiJam(), null);
        if (bentrok != null) {
            throw new IllegalStateException(String.format(
                    "Jadwal BENTROK dengan pemesanan Tim '%s' (%s s.d %s)!",
                    bentrok.getNamaTim(), bentrok.getJamMulai(), bentrok.getJamSelesai()
            ));
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME, true))) {
            pw.println(jadwal.toCsvLine());
        }
    }

    /**
     * Menghapus jadwal berdasarkan ID Booking.
     */
    public synchronized boolean hapus(String idBooking) {
        List<JadwalSewa> list = getAllJadwal();
        boolean ditemukan = false;

        List<JadwalSewa> filtered = new ArrayList<>();
        for (JadwalSewa item : list) {
            if (item.getIdBooking().equalsIgnoreCase(idBooking)) {
                ditemukan = true;
            } else {
                filtered.add(item);
            }
        }

        if (ditemukan) {
            tulisUlangCsv(filtered);
        }
        return ditemukan;
    }

    private synchronized void tulisUlangCsv(List<JadwalSewa> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME, false))) {
            pw.println(HEADER);
            for (JadwalSewa item : list) {
                pw.println(item.toCsvLine());
            }
        } catch (IOException e) {
            System.err.println("Gagal menulis ulang file CSV: " + e.getMessage());
        }
    }

    /**
     * Menghasilkan ID Booking berikutnya secara otomatis (misal: BKG-101, BKG-102).
     */
    public synchronized String generateNextId() {
        List<JadwalSewa> list = getAllJadwal();
        int maxNumber = 100;

        for (JadwalSewa j : list) {
            String id = j.getIdBooking();
            if (id != null && id.toUpperCase().startsWith("BKG-")) {
                try {
                    int num = Integer.parseInt(id.substring(4).trim());
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("BKG-%03d", maxNumber + 1);
    }

    public static String getFileName() {
        return FILE_NAME;
    }
}
