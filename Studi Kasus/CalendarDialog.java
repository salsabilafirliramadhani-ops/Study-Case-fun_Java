import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Dialog Kalender Interaktif untuk Pemilihan Tanggal Main.
 * Dibuat menggunakan murni Java Swing tanpa dependensi eksternal.
 */
public class CalendarDialog extends JDialog {

    private LocalDate selectedDate;
    private YearMonth currentYearMonth;
    private final JPanel daysPanel;
    private final JLabel lblMonthYear;
    private final Locale idLocale = Locale.of("id", "ID");
    private boolean confirmed = false;

    public CalendarDialog(Window parent, LocalDate initialDate) {
        super(parent, "Pilih Tanggal Reservasi", ModalityType.APPLICATION_MODAL);
        this.selectedDate = (initialDate != null) ? initialDate : LocalDate.now();
        this.currentYearMonth = YearMonth.from(this.selectedDate);

        setSize(380, 360);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // Header Panel (Navigasi Bulan)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Dark Slate
        headerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JButton btnPrev = createNavButton("<");
        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        JButton btnNext = createNavButton(">");
        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMonthYear.setForeground(Color.WHITE);

        headerPanel.add(btnPrev, BorderLayout.WEST);
        headerPanel.add(lblMonthYear, BorderLayout.CENTER);
        headerPanel.add(btnNext, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Kalender Grid)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);

        // Nama Hari
        JPanel dayNamesPanel = new JPanel(new GridLayout(1, 7, 4, 4));
        dayNamesPanel.setBackground(new Color(241, 245, 249));
        dayNamesPanel.setBorder(new EmptyBorder(8, 12, 8, 12));
        String[] daysOfWeek = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};
        for (String day : daysOfWeek) {
            JLabel lbl = new JLabel(day, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lbl.setForeground(new Color(71, 85, 105));
            dayNamesPanel.add(lbl);
        }
        centerPanel.add(dayNamesPanel, BorderLayout.NORTH);

        daysPanel = new JPanel(new GridLayout(6, 7, 4, 4));
        daysPanel.setBackground(Color.WHITE);
        daysPanel.setBorder(new EmptyBorder(10, 12, 10, 12));
        centerPanel.add(daysPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel (Quick Buttons)
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottomPanel.setBackground(new Color(248, 250, 252));
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnToday = new JButton("Hari Ini");
        btnToday.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnToday.addActionListener(e -> {
            selectedDate = LocalDate.now();
            currentYearMonth = YearMonth.from(selectedDate);
            updateCalendar();
        });

        JButton btnTomorrow = new JButton("Besok");
        btnTomorrow.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTomorrow.addActionListener(e -> {
            selectedDate = LocalDate.now().plusDays(1);
            currentYearMonth = YearMonth.from(selectedDate);
            updateCalendar();
        });

        JButton btnPilih = new JButton("Pilih Tanggal Ini");
        btnPilih.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPilih.setBackground(new Color(5, 150, 105));
        btnPilih.setForeground(Color.WHITE);
        btnPilih.setFocusPainted(false);
        btnPilih.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        bottomPanel.add(btnToday);
        bottomPanel.add(btnTomorrow);
        bottomPanel.add(btnPilih);
        add(bottomPanel, BorderLayout.SOUTH);

        updateCalendar();
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(30, 41, 59));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateCalendar() {
        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, idLocale);
        lblMonthYear.setText(monthName.toUpperCase(idLocale) + " " + currentYearMonth.getYear());

        daysPanel.removeAll();

        LocalDate firstDay = currentYearMonth.atDay(1);
        int dayOfWeekValue = firstDay.getDayOfWeek().getValue(); // 1 = Sen, 7 = Min
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Slot kosong sebelum tanggal 1
        for (int i = 1; i < dayOfWeekValue; i++) {
            daysPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();

        // Tanggal 1 s.d. akhir bulan
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btnDay.setFocusPainted(false);
            btnDay.setMargin(new Insets(2, 2, 2, 2));
            btnDay.setCursor(new Cursor(Cursor.HAND_CURSOR));

            boolean isSelected = date.equals(selectedDate);
            boolean isToday = date.equals(today);

            if (isSelected) {
                btnDay.setBackground(new Color(5, 150, 105)); // Emerald
                btnDay.setForeground(Color.WHITE);
                btnDay.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else if (isToday) {
                btnDay.setBackground(new Color(254, 243, 199)); // Amber light
                btnDay.setForeground(new Color(180, 83, 9));
                btnDay.setBorder(BorderFactory.createLineBorder(new Color(245, 158, 11), 1));
            } else {
                btnDay.setBackground(Color.WHITE);
                btnDay.setForeground(new Color(30, 41, 59));
                btnDay.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
            }

            btnDay.addActionListener(e -> {
                selectedDate = date;
                confirmed = true;
                dispose();
            });

            daysPanel.add(btnDay);
        }

        int totalCells = (dayOfWeekValue - 1) + daysInMonth;
        int remainingCells = 42 - totalCells;
        for (int i = 0; i < remainingCells; i++) {
            daysPanel.add(new JLabel(""));
        }

        daysPanel.revalidate();
        daysPanel.repaint();
    }

    public static LocalDate showDialog(Window parent, LocalDate initialDate) {
        CalendarDialog dialog = new CalendarDialog(parent, initialDate);
        dialog.setVisible(true);
        return dialog.confirmed ? dialog.selectedDate : initialDate;
    }
}
