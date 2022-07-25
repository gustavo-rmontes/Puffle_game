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

    int estado = AZUL; // controla as transições de imagem do jogador 1
    int posX = 10; // controla a posição horizontal do jogador
    final int posXPlayer2 = 710; // posição constante do jogador 2

    Image img[] = new Image[20];
    Timer timer;
    Desenho des = new Desenho();
    
    class Desenho extends JPanel{

        Desenho(){
            try{
                setPreferredSize(new Dimension(828, 467));
                img[FUNDO] = ImageIO.read(new File("fundo.jpg"));
                img[PRETO] = ImageIO.read(new File("puffle.png"));
                img[AZUL] = ImageIO.read(new File("puffle_azul.png"));
                img[COME] = ImageIO.read(new File("azul_come.jpg"));
            } catch(IOException e){
                JOptionPane.showMessageDialog(this, "Erro no carregamento da imagem\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(img[FUNDO], 0, 0, getSize().width, getSize().height, this);
            g.drawImage(img[estado], posX, getSize().height - img[estado].getHeight(this) - 10, this);
            g.drawImage(img[PRETO], posXPlayer2, getSize().height - img[PRETO].getHeight(this) - 10, this);
            Toolkit.getDefaultToolkit().sync();    
        }
    }

    void andaPDireita(){
        if(estado == COME)
            estado = AZUL;
        posX += 10;
        repaint();
    }
    void andaPEsquerda(){
        if(estado == COME)
            estado = AZUL;
        posX -= 10;
        repaint();
    }
    void comeFruta(){
        if(estado == AZUL)
            estado = COME;
        repaint();
    }

    class TrataTeclas extends KeyAdapter{ // classe que trata o movimento das teclas
        public void keyPressed(KeyEvent e){
            if(e.getKeyCode() == KeyEvent.VK_RIGHT) { // Tecla '->'
                andaPDireita();
            }
            else if(e.getKeyCode() == KeyEvent.VK_LEFT){ // Tecla '<-'
                andaPEsquerda();
            }
            else if(e.getKeyCode() == KeyEvent.VK_A){ // Tecla 'A'
                comeFruta();
            }
        }
    }

    Jogo(){
        super("Puffle");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(des);
        pack();
        setVisible(true);
        addKeyListener(new TrataTeclas()); // classe que trata os eventos ligados às teclas
        // timer = new Timer(100, new ActionListener(){
        //     @Override
        //     public void actionPerformed(ActionEvent ae){
        //         andaPDireita();
        //         andaPEsquerda();
        //         comeFruta();
        //     }
        // });
    }

    static public void main(String args[]){
        Jogo a = new Jogo();
    }

}