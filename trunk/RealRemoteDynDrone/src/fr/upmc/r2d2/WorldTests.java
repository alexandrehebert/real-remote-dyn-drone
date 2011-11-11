//	WorldtTests.java --- 
package fr.upmc.r2d2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class WorldTests {

    public static void main(String[] args) throws Exception {
        final Semaphore sem = new Semaphore(1);
        new ApplicationController(sem);
        sem.acquire();
        World world = new World();
        sem.acquire();
        world.start();
        System.out.println("$ world running");
    }
}

class ApplicationController extends JFrame {

    private static final long serialVersionUID = 1L;
    protected JButton stopButton;
    protected JButton startButton;

    public ApplicationController(Semaphore sem) {
        super("Application Controller");
        this.setSize(160, 75);
        this.getContentPane().setLayout(
                new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        StartButton startB = new StartButton(sem);
        StopButton stopB = new StopButton();
        stopButton.setEnabled(false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                if (!startButton.isEnabled())
                    System.out.println("$ world stopped");
            }
        });
        this.addWindowListener(new WindowListener() {
            public void windowClosing(WindowEvent e) {
                System.exit(1);
            }
            public void windowOpened(WindowEvent e) {}
            public void windowClosed(WindowEvent e) {}
            public void windowIconified(WindowEvent e) {}
            public void windowDeiconified(WindowEvent e) {}
            public void windowActivated(WindowEvent e) {}
            public void windowDeactivated(WindowEvent e) {}
        });
        this.add(startB);
        this.add(stopB);
        this.setVisible(true);
    }
    
    class StartButton extends JPanel implements ActionListener {

        private static final long serialVersionUID = 1L;
        protected Semaphore sem;

        public StartButton(Semaphore sem) {
            super();
            startButton = new JButton("start");
            startButton.addActionListener(StartButton.this);
            this.add(startButton);
            this.sem = sem;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("$ world starting");
            this.sem.release();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    class StopButton extends JPanel implements ActionListener {

        private static final long serialVersionUID = 1L;

        public StopButton() {
            super();
            stopButton = new JButton("stop");
            stopButton.addActionListener(StopButton.this);
            this.add(stopButton);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    
}
// $Id$