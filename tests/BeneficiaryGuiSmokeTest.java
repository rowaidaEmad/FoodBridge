import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import foodbridge.controllers.*;
import foodbridge.views.HomeView;
import foodbridge.views.auth.LoginView;

/** Opens actual Swing screens against the isolated database and checks their loaded contents. */
public class BeneficiaryGuiSmokeTest {
    private static final List<String> errors=new ArrayList<>();
    private static int checks;
    private static javax.swing.Timer dialogs;
    private static void check(boolean ok,String message) { checks++; if(!ok) throw new AssertionError(message); }
    private static <T extends Component> List<T> components(Container parent,Class<T> type) {
        List<T> found=new ArrayList<>();
        for(Component c:parent.getComponents()) { if(type.isInstance(c)) found.add(type.cast(c)); if(c instanceof Container child) found.addAll(components(child,type)); }
        return found;
    }
    private static JFrame frame(String title) {
        for(Window w:Window.getWindows()) if(w instanceof JFrame f && f.getTitle().equals(title) && f.isVisible()) return f;
        throw new AssertionError("Missing window: "+title);
    }
    private static JButton button(Container parent,String text) {
        return components(parent,JButton.class).stream().filter(b -> text.equals(b.getText())).findFirst().orElseThrow(() -> new AssertionError("Missing button: "+text));
    }
    private static void awaitReady(String title,String buttonText) throws Exception {
        long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(15);
        long stableSince=0;
        while(System.nanoTime()<deadline) {
            boolean[] ready={false};
            SwingUtilities.invokeAndWait(() -> ready[0]=button(frame(title),buttonText).isEnabled()
                && java.util.Arrays.stream(Window.getWindows()).noneMatch(w -> w instanceof JDialog && w.isVisible()));
            if(ready[0]) {
                if(stableSince==0) stableSince=System.nanoTime();
                // Confirmation dialogs can start a second refresh: wait until both have settled.
                if(System.nanoTime()-stableSince>=java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(200)) return;
            } else stableSince=0;
            Thread.sleep(40);
        }
        throw new AssertionError("Screen did not finish loading: "+title);
    }
    private static void capture(String title,String filename) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame f=frame(title); BufferedImage image=new BufferedImage(f.getWidth(),f.getHeight(),BufferedImage.TYPE_INT_RGB);
                Graphics2D g=image.createGraphics(); f.printAll(g); g.dispose();
                Path path=Path.of("target","beneficiary-verification",filename); Files.createDirectories(path.getParent()); ImageIO.write(image,"png",path.toFile());
            } catch(Exception e) { throw new RuntimeException(e); }
        });
    }
    public static void run(AuthController beneficiary,AuthController admin) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            dialogs=new javax.swing.Timer(40,e -> {
                for(Window w:Window.getWindows()) if(w instanceof JDialog dialog && dialog.isVisible()) {
                    for(JOptionPane pane:components(dialog,JOptionPane.class)) {
                        if(pane.getMessageType()==JOptionPane.ERROR_MESSAGE) errors.add(String.valueOf(pane.getMessage()));
                        dialog.dispose();
                    }
                }
            }); dialogs.start();
        });
        try {
            SwingUtilities.invokeAndWait(() -> {
                HomeView home=new HomeView(beneficiary.getCurrentUser(),beneficiary,new LoginView(beneficiary)); home.show();
                JFrame f=frame("FoodBridge - Home");
                check(components(f,JButton.class).stream().noneMatch(b -> b.getText().equals("Manage beneficiaries")),"Beneficiary home hides admin actions");
                button(f,"My profile").doClick();
            });
            awaitReady("FoodBridge - My beneficiary profile","Save");
            SwingUtilities.invokeAndWait(() -> {
                JFrame f=frame("FoodBridge - My beneficiary profile");
                check(components(f,JTextField.class).get(0).getText().equals("5"),"Profile loads saved household");
                components(f,JTextField.class).get(0).setText("6"); button(f,"Save").doClick();
            });
            awaitReady("FoodBridge - My beneficiary profile","Save");
            check(new BeneficiaryController(beneficiary).getProfile().getHouseholdSize()==6,"GUI Save persists profile");
            SwingUtilities.invokeAndWait(() -> button(frame("FoodBridge - Home"),"My requests").doClick());
            awaitReady("FoodBridge - My donation requests","Submit request");
            SwingUtilities.invokeAndWait(() -> {
                JFrame f=frame("FoodBridge - My donation requests");
                check(components(f,JTable.class).get(0).getRowCount()==2,"Request table loads history");
                components(f,JTextField.class).get(0).setText("3.5"); button(f,"Submit request").doClick();
            });
            awaitReady("FoodBridge - My donation requests","Submit request");
            // Submission starts a second background refresh after the confirmation dialog.
            awaitTableRows("FoodBridge - My donation requests",3);
            capture("FoodBridge - My donation requests","requests.png");
            SwingUtilities.invokeAndWait(() -> button(frame("FoodBridge - Home"),"Received distributions").doClick());
            awaitReady("FoodBridge - Received distributions","Refresh");
            SwingUtilities.invokeAndWait(() -> check(components(frame("FoodBridge - Received distributions"),JTable.class).get(0).getRowCount()==2,"Received deliveries populated"));
            SwingUtilities.invokeAndWait(() -> button(frame("FoodBridge - Home"),"Logout").doClick());
            SwingUtilities.invokeAndWait(() -> {
                check(beneficiary.getCurrentUser()==null,"GUI logout clears session");
                check(java.util.Arrays.stream(Window.getWindows()).noneMatch(w -> w instanceof JFrame f && f.isVisible() && f.getTitle().equals("FoodBridge - My donation requests")),"Logout closes module windows");
                new HomeView(admin.getCurrentUser(),admin,new LoginView(admin)).show();
                button(frame("FoodBridge - Home"),"Manage beneficiaries").doClick();
            });
            awaitReady("FoodBridge - Manage beneficiaries","Refresh");
            SwingUtilities.invokeAndWait(() -> {
                JFrame f=frame("FoodBridge - Manage beneficiaries");
                check(components(f,JTable.class).get(0).getRowCount()==2,"Admin beneficiary table loaded");
                JTabbedPane tabs=components(f,JTabbedPane.class).get(0); tabs.setSelectedIndex(1);
                JTable table=components(f,JTable.class).get(1);
                for(int row=0;row<table.getRowCount();row++) if("PENDING".equals(table.getValueAt(row,6))) { table.setRowSelectionInterval(row,row); break; }
                button(f,"Approve selected").doClick();
            });
            awaitReady("FoodBridge - Manage beneficiaries","Refresh");
            // Wait for the post-action refresh as well.
            long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
            while(System.nanoTime()<deadline && new BeneficiaryController(admin).listAllRequests().stream().anyMatch(r -> r.getStatus().equals("PENDING"))) Thread.sleep(40);
            check(new BeneficiaryController(admin).listAllRequests().stream().noneMatch(r -> r.getStatus().equals("PENDING")),"GUI approval persists");
            SwingUtilities.invokeAndWait(() -> {
                JFrame f=frame("FoodBridge - Manage beneficiaries"); components(f,JTabbedPane.class).get(0).setSelectedIndex(2);
                List<JTextField> fields=components(f,JTextField.class); fields.get(fields.size()-1).setText("1.25");
                button(f,"Record distribution").doClick();
            });
            awaitReady("FoodBridge - Manage beneficiaries","Refresh");
            check(new foodbridge.repositories.DistributionRepository().findAll().size()==5,"GUI records delivery");
            SwingUtilities.invokeAndWait(() -> components(frame("FoodBridge - Manage beneficiaries"),JTabbedPane.class).get(0).setSelectedIndex(1));
            capture("FoodBridge - Manage beneficiaries","admin.png");
            SwingUtilities.invokeAndWait(() -> button(frame("FoodBridge - Home"),"Impact reports").doClick());
            awaitReady("FoodBridge - Impact report","Generate report");
            SwingUtilities.invokeAndWait(() -> check(components(frame("FoodBridge - Impact report"),JLabel.class).stream().anyMatch(l -> l.getText()!=null && l.getText().contains("Households served: 2")),"Report GUI displays computed totals"));
            capture("FoodBridge - Impact report","impact.png");
            check(errors.isEmpty(),"No GUI error dialogs: "+errors);
            System.out.println("PASS: "+checks+" Swing GUI checks.");
        } finally {
            SwingUtilities.invokeAndWait(() -> { dialogs.stop(); for(Window w:Window.getWindows()) w.dispose(); });
        }
    }
    private static void awaitTableRows(String title,int count) throws Exception {
        long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(10);
        while(System.nanoTime()<deadline) {
            boolean[] ready={false}; SwingUtilities.invokeAndWait(() -> ready[0]=components(frame(title),JTable.class).get(0).getRowCount()==count);
            if(ready[0]) return; Thread.sleep(40);
        }
        throw new AssertionError("Table did not refresh: "+title);
    }
}
