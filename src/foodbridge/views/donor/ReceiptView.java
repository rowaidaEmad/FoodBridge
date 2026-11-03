package foodbridge.views.donor;

import foodbridge.controllers.DonationController;
import foodbridge.models.donations.Receipt;
import foodbridge.patterns.decorator.ReceiptOptions;
import foodbridge.views.BackBar;
import foodbridge.views.View;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

/**
 * Screen; talks only to DonationController.
 */
public class ReceiptView implements View {
    private final DonationController donationController;
    private final int donationId;
    private final JDialog dialog = new JDialog();
    private final JCheckBox thankYou = new JCheckBox("Include thank-you message");
    private final JCheckBox taxInfo = new JCheckBox("Include tax information");
    private final JTextArea text = new JTextArea();
    private final JButton refresh = new JButton("Refresh receipt");
    private Receipt receipt;

    public ReceiptView(DonationController donationController, int donationId) {
        this.donationController = donationController;
        this.donationId = donationId;
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT));
        options.add(thankYou);
        options.add(taxInfo);
        options.add(refresh);
        thankYou.addActionListener(e -> reload());
        taxInfo.addActionListener(e -> reload());
        refresh.addActionListener(e -> reload());

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        content.add(BackBar.create(dialog::dispose), BorderLayout.NORTH);
        content.add(new JScrollPane(text), BorderLayout.CENTER);
        content.add(options, BorderLayout.SOUTH);
        dialog.setTitle("FoodBridge - Receipt for donation #" + donationId);
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(content);
        dialog.setSize(760, 480);
        dialog.setLocationRelativeTo(null);
    }

    @Override
    public void show() {
        reload();
        dialog.setVisible(true);
    }

    /** JDBC work runs off the Swing event thread, including the initial create-or-load. */
    private void reload() {
        ReceiptOptions options = new ReceiptOptions(thankYou.isSelected(), taxInfo.isSelected());
        setBusy(true);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                if (receipt == null) receipt = donationController.createReceipt(donationId);
                return donationController.getReceiptText(receipt.getId(), options);
            }

            @Override
            protected void done() {
                if (!dialog.isDisplayable()) return;
                try {
                    text.setText(get());
                    text.setCaretPosition(0);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getCause().getMessage(), "Receipt",
                            JOptionPane.ERROR_MESSAGE);
                    if (receipt == null) dialog.dispose();
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        thankYou.setEnabled(!busy);
        taxInfo.setEnabled(!busy);
        refresh.setEnabled(!busy);
    }
}
