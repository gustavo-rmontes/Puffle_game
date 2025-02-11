// Autores:
// 1 - Gustavo Ribeiro Montes, RA: 211024899
// 2 - Maria Vitoria Brito, RA: 211021164 

// criar a class Comida para poder mudar a comX, comY, dy

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

class Comida{
    int comX, comY=0, comDX, comDY;
    Random random = new Random();
    Comida(){
        comX = random.nextInt(700);
    }
}

public class Jogo extends JFrame{
    // Constantes para o vetor de imagens a serem inseridas
    final int FUNDO = 0;
    final int PRETO = 1;
    final int ESTADO_AZUL = 2;
    final int ESTADO_COME = 3;
    final int COMIDA = 4;

    List<Comida> listaComida = new LinkedList<Comida>();

    int estado = ESTADO_AZUL; // controla as transições de imagem do jogador 1
    int posX = 10; // controla a posição horizontal do jogador
    final int posXPlayer2 = 710; // posição constante do jogador 2

    Image img[] = new Image[20];
    Timer t;
    Desenho des = new Desenho();
    
    class Desenho extends JPanel{
        Desenho(){
            try{
                setPreferredSize(new Dimension(828, 467));
                // Tela
                img[FUNDO] = ImageIO.read(new File("figuras/fundo.jpg"));
                // Alvo (comida)
                img[COMIDA] = ImageIO.read(new File("figuras/food1.png"));
                // Personagens e suas transições
                img[PRETO] = ImageIO.read(new File("figuras/puffle.png"));
                img[ESTADO_AZUL] = ImageIO.read(new File("figuras/puffle_azul.png"));
                img[ESTADO_COME] = ImageIO.read(new File("figuras/azul_come.png"));
            } catch(IOException e){
                JOptionPane.showMessageDialog(this, "Erro no carregamento da imagem\n"+e, "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            // Tela (fundo)
            g.drawImage(img[FUNDO], 0, 0, getSize().width, getSize().height, this);
            // Jogador 1
            g.drawImage(img[estado], posX, getSize().height - img[estado].getHeight(this) - 10, this);
            // Jogador 2 (constante)
            g.drawImage(img[PRETO], posXPlayer2, getSize().height - img[PRETO].getHeight(this) - 10, this);
            // Gera os alvos (comidas)
            for(Comida c: listaComida){
                g.drawImage(img[COMIDA], c.comX, c.comY, this);
            }
            Toolkit.getDefaultToolkit().sync();    
        }
    }

    
    // Função que decrementa a posição y do alvo (comida)
    public void inc() {
        for(Comida c: listaComida){
            c.comY++;
        }
    }
    
    int contaIntervalo = 0;
    
    void controlaComida(){
        if(contaIntervalo++ > 30){
            contaIntervalo = 0;
            listaComida.add(new Comida());
        }
        for(Iterator<Comida> it = listaComida.iterator(); it.hasNext();){
            Comida c = it.next(); 
            if(c.comY > getHeight()){
                it.remove();
            }
        }
    }

    // Funções de movimentação
    void andaPDireita(){
        if(estado == ESTADO_COME)
            estado = ESTADO_AZUL;
        posX += 10;
        repaint();
    }
    void andaPEsquerda(){
        if(estado == ESTADO_COME)
            estado = ESTADO_AZUL;
        posX -= 10;
        repaint();
    }
    void comeFruta(){
        if(estado == ESTADO_AZUL)
            estado = ESTADO_COME;
        else if(estado == ESTADO_COME)
            estado = ESTADO_AZUL;
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
            else if(e.getKeyCode() == KeyEvent.VK_C){ // Tecla 'C'
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
        
        // Timer para que o alvo (comida) desça
        t = new Timer(30, new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                controlaComida();
                inc();
                repaint();
            }
        });
        t.start();
    }

    static public void main(String args[]){
        new Jogo();
    }
}