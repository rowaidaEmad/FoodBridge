package foodbridge.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 * Read-only date field that opens a calendar when clicked; getDate() is null until a day is picked.
 */
public class DatePicker extends JPanel {
    private static final DateTimeFormatter MONTH_TITLE = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final JTextField field = new JTextField(9);
    private final JPopupMenu popup = new JPopupMenu();
    private final JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel daysGrid = new JPanel(new GridLayout(0, 7, 2, 2));
    private LocalDate date;
    private LocalDate minDate;
    private YearMonth shownMonth = YearMonth.now();

    public DatePicker() {
        super(new BorderLayout(2, 0));
        field.setEditable(false);
        // Keep the normal white look; read-only fields are grey by default and look disabled.
        // A plain Color (not the look-and-feel's ColorUIResource), or the look-and-feel greys it again.
        field.setBackground(new Color(UIManager.getColor("TextField.background").getRGB()));
        field.setToolTipText("Click to pick a date");
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openPopup();
            }
        });
        JButton open = new JButton("▾");
        open.setMargin(new Insets(0, 4, 0, 4));
        open.setFocusable(false);
        open.addActionListener(e -> openPopup());

        add(field, BorderLayout.CENTER);
        add(open, BorderLayout.EAST);
        popup.add(calendarPanel());
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
        field.setText(date == null ? "" : date.toString());
    }

    /** Days before this one are disabled in the calendar. */
    public void setMinDate(LocalDate minDate) {
        this.minDate = minDate;
    }

    private JPanel calendarPanel() {
        JButton prev = new JButton("<");
        prev.addActionListener(e -> showMonth(shownMonth.minusMonths(1)));
        JButton next = new JButton(">");
        next.addActionListener(e -> showMonth(shownMonth.plusMonths(1)));
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD));

        JPanel header = new JPanel(new BorderLayout(4, 0));
        header.add(prev, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);

        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> {
            setDate(null);
            popup.setVisible(false);
        });
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(clear, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.add(header, BorderLayout.NORTH);
        panel.add(daysGrid, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private void openPopup() {
        shownMonth = YearMonth.from(date != null ? date : LocalDate.now());
        showMonth(shownMonth);
        popup.show(this, 0, getHeight());
    }

    private void showMonth(YearMonth month) {
        shownMonth = month;
        monthLabel.setText(month.format(MONTH_TITLE));
        daysGrid.removeAll();
        for (DayOfWeek day : DayOfWeek.values()) {
            daysGrid.add(new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH), SwingConstants.CENTER));
        }
        // Monday-first grid: leave blank cells before the 1st.
        for (int i = 1; i < month.atDay(1).getDayOfWeek().getValue(); i++) {
            daysGrid.add(new JLabel());
        }
        LocalDate today = LocalDate.now();
        for (int d = 1; d <= month.lengthOfMonth(); d++) {
            LocalDate day = month.atDay(d);
            JButton button = new JButton(String.valueOf(d));
            button.setMargin(new Insets(2, 2, 2, 2));
            button.setEnabled(minDate == null || !day.isBefore(minDate));
            if (day.equals(date) || day.equals(today)) {
                button.setFont(button.getFont().deriveFont(Font.BOLD));
            }
            if (day.equals(date)) {
                button.setBorder(BorderFactory.createLineBorder(button.getForeground(), 2));
            }
            button.addActionListener(e -> {
                setDate(day);
                popup.setVisible(false);
            });
            daysGrid.add(button);
        }
        daysGrid.revalidate();
        daysGrid.repaint();
        popup.pack();
    }
}
