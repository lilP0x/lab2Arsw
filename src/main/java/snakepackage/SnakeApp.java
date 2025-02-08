package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import java.util.concurrent.CountDownLatch;
import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * @author jd-
 *
 */
public class SnakeApp {

    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private CountDownLatch latch = new CountDownLatch(MAX_THREADS);
    private static JDialog pauseDialog;
    private static final Cell[] spawn = {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2,
        3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
        GridSize.GRID_HEIGHT - 2)};
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];
    
    private JButton startButton = new JButton("Iniciar");
    private JButton pauseButton = new JButton("Pausar");
    private JButton resumeButton = new JButton("Reanudar");
        

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();
        
        frame.add(board,BorderLayout.CENTER);
        
        JPanel actionsBPabel=new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        startButton = new JButton("Iniciar ");
        startButton.addActionListener(e -> {
            System.out.println("Inicio");
            start();
            startButton.setEnabled(false);  
            pauseButton.setEnabled(true);   
            resumeButton.setEnabled(false);
        });
        pauseButton = new JButton("Pausar");
        pauseButton.addActionListener(e -> {
            System.out.println("Pausa");
            paused();
            pauseButton.setEnabled(false);   
            resumeButton.setEnabled(true);
        });
        resumeButton = new JButton("Reanudar");
        resumeButton.addActionListener(e -> {
            System.out.println("Volver");
            resume();
            pauseButton.setEnabled(true);   
            resumeButton.setEnabled(false);
        });

        actionsBPabel.add(startButton);
        actionsBPabel.add(pauseButton);
        actionsBPabel.add(resumeButton);
        frame.add(actionsBPabel,BorderLayout.SOUTH);

    }

    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {
        for (int i = 0; i != MAX_THREADS; i++) {
            
            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
        }
        frame.setVisible(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await(); 
                    System.out.println("Todas las serpientes han muerto.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++) {
            System.out.println("["+i+"] :"+thread[i].getState());
        }
    }

    private void start(){
        for (int i = 0; i != MAX_THREADS; i++) {
            thread[i].start();
            System.out.println("Serpiente " + (i + 1) + " iniciada.");
        }
    }

    private void paused(){
        for (int i = 0; i != MAX_THREADS; i++) {
            snakes[i].pausedThread();
            System.out.println("Serpiente " + (i + 1) + " detenida.");
        }

    }

    private void resume(){
        for (int i = 0; i != MAX_THREADS; i++) {
            synchronized (snakes[i]) {  
                snakes[i].reiniceThread(); 
            }
            System.out.println("Serpiente " + (i + 1) + " reanudada.");
        }
    }

    private int snakedLogest(){
        int[] longest = new int[MAX_THREADS];
        for (int i = 0; i != MAX_THREADS; i++) {
            synchronized (snakes[i]) {  
                longest[i] = snakes[i].getSize(); 
            }  
        }
        int[] result = findMaxAndIndex(longest);
        return snakes[1].getIdt();
    }

    private int[] findMaxAndIndex(int[] array) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException("El arreglo está vacío o es nulo.");
        }
    
        int maxIndex = 0; // Índice del número mayor
        int maxValue = array[0]; // Valor del número mayor
    
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
                maxIndex = i;
            }
        }
    
        return new int[]{maxValue, maxIndex}; // Retorna {valor máximo, índice}
    }
    

    public static SnakeApp getApp() {
        return app;
    }

}
