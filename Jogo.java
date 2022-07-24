import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;

class Jogo extends JFrame{
    // Constantes para o vetor de imagens a serem inseridas
    final int FUNDO = 0;
    final int PRETO = 1;
    final int AZUL = 2;
    final int COME = 3;

    int estado = AZUL; 

    Image img[] = new Image[20];
    int posX = 10; // controla a posição horizontal do jogador
    Timer timer;
    Desenho des = new Desenho();
    
    class Desenho extends JPanel{

        Desenho(){
            try{
                setPreferredSize(new Dimension(828, 467));
                img[FUNDO] = ImageIO.read(new File("fundo.jpg"));
                img[PRETO] = ImageIO.read(new File("puffle.png"));
                img[AZUL] = ImageIO.read(new File("puffle_azul.png"));
                // img[COME] = ImageIO.read(new File("azul_come.jpg"));
            } catch(IOException e){
                JOptionPane.showMessageDialog(this, "Erro no carregamento da imagem\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(img[FUNDO], 0, 0, getSize().width, getSize().height, this);
            g.drawImage(img[AZUL], posX, getSize().height - img[AZUL].getHeight(this) - 10, this);
            g.drawImage(img[PRETO], posX+700, getSize().height - img[AZUL].getHeight(this) - 10, this);
            Toolkit.getDefaultToolkit().sync();    
        }
    }

    void andaPDireita(){
        // if(estado == COME){
        //     estado = AZUL;
        // }
        posX += 10;
        repaint();
    }
    void andaPEsquerda(){
        // if(estado == COME){
        //     estado = AZUL;
        // }
        posX -= 10;
        repaint();
    }
    // void comeFruta(){
    //     if(estado == AZUL){
    //         estado = COME;
    //     }
    // }

    class TrataTeclas extends KeyAdapter{ // classe que trata o movimento das teclas
        public void KeyPressed(KeyEvent e){
            if(e.getKeyCode() == KeyEvent.VK_RIGHT) { // Tecla '->'
                // estado = AZUL;
                andaPDireita();
            }
            else if(e.getKeyCode() == KeyEvent.VK_LEFT){ // Tecla '<-'
                // estado = AZUL;
                andaPEsquerda();
            }
            // else if(e.getKeyCode() == KeyEvent.VK_S){ // Tecla 'S'
            //     estado = COME;
            //     comeFruta();
            // }
        }
    }

    Jogo(){
        super("Puffle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(des);
        pack();
        setVisible(true);
        addKeyListener(new TrataTeclas()); // classe que trata os eventos ligados às teclas
        timer = new Timer(100, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                andaPDireita();
                andaPEsquerda();
                // comeFruta();
            }
        });
        timer.start();
    }

    static public void main(String args[]){
        Jogo a = new Jogo();
    }

}
