package org.parabot.core.ui.components;

import org.parabot.core.Core;
import org.parabot.core.io.ProgressListener;
import org.parabot.core.settings.Configuration;
import org.parabot.core.ui.ServerSelector;
import org.parabot.core.ui.fonts.Fonts;
import org.parabot.core.ui.images.Images;
import org.parabot.core.user.UserAuthenticator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/**
 * An informative JPanel which tells the user what bot is doing
 *
 * @author Everel, EmmaStone
 */
public class VerboseLoader extends JPanel implements ProgressListener {
    public static final int STATE_LOADING = 1;
    private static final long serialVersionUID = 7412412644921803896L;
    private static final int STATE_AUTHENTICATION = 0;
    private static final int STATE_SERVER_SELECT = 2;
    private static VerboseLoader current;
    private static String state = "Initializing loader...";
    private int currentState;

    private FontMetrics fontMetrics;
    private BufferedImage background, banner, loginBox;
    private ProgressBar progressBar;
    private JPanel loginPanel;

    private VerboseLoader() {
        if (current != null) {
            throw new IllegalStateException("MainScreenComponent already made.");
        }
        current = this;
        this.background = Images.getResource("/storage/images/background.png");
        this.banner = Images.getResource("/storage/images/logo.png");
        this.loginBox = Images.getResource("/storage/images/login.png");
        this.progressBar = new ProgressBar(400, 20);
        setLayout(new GridBagLayout());
        setSize(775, 510);
        setPreferredSize(new Dimension(775, 510));
        setDoubleBuffered(true);
        setOpaque(false);

        if (Core.isMode(Core.LaunchMode.LOCAL_ONLY)) {
            currentState = STATE_SERVER_SELECT;
        }

        if (currentState == STATE_AUTHENTICATION) {
            addLoginPanel();
        } else if (currentState == STATE_SERVER_SELECT) {
            addServerPanel();
        }
    }

    /**
     * Gets instance of this panel
     *
     * @return instance of this panel
     */
    public static VerboseLoader get() {
        return current == null ? new VerboseLoader() : current;
    }

    /**
     * Updates the status message and repaints the panel
     *
     * @param message
     */
    public static void setState(final String message) {
        state = message;
        current.repaint();
    }

    public void addServerPanel() {
        JPanel servers = Core.getInjector().getInstance(ServerSelector.class);
        GridBagLayout bagLayout = (GridBagLayout) getLayout();
        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.SOUTH;
        c.insets = new Insets(0, 0, 25, 0);

        bagLayout.setConstraints(servers, c);
        add(servers);
    }

    public void addLoginPanel() {
        loginPanel = new JPanel();
        loginPanel.setOpaque(false);

        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));

        final JButton login = new JButton("Login");

        login.setAlignmentX(Box.CENTER_ALIGNMENT);
        login.setOpaque(false);

        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UserAuthenticator authenticator = Core.getInjector().getInstance(UserAuthenticator.class);
                if (authenticator.login()) {
                    authenticator.afterLogin();
                    switchState(STATE_SERVER_SELECT);
                }
            }
        });

        loginPanel.add(login);
        loginPanel.add(Box.createRigidArea(new Dimension(0, 4)));

        add(loginPanel, new GridBagConstraints());
    }

    public void switchState(int state) {
        removeAll();
        if (state == STATE_AUTHENTICATION) {
            addLoginPanel();
        } else if (state == STATE_SERVER_SELECT) {
            addServerPanel();
        }
        this.currentState = state;
        revalidate();
    }

    /**
     * Paints on this panel
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);


        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.drawImage(background, 0, 0, null);
        float[] scales = {1f, 1f, 1f, 0.9f};
        float[] offsets = new float[4];
        RescaleOp rop = new RescaleOp(scales, offsets, null);
        g.drawImage(banner, rop, 0, 0);

        g.setStroke(new BasicStroke(5));
        g.setPaint(Color.WHITE);

        g.draw(new Line2D.Float(0, 1, this.getWidth(), 1)); //TOP
        g.draw(new Line2D.Float(0, 0, 0, 120)); //LEFT
        g.draw(new Line2D.Float(0, 120, this.getWidth(), 120)); //BOTTOM
        g.draw(new Line2D.Float(this.getWidth() - 6, 0, this.getWidth() - 6, 120)); //RIGHT

        g.setColor(Color.white);

        g.setFont(Fonts.getResource("leelawadee.ttf", 30));
        g.getFont().deriveFont(Font.BOLD);
        g.drawString(Configuration.BOT_TITLE, 20, 50);

        g.setFont(Fonts.getResource("leelawadee.ttf", 15));
        g.getFont().deriveFont(Font.ITALIC);
        g.drawString(Configuration.BOT_SLOGAN, 20, 85);

        if (fontMetrics == null) {
            fontMetrics = g.getFontMetrics();
        }

        if (currentState == STATE_AUTHENTICATION) {
            g.drawImage(loginBox, loginPanel.getX() - 30, loginPanel.getY() - 22, null);
        }

        g.setColor(Color.white);

        if (currentState == STATE_LOADING) {
            progressBar.draw(g, (getWidth() / 2) - 200, 220);
            g.setFont(Fonts.getResource("leelawadee.ttf"));
            int x = (getWidth() / 2) - (fontMetrics.stringWidth(state) / 2);
            g.drawString(state, x, 200);
        }


        g.setFont(Fonts.getResource("leelawadee.ttf"));
        final String version = Configuration.BOT_VERSION.get();
        g.drawString(version,
                getWidth() - g.getFontMetrics().stringWidth(version) - 10,
                getHeight() - 12);
    }

    @Override
    public void onProgressUpdate(double value) {
        progressBar.setValue(value);
        this.repaint();
    }

    @Override
    public void updateDownloadSpeed(double mbPerSecond) {
        progressBar.setText(String.format("(%.2fMB/s)", mbPerSecond));
    }
}