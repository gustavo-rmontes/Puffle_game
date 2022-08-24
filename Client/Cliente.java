// Autores:
// 1 - Gustavo Ribeiro Montes, RA: 211024899
// 2 - Maria Vitoria Brito, RA: 211021164 

import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;
import java.util.List;
import java.util.Random;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;


public class Cliente extends JFrame {
  Rectangle[] rect;
  Rede rede = new Rede();
  Ambiente ambiente;

  Cliente() {
    super("Puffle Game");
    ambiente = new Ambiente(this);
    rede.recebeDadosIniciais(ambiente);
    
    iniciaJanela();
    tratamentoTeclas();
    controleDoJogo();
  }

  void iniciaJanela() {    
    add(ambiente);
    pack();
    setResizable(false);
    setVisible(true);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
  
  void tratamentoTeclas() {
    addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {
          rede.enviaComandosEDadosDoJogador(Jogador.ESTADO_AZUL);
        } else if (e.getKeyCode() == KeyEvent.VK_C) {
          rede.enviaComandosEDadosDoJogador(Jogador.ESTADO_COME);
        } 
      }
    });
  }
  
  void controleDoJogo() {
    new Thread(new Runnable() {
      public void run() {
        while (true) {
          rede.recebeSituacao(ambiente.jogador);
          rede.recebeSituacao(ambiente.adversario);
          rede.recebePlacar(ambiente);
          ambiente.repaint();
        }
      }
    }).start();
  }
  
  static public void main(String[] args) {
    new Cliente();
  }
}

class Posicao {
    int x;
    int y;
  
    Posicao(int x, int y) {
      this.x = x;
      this.y = y;
    }
}

class Rede{
    DataOutputStream os = null;
    Socket socket = null;
    DataInputStream is = null;
    Boolean temDados = false;

    Rede() {
        try {
            socket = new Socket("127.0.0.1", 8080);
            os = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());
        } catch (UnknownHostException e) {
            Mensagem.erroFatalExcecao("Servidor desconhecido!", e);
        } catch (IOException e) {
            Mensagem.erroFatalExcecao("A conexao com o servidor não pode ser criada!", e);
        }
    }

    public void enviaComandosEDadosDoJogador(int estado) {
        try {
            os.writeInt(estado);
            os.flush();
        } catch (IOException e) {
            Mensagem.erroFatalExcecao("A imagem não pode ser enviada!", e);
        }
    }
    public void enviaDadosDaComida(int comX, int comY) {
        try {
            os.writeInt(comX);
            os.writeInt(comY);
            os.flush();
        } catch (IOException e) {
            Mensagem.erroFatalExcecao("A imagem não pode ser enviada!", e);
        }
    }

    public void recebeSituacao(Jogador jogador) {
        try {
            // System.out.print("pos ");
            jogador.posicao(is.readInt());
            // System.out.print(jogador.x + " est ");
            jogador.estado(is.readInt());
            // System.out.print(jogador.estado + " inv ");
            jogador.inverte(is.readBoolean());
            // System.out.println(jogador.invertido + " Ok ");
        } catch (IOException e) {
            Mensagem.erroFatalExcecao("Jogo terminado pelo servidor.", e);
        }
    }

    public void recebePlacar(Ambiente ambiente) {
        try {
            ambiente.jogador.pontos = is.readInt();
            ambiente.adversario.pontos = is.readInt();
        } catch (IOException e) {
            Mensagem.erroFatalExcecao("Jogo terminado pelo servidor.", e);
        }
    }

    public void recebeDadosIniciais(Ambiente ambiente) {
        try {
            int largJogo = is.readInt();
            int altuJogo = is.readInt();
            ambiente.jogador.posicao(50, altuJogo - 15);
            ambiente.adversario.posicao(largJogo - 50, altuJogo - 15);
            ambiente.ajustaTamanho(largJogo, altuJogo);
        } catch (IOException e) {
            Mensagem.erroFatalExcecao(e);
        }
    }
}

class Ambiente extends JPanel{
    static final int FUNDO = 0; 
    static final int COMIDA = 1; 

    int largJogo = 828;
    int altuJogo = 467;
    JogadorDesenho jogador;
    JogadorDesenho adversario;
    List<ComidaDesenho> listaComida;
    Image[] imagens = new Image[3];

    Ambiente(Cliente jogo) {
        carregaImagens();
        jogador = new JogadorDesenho(jogo);
        adversario = new JogadorDesenho(jogo);
        listaComida = new LinkedList<ComidaDesenho>();
        adversario.inverte(true);
    }

    void carregaImagens() {
        try {
            imagens[FUNDO] = ImageIO.read(new File("figuras/fundo.jpg"));
            imagens[COMIDA] = ImageIO.read(new File("figuras/food1.png"));
        } catch (IOException e) {
            Mensagem.erroFatalExcecao(this, "A imagem não pôde ser carregada!", e);
        }
    }

    public void ajustaTamanho(int largJogo, int altuJogo) {
        this.largJogo = largJogo;
        this.altuJogo = altuJogo;
        setPreferredSize(new Dimension(largJogo, altuJogo));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clipRect(5, 5, getWidth() - 10, getHeight() - 10);

        g.drawImage(imagens[FUNDO], 0, 0, largJogo, altuJogo, this);

        // Gera os alvos (comidas)
        for(Comida c: listaComida){
            g.drawImage(imagens[COMIDA], c.comX, c.comY, this);
        }

        jogador.desenha(g);
        adversario.desenha(g);

        desenhaPlacar(g);

        Toolkit.getDefaultToolkit().sync();
    }

