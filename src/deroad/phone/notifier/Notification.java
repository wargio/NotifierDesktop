package deroad.phone.notifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

public class Notification implements Runnable {
    
    ImageIcon icon = null;
    JFrame frame = new JFrame();
    GridBagConstraints constraints = new GridBagConstraints();
    int timeout = 0;
    
    public Notification(String header, String message, int timeout) {
        this.timeout = timeout;
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("data/tray.png"));
        icon = new ImageIcon(image.getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
        frame.setLayout(new GridBagLayout());
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(0, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        JLabel headingLabel = new JLabel(header);
        headingLabel.setIcon(icon);
        headingLabel.setOpaque(false);
        frame.add(headingLabel, constraints);
        constraints.gridx++;
        constraints.weightx = 0f;
        constraints.weighty = 0f;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTH;
        JButton cloesButton = new JButton(new AbstractAction("Hide") {
            @Override
            public void actionPerformed(final ActionEvent e) {
                frame.dispose();
            }
        });
        cloesButton.setMargin(new Insets(0, 0, 0, 0));
        cloesButton.setFocusable(false);
        frame.add(cloesButton, constraints);
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weightx = 1.0f;
        constraints.weighty = 1.0f;
        constraints.insets = new Insets(0, 5, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        JTextArea messageLabel = new JTextArea(message);
        messageLabel.setEditable(false);
        messageLabel.setBackground(frame.getBackground());
        frame.add(messageLabel, constraints);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Insets taskbar = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());
        frame.pack();
        frame.setSize(300, frame.getHeight());
        int x = screen.width - frame.getWidth();
        int y = screen.height - taskbar.bottom - 30;
        synchronized (Main.n_threads) {
            y -= frame.getHeight() * Main.n_threads;
        }
        frame.setLocation(x, y);
//        frame.getContentPane().setBackground(new Color(217, 243, 229));
        frame.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(38, 40, 53)));
    }
    
    @Override
    public void run() {
        frame.setVisible(true);
        Toolkit.getDefaultToolkit().beep();
        try {
            Thread.sleep(timeout); // time after which pop up will be disappeared.
            frame.dispose();
            synchronized (Main.n_threads) {
                Main.n_threads--;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}
