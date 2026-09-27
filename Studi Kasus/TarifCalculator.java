import java.time.LocalTime;

/**
 * Service kalkulasi tarif Futsal & Mini Soccer Arena Malang.
 * Menghitung perbedaan tarif otomatis antara siang vs malam (lampu penerangan).
 */
public class TarifCalculator {

    // Konstanta Tarif Lapangan Futsal
    public static final double TARIF_SIANG_PER_JAM = 120_000.0; // 08:00 - 18:00 (Pencahayaan Alami)
    public static final double TARIF_MALAM_PER_JAM = 180_000.0; // 18:00 - 23:00 (Dengan Lampu Sorot Arena)
    public static final LocalTime BATAS_MALAM = LocalTime.of(18, 0);
    public static final LocalTime JAM_BUKA = LocalTime.of(8, 0);
    public static final LocalTime JAM_TUTUP = LocalTime.of(23, 0);

    public static class TarifResult {
        private final int durasiTotal;
        private final int jamSiang;
        private final int jamMalam;
        private final double subtotalSiang;
        private final double subtotalMalam;
        private final double totalTarif;
        private final double minimalDP;
        private final String ringkasan;

        public TarifResult(int durasiTotal, int jamSiang, int jamMalam,
                           double subtotalSiang, double subtotalMalam,
                           double totalTarif, double minimalDP, String ringkasan) {
            this.durasiTotal = durasiTotal;
            this.jamSiang = jamSiang;
            this.jamMalam = jamMalam;
            this.subtotalSiang = subtotalSiang;
            this.subtotalMalam = subtotalMalam;
            this.totalTarif = totalTarif;
            this.minimalDP = minimalDP;
            this.ringkasan = ringkasan;
        }

        public int getDurasiTotal() { return durasiTotal; }
        public int getJamSiang() { return jamSiang; }
        public int getJamMalam() { return jamMalam; }
        public double getSubtotalSiang() { return subtotalSiang; }
        public double getSubtotalMalam() { return subtotalMalam; }
        public double getTotalTarif() { return totalTarif; }
        public double getMinimalDP() { return minimalDP; }
        public String getRingkasan() { return ringkasan; }
    }

    /**
     * Hitung rincian tarif berdasarkan jam mulai dan durasi sewa.
     * Secara otomatis menghitung per jam: siang (sebelum 18:00) vs malam (mulai 18:00).
     */
    public static TarifResult hitungTarif(LocalTime jamMulai, int durasiJam) {
        int jamSiang = 0;
        int jamMalam = 0;

        for (int i = 0; i < durasiJam; i++) {
            LocalTime slot = jamMulai.plusHours(i);
            if (slot.isBefore(BATAS_MALAM)) {
                jamSiang++;
            } else {
                jamMalam++;
            }
        }

        double subtotalSiang = jamSiang * TARIF_SIANG_PER_JAM;
        double subtotalMalam = jamMalam * TARIF_MALAM_PER_JAM;
        double totalTarif = subtotalSiang + subtotalMalam;
        double minimalDP = totalTarif * 0.50; // DP minimal 50%

        StringBuilder sb = new StringBuilder();
        if (jamSiang > 0 && jamMalam > 0) {
            sb.append(String.format("%d Jam Siang (@Rp %,.0f) + %d Jam Malam + Lampu (@Rp %,.0f)",
                    jamSiang, TARIF_SIANG_PER_JAM, jamMalam, TARIF_MALAM_PER_JAM));
        } else if (jamMalam > 0) {
            sb.append(String.format("%d Jam Malam + Lampu Sorot (@Rp %,.0f)",
                    jamMalam, TARIF_MALAM_PER_JAM));
        } else {
            sb.append(String.format("%d Jam Siang (@Rp %,.0f)",
                    jamSiang, TARIF_SIANG_PER_JAM));
        }

        return new TarifResult(durasiJam, jamSiang, jamMalam, subtotalSiang, subtotalMalam,
                totalTarif, minimalDP, sb.toString());
    }

    public static boolean isJamMalam(LocalTime time) {
        return !time.isBefore(BATAS_MALAM);
    }
}
