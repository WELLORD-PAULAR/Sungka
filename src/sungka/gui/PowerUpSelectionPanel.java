package sungka.gui;

import sungka.powerups.PowerUpManager;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Reusable panel listing available power-ups with checkboxes.
// Exposes selected codes as a Set<String> (single-letter keys).
public final class PowerUpSelectionPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final transient Map<String, JCheckBox> boxes = new LinkedHashMap<>();

    public PowerUpSelectionPanel() {
        this(null);
    }

    public PowerUpSelectionPanel(Set<String> selected) {
        // create an inner content panel to avoid passing 'this' to BoxLayout
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        Map<String, String> defs = PowerUpManager.getAvailableDefinitions();
        for (Map.Entry<String, String> e : defs.entrySet()) {
            String code = e.getKey();
            String name = e.getValue();
            boolean sel = (selected == null) ? true : selected.contains(code);
            JCheckBox cb = new JCheckBox(code + " — " + name, sel);
            boxes.put(code, cb);
            content.add(cb);
        }
        setLayout(new BorderLayout());
        add(content, BorderLayout.NORTH);
    }

    public Set<String> getSelectedCodes() {
        Set<String> out = new LinkedHashSet<>();
        for (Map.Entry<String, JCheckBox> e : boxes.entrySet()) {
            if (e.getValue().isSelected()) out.add(e.getKey());
        }
        return out;
    }

    public void setSelectedCodes(Set<String> codes) {
        for (Map.Entry<String, JCheckBox> e : boxes.entrySet()) {
            e.getValue().setSelected(codes == null || codes.contains(e.getKey()));
        }
    }
}
