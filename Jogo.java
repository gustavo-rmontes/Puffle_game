import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

class Jogo extends JFrame{
    final int FUNDO = 0;
    final int PRETO = 1;
    final int AZUL = 2;
    int estado = AZUL;


    Image img[] = new Image[20];
    int posX = 10;
    Timer timer;
    Desenho des = new Desenho();

    class Desenho extends JPanel{

        Desenho(){
            try{
                setPreferredSize(new Dimension(828, 467));
                img[FUNDO] = ImageIO.read(new File("fundo.jpg"));
                img[PRETO] = ImageIO.read(new File("puffle.png"));
                img[AZUL] = ImageIO.read(new File("puffle_azul.png"));
            } catch(IOException e){
                JOptionPane.showMessageDialog(this, "Erro no carregamento da imagem\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(img[FUNDO], 0, 0, getSize().width, getSize().height, this);
            g.drawImage(img[AZUL], posX, getSize().height - img[AZUL].getHeight(this) - 10, this);
            g.drawImage(img[PRETO], posX, getSize().height - img[AZUL].getHeight(this) - 10, this);
            Toolkit.getDefaultToolkit().sync();    
        }
    }

    Jogo(){
        super("Puffle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(des);
        pack();
        setVisible(true);
    }

    static public void main(String args[]){
        Jogo a = new Jogo();
    }

}
