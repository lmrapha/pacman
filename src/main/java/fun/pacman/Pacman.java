package fun.pacman;

import javax.swing.*;

public class Pacman extends JFrame {

    /**
     * @author lmr
     */

    public Pacman() {
        add(new Model());
    }


    public static void main(String[] args) {
        Pacman pac = new Pacman();
        pac.setVisible(true);
        pac.setTitle("Pacman Game");
        pac.setSize(380,420);
        pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
        pac.setLocationRelativeTo(null);

    }
}
