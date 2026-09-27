import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dialog Struk / Bukti Resmi Reservasi Lapangan.
 */
public class StrukDialog extends JDialog {

    private final JadwalSewa jadwal;

    public StrukDialog(Window parent, JadwalSewa jadwal) {
        super(parent, "Bukti Pemesanan Lapangan - " + jadwal.getIdBooking(), ModalityType.APPLICATION_MODAL);
        this.jadwal = jadwal;

        setSize(480, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // Content Area (Struk Receipt Card)
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextArea txtReceipt = new JTextArea();
        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReceipt.setBackground(new Color(254, 252, 232)); // Light cream paper look
        txtReceipt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        txtReceipt.setText(generateReceiptText());
        contentPanel.add(new JScrollPane(txtReceipt), BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnSimpanTxt = new JButton("💾 Simpan Struk (.txt)");
        btnSimpanTxt.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSimpanTxt.setFocusPainted(false);
        btnSimpanTxt.addActionListener(e -> simpanKeFile());

        JButton btnTutup = new JButton("Tutup");
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTutup.setBackground(new Color(15, 23, 42));
        btnTutup.setForeground(Color.WHITE);
        btnTutup.setFocusPainted(false);
        btnTutup.addActionListener(e -> dispose());

        bottomPanel.add(btnSimpanTxt);
        bottomPanel.add(btnTutup);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private String generateReceiptText() {
        DateTimeFormatter tglFmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale.of("id", "ID"));
        TarifCalculator.TarifResult tarif = TarifCalculator.hitungTarif(jadwal.getJamMulai(), jadwal.getDurasiJam());

        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("        FUTSAL & MINI SOCCER ARENA MALANG        \n");
        sb.append("   Jl. Soekarno-Hatta, Malang - Jawa Timur        \n");
        sb.append("            WhatsApp: 0812-3456-7890             \n");
        sb.append("==================================================\n");
        sb.append(String.format("ID Pemesanan : %s\n", jadwal.getIdBooking()));
        sb.append(String.format("Nama Tim     : %s\n", jadwal.getNamaTim()));
        sb.append(String.format("Kontak (WA)  : %s\n", jadwal.getNoHp()));
        sb.append(String.format("Tanggal Main : %s\n", jadwal.getTanggalMain().format(tglFmt)));
        sb.append(String.format("Slot Waktu   : %s s.d. %s WIB (%d Jam)\n",
                jadwal.getJamMulai(), jadwal.getJamSelesai(), jadwal.getDurasiJam()));
        sb.append("--------------------------------------------------\n");
        sb.append("RINCIAN TARIF SEWA:\n");
        if (tarif.getJamSiang() > 0) {
            sb.append(String.format(" - Siang (08:00-18:00) : %d Jam x Rp %,.0f\n",
                    tarif.getJamSiang(), TarifCalculator.TARIF_SIANG_PER_JAM));
            sb.append(String.format("   Subtotal Siang      : Rp %,.0f\n", tarif.getSubtotalSiang()));
        }
        if (tarif.getJamMalam() > 0) {
            sb.append(String.format(" - Malam (+ Lampu)     : %d Jam x Rp %,.0f\n",
                    tarif.getJamMalam(), TarifCalculator.TARIF_MALAM_PER_JAM));
            sb.append(String.format("   Subtotal Malam      : Rp %,.0f\n", tarif.getSubtotalMalam()));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("TOTAL BIAYA SEWA : Rp %,.0f\n", jadwal.getTotalTarif()));
        sb.append(String.format("UANG MUKA (DP)   : Rp %,.0f\n", jadwal.getUangDP()));
        sb.append(String.format("STATUS DP        : %s\n", jadwal.getStatusDP()));
        sb.append(String.format("SISA PELUNASAN   : Rp %,.0f\n", jadwal.getSisaBayar()));
        sb.append("==================================================\n");
        sb.append("CATATAN & ATURAN ARENA:\n");
        sb.append("1. Harap hadir 15 menit sebelum jam main dimulai.\n");
        sb.append("2. Wajib menggunakan sepatu futsal/turf standar.\n");
        sb.append("3. Pelunasan sisa biaya sewa dilakukan di kasir\n");
        sb.append("   sebelum tim memasuki lapangan.\n");
        sb.append("4. Pembatalan H-1 tidak mengembalikan uang DP.\n");
        sb.append("==================================================\n");
        sb.append("       TERIMA KASIH ATAS KUNJUNGAN ANDA!          \n");
        sb.append("==================================================\n");
        return sb.toString();
    }

    private void simpanKeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Struk_" + jadwal.getIdBooking() + ".txt"));
        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.write(generateReceiptText());
                JOptionPane.showMessageDialog(this,
                        "Struk berhasil disimpan di:\n" + file.getAbsolutePath(),
                        "Sukses Menyimpan Struk", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan file: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
