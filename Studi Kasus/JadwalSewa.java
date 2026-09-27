import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Entitas Model JadwalSewa
 * Menyimpan data pemesanan lapangan Futsal & Mini Soccer Arena Malang
 */
public class JadwalSewa {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Atribut sesuai rancangan OOP studi kasus
    private String idBooking;
    private String namaTim;
    private String noHp;
    private LocalDate tanggalMain;
    private LocalTime jamMulai;
    private int durasiJam;
    private String statusDP;
    private double totalTarif;

    // Atribut pendukung
    private double uangDP;

    public JadwalSewa() {
    }

    public JadwalSewa(String idBooking, String namaTim, String noHp, LocalDate tanggalMain,
                      LocalTime jamMulai, int durasiJam, double uangDP, String statusDP, double totalTarif) {
        this.idBooking = idBooking;
        this.namaTim = namaTim;
        this.noHp = noHp;
        this.tanggalMain = tanggalMain;
        this.jamMulai = jamMulai;
        this.durasiJam = durasiJam;
        this.uangDP = uangDP;
        this.statusDP = statusDP;
        this.totalTarif = totalTarif;
    }

    // Getters & Setters
    public String getIdBooking() {
        return idBooking;
    }

    public void setIdBooking(String idBooking) {
        this.idBooking = idBooking;
    }

    public String getNamaTim() {
        return namaTim;
    }

    public void setNamaTim(String namaTim) {
        this.namaTim = namaTim;
    }

    public String getNoHp() {
        return noHp;
    }

    public void setNoHp(String noHp) {
        this.noHp = noHp;
    }

    public LocalDate getTanggalMain() {
        return tanggalMain;
    }

    public void setTanggalMain(LocalDate tanggalMain) {
        this.tanggalMain = tanggalMain;
    }

    public LocalTime getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(LocalTime jamMulai) {
        this.jamMulai = jamMulai;
    }

    public int getDurasiJam() {
        return durasiJam;
    }

    public void setDurasiJam(int durasiJam) {
        this.durasiJam = durasiJam;
    }

    public String getStatusDP() {
        return statusDP;
    }

    public void setStatusDP(String statusDP) {
        this.statusDP = statusDP;
    }

    public double getTotalTarif() {
        return totalTarif;
    }

    public void setTotalTarif(double totalTarif) {
        this.totalTarif = totalTarif;
    }

    public double getUangDP() {
        return uangDP;
    }

    public void setUangDP(double uangDP) {
        this.uangDP = uangDP;
    }

    // Business Logic Methods
    public LocalTime getJamSelesai() {
        return jamMulai.plusHours(durasiJam);
    }

    public double getSisaBayar() {
        double sisa = totalTarif - uangDP;
        return Math.max(0, sisa);
    }

    public boolean isLunas() {
        return uangDP >= totalTarif;
    }

    /**
     * Konversi ke baris CSV:
     * idBooking,namaTim,noHp,tanggalMain,jamMulai,durasiJam,uangDP,statusDP,totalTarif
     */
    public String toCsvLine() {
        return escapeCsv(idBooking) + "," +
               escapeCsv(namaTim) + "," +
               escapeCsv(noHp) + "," +
               tanggalMain.format(DATE_FORMATTER) + "," +
               jamMulai.format(TIME_FORMATTER) + "," +
               durasiJam + "," +
               String.format("%.0f", uangDP) + "," +
               escapeCsv(statusDP) + "," +
               String.format("%.0f", totalTarif);
    }

    public static JadwalSewa fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = parseCsvLine(line);
        if (parts.length < 9) {
            return null;
        }

        try {
            String idBooking = parts[0].trim();
            String namaTim = parts[1].trim();
            String noHp = parts[2].trim();
            LocalDate tanggalMain = LocalDate.parse(parts[3].trim(), DATE_FORMATTER);
            LocalTime jamMulai = LocalTime.parse(parts[4].trim(), TIME_FORMATTER);
            int durasiJam = Integer.parseInt(parts[5].trim());
            double uangDP = Double.parseDouble(parts[6].trim());
            String statusDP = parts[7].trim();
            double totalTarif = Double.parseDouble(parts[8].trim());

            return new JadwalSewa(idBooking, namaTim, noHp, tanggalMain, jamMulai, durasiJam, uangDP, statusDP, totalTarif);
        } catch (Exception e) {
            System.err.println("Gagal mem-parsing baris CSV: " + line + " -> " + e.getMessage());
            return null;
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "JadwalSewa{" +
                "idBooking='" + idBooking + '\'' +
                ", namaTim='" + namaTim + '\'' +
                ", tanggalMain=" + tanggalMain +
                ", jam=" + jamMulai + "-" + getJamSelesai() +
                ", totalTarif=" + totalTarif +
                ", statusDP='" + statusDP + '\'' +
                '}';
    }
}
