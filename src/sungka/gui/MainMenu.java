package sungka.gui;

import sungka.config.GameConfig;

import javax.swing.*;
import java.awt.*;

public final class MainMenu extends JFrame {
    private static final long serialVersionUID = 1L;

    public MainMenu() {
        // Note: do not cache GameConfig values here — read latest values when starting a new game.
        setTitle("Sungka - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        setResizable(false);

        JLabel title = new JLabel("Sungka", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Game mode selection
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 4));
        JRadioButton pvp = new JRadioButton("2 Players (Local)");
        JRadioButton vsai = new JRadioButton("Play vs AI");
        ButtonGroup mg = new ButtonGroup();
        mg.add(pvp); mg.add(vsai);
        pvp.setSelected(true);
        modePanel.add(pvp); modePanel.add(vsai);
        center.add(modePanel);
        center.add(Box.createRigidArea(new Dimension(0,10)));

        JPanel aiSidePanel = new JPanel(new FlowLayout(FlowLayout.CENTER,8,4));
        aiSidePanel.add(new JLabel("AI side:"));
        JComboBox<String> aiSide = new JComboBox<>(new String[]{"Player A (top)", "Player B (bottom)"});
        aiSidePanel.add(aiSide);
        aiSidePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(aiSidePanel);
        center.add(Box.createRigidArea(new Dimension(0,10)));

        // Difficulty selection for AI
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,8,4));
        difficultyPanel.add(new JLabel("AI difficulty:"));
        JComboBox<String> difficulty = new JComboBox<>(new String[]{"Easy","Medium","Hard"});
        difficulty.setSelectedIndex(1); // default Medium
        difficultyPanel.add(difficulty);
        difficultyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(difficultyPanel);
        center.add(Box.createRigidArea(new Dimension(0,10)));

        // start with AI settings disabled unless 'Play vs AI' is selected
        aiSide.setEnabled(false);
        difficulty.setEnabled(false);

        pvp.addActionListener(e -> {
            aiSide.setEnabled(false);
            difficulty.setEnabled(false);
        });
        vsai.addActionListener(e -> {
            aiSide.setEnabled(true);
            difficulty.setEnabled(true);
        });
        aiSidePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(aiSidePanel);
        center.add(Box.createRigidArea(new Dimension(0,10)));

        JButton newGame = new JButton("New Game");
        newGame.setAlignmentX(Component.CENTER_ALIGNMENT);
        newGame.setMaximumSize(new Dimension(200, 40));
        newGame.addActionListener(e -> {
            boolean playVsAI = vsai.isSelected();
            String side = (String) aiSide.getSelectedItem();
            boolean aiPlaysA = "Player A (top)".equals(side);
            String diff = (String) difficulty.getSelectedItem();
            // Read latest central GameConfig now (so Configure changes are applied)
            java.util.Set<String> cfgPUsNow = GameConfig.getInstance().getEnabledPowerUps();
            int cfgThreshNow = GameConfig.getInstance().getWinThreshold();
            SwingUtilities.invokeLater(() -> {
                new SungkaGUI(playVsAI, aiPlaysA, diff, cfgPUsNow, cfgThreshNow).setVisible(true);
            });
            dispose();
        });

        JButton configure = new JButton("Configure Game");
        configure.setAlignmentX(Component.CENTER_ALIGNMENT);
        configure.setMaximumSize(new Dimension(200, 40));
        configure.addActionListener(e -> showConfigDialog());

        JButton instructions = new JButton("Instructions");
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructions.setMaximumSize(new Dimension(200, 40));
        instructions.addActionListener(e -> InstructionsDialog.show(this));

        JButton exit = new JButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setMaximumSize(new Dimension(200, 40));
        exit.addActionListener(e -> System.exit(0));

        center.add(newGame);
        center.add(Box.createRigidArea(new Dimension(0,10)));
        center.add(configure);
        center.add(Box.createRigidArea(new Dimension(0,10)));
        center.add(Box.createRigidArea(new Dimension(0,10)));
        center.add(instructions);
        center.add(Box.createRigidArea(new Dimension(0,10)));
        center.add(exit);

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel ver = new JLabel("v3.0");
        footer.add(ver);
        add(footer, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void showConfigDialog() {
        PowerUpConfigPanel panel = new PowerUpConfigPanel();
        JDialog dlg = new JDialog(this, "Game Configuration", true);
        dlg.setLayout(new BorderLayout(8,8));
        dlg.add(panel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        JButton ok = new JButton("OK");
        bottom.add(cancel); bottom.add(ok);
        dlg.add(bottom, BorderLayout.SOUTH);

        cancel.addActionListener(ev -> dlg.dispose());
        ok.addActionListener(ev -> {
            panel.applyToConfig();
            dlg.dispose();
        });

        dlg.pack(); dlg.setLocationRelativeTo(this); dlg.setVisible(true);
    }

    
}
