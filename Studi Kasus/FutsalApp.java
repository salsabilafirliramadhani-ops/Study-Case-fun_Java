import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Aplikasi GUI Reservasi Lapangan Futsal & Mini Soccer Arena Malang.
 * Dibuat menggunakan Java Swing dengan tema modern dan responsif.
 */
public class FutsalApp extends JFrame {

    private final JadwalRepository repository;
    private final DecimalFormat rupiahFmt = new DecimalFormat("#,###");
    private final DateTimeFormatter tglDisplayFmt = DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.of("id", "ID"));

    // Form Components
    private JTextField txtIdBooking;
    private JTextField txtNamaTim;
    private JTextField txtNoHp;
    private JTextField txtTanggal;
    private LocalDate selectedDate;
    private JComboBox<String> cbJamMulai;
    private JComboBox<String> cbDurasi;
    private JLabel lblJamSelesai;

    // Kalkulasi Tarif & DP
    private JLabel lblTarifSiangInfo;
    private JLabel lblTarifMalamInfo;
    private JLabel lblTotalTarif;
    private JLabel lblMinimalDP;
    private JTextField txtUangDP;
    private JLabel lblStatusDPPreview;
    private JLabel lblSisaPelunasan;

    // Timeline Visual Slot
    private JPanel panelTimelineSlots;
    private JLabel lblTimelineTanggal;

    // Tabel Reservasi
    private JTable tableJadwal;
    private DefaultTableModel tableModel;
    private JTextField txtCari;

    public FutsalApp() {
        super("Futsal & Mini Soccer Arena Malang — Sistem Reservasi Lapangan");
        this.repository = new JadwalRepository();
        this.selectedDate = LocalDate.now();

        initUI();
        loadTableData();
        refreshTimeline();
        resetForm();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(241, 245, 249));
        setLayout(new BorderLayout());

        // 1. Header Bar
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Main Content (Split Left: Form Input, Right: Visual Timeline & Table)
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBackground(new Color(241, 245, 249));
        mainPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Panel Kiri: Form Reservasi
        JPanel leftPanel = createFormPanel();
        leftPanel.setPreferredSize(new Dimension(460, 0));
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // Panel Kanan: Timeline Slot & Tabel Data
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ==========================================
    // 1. HEADER PANEL
    // ==========================================
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42)); // Slate 900
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("⚽ FUTSAL & MINI SOCCER ARENA MALANG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Sistem Manajemen Slot Lapangan — Mencegah Bentrok Sewa Antar Fakultas & Angkatan");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(148, 163, 184)); // Slate 400

        brandPanel.add(lblTitle);
        brandPanel.add(lblSub);
        header.add(brandPanel, BorderLayout.WEST);

        // Badges info operasional
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        infoPanel.setOpaque(false);

        JLabel badgeJam = createBadge("🕒 Operasional: 08.00 - 23.00 WIB", new Color(30, 41, 59), Color.WHITE);
        JLabel badgeLokasi = createBadge("📍 Jl. Soekarno-Hatta, Malang", new Color(6, 78, 59), new Color(110, 231, 183));

        infoPanel.add(badgeJam);
        infoPanel.add(badgeLokasi);
        header.add(infoPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(6, 12, 6, 12));
        return badge;
    }

    // ==========================================
    // 2. FORM RESERVASI (PANEL KIRI)
    // ==========================================
    private JPanel createFormPanel() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel lblFormTitle = new JLabel("📋 Formulir Reservasi Baru");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblFormTitle.setForeground(new Color(15, 23, 42));
        lblFormTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        card.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(Color.WHITE);

        // ID Booking
        txtIdBooking = createStyledTextField();
        txtIdBooking.setEditable(false);
        txtIdBooking.setBackground(new Color(241, 245, 249));
        fieldsPanel.add(createFieldGroup("ID Pemesanan (Auto):", txtIdBooking));

        // Nama Tim
        txtNamaTim = createStyledTextField();
        fieldsPanel.add(createFieldGroup("Nama Tim / Pemesan (Fakultas/Angkatan):", txtNamaTim));

        // No HP / WhatsApp
        txtNoHp = createStyledTextField();
        fieldsPanel.add(createFieldGroup("Nomor Kontak (WhatsApp):", txtNoHp));

        // Pemilihan Tanggal
        JPanel dateGroup = new JPanel(new BorderLayout(8, 0));
        dateGroup.setOpaque(false);
        txtTanggal = createStyledTextField();
        txtTanggal.setEditable(false);
        txtTanggal.setText(selectedDate.format(tglDisplayFmt));

        JButton btnPilihTanggal = new JButton("📅 Kalender...");
        btnPilihTanggal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPilihTanggal.setBackground(new Color(241, 245, 249));
        btnPilihTanggal.setFocusPainted(false);
        btnPilihTanggal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPilihTanggal.addActionListener(e -> {
            LocalDate chosen = CalendarDialog.showDialog(this, selectedDate);
            if (chosen != null) {
                selectedDate = chosen;
                txtTanggal.setText(selectedDate.format(tglDisplayFmt));
                updateTarifKalkulasi();
                refreshTimeline();
            }
        });

        dateGroup.add(txtTanggal, BorderLayout.CENTER);
        dateGroup.add(btnPilihTanggal, BorderLayout.EAST);
        fieldsPanel.add(createFieldGroup("Tanggal Sewa Lapangan:", dateGroup));

        // Pilihan Slot Jam & Durasi (Grid 2 Kolom)
        JPanel slotRow = new JPanel(new GridLayout(1, 2, 10, 0));
        slotRow.setOpaque(false);

        cbJamMulai = new JComboBox<>(new String[]{
                "08:00 (☀️ Siang)", "09:00 (☀️ Siang)", "10:00 (☀️ Siang)",
                "11:00 (☀️ Siang)", "12:00 (☀️ Siang)", "13:00 (☀️ Siang)",
                "14:00 (☀️ Siang)", "15:00 (☀️ Siang)", "16:00 (☀️ Siang)",
                "17:00 (☀️ Siang)", "18:00 (🌙 Lampu)", "19:00 (🌙 Lampu)",
                "20:00 (🌙 Lampu)", "21:00 (🌙 Lampu)", "22:00 (🌙 Lampu)"
        });
        cbJamMulai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbJamMulai.setBackground(Color.WHITE);
        cbJamMulai.addActionListener(e -> updateTarifKalkulasi());

        cbDurasi = new JComboBox<>(new String[]{"1 Jam", "2 Jam", "3 Jam", "4 Jam"});
        cbDurasi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDurasi.setBackground(Color.WHITE);
        cbDurasi.addActionListener(e -> updateTarifKalkulasi());

        JPanel p1 = new JPanel(new BorderLayout(0, 4));
        p1.setOpaque(false);
        p1.add(new JLabel("Jam Mulai (08-23):"), BorderLayout.NORTH);
        p1.add(cbJamMulai, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout(0, 4));
        p2.setOpaque(false);
        p2.add(new JLabel("Durasi Sewa:"), BorderLayout.NORTH);
        p2.add(cbDurasi, BorderLayout.CENTER);

        slotRow.add(p1);
        slotRow.add(p2);
        fieldsPanel.add(slotRow);

        lblJamSelesai = new JLabel("Jam Selesai: 09:00 WIB");
        lblJamSelesai.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblJamSelesai.setForeground(new Color(100, 116, 139));
        lblJamSelesai.setBorder(new EmptyBorder(4, 0, 10, 0));
        fieldsPanel.add(lblJamSelesai);

        // BOX KALKULASI TARIF OTOMATIS (SIANG VS MALAM)
        JPanel boxTarif = new JPanel(new BorderLayout());
        boxTarif.setBackground(new Color(248, 250, 252));
        boxTarif.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        " 💰 Rincian Tarif Otomatis (Siang vs Malam + Lampu) ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 12),
                        new Color(15, 23, 42)
                ),
                new EmptyBorder(8, 12, 10, 12)
        ));

        JPanel detailTarif = new JPanel(new GridLayout(4, 1, 0, 4));
        detailTarif.setOpaque(false);

        lblTarifSiangInfo = new JLabel("☀️ Siang (08-18): Rp 120.000/jam");
        lblTarifSiangInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblTarifMalamInfo = new JLabel("🌙 Malam + Lampu (18-23): Rp 180.000/jam");
        lblTarifMalamInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        lblTotalTarif = new JLabel("TOTAL TARIF: Rp 120.000");
        lblTotalTarif.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotalTarif.setForeground(new Color(5, 150, 105)); // Emerald

        lblMinimalDP = new JLabel("Minimal DP (50%): Rp 60.000");
        lblMinimalDP.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMinimalDP.setForeground(new Color(217, 119, 6)); // Amber dark

        detailTarif.add(lblTarifSiangInfo);
        detailTarif.add(lblTarifMalamInfo);
        detailTarif.add(lblTotalTarif);
        detailTarif.add(lblMinimalDP);
        boxTarif.add(detailTarif, BorderLayout.CENTER);
        fieldsPanel.add(boxTarif);

        // INPUT UANG MUKA (DP)
        JPanel dpPanel = new JPanel();
        dpPanel.setLayout(new BoxLayout(dpPanel, BoxLayout.Y_AXIS));
        dpPanel.setOpaque(false);
        dpPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblDPTitle = new JLabel("Input Uang Muka (DP Minimal 50%):");
        lblDPTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dpPanel.add(lblDPTitle);

        JPanel dpInputRow = new JPanel(new BorderLayout(6, 0));
        dpInputRow.setOpaque(false);

        txtUangDP = createStyledTextField();
        txtUangDP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                validasiUangDP();
            }
        });

        // Tombol Shortcut DP 50% & 100%
        JPanel btnShortcutPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        btnShortcutPanel.setOpaque(false);

        JButton btnDP50 = new JButton("DP 50%");
        btnDP50.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDP50.setBackground(new Color(241, 245, 249));
        btnDP50.setFocusPainted(false);
        btnDP50.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDP50.addActionListener(e -> setShortcutDP(0.50));

        JButton btnDP100 = new JButton("Lunas (100%)");
        btnDP100.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDP100.setBackground(new Color(241, 245, 249));
        btnDP100.setFocusPainted(false);
        btnDP100.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDP100.addActionListener(e -> setShortcutDP(1.00));

        btnShortcutPanel.add(btnDP50);
        btnShortcutPanel.add(btnDP100);

        dpInputRow.add(txtUangDP, BorderLayout.CENTER);
        dpInputRow.add(btnShortcutPanel, BorderLayout.EAST);
        dpPanel.add(dpInputRow);

        lblStatusDPPreview = new JLabel("Status DP: Belum dimasukkan");
        lblStatusDPPreview.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatusDPPreview.setForeground(new Color(220, 38, 38));
        lblStatusDPPreview.setBorder(new EmptyBorder(4, 0, 2, 0));
        dpPanel.add(lblStatusDPPreview);

        lblSisaPelunasan = new JLabel("Sisa Pelunasan di Lokasi: Rp 0");
        lblSisaPelunasan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSisaPelunasan.setForeground(new Color(71, 85, 105));
        dpPanel.add(lblSisaPelunasan);

        fieldsPanel.add(dpPanel);

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        card.add(scrollPane, BorderLayout.CENTER);

        // ACTION BUTTONS (BOTTOM)
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        actionPanel.setOpaque(false);
        actionPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnCekSlot = new JButton("🔍 Cek Ketersediaan Slot");
        btnCekSlot.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCekSlot.setBackground(new Color(241, 245, 249));
        btnCekSlot.setForeground(new Color(30, 41, 59));
        btnCekSlot.setFocusPainted(false);
        btnCekSlot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCekSlot.addActionListener(e -> actionCekKetersediaan());

        JButton btnSimpan = new JButton("💾 Simpan & Konfirmasi Reservasi");
        btnSimpan.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSimpan.setBackground(new Color(5, 150, 105)); // Emerald Green
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setFocusPainted(false);
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSimpan.addActionListener(e -> actionSimpanBooking());

        JButton btnReset = new JButton("🔄 Reset Formulir");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setBackground(Color.WHITE);
        btnReset.setForeground(new Color(100, 116, 139));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> resetForm());

        actionPanel.add(btnCekSlot);
        actionPanel.add(btnSimpan);
        actionPanel.add(btnReset);
        card.add(actionPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFieldGroup(String labelText, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setOpaque(false);
        group.setBorder(new EmptyBorder(0, 0, 8, 0));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(new Color(51, 65, 85));

        group.add(label, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
    }

    private JTextField createStyledTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    // ==========================================
    // 3. PANEL KANAN (TIMELINE SLOTS & TABEL)
    // ==========================================
    private JPanel createRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 16));
        right.setOpaque(false);

        JPanel timelineCard = new JPanel(new BorderLayout(0, 8));
        timelineCard.setBackground(Color.WHITE);
        timelineCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel timelineHeader = new JPanel(new BorderLayout());
        timelineHeader.setOpaque(false);

        JLabel lblTHeader = new JLabel("📊 Visualisasi Slot Jam Lapangan (08:00 - 23:00 WIB)");
        lblTHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTHeader.setForeground(new Color(15, 23, 42));

        lblTimelineTanggal = new JLabel(selectedDate.format(tglDisplayFmt));
        lblTimelineTanggal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTimelineTanggal.setForeground(new Color(5, 150, 105));

        timelineHeader.add(lblTHeader, BorderLayout.WEST);
        timelineHeader.add(lblTimelineTanggal, BorderLayout.EAST);
        timelineCard.add(timelineHeader, BorderLayout.NORTH);

        panelTimelineSlots = new JPanel(new GridLayout(3, 5, 8, 8));
        panelTimelineSlots.setOpaque(false);
        timelineCard.add(panelTimelineSlots, BorderLayout.CENTER);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legendPanel.setOpaque(false);
        legendPanel.add(createLegendItem(new Color(220, 252, 231), new Color(22, 101, 52), "Tersedia (Kosong)"));
        legendPanel.add(createLegendItem(new Color(254, 226, 226), new Color(153, 27, 27), "Terisi (Bentrok jika dipilih)"));
        legendPanel.add(createLegendItem(new Color(254, 243, 199), new Color(146, 64, 14), "Malam + Lampu"));
        timelineCard.add(legendPanel, BorderLayout.SOUTH);

        right.add(timelineCard, BorderLayout.NORTH);

        JPanel tableCard = new JPanel(new BorderLayout(0, 10));
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel filterPanel = new JPanel(new BorderLayout(10, 0));
        filterPanel.setOpaque(false);

        JLabel lblTableTitle = new JLabel("📁 Data Persistence (jadwal_futsal.csv)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(new Color(15, 23, 42));

        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setOpaque(false);
        txtCari = createStyledTextField();
        txtCari.setPreferredSize(new Dimension(200, 30));
        txtCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                cariDataTabel();
            }
        });

        searchBox.add(new JLabel("🔍 Cari:"), BorderLayout.WEST);
        searchBox.add(txtCari, BorderLayout.CENTER);

        filterPanel.add(lblTableTitle, BorderLayout.WEST);
        filterPanel.add(searchBox, BorderLayout.EAST);
        tableCard.add(filterPanel, BorderLayout.NORTH);

        String[] columns = {
                "ID Booking", "Nama Tim", "No HP", "Tanggal Main",
                "Jam", "Durasi", "Total Tarif", "Uang DP", "Status DP"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableJadwal = new JTable(tableModel);
        tableJadwal.setRowHeight(32);
        tableJadwal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableJadwal.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableJadwal.setShowGrid(true);
        tableJadwal.setGridColor(new Color(241, 245, 249));

        JTableHeader th = tableJadwal.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(241, 245, 249));
        th.setForeground(new Color(30, 41, 59));
        th.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableJadwal.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableJadwal.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableJadwal.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tableJadwal.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scrollTable = new JScrollPane(tableJadwal);
        scrollTable.setBorder(new LineBorder(new Color(226, 232, 240), 1));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        tableControlPanel.setOpaque(false);

        JButton btnLihatStruk = new JButton("🧾 Cetak / Lihat Struk");
        btnLihatStruk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLihatStruk.setBackground(new Color(241, 245, 249));
        btnLihatStruk.setFocusPainted(false);
        btnLihatStruk.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLihatStruk.addActionListener(e -> actionLihatStruk());

        JButton btnHapus = new JButton("❌ Batalkan Booking");
        btnHapus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnHapus.setBackground(new Color(254, 226, 226));
        btnHapus.setForeground(new Color(185, 28, 28));
        btnHapus.setFocusPainted(false);
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapus.addActionListener(e -> actionHapusBooking());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefresh.setBackground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadTableData();
            refreshTimeline();
        });

        tableControlPanel.add(btnLihatStruk);
        tableControlPanel.add(btnHapus);
        tableControlPanel.add(btnRefresh);
        tableCard.add(tableControlPanel, BorderLayout.SOUTH);

        right.add(tableCard, BorderLayout.CENTER);
        return right;
    }

    private JPanel createLegendItem(Color bg, Color fg, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel box = new JLabel("  ");
        box.setOpaque(true);
        box.setBackground(bg);
        box.setBorder(new LineBorder(fg, 1));
        box.setPreferredSize(new Dimension(14, 14));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        label.setForeground(new Color(71, 85, 105));

        p.add(box);
        p.add(label);
        return p;
    }

    // ==========================================
    // LOGIKA PERHITUNGAN & VALIDASI REAL-TIME
    // ==========================================
    private LocalTime getSelectedJamMulai() {
        int index = cbJamMulai.getSelectedIndex();
        return LocalTime.of(8 + index, 0);
    }

    private int getSelectedDurasi() {
        return cbDurasi.getSelectedIndex() + 1; // 1 s.d. 4 Jam
    }

    private void updateTarifKalkulasi() {
        LocalTime jamMulai = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        LocalTime jamSelesai = jamMulai.plusHours(durasi);

        lblJamSelesai.setText(String.format("Jam Selesai: %s WIB", jamSelesai.format(DateTimeFormatter.ofPattern("HH:mm"))));

        if (jamSelesai.isAfter(TarifCalculator.JAM_TUTUP)) {
            lblJamSelesai.setText(String.format("⚠️ Melewati batas tutup arena (23:00)! Selesai: %s WIB",
                    jamSelesai.format(DateTimeFormatter.ofPattern("HH:mm"))));
            lblJamSelesai.setForeground(new Color(220, 38, 38));
        } else {
            lblJamSelesai.setForeground(new Color(100, 116, 139));
        }

        TarifCalculator.TarifResult tarif = TarifCalculator.hitungTarif(jamMulai, durasi);

        if (tarif.getJamSiang() > 0) {
            lblTarifSiangInfo.setText(String.format("☀️ Siang: %d Jam x Rp %,.0f = Rp %,.0f",
                    tarif.getJamSiang(), TarifCalculator.TARIF_SIANG_PER_JAM, tarif.getSubtotalSiang()));
            lblTarifSiangInfo.setVisible(true);
        } else {
            lblTarifSiangInfo.setText("☀️ Siang (08-18): 0 Jam");
        }

        if (tarif.getJamMalam() > 0) {
            lblTarifMalamInfo.setText(String.format("🌙 Malam (+ Lampu): %d Jam x Rp %,.0f = Rp %,.0f",
                    tarif.getJamMalam(), TarifCalculator.TARIF_MALAM_PER_JAM, tarif.getSubtotalMalam()));
            lblTarifMalamInfo.setVisible(true);
        } else {
            lblTarifMalamInfo.setText("🌙 Malam + Lampu (18-23): 0 Jam");
        }

        lblTotalTarif.setText(String.format("TOTAL TARIF: Rp %,.0f", tarif.getTotalTarif()));
        lblMinimalDP.setText(String.format("Minimal DP (50%%): Rp %,.0f", tarif.getMinimalDP()));

        validasiUangDP();
    }

    private void setShortcutDP(double percentage) {
        TarifCalculator.TarifResult tarif = TarifCalculator.hitungTarif(getSelectedJamMulai(), getSelectedDurasi());
        double nominal = tarif.getTotalTarif() * percentage;
        txtUangDP.setText(String.format("%.0f", nominal));
        validasiUangDP();
    }

    private boolean validasiUangDP() {
        TarifCalculator.TarifResult tarif = TarifCalculator.hitungTarif(getSelectedJamMulai(), getSelectedDurasi());
        String text = txtUangDP.getText().replaceAll("[^0-9]", "");

        if (text.isEmpty()) {
            lblStatusDPPreview.setText("Status DP: Belum dimasukkan (Minimal 50%)");
            lblStatusDPPreview.setForeground(new Color(220, 38, 38));
            lblSisaPelunasan.setText("Sisa Pelunasan di Lokasi: Rp " + rupiahFmt.format(tarif.getTotalTarif()));
            return false;
        }

        try {
            double uangDP = Double.parseDouble(text);
            double minimalDP = tarif.getMinimalDP();
            double sisa = Math.max(0, tarif.getTotalTarif() - uangDP);
            lblSisaPelunasan.setText(String.format("Sisa Pelunasan di Lokasi: Rp %,.0f", sisa));

            if (uangDP >= tarif.getTotalTarif()) {
                lblStatusDPPreview.setText("✅ Pembayaran Lunas (100%)");
                lblStatusDPPreview.setForeground(new Color(5, 150, 105));
                return true;
            } else if (uangDP >= minimalDP) {
                lblStatusDPPreview.setText(String.format("✅ DP Valid (%.0f%%) - Memenuhi Syarat Minimal 50%%",
                        (uangDP / tarif.getTotalTarif()) * 100));
                lblStatusDPPreview.setForeground(new Color(5, 150, 105));
                return true;
            } else {
                lblStatusDPPreview.setText(String.format("⚠️ DP Kurang! Minimal 50%% (Rp %,.0f)", minimalDP));
                lblStatusDPPreview.setForeground(new Color(220, 38, 38));
                return false;
            }
        } catch (NumberFormatException ex) {
            lblStatusDPPreview.setText("⚠️ Format nominal angka tidak valid!");
            lblStatusDPPreview.setForeground(new Color(220, 38, 38));
            return false;
        }
    }

    // ==========================================
    // REFRESH VISUAL TIMELINE SLOTS
    // ==========================================
    private void refreshTimeline() {
        lblTimelineTanggal.setText(selectedDate.format(tglDisplayFmt));
        panelTimelineSlots.removeAll();

        List<JadwalSewa> bookingsHariIni = repository.getJadwalByTanggal(selectedDate);

        for (int h = 8; h <= 22; h++) {
            LocalTime slotTime = LocalTime.of(h, 0);
            LocalTime slotEnd = slotTime.plusHours(1);

            JadwalSewa bookingFound = null;
            for (JadwalSewa b : bookingsHariIni) {
                if (slotTime.isBefore(b.getJamSelesai()) && slotEnd.isAfter(b.getJamMulai())) {
                    bookingFound = b;
                    break;
                }
            }

            JPanel slotBox = new JPanel(new BorderLayout(0, 2));
            slotBox.setBorder(new EmptyBorder(4, 6, 4, 6));

            JLabel lblJam = new JLabel(String.format("%02d:00-%02d:00", h, h + 1), SwingConstants.CENTER);
            lblJam.setFont(new Font("Segoe UI", Font.BOLD, 11));

            JLabel lblStatus = new JLabel("", SwingConstants.CENTER);
            lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            boolean isMalam = TarifCalculator.isJamMalam(slotTime);

            if (bookingFound != null) {
                slotBox.setBackground(new Color(254, 226, 226)); // Red light
                slotBox.setBorder(new LineBorder(new Color(239, 68, 68), 1));
                lblJam.setForeground(new Color(153, 27, 27));
                lblStatus.setText("🔴 " + truncate(bookingFound.getNamaTim(), 12));
                lblStatus.setForeground(new Color(185, 28, 28));
                slotBox.setToolTipText(String.format("Terisi: %s (%s - %s)",
                        bookingFound.getNamaTim(), bookingFound.getJamMulai(), bookingFound.getJamSelesai()));
            } else {
                if (isMalam) {
                    slotBox.setBackground(new Color(254, 249, 195)); // Yellow light
                    slotBox.setBorder(new LineBorder(new Color(234, 179, 8), 1));
                    lblJam.setForeground(new Color(146, 64, 14));
                    lblStatus.setText("🌙 Rp 180k/jam");
                    lblStatus.setForeground(new Color(161, 98, 7));
                } else {
                    slotBox.setBackground(new Color(240, 253, 244)); // Green light
                    slotBox.setBorder(new LineBorder(new Color(34, 197, 94), 1));
                    lblJam.setForeground(new Color(21, 128, 61));
                    lblStatus.setText("🟢 Tersedia");
                    lblStatus.setForeground(new Color(22, 101, 52));
                }
                slotBox.setToolTipText("Slot Kosong / Bebas Reservasi");
            }

            slotBox.add(lblJam, BorderLayout.NORTH);
            slotBox.add(lblStatus, BorderLayout.CENTER);
            panelTimelineSlots.add(slotBox);
        }

        panelTimelineSlots.revalidate();
        panelTimelineSlots.repaint();
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 1) + "…";
    }

    // ==========================================
    // AKSI FORM & OPERASI DATABASE CSV
    // ==========================================
    private void actionCekKetersediaan() {
        LocalTime jamMulai = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        LocalTime jamSelesai = jamMulai.plusHours(durasi);

        if (jamSelesai.isAfter(TarifCalculator.JAM_TUTUP)) {
            JOptionPane.showMessageDialog(this,
                    "Jadwal tidak valid!\nArena Futsal tutup pada pukul 23:00 WIB.\n" +
                    "Waktu selesai Anda: " + jamSelesai + " WIB.",
                    "Melewati Jam Operasional", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JadwalSewa bentrok = repository.findBentrok(selectedDate, jamMulai, durasi, null);
        if (bentrok != null) {
            JOptionPane.showMessageDialog(this,
                    "❌ JADWAL BENTROK!\n\n" +
                    "Slot pada jam tersebut sudah dipesan oleh:\n" +
                    "• Tim        : " + bentrok.getNamaTim() + "\n" +
                    "• Kontak     : " + bentrok.getNoHp() + "\n" +
                    "• Jam Sewa   : " + bentrok.getJamMulai() + " s.d. " + bentrok.getJamSelesai() + " WIB\n\n" +
                    "Silakan pilih slot jam lain yang masih tersedia (berwarna hijau pada visualizer).",
                    "Peringatan Bentrok Jadwal", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "✅ SLOT TERSEDIA!\n\n" +
                    "Tanggal    : " + selectedDate.format(tglDisplayFmt) + "\n" +
                    "Jam Sewa   : " + jamMulai + " s.d. " + jamSelesai + " WIB (" + durasi + " Jam)\n\n" +
                    "Slot aman dan siap untuk di-booking!",
                    "Slot Lapangan Tersedia", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void actionSimpanBooking() {
        String namaTim = txtNamaTim.getText().trim();
        String noHp = txtNoHp.getText().trim();
        String rawDP = txtUangDP.getText().replaceAll("[^0-9]", "");

        if (namaTim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama Tim / Pemesan wajib diisi!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
            txtNamaTim.requestFocus();
            return;
        }

        if (noHp.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor kontak WhatsApp wajib diisi!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
            txtNoHp.requestFocus();
            return;
        }

        LocalTime jamMulai = getSelectedJamMulai();
        int durasi = getSelectedDurasi();
        LocalTime jamSelesai = jamMulai.plusHours(durasi);

        if (jamSelesai.isAfter(TarifCalculator.JAM_TUTUP)) {
            JOptionPane.showMessageDialog(this,
                    "Jam sewa melebihi jam operasional tutup arena (23:00 WIB)!",
                    "Validasi Jam Operasional", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TarifCalculator.TarifResult tarif = TarifCalculator.hitungTarif(jamMulai, durasi);

        if (rawDP.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Uang Muka (DP) wajib diinputkan! Minimal 50% = Rp " + rupiahFmt.format(tarif.getMinimalDP()),
                    "Validasi DP", JOptionPane.WARNING_MESSAGE);
            txtUangDP.requestFocus();
            return;
        }

        double uangDP;
        try {
            uangDP = Double.parseDouble(rawDP);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Format nominal DP tidak valid!", "Validasi DP", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (uangDP < tarif.getMinimalDP()) {
            JOptionPane.showMessageDialog(this,
                    "Nominal DP tidak memenuhi syarat minimal 50%!\n" +
                    "Total Biaya  : Rp " + rupiahFmt.format(tarif.getTotalTarif()) + "\n" +
                    "Minimal DP   : Rp " + rupiahFmt.format(tarif.getMinimalDP()) + "\n" +
                    "Input Anda   : Rp " + rupiahFmt.format(uangDP),
                    "Uang Muka Kurang", JOptionPane.WARNING_MESSAGE);
            txtUangDP.requestFocus();
            return;
        }

        String statusDP;
        if (uangDP >= tarif.getTotalTarif()) {
            statusDP = "Lunas (100%)";
        } else {
            statusDP = String.format("DP (Rp %,.0f)", uangDP);
        }

        String idBooking = txtIdBooking.getText();
        JadwalSewa jadwalBaru = new JadwalSewa(
                idBooking, namaTim, noHp, selectedDate, jamMulai, durasi, uangDP, statusDP, tarif.getTotalTarif()
        );

        try {
            repository.simpan(jadwalBaru);

            JOptionPane.showMessageDialog(this,
                    "🎉 RESERVASI BERHASIL DISIMPAN!\n\n" +
                    "ID Booking   : " + idBooking + "\n" +
                    "Tim          : " + namaTim + "\n" +
                    "Tanggal      : " + selectedDate.format(tglDisplayFmt) + "\n" +
                    "Jam          : " + jamMulai + " - " + jamSelesai + " WIB\n" +
                    "Data berhasil dicatat ke " + JadwalRepository.getFileName(),
                    "Sukses Reservasi", JOptionPane.INFORMATION_MESSAGE);

            int cetak = JOptionPane.showConfirmDialog(this,
                    "Apakah Anda ingin melihat / mencetak struk bukti pemesanan?",
                    "Cetak Struk", JOptionPane.YES_NO_OPTION);
            if (cetak == JOptionPane.YES_OPTION) {
                new StrukDialog(this, jadwalBaru).setVisible(true);
            }

            loadTableData();
            refreshTimeline();
            resetForm();

        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this,
                    "❌ PENYIMPANAN DITOLAK!\n\n" + e.getMessage(),
                    "Bentrok Jadwal Terdeteksi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan teknis saat menyimpan berkas CSV: " + e.getMessage(),
                    "Error Persistence", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actionLihatStruk() {
        int selectedRow = tableJadwal.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih baris pemesanan pada tabel terlebih dahulu!",
                    "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idBooking = tableModel.getValueAt(selectedRow, 0).toString();
        List<JadwalSewa> all = repository.getAllJadwal();
        for (JadwalSewa j : all) {
            if (j.getIdBooking().equalsIgnoreCase(idBooking)) {
                new StrukDialog(this, j).setVisible(true);
                return;
            }
        }
    }

    private void actionHapusBooking() {
        int selectedRow = tableJadwal.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih jadwal yang ingin dibatalkan pada tabel!",
                    "Perhatian", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String idBooking = tableModel.getValueAt(selectedRow, 0).toString();
        String namaTim = tableModel.getValueAt(selectedRow, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin membatalkan pemesanan ID: " + idBooking + " (" + namaTim + ")?\n" +
                "Data akan dihapus permanen dari berkas " + JadwalRepository.getFileName() + ".",
                "Konfirmasi Pembatalan Booking", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean sukses = repository.hapus(idBooking);
            if (sukses) {
                JOptionPane.showMessageDialog(this, "Pemesanan berhasil dibatalkan dan dihapus.");
                loadTableData();
                refreshTimeline();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus data booking!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        List<JadwalSewa> list = repository.getAllJadwal();

        for (JadwalSewa j : list) {
            tableModel.addRow(new Object[]{
                    j.getIdBooking(),
                    j.getNamaTim(),
                    j.getNoHp(),
                    j.getTanggalMain().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    j.getJamMulai() + " - " + j.getJamSelesai(),
                    j.getDurasiJam() + " Jam",
                    "Rp " + rupiahFmt.format(j.getTotalTarif()),
                    "Rp " + rupiahFmt.format(j.getUangDP()),
                    j.getStatusDP()
            });
        }
    }

    private void cariDataTabel() {
        String keyword = txtCari.getText().trim().toLowerCase();
        tableModel.setRowCount(0);
        List<JadwalSewa> list = repository.getAllJadwal();

        for (JadwalSewa j : list) {
            boolean match = j.getNamaTim().toLowerCase().contains(keyword) ||
                            j.getIdBooking().toLowerCase().contains(keyword) ||
                            j.getNoHp().contains(keyword) ||
                            j.getTanggalMain().toString().contains(keyword);

            if (match) {
                tableModel.addRow(new Object[]{
                        j.getIdBooking(),
                        j.getNamaTim(),
                        j.getNoHp(),
                        j.getTanggalMain().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        j.getJamMulai() + " - " + j.getJamSelesai(),
                        j.getDurasiJam() + " Jam",
                        "Rp " + rupiahFmt.format(j.getTotalTarif()),
                        "Rp " + rupiahFmt.format(j.getUangDP()),
                        j.getStatusDP()
                });
            }
        }
    }

    private void resetForm() {
        txtIdBooking.setText(repository.generateNextId());
        txtNamaTim.setText("");
        txtNoHp.setText("");
        selectedDate = LocalDate.now();
        txtTanggal.setText(selectedDate.format(tglDisplayFmt));
        cbJamMulai.setSelectedIndex(0); // 08:00
        cbDurasi.setSelectedIndex(0);   // 1 Jam
        txtUangDP.setText("");
        updateTarifKalkulasi();
        refreshTimeline();
    }
}
