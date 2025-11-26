package sungka.gui;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {
    public MainMenu() {
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
            SwingUtilities.invokeLater(() -> {
                new SungkaGUI(playVsAI, aiPlaysA, diff).setVisible(true);
            });
            dispose();
        });

        JButton instructions = new JButton("Instructions");
        instructions.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructions.setMaximumSize(new Dimension(200, 40));
        instructions.addActionListener(e -> showInstructions());

        JButton exit = new JButton("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setMaximumSize(new Dimension(200, 40));
        exit.addActionListener(e -> System.exit(0));

        center.add(newGame);
        center.add(Box.createRigidArea(new Dimension(0,10)));
        center.add(instructions);
        center.add(Box.createRigidArea(new Dimension(0,10)));
        center.add(exit);

        add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel ver = new JLabel("v1.0");
        footer.add(ver);
        add(footer, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void showInstructions() {
        String help = "Rules/Controls:\n" +
                "- Click your own pit to play. If the pit contains a power-up, it will activate instead of sowing.\n" +
                "- Capturing an opponent pit steals its power-up (moved to a random empty pit on your side).\n" +
                "- After your turn ends, the system refills your side with random power-ups until you have 3.\n" +
                "- Power-up codes shown on a pit: D(DoubleCapture), B(BonusTurn), R(Reverse), M(Magnet), S(StealShells), P(PitShield), A(AddShells), W(SwapHouses), K(SkipOpp), L(LuckyDrop).\n";
        JOptionPane.showMessageDialog(this, help, "Instructions", JOptionPane.INFORMATION_MESSAGE);
    }
}
