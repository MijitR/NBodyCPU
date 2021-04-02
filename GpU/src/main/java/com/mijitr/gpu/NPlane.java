package com.mijitr.gpu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author rwhil
 */
public class NPlane {
    
    public static final int NUM_PLAYERS_DEF, WINDOW_DELAY;
    
    public static final int WIDTH = 1000, HEIGHT = 700;
    
    public static final ThreadLocalRandom RAND = ThreadLocalRandom.current();
    
    static {
        NUM_PLAYERS_DEF = 630;
        WINDOW_DELAY = 20;
    }
    
    private final List<NBody> players;
    
    private JFrame frame;
    
    private JPanel panel;
    
    private transient boolean isInterrupted;
    
    public NPlane(final int numPlayers) throws InterruptedException, InvocationTargetException {
       players = new ArrayList<>(numPlayers);
       final Dimension dims = new Dimension(WIDTH,HEIGHT);
       for(int i = 0; i < numPlayers; i ++) {
           players.add(new NBody(dims, i));
       }
       for(int i = 0; i < numPlayers; i ++) {
           final NBody first = players.remove(0);
           first.subsume(players);
           players.add(first);
       }
       
       SwingUtilities.invokeAndWait(
        new Runnable(){
            @Override
            public final void run() {
                createAndShowGUI();
            }
        }
       );
    }
    
    public final void createAndShowGUI() {
       frame = new JFrame(String.format("%s-Body Simulation", NUM_PLAYERS_DEF));
       
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
       frame.setSize(WIDTH+35,HEIGHT+35);
       frame.setBackground(Color.black);
       
       frame.getContentPane().setBackground(Color.BLACK);
       frame.addWindowListener(new WindowListener(){
           @Override
           public final void windowActivated(final WindowEvent e) {
               
           }
           @Override
           public final void windowClosed(final WindowEvent e) {
                    System.out.println("User closed window");
               NPlane.this.isInterrupted = true;
               NPlane.this.frame.dispose();
           }
           @Override
           public final void windowClosing(final WindowEvent e) {
                    System.out.println("User closing window");
               NPlane.this.isInterrupted = true;
               
           }
           @Override
           public final void windowIconified(final WindowEvent e) {
               
           }
           @Override
           public final void windowDeiconified(final WindowEvent e) {
               
           }
           @Override
           public final void windowOpened(final WindowEvent e) {
               System.out.println("Sourced NBody Plane");
           }
           @Override
           public final void windowDeactivated(final WindowEvent e) {
               
           }
       });
       
       panel = new JPanel(){
            @Override
            public final Dimension getPreferredSize() {
                super.setBackground(Color.black);
                return new Dimension(WIDTH,HEIGHT);
            }
            @Override
            public final Dimension getMinimumSize() {
                return new Dimension(50,50);
            }
            
            @Override
            public final void paintComponent(final Graphics g) {
                super.paintComponent(g);
                final Graphics2D g2 = (Graphics2D)g.create();
                g2.setColor(Color.RED);
                g2.fillRect(0, 0, WIDTH, HEIGHT);
                players.forEach(p->g2.fill(p.getCircle(g2)));
                g2.dispose();
            }
        };
       //panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
       
       //panel.add(Box.createRigidArea(new Dimension(WIDTH,HEIGHT)));
       panel.setBounds(35, 35, WIDTH, HEIGHT);
       
       frame.getContentPane().add(panel);
       
       //frame.getContentPane().add(panel);
       
       //frame.pack();
       frame.setVisible(true);
    }
    
    public final void calc() {
        players.stream().parallel().forEach(
            nb -> nb.operate()
       );
    }
    
    public final void post() {
        /*SwingUtilities.invokeLater(
            new Runnable() {
                @Override
                public final void run() {
                    super
                }
            }
        );*/
        frame.repaint();
    }
    
    public final void fly() {
        final Timer t = new Timer();
        final TimerTask tt = new TimerTask() {
            @Override
            public final void run() {
                calc();
                post();
                if(NPlane.this.isInterrupted) {
                    System.out.println("User closed window perms");
                    this.cancel();
                    //t.cancel();
                    t.purge();
                }
            }
        };
        t.scheduleAtFixedRate(tt, 0, WINDOW_DELAY);
    }

    public final void flyFree() {
        long frames = 0l, timer = System.currentTimeMillis();
        float maxFPS = 0f;
        while(!NPlane.this.isInterrupted) {
            calc();
            post();
            frames ++;
            if(frames % 10 == 0) {
                float fps = ((float)frames)*1000f/(float)(System.currentTimeMillis()-timer);
                if(fps>maxFPS) {
                    maxFPS = fps;
                }
                NPlane.this.frame.setTitle(String.format("%s-Body Simulation FPS: %.2f max@: %.2f", NUM_PLAYERS_DEF, fps, maxFPS));
            }
        }
    }
    
}
