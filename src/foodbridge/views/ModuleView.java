package foodbridge.views;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Shared Swing plumbing: repositories are called by controllers on a worker thread. */
public abstract class ModuleView implements View {
    protected final JFrame frame;
    protected final JPanel content=new JPanel(new BorderLayout(12,12));
    private boolean busy;
    protected ModuleView(String title,int width,int height) {
        frame=new JFrame(title); frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        content.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        frame.setContentPane(content); frame.setSize(width,height); frame.setLocationRelativeTo(null);
    }
    @Override public void show() { frame.setVisible(true); refresh(); }
    protected abstract void refresh();
    protected final <T> void task(Supplier<T> operation,Consumer<T> success) {
        if(busy) return;
        busy=true;
        Map<Component,Boolean> enabled=new LinkedHashMap<>();
        disable(content,enabled); frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T,Void>() {
            @Override protected T doInBackground() { return operation.get(); }
            @Override protected void done() {
                enabled.forEach(Component::setEnabled); busy=false; frame.setCursor(Cursor.getDefaultCursor());
                try { T value=get(); if(frame.isDisplayable()) success.accept(value); }
                catch(InterruptedException e) { Thread.currentThread().interrupt(); }
                catch(ExecutionException e) { if(frame.isDisplayable()) error(e.getCause()); }
                catch(RuntimeException e) { if(frame.isDisplayable()) error(e); }
            }
        }.execute();
    }
    private void disable(Component component,Map<Component,Boolean> enabled) {
        enabled.put(component,component.isEnabled()); component.setEnabled(false);
        if(component instanceof Container container) for(Component child:container.getComponents()) disable(child,enabled);
    }
    protected final void error(Throwable e) {
        String message=e instanceof NumberFormatException ? "Enter a valid number." : e.getMessage();
        JOptionPane.showMessageDialog(frame,message==null ? "Could not complete this action." : message,"FoodBridge",JOptionPane.ERROR_MESSAGE);
    }
    protected final void message(String text) { JOptionPane.showMessageDialog(frame,text,"FoodBridge",JOptionPane.INFORMATION_MESSAGE); }
    protected final JButton button(String text,Runnable action) {
        JButton button=new JButton(text); button.addActionListener(e -> { try { action.run(); } catch(RuntimeException ex) { error(ex); } }); return button;
    }
    protected static DefaultTableModel tableModel(String... columns) {
        return new DefaultTableModel(columns,0) { @Override public boolean isCellEditable(int row,int column) { return false; } };
    }
    protected static JTable table(DefaultTableModel model) {
        JTable table=new JTable(model); table.setAutoCreateRowSorter(true); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setRowHeight(24); return table;
    }
    protected static void field(JPanel panel,String label,Component input) { panel.add(new JLabel(label)); panel.add(input); }
    protected static JPanel form() { return new JPanel(new GridLayout(0,2,8,8)); }
    protected static <T> void choices(JComboBox<T> combo,java.util.List<T> values) {
        combo.removeAllItems(); values.forEach(combo::addItem);
    }
}
