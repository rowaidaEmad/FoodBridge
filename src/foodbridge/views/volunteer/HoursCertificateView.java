package foodbridge.views.volunteer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import foodbridge.controllers.VolunteerController;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Screen; talks only to VolunteerController.
 */
public class HoursCertificateView implements View {
    private final int volunteerId;
    private final VolunteerController volunteerController;
    private final JFrame frame = new JFrame("FoodBridge - Hours & Certificates");
    private final JLabel totalLabel = new JLabel();
    private final JTable hoursTable = SwingSupport.readOnlyTable("Date", "Hours", "Source", "Note");
    private final JTextField hoursField = new JTextField(5);
    private final JTextField noteField = new JTextField(20);

    public HoursCertificateView(int volunteerId, VolunteerController volunteerController) {
        this.volunteerId = volunteerId;
        this.volunteerController = volunteerController;

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(totalLabel, BorderLayout.NORTH);
        content.add(SwingSupport.scroll(hoursTable, 620, 240), BorderLayout.CENTER);
        content.add(actions(), BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(content);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        SwingSupport.attempt(frame, () -> {
            refresh();
            frame.setVisible(true);
        });
    }

    private JPanel actions() {
        JButton logButton = new JButton("Log Hours");
        logButton.addActionListener(e -> logHours());
        JButton certificateButton = new JButton("Get Certificate");
        certificateButton.addActionListener(e -> showCertificate());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(new JLabel("Hours:"));
        actions.add(hoursField);
        actions.add(new JLabel("Note:"));
        actions.add(noteField);
        actions.add(logButton);
        actions.add(certificateButton);
        return actions;
    }

    private void logHours() {
        SwingSupport.attempt(frame, () -> {
            volunteerController.logHours(volunteerId, SwingSupport.parseHours(hoursField.getText()), noteField.getText().trim());
            hoursField.setText("");
            noteField.setText("");
            refresh();
        });
    }

    private void showCertificate() {
        SwingSupport.attempt(frame, () -> {
            JTextArea text = new JTextArea(volunteerController.requestCertificate(volunteerId), 16, 48);
            text.setEditable(false);
            text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            Object[] options = { "Save", "Close" };
            int choice = JOptionPane.showOptionDialog(frame, new JScrollPane(text), "Certificate",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);
            if (choice == 0) {
                saveCertificate(text.getText());
            }
        });
    }

    private void saveCertificate(String text) {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(chooser.getSelectedFile().toPath(), text);
            JOptionPane.showMessageDialog(frame, "Certificate saved.");
        } catch (IOException e) {
            throw new IllegalStateException("Could not save the file: " + e.getMessage(), e);
        }
    }

    private void refresh() {
        totalLabel.setText(String.format("Total hours: %.2f", volunteerController.getVolunteer(volunteerId).getTotalHours()));
        SwingSupport.setRows(hoursTable, hoursRows());
    }

    private List<Object[]> hoursRows() {
        return volunteerController.viewHours(volunteerId).stream()
                .map(entry -> new Object[] { SwingSupport.formatDateTime(entry.getLoggedAt()),
                        String.format("%.2f", entry.getHours()), entry.getSource(), SwingSupport.orEmpty(entry.getNote()) })
                .toList();
    }
}
