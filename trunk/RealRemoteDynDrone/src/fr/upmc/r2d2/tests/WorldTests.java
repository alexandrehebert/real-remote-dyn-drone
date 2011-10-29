//	WorldtTests.java --- 
package fr.upmc.r2d2.tests;

import fr.upmc.dtgui.robot.Robot;
import fr.upmc.dtgui.tests.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class WorldTests {
    
    public static void main(String[] args) {
        
        final Semaphore sem = new Semaphore(1);
        final boolean show = Boolean.parseBoolean(args[0]);
        final ApplicationController applicationController = new ApplicationController(sem, show);
        
        try {
            sem.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        Class<? extends Robot>[] robotsClassLoader = new Class[] {AnotherLittleRobot.class, FirstLittleRobot.class};
        World world = new World(show);
        
        try {
            sem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        world.start();
        
    }
}

class ApplicationController extends JFrame {

    private static final long serialVersionUID = 1L;

    public ApplicationController(Semaphore sem, boolean show) {
        super("Application Controller");
        this.setSize(100, 100);
        this.setLocationRelativeTo(null);
        this.getContentPane().setLayout(
                new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
        StartButton startB = new StartButton(sem);
        StopButton stopB = new StopButton();
        this.add(startB);
        this.add(stopB);
        this.setVisible(show);
    }
}

class StartButton extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    protected JButton startButton;
    protected Semaphore sem;

    public StartButton(Semaphore sem) {
        super();
        this.startButton = new JButton("start");
        this.add(this.startButton);
        this.startButton.addActionListener(this);
        this.sem = sem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.sem.release();
    }
}

class StopButton extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    protected JButton stopButton;

    public StopButton() {
        super();
        this.stopButton = new JButton("stop");
        this.add(this.stopButton);
        this.stopButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}

// $Id$