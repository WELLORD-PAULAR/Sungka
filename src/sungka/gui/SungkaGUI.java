package sungka.gui;

import sungka.ai.SimpleAI;
import sungka.core.SungkaGame;
import sungka.config.GameConfig;
import sungka.model.Pit;
import sungka.model.Player;
import sungka.powerups.PowerUp;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class SungkaGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private transient final SungkaGame game;
    private final JButton[] pitButtons = new JButton[16];
    private final JTextArea logArea = new JTextArea(6, 40);
    private boolean animating = false;

    // scoreboard / history
    private JProgressBar progressA;
    private JProgressBar progressB;
    private JLabel activeALabel = new JLabel("Active: None");
    private JLabel activeBLabel = new JLabel("Active: None");
    private JLabel powerCountALabel = new JLabel("Power-ups left: 2");
    private JLabel powerCountBLabel = new JLabel("Power-ups left: 2");
    private transient final java.util.Map<Player, Boolean> limitWarnShown = new java.util.HashMap<>();
    private int prevACount = -1;
    private int prevBCount = -1;
    private Timer pulseTimer;

    // AI support
    private final boolean aiEnabled;
    private transient final Player aiPlayer; // which Player is controlled by AI, null if none
    private transient final SimpleAI ai;
    private final Timer aiTimer;
    // status labels (top row empty slots)
    private JLabel directionLabel;
    private JLabel turnLabel;

    public SungkaGUI() { this(false, false, "Medium", null, GameConfig.getInstance().getWinThreshold()); }

    public SungkaGUI(boolean aiEnabled, boolean aiPlaysA, String difficultyStr) {
        this(aiEnabled, aiPlaysA, difficultyStr, null, GameConfig.getInstance().getWinThreshold());
    }

    public SungkaGUI(boolean aiEnabled, boolean aiPlaysA, String difficultyStr, java.util.Set<String> enabledPowerUps, int winThreshold) {
        this.aiEnabled = aiEnabled;
        SimpleAI.Difficulty d;
        try { d = SimpleAI.Difficulty.valueOf(difficultyStr.toUpperCase()); }
        catch (Exception ex) { d = SimpleAI.Difficulty.MEDIUM; }
        this.ai = aiEnabled ? new SimpleAI(d) : null;
        game = new SungkaGame();
        // apply configuration (if provided) — fall back to central GameConfig when null/invalid
        java.util.Set<String> cfgPUs = enabledPowerUps;
        if (cfgPUs == null) cfgPUs = GameConfig.getInstance().getEnabledPowerUps();
        if (cfgPUs != null) game.setAllowedPowerUps(cfgPUs);
        int cfgThresh = (winThreshold > 0) ? winThreshold : GameConfig.getInstance().getWinThreshold();
        game.setWinThreshold(cfgThresh);
        this.aiPlayer = aiEnabled ? (aiPlaysA ? game.playerA : game.playerB) : null;

        game.setLogger((s) -> SwingUtilities.invokeLater(() -> {
            logArea.append(s + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }));

        setTitle("Sungka with 10 Power-Ups");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Board columns: left and right vertical panels so logs remain in the middle
        JPanel leftColumn = new JPanel(new GridLayout(9,1,8,8));
        leftColumn.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel rightColumn = new JPanel(new GridLayout(9,1,8,8));
        rightColumn.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // top row: left = turn label, right = opponent house (pit 15)
        turnLabel = new JLabel("Turn: " + game.getCurrent().getName(), SwingConstants.CENTER);
        turnLabel.setPreferredSize(new Dimension(140,60));
        turnLabel.setOpaque(true); turnLabel.setBackground(new Color(245,245,245));
        leftColumn.add(turnLabel);
        JButton houseTopRight = createPitButton(15);
        houseTopRight.setPreferredSize(new Dimension(140,60));
        rightColumn.add(houseTopRight);

        // rows 1..7: small pits — align opposites vertically
        for (int row = 1; row <= 7; row++) {
            int leftIdx = row - 1; // 0..6
            int rightIdx = 14 - (row - 1); // 14..8
            JButton leftBtn = createPitButton(leftIdx);
            leftBtn.setPreferredSize(new Dimension(140,60));
            leftColumn.add(leftBtn);
            JButton rightBtn = createPitButton(rightIdx);
            rightBtn.setPreferredSize(new Dimension(140,60));
            rightColumn.add(rightBtn);
        }

        // bottom row: left = our house (7), right = sow-direction label
        JButton houseLeft = createPitButton(7);
        houseLeft.setPreferredSize(new Dimension(140,60));
        leftColumn.add(houseLeft);
        directionLabel = new JLabel("Sow: " + (game.isReverseSowing() ? "←" : "→"), SwingConstants.CENTER);
        directionLabel.setPreferredSize(new Dimension(140,60));
        directionLabel.setOpaque(true); directionLabel.setBackground(new Color(245,245,245));
        rightColumn.add(directionLabel);

        // Center: scoreboard, controls, log
        JPanel centerControls = new JPanel(new BorderLayout(8,8));
        centerControls.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // top controls + scoreboard
        // Top controls: left = active-powerups, center = Help, right = remaining power-up counters
        JPanel topControls = new JPanel(new BorderLayout(8,8));
        JPanel leftActive = new JPanel(); leftActive.setLayout(new BoxLayout(leftActive, BoxLayout.Y_AXIS));
        leftActive.add(activeALabel);
        leftActive.add(activeBLabel);

        JButton hint = new JButton("Show Help");
        hint.addActionListener(e -> showHelp());

        JPanel rightCounts = new JPanel(); rightCounts.setLayout(new BoxLayout(rightCounts, BoxLayout.Y_AXIS));
        rightCounts.add(powerCountALabel);
        rightCounts.add(powerCountBLabel);

        topControls.add(leftActive, BorderLayout.WEST);
        topControls.add(hint, BorderLayout.CENTER);
        topControls.add(rightCounts, BorderLayout.EAST);

        // Progress bars sized to this game's win threshold
        progressA = new JProgressBar(0, game.getWinThreshold());
        progressB = new JProgressBar(0, game.getWinThreshold());
        progressA.setPreferredSize(new Dimension(320,28));
        progressB.setPreferredSize(new Dimension(320,28));
        progressA.setStringPainted(true);
        progressB.setStringPainted(true);

        // labels are initialized at field declaration

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
        JPanel pa = new JPanel(new FlowLayout(FlowLayout.CENTER)); pa.add(new JLabel("Player A")); pa.add(progressA);
        JPanel pb = new JPanel(new FlowLayout(FlowLayout.CENTER)); pb.add(new JLabel("Player B")); pb.add(progressB);
        scorePanel.add(pa); scorePanel.add(pb);

        JPanel combinedTop = new JPanel(new BorderLayout());
        combinedTop.add(scorePanel, BorderLayout.NORTH);
        combinedTop.add(topControls, BorderLayout.SOUTH);
        centerControls.add(combinedTop, BorderLayout.NORTH);

        logArea.setEditable(false);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setPreferredSize(new Dimension(360,400));
        centerControls.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh UI");
        refresh.addActionListener(e -> updateBoard());
        bottom.add(refresh);
        centerControls.add(bottom, BorderLayout.SOUTH);

        add(leftColumn, BorderLayout.WEST);
        add(centerControls, BorderLayout.CENTER);
        add(rightColumn, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);

        // window close behavior
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                if (aiTimer != null) aiTimer.stop();
                SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
            }
        });

        // AI timer
        if (aiEnabled) {
            aiTimer = new Timer(700, e -> {
                try {
                    if (animating) return; // skip AI actions while an animation is in progress
                    if (game.getCurrent() == aiPlayer) {
                        int move = ai.chooseMove(game, aiPlayer);
                        if (move >= 0) {
                            Pit p = game.board[move];
                            if (p.hasPowerUp() && aiPlayer.ownsPit(move)) {
                                game.activatePowerUpInPit(move);
                                updateBoard();
                            } else if (aiPlayer.ownsPit(move) && p.getStones() > 0) {
                                List<Integer> seq = game.previewSowSequence(move);
                                if (seq != null && !seq.isEmpty()) {
                                    // use GUI animation for AI move
                                    animateSow(move, seq);
                                } else {
                                    game.makeMove(move);
                                    updateBoard();
                                }
                            }
                        }
                    }
                } catch (Exception _) { }
            });
            aiTimer.setRepeats(true); aiTimer.start();
        } else aiTimer = null;

        // initial update
        updateBoard();
    }

        private void showHelp() {
            StringBuilder sb = new StringBuilder();
            sb.append("Help — Current Rules & UI\n\n");
            sb.append("Board / Controls:\n");
            sb.append("- Click one of your pits to sow its shells. Houses (stores) are not playable.\n");
            sb.append("- If a pit you own contains a power-up, clicking it will prompt you to activate that power-up instead of sowing.\n\n");

            sb.append("Power-ups (summary):\n");
            sb.append("- Power-ups appear on pits (single-letter code). Examples: P=PitShield, S=Steal, B=BonusTurn, R=Reverse, etc.\n");
            sb.append("- Activating a power-up does NOT consume your normal sowing turn — you may still sow after activation unless an effect changes the turn.\n");
            sb.append("- When you capture an opponent pit that contains a power-up, that power-up is activated immediately for you if you have remaining activations this turn.\n\n");

            sb.append("Activation limits & wipe behavior:\n");
            sb.append("- Each player may activate up to 2 power-ups per turn (this includes manual activations and immediate activations from captures).\n");
            sb.append("- Once a player reaches the 2-per-turn activation limit, ALL power-ups on that player's side are cleared (wiped) immediately.\n\n");

            sb.append("PitShield (P):\n");
            sb.append("- Protects a single pit for 2 turns. While protected, the pit cannot be captured or have its power-up stolen/refilled. Protection is removed when a blocked capture occurs. Protected pits are outlined in the UI.\n\n");

            sb.append("Power-up refresh & distribution:\n");
            sb.append("- At the end of a player's turn the system may refill power-ups on that player's side up to the cap (skips protected pits).\n");
            sb.append("- Captured power-ups are handled immediately (activation if possible) or placed/stored on the captor's side when possible.\n\n");

            sb.append("UI indicators:\n");
            sb.append("- Active power-up(s) for each player are shown left of the Help button.\n");
            sb.append("- Remaining per-turn activations are shown right of the Help button as 'Power-ups left: N'.\n\n");

            sb.append("AI notes:\n");
            sb.append("- Built-in AI animates its sowing moves in the GUI and uses search with a small online learning bias.\n\n");

            sb.append("Tips:\n");
            sb.append("- Plan activations: hitting the 2-per-turn limit will wipe your side's power-ups.\n");
            sb.append("- Use PitShield to protect high-value pits.\n");

            JOptionPane.showMessageDialog(this, sb.toString(), "Help", JOptionPane.INFORMATION_MESSAGE);
        }

    

    private Icon makePitIcon(int stones, PowerUp pu, boolean isHouse) {
        int w = 120, h = 60;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g.setColor(new Color(245,245,245));
        g.fillRect(0,0,w,h);

        // draw shell circle
        int cx = 36, cy = h/2, r = 26;
        g.setColor(isHouse ? new Color(200,220,255) : new Color(255,250,200));
        g.fill(new Ellipse2D.Double(cx-r, cy-r, r*2, r*2));
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(2));
        g.draw(new Ellipse2D.Double(cx-r, cy-r, r*2, r*2));

        // draw number inside circle
        String s = String.valueOf(stones);
        Font f = new Font("SansSerif", Font.BOLD, 16);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics(f);
        int tx = cx - fm.stringWidth(s)/2;
        int ty = cy + fm.getAscent()/2 - 3;
        g.setColor(Color.BLACK);
        g.drawString(s, tx, ty);

        // draw power-up small box on right if present
        if (pu != null) {
            int bx = w - 34, by = 12, bw = 28, bh = 28;
            g.setColor(new Color(220, 120, 180));
            g.fillRoundRect(bx, by, bw, bh, 6, 6);
            g.setColor(Color.WHITE);
            String code = pu.getCode();
            Font sf = new Font("SansSerif", Font.BOLD, 14);
            g.setFont(sf);
            FontMetrics sfm = g.getFontMetrics(sf);
            int sx = bx + (bw - sfm.stringWidth(code))/2;
            int sy = by + (bh + sfm.getAscent())/2 - 3;
            g.drawString(code, sx, sy);
        }

        g.dispose();
        return new ImageIcon(img);
    }

    private JButton createPitButton(int index) {
        JButton btn = new JButton();
        pitButtons[index] = btn;
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.addActionListener(e -> handlePitClick(index));
        return btn;
    }

    private void handlePitClick(int idx) {
        if (animating) return; // ignore clicks while animating
        Player cur = game.getCurrent();
        Pit pit = game.board[idx];

        if (!cur.ownsPit(idx) && !pit.isHouse()) {
            JOptionPane.showMessageDialog(this, "That's not your pit.", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pit.hasPowerUp() && cur.ownsPit(idx)) {
            PowerUp p = pit.getPowerUp();
            int confirm = JOptionPane.showConfirmDialog(this, "Activate " + p.getName() + "?\n" + p.getDescription(),
                    "Activate Power-Up", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = game.activatePowerUpInPit(idx);
                if (!ok) {
                    JOptionPane.showMessageDialog(this, "Couldn't activate that power-up.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                updateBoard();
                return;
            } else {
                return;
            }
        }

        if (pit.isHouse()) {
            JOptionPane.showMessageDialog(this, "Can't play a house.", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (pit.getStones() == 0) {
            JOptionPane.showMessageDialog(this, "Pit is empty.", "Invalid", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // perform animated sow preview then execute move
        List<Integer> seq = game.previewSowSequence(idx);
        if (seq.isEmpty()) {
            // no sowing sequence (shouldn't happen) — fallback to immediate move
            boolean ok = game.makeMove(idx);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Invalid move.", "Invalid", JOptionPane.WARNING_MESSAGE);
                return;
            }
            updateBoard();
        } else {
            animateSow(idx, seq);
        }
    }

    private void animateSow(int startIdx, List<Integer> seq) {
        animating = true;
        // snapshot displayed counts
        final int[] display = new int[16];
        for (int i = 0; i < 16; i++) display[i] = game.board[i].getStones();

        // empty start pit visually
        display[startIdx] = 0;
        pitButtons[startIdx].setText("0");

        // disable buttons visually by setting cursor; clicks are ignored via animating flag
        Cursor oldCursor = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final int[] step = {0};
        Timer t = new Timer(120, null);
        t.addActionListener(e -> {
            if (step[0] < seq.size()) {
                int dest = seq.get(step[0]++);
                display[dest]++;
                pitButtons[dest].setText(String.valueOf(display[dest]));
                // flash destination briefly
                pitButtons[dest].setBackground(new Color(255, 230, 150));
            } else {
                t.stop();
                // small pause then apply actual game move and refresh board
                Timer finish = new Timer(200, ev -> {
                    try {
                        game.makeMove(startIdx);
                    } catch (Exception ex) {
                        // swallow
                    }
                    updateBoard();
                    setCursor(oldCursor);
                    animating = false;
                });
                finish.setRepeats(false);
                finish.start();
            }
        });
        t.setRepeats(true);
        t.start();
    }

    private void updateBoard() {
        for (int i = 0; i < 16; i++) {
            Pit p = game.board[i];
            PowerUp pu = p.hasPowerUp() ? p.getPowerUp() : null;
            Icon ic = makePitIcon(p.getStones(), pu, p.isHouse());
            pitButtons[i].setIcon(ic);
            pitButtons[i].setText("");
            if (i == game.playerA.getHouseIndex() || i == game.playerB.getHouseIndex()) {
                pitButtons[i].setBackground(new Color(220, 230, 255));
            } else if (game.getCurrent().ownsPit(i)) {
                pitButtons[i].setBackground(new Color(255, 250, 200));
            } else if (p.hasPowerUp()) {
                pitButtons[i].setBackground(new Color(200, 255, 230));
            } else {
                pitButtons[i].setBackground(new Color(245, 245, 245));
            }
            // indicate protected pit (from PitShield)
            Integer protA = game.getProtectedPit(game.playerA);
            Integer protB = game.getProtectedPit(game.playerB);
            if ((protA != null && protA == i) || (protB != null && protB == i)) {
                pitButtons[i].setBorder(new LineBorder(new Color(200,60,60), 3));
            } else {
                pitButtons[i].setBorder(UIManager.getBorder("Button.border"));
            }
        }
        setTitle("Turn: " + game.getCurrent().getName());
        // update status labels
        if (turnLabel != null) turnLabel.setText("Turn: " + game.getCurrent().getName());
        if (directionLabel != null) directionLabel.setText("Sow: " + (game.isReverseSowing() ? "←" : "→"));

        // check win condition
        Player winner = game.checkForWinner();
        if (winner != null) {
            if (aiTimer != null) aiTimer.stop();
            appendMatchHistory(winner.getName(), game.board[game.playerA.getHouseIndex()].getStones(), game.board[game.playerB.getHouseIndex()].getStones());
            JOptionPane.showMessageDialog(this, winner.getName() + " reached " + game.getWinThreshold() + " shells and wins!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
            dispose();
        }

        // update scoreboard (progress bars)
        int aCount = game.board[game.playerA.getHouseIndex()].getStones();
        int bCount = game.board[game.playerB.getHouseIndex()].getStones();
        progressA.setValue(aCount);
        progressA.setString(aCount + " / " + game.getWinThreshold());
        progressB.setValue(bCount);
        progressB.setString(bCount + " / " + game.getWinThreshold());

        // show active power-ups for each player
        if (activeALabel != null) {
            PowerUp ap = game.getActivePowerUp(game.playerA);
            activeALabel.setText("Active: " + (ap != null ? ap.getName() : "None"));
        }
        if (activeBLabel != null) {
            PowerUp bp = game.getActivePowerUp(game.playerB);
            activeBLabel.setText("Active: " + (bp != null ? bp.getName() : "None"));
        }
        // update per-turn remaining counters
        int leftA = Math.max(0, 2 - game.getPowerUpsUsedThisTurn(game.playerA));
        int leftB = Math.max(0, 2 - game.getPowerUpsUsedThisTurn(game.playerB));
        if (powerCountALabel != null) powerCountALabel.setText("Power-ups left: " + leftA);
        if (powerCountBLabel != null) powerCountBLabel.setText("Power-ups left: " + leftB);

        // If the current (human) player has hit the limit, prompt them to select a pit (only once)
        if (!aiEnabled) {
            Player cur = game.getCurrent();
            int left = Math.max(0, 2 - game.getPowerUpsUsedThisTurn(cur));
            boolean shown = limitWarnShown.getOrDefault(cur, false);
            if (left == 0 && !shown) {
                JOptionPane.showMessageDialog(this, cur.getName() + " reached the power-up limit. All power-ups on their side were wiped. Please select a pit to sow.", "Power-up limit reached", JOptionPane.INFORMATION_MESSAGE);
                limitWarnShown.put(cur, true);
            }
            if (left > 0) limitWarnShown.put(cur, false);
        }

        // highlight when within 5 shells of winning (pulse border and beep once on crossing)
        int thresh = game.getWinThreshold();
        boolean aNear = (aCount < thresh && thresh - aCount <= 5);
        boolean bNear = (bCount < thresh && thresh - bCount <= 5);

        // handle beep on crossing into near-win
        if (prevACount >= 0 && ! (prevACount < thresh && thresh - prevACount <= 5) && aNear) playNearWinSound();
        if (prevBCount >= 0 && ! (prevBCount < thresh && thresh - prevBCount <= 5) && bNear) playNearWinSound();

        prevACount = aCount; prevBCount = bCount;

        if (pulseTimer == null) {
            pulseTimer = new Timer(400, ev -> {
                // toggle border for near players
                javax.swing.border.Border aBorder = progressA.getBorder();
                javax.swing.border.Border bBorder = progressB.getBorder();
                if (aNear) progressA.setBorder(new LineBorder(new Color(220,80,10), (aBorder instanceof LineBorder && ((LineBorder)aBorder).getThickness() == 3) ? 1 : 3));
                else progressA.setBorder(UIManager.getBorder("ProgressBar.border"));
                if (bNear) progressB.setBorder(new LineBorder(new Color(220,80,10), (bBorder instanceof LineBorder && ((LineBorder)bBorder).getThickness() == 3) ? 1 : 3));
                else progressB.setBorder(UIManager.getBorder("ProgressBar.border"));
            });
            pulseTimer.setRepeats(true);
            pulseTimer.start();
        }
    }

    private void playNearWinSound() {
        try {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception ex) {
            // ignore
        }
    }

    private void appendMatchHistory(String winner, int aCount, int bCount) {
        String file = "match_history.txt";
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String line = ts + " | Winner: " + winner + " | A=" + aCount + " | B=" + bCount + System.lineSeparator();
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(line);
        } catch (IOException ioe) {
            // ignore history write failures
        }
    }
}
