import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

import foodbridge.controllers.DonationController;
import foodbridge.models.users.Donor;
import foodbridge.repositories.ReceiptRepository;
import foodbridge.views.donor.PayDonationView;

/** Exercises navigation and option changes through real Swing controls against the isolated database. */
public class ReceiptGuiSmokeTest {
    private static <T extends Component> List<T> components(Container parent, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) result.add(type.cast(child));
            if (child instanceof Container container) result.addAll(components(container, type));
        }
        return result;
    }

    private static JDialog dialog(String title) {
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog d && d.isVisible() && d.getTitle().equals(title)) return d;
        }
        return null;
    }

    private static JButton button(Container parent, String title) {
        return components(parent, JButton.class).stream().filter(b -> b.getText().equals(title))
                .findFirst().orElseThrow();
    }

    private static JDialog await(String title, boolean receipt) throws Exception {
        long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(15);
        while (System.nanoTime() < deadline) {
            JDialog[] ready = { null };
            SwingUtilities.invokeAndWait(() -> {
                JDialog d = dialog(title);
                if (d != null && (!receipt || (button(d, "Refresh receipt").isEnabled()
                        && !components(d, JTextArea.class).get(0).getText().isBlank()))) ready[0] = d;
            });
            if (ready[0] != null) return ready[0];
            Thread.sleep(40);
        }
        throw new AssertionError("Window did not load: " + title);
    }

    public static void run(DonationController controller, Donor donor, int donationId) throws Exception {
        List<String> errors = new ArrayList<>();
        javax.swing.Timer[] errorCloser = { null };
        try {
            SwingUtilities.invokeAndWait(() -> {
                errorCloser[0] = new javax.swing.Timer(40, event -> {
                    for (Window w : Window.getWindows()) {
                        if (w instanceof JDialog d && d.isVisible()) {
                            for (JOptionPane pane : components(d, JOptionPane.class)) {
                                errors.add(String.valueOf(pane.getMessage()));
                                d.dispose();
                            }
                        }
                    }
                });
                errorCloser[0].start();
            });
            SwingUtilities.invokeLater(() -> new PayDonationView(controller, donor).show());
            JDialog donations = await("FoodBridge - My donations", false);
            SwingUtilities.invokeAndWait(() -> {
                JTable table = components(donations, JTable.class).get(0);
                for (int row = 0; row < table.getRowCount(); row++) {
                    if (((Integer) table.getValueAt(row, 0)) == donationId) {
                        table.setRowSelectionInterval(row, row); return;
                    }
                }
                throw new AssertionError("Donation missing from table");
            });
            SwingUtilities.invokeLater(() -> button(donations, "Receipt").doClick());
            String title = "FoodBridge - Receipt for donation #" + donationId;
            JDialog receipt = await(title, true);
            SwingUtilities.invokeAndWait(() -> {
                String text = components(receipt, JTextArea.class).get(0).getText();
                ReceiptModuleTest.check(text.contains("250.50") && !text.contains("Thank you")
                        && !text.contains("Tax information"), "GUI initially shows basic receipt");
                components(receipt, JCheckBox.class).get(0).doClick();
            });
            await(title, true);
            SwingUtilities.invokeAndWait(() -> {
                String text = components(receipt, JTextArea.class).get(0).getText();
                ReceiptModuleTest.check(text.contains("Thank you") && !text.contains("Tax information"), "GUI thank-you only");
                components(receipt, JCheckBox.class).get(1).doClick();
            });
            await(title, true);
            SwingUtilities.invokeAndWait(() -> {
                String text = components(receipt, JTextArea.class).get(0).getText();
                ReceiptModuleTest.check(text.contains("Thank you") && text.contains("Tax information"), "GUI both options");
                try {
                    BufferedImage image = new BufferedImage(receipt.getWidth(), receipt.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics(); receipt.printAll(graphics); graphics.dispose();
                    Path output = Path.of("target/receipt-verification/receipt.png");
                    Files.createDirectories(output.getParent()); ImageIO.write(image, "png", output.toFile());
                } catch (Exception ex) { throw new RuntimeException(ex); }
                components(receipt, JCheckBox.class).get(0).doClick();
            });
            await(title, true);
            SwingUtilities.invokeAndWait(() -> {
                String text = components(receipt, JTextArea.class).get(0).getText();
                ReceiptModuleTest.check(!text.contains("Thank you") && text.contains("Tax information"), "GUI tax only");
                button(receipt, "Refresh receipt").doClick();
            });
            await(title, true);
            ReceiptModuleTest.check(new ReceiptRepository().findAll().size() == 2, "GUI reuses persisted receipt");
            SwingUtilities.invokeAndWait(() -> ReceiptModuleTest.check(errors.isEmpty(), "No GUI errors: " + errors));
            System.out.println("PASS: 6 receipt Swing GUI assertions.");
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                if (errorCloser[0] != null) errorCloser[0].stop();
                for (Window window : Window.getWindows()) window.dispose();
            });
        }
    }
}
