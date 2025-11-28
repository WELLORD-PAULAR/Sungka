package sungka.gui;

import sungka.config.GameConfig;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

// Panel encapsulating game configuration UI for power-ups and win threshold.
// Contains selection panel and a spinner for threshold.
public final class PowerUpConfigPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final PowerUpSelectionPanel selectionPanel;
    private final JSpinner thresholdSpinner;

    public PowerUpConfigPanel() {
        this(GameConfig.getInstance().getEnabledPowerUps(), GameConfig.getInstance().getWinThreshold());
    }

    public PowerUpConfigPanel(Set<String> enabledCodes, int initialThreshold) {
        setLayout(new BorderLayout(8,8));

        // Title
        JLabel title = new JLabel("Power-Up Configuration", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        add(title, BorderLayout.NORTH);

        // Center: selection panel wrapped in scroll pane for smaller windows
        selectionPanel = new PowerUpSelectionPanel(enabledCodes);
        JScrollPane sp = new JScrollPane(selectionPanel);
        sp.setPreferredSize(new Dimension(360, 240));
        add(sp, BorderLayout.CENTER);

        // Bottom: threshold control
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Win threshold (shells in house):"));
        thresholdSpinner = new JSpinner(new SpinnerNumberModel(initialThreshold, 1, 1000, 1));
        bottom.add(thresholdSpinner);
        add(bottom, BorderLayout.SOUTH);
    }

    public Set<String> getSelectedPowerUpCodes() {
        return selectionPanel.getSelectedCodes();
    }

    public int getWinThreshold() {
        return (Integer) thresholdSpinner.getValue();
    }

    public void applyToConfig() {
        GameConfig.getInstance().setEnabledPowerUps(getSelectedPowerUpCodes());
        GameConfig.getInstance().setWinThreshold(getWinThreshold());
    }
}
