package foodbridge.views.admin;

import java.awt.BorderLayout;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import foodbridge.controllers.ReportController;
import foodbridge.views.ModuleView;

/** Basic program activity report; quantities are never assumed to be meals. */
public class ImpactReportView extends ModuleView {
    private final ReportController controller;
    private final JTextField start=new JTextField(LocalDate.now().withDayOfMonth(1).toString());
    private final JTextField end=new JTextField(LocalDate.now().toString());
    private final JLabel summary=new JLabel("Choose a date range and generate a report.");
    private final DefaultTableModel rows=tableModel("Volunteer ID","Volunteer","Deliveries");
    public ImpactReportView(ReportController controller) {
        super("FoodBridge - Impact report",800,450); this.controller=controller;
        JPanel dates=form(); field(dates,"Start date (yyyy-MM-dd)",start); field(dates,"End date (yyyy-MM-dd)",end);
        content.add(dates,BorderLayout.NORTH); content.add(new JScrollPane(table(rows)),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,8)); bottom.add(summary,BorderLayout.CENTER); bottom.add(button("Generate report",this::refresh),BorderLayout.SOUTH); content.add(bottom,BorderLayout.SOUTH);
    }
    @Override protected void refresh() {
        try {
            LocalDate from=LocalDate.parse(start.getText().trim()),to=LocalDate.parse(end.getText().trim());
            task(() -> controller.generateReport(from,to),report -> {
                summary.setText("Households served: "+report.getFamiliesServed()+" | Deliveries: "+report.getDeliveryCount()+" | Meals: N/A (no counting rule defined)");
                rows.setRowCount(0); report.getDeliveriesPerVolunteer().forEach((v,count) -> rows.addRow(new Object[]{v.getId(),v.getName(),count}));
            });
        } catch(RuntimeException e) { error(e); }
    }
}
