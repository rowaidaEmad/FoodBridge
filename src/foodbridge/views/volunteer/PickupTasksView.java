package foodbridge.views.volunteer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.IntConsumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;

import foodbridge.controllers.PickupController;
import foodbridge.models.operations.PickupTask;
import foodbridge.views.SwingSupport;
import foodbridge.views.View;

/**
 * Screen; talks only to PickupController.
 */
public class PickupTasksView implements View {
    private final int volunteerId;
    private final PickupController pickupController;
    private final JFrame frame = new JFrame("FoodBridge - Pickup Tasks");
    private final JTable availableTable =
            SwingSupport.readOnlyTable("ID", "Items", "Pickup window", "Required skill", "Beneficiary");
    private final JTable myTasksTable =
            SwingSupport.readOnlyTable("ID", "Items", "Status", "Hours", "Beneficiary");

    public PickupTasksView(int volunteerId, PickupController pickupController) {
        this.volunteerId = volunteerId;
        this.pickupController = pickupController;

        JPanel content = new JPanel(new GridLayout(2, 1, 8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(section("Available tasks", availableTable, availableActions()));
        content.add(section("My tasks", myTasksTable, myTaskActions()));

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

    private JPanel section(String title, JTable table, JPanel actions) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(SwingSupport.scroll(table, 680, 150), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel availableActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(actionButton("Accept", availableTable, this::accept));
        return actions;
    }

    private JPanel myTaskActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(actionButton("Decline", myTasksTable, id -> pickupController.declineTask(id, volunteerId)));
        actions.add(actionButton("Picked Up", myTasksTable, id -> pickupController.markPickedUp(id, volunteerId)));
        actions.add(actionButton("Delivered", myTasksTable, id -> pickupController.markDelivered(id, volunteerId)));
        return actions;
    }

    private JButton actionButton(String label, JTable table, IntConsumer action) {
        JButton button = new JButton(label);
        button.addActionListener(e -> SwingSupport.attempt(frame, () -> {
            action.accept(SwingSupport.selectedId(table));
            refresh();
        }));
        return button;
    }

    private void accept(int taskId) {
        if (!pickupController.acceptTask(taskId, volunteerId)) {
            JOptionPane.showMessageDialog(frame, "This task was already taken.");
        }
    }

    private void refresh() {
        SwingSupport.setRows(availableTable, availableRows());
        SwingSupport.setRows(myTasksTable, myTaskRows());
    }

    private List<Object[]> availableRows() {
        return pickupController.listAvailable(volunteerId).stream()
                .map(task -> new Object[] { task.getId(), task.getItemsSummary(), task.getDonation().getPickupWindow(),
                        SwingSupport.orEmpty(task.getRequiredSkill()), beneficiaryName(task) })
                .toList();
    }

    private List<Object[]> myTaskRows() {
        return pickupController.listMine(volunteerId).stream()
                .map(task -> new Object[] { task.getId(), task.getItemsSummary(), task.getStatus(),
                        String.format("%.2f", task.getHoursSpent()), beneficiaryName(task) })
                .toList();
    }

    private String beneficiaryName(PickupTask task) {
        return task.getBeneficiary() == null ? "" : task.getBeneficiary().getName();
    }
}