    void desenhaPlacar(Graphics g) {
        Font f = new Font("Arial", Font.BOLD, 50);
        g.setFont(f);
        String s = jogador.pontos + " x " + adversario.pontos;
        FontMetrics fm = g.getFontMetrics();
        int x = (largJogo - fm.stringWidth(s)) / 2;
        g.drawString(s, x - 1, fm.getHeight() - 1);
        g.setColor(Color.BLUE);
        g.drawString(s, x + 1, fm.getHeight() + 1);
    }
}

class Tamanho {
    int larg, altu;
  
    Tamanho(int largura, int altura) {
      larg = largura;
      altu = altura;
    }
}

class Comida{
    int comX, comY=0, comDX, comDY;
    Random random = new Random();
    Rectangle rectColisao;
    Tamanho tam;
    Comida(){
        /** tamanho obtido das imagem do alvo(comida)*/
        tam = new Tamanho(25, 25);
        comX = random.nextInt(700);
        rectColisao = new Rectangle(comX, comY, tam.larg, tam.altu);
    }
}

class ComidaDesenho extends Comida{
    JFrame janelaDona;
    Image imagemComida;

    ComidaDesenho(JFrame janelaDona) {
        this.janelaDona = janelaDona;
        carregaImagens();
    }

    void carregaImagens() {
    try {
        imagemComida = ImageIO.read(new File("figuras/food1.png"));
    } catch (IOException e) {
        Mensagem.erroFatalExcecao(janelaDona, "A imagem não pôde ser carregada!", e);
        }
    }

    void desenha(Graphics g) {
        g.drawImage(imagemComida, comX, comY, janelaDona);
        g.drawRect(rectColisao.x, rectColisao.y, rectColisao.width, rectColisao.height);
    }
}

class Jogador {
    static final int ESTADO_AZUL = 0;
    static final int ESTADO_COME = 1;

    int x, y; // o y indica a posicao do pe' do jogador
    /** contem a posicao onde a imagem sera desenhada */
    Rectangle rectColisao;
    /** indica se a imagem deve ser desenhada invertida */
    boolean invertido = false;
    int estado = ESTADO_AZUL;
    /** tamanhos obtidos das imagens do jogador */
    Tamanho[] tamanho = new Tamanho[2];
    /** deslocamento até o centro da imagem */
    int dxCentroJogador = 32;
    /** pontos marcados */
    int pontos = 0;

    Jogador() {
        inicia();
    }

    Jogador(int x, int y) {
        inicia();
        posicao(x, y);
    }

    void inicia() {
        defineTamanhoJogador();
        rectColisao = new Rectangle(0, 0, tamanho[estado].larg, tamanho[estado].altu);
        estado(ESTADO_AZUL);
    }

    void defineTamanhoJogador() {
        tamanho[ESTADO_AZUL] = new Tamanho(50, 100);
        tamanho[ESTADO_COME] = new Tamanho(25, 50);
    }

    void inverte(boolean invertido) {
        this.invertido = invertido;
        posicao(x);
    }

    void estado(int estado) {
        this.estado = estado;
        rectColisao.setSize(tamanho[estado].larg, tamanho[estado].altu);
        posicao(x, y); // se invertido, ajusta a nova posicao do retangulo
    }

    void posicao(int x, int y) {
        this.y = y;
        rectColisao.y = y - rectColisao.height; // faz o y ser o pe' da figura
        posicao(x);
    }

    void posicao(int x) {
        this.x = x;
        if (invertido) {
            rectColisao.setLocation(x - tamanho[estado].larg + dxCentroJogador, rectColisao.y);
        } else {
            rectColisao.setLocation(x - dxCentroJogador, rectColisao.y);
        }
    }

    boolean verificaColisao(Comida j) {
        return rectColisao.intersects(j.rectColisao);
    }
}  

class JogadorDesenho extends Jogador{
    JFrame janelaDona;
    Image[] imagens = new Image[2];
    /** mostra pequeno movimento para avatar parecer vivo */
    int pequenoMovimento = 0;

    JogadorDesenho(JFrame janelaDona) {
        this.janelaDona = janelaDona;
        carregaImagens();
    }

    JogadorDesenho(int x, int y, JFrame janelaDona) {
        super(x, y);
        this.janelaDona = janelaDona;
        carregaImagens();
    }

    void carregaImagens() {
    try {
        imagens[ESTADO_AZUL] = ImageIO.read(new File("figuras/puffle_azul.png"));
        imagens[ESTADO_COME] = ImageIO.read(new File("figuras/azul_come.png"));
    } catch (IOException e) {
        Mensagem.erroFatalExcecao(janelaDona, "A imagem não pôde ser carregada!", e);
        }
    }

    void desenha(Graphics g) {
        int larg = rectColisao.width;
        int x = rectColisao.x;
        if (invertido) {
            larg = -larg;
            x -= larg;
        }
        g.drawImage(imagens[estado], x, rectColisao.y - pequenoMovimento, larg, rectColisao.height + pequenoMovimento,
            janelaDona);
        g.drawRect(rectColisao.x, rectColisao.y, rectColisao.width, rectColisao.height);
    }
}

class Mensagem{
    static void erroFatalExcecao(String msg, Exception ex) {
        erroFatalExcecao(null, msg, ex);
    }

    static void erroFatalExcecao(Exception ex) {
        erroFatalExcecao(null, null, ex);
    }

    static void erroFatalExcecao(Component janela, String msg, Exception ex) {
        StringWriter str = new StringWriter();
        ex.printStackTrace(new PrintWriter(str));
        if (msg == null) {
            msg = str.toString();
        } else {
            msg += "\n" + str;
        }
        JOptionPane.showMessageDialog(janela, msg, "Erro", JOptionPane.ERROR_MESSAGE);
        System.exit(10);
    }
}