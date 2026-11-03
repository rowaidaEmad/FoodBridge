package foodbridge.views;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The "← Back" bar shown at the top of every page; Back returns to the previous screen (Home).
 */
public final class BackBar {
    private BackBar() {
    }

    public static JPanel create(Runnable onBack) {
        JButton back = new JButton("← Back");
        back.addActionListener(e -> onBack.run());
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.add(back);
        return bar;
    }
}
