package sungka;

import javax.swing.SwingUtilities;
import sungka.gui.MainMenu;

public class SungkaApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
