package sungka.gui;

import javax.swing.*;
import java.awt.*;

 // Simple modal dialog that shows the game's instructions.
public class InstructionsDialog {
    private static final String TEXT = """
                                       Rules/Controls:
                                       - Click one of your pits to sow its shells. Houses (stores) are not playable.
                                       - If a pit you own contains a power-up, clicking it will prompt you to activate that power-up instead of sowing.
                                       - Power-ups appear on pits and have single-letter codes shown in the UI.
                                       - Use the Game Configuration to enable/disable specific power-ups or change the win threshold.
                                       """;
                                       
    public static void show(Component parent) {
        JTextArea ta = new JTextArea(TEXT);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(480, 220));
        JOptionPane.showMessageDialog(parent, sp, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }
}
