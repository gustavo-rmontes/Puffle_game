// Autores:
// 1 - Gustavo Ribeiro Montes, RA: 211024899
// 2 - Maria Vitoria Brito, RA: 211021164 

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.awt.Rectangle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Random;

public class Servidor{

    Servidor() {
        ServerSocket serverSocket;

        serverSocket = iniciaServer(8080);

        while (true) {
            Jogo jogo = new Jogo(2);
            int numMaxPlayers = jogo.numMaxPlayers();
            for (int i = 0; i < numMaxPlayers; i++) {
                Socket clientSocket = esperaCliente(serverSocket);
                jogo.addPlayer(clientSocket);
            }
            jogo.iniciaLogica(new Logica(jogo));
            jogo.inicia();
        }
    }

    ServerSocket iniciaServer(int porto) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(porto);
        } catch (IOException e) {
            System.out.println("Não pode escutar o porto: " + porto + "\n" + e);
            System.exit(1);
        }
        return serverSocket;
    }

    Socket esperaCliente(ServerSocket serverSocket) {
        Socket clientSocket = null;
        try {
            System.out.println("Esperando conexao de um jogador");
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Accept falhou: " + serverSocket.getLocalPort() + "\n" + e);
            System.exit(1);
        }
        System.out.println("Accept funcionou");
        return clientSocket;
    }

    public static void main(String args[]) {
        new Servidor();
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

class Jogo {
    static final int LARG_JOGO = 828;
    static final int ALT_JOGO = 467;
    int jogadoresConectados = 0;
    DataOutputStream[] os;
    DataInputStream[] is;
    Jogador[] players;
    int[] pontos = {0, 0};
    boolean continua = true;
    Logica logica;

    Jogo(int numMaxPlayers) {
        os = new DataOutputStream[numMaxPlayers];
        is = new DataInputStream[numMaxPlayers];
        players = new Jogador[numMaxPlayers];
    }

    public void addPlayer(Socket clientSocket) {
        iniciaPlayer(jogadoresConectados, clientSocket);
        enviaDadosIniciais(jogadoresConectados);
        iniciaThreadJogadorRecebe(jogadoresConectados);
        jogadoresConectados++;
    }

    public int numMaxPlayers() {
        return players.length;
    }

    public void iniciaPlayer(int numPlayer, Socket clientSocket) {
        try {
            os[numPlayer] = new DataOutputStream(clientSocket.getOutputStream());
            is[numPlayer] = new DataInputStream(clientSocket.getInputStream());
            if(numPlayer == 0) {
                players[numPlayer] = new Jogador(50, ALT_JOGO - 15);
            } else {
                players[numPlayer] = new Jogador(LARG_JOGO - 50, ALT_JOGO - 15);
                players[numPlayer].inverte(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
            continua = false;
        }
    }

    public void iniciaLogica(Logica logica) {
        this.logica = logica;
    }

    public void inicia() {
        iniciaThreadJogoEnvio();
    }

    public void enviaDadosIniciais(int numPlayerDestino) {
        try {
            os[numPlayerDestino].writeInt(LARG_JOGO);
            os[numPlayerDestino].writeInt(ALT_JOGO);
        } catch (IOException e) {
            e.printStackTrace();
            continua = false;
        }
    }

    public void recebeComandosEDadosDoJogador(int numDoJogador) {
        try {
          players[numDoJogador].estado = is[numDoJogador].readInt();
          // System.out.println("estado: " + jogadores[numDoJogador].estado);
        } catch (IOException e) {
          e.printStackTrace();
          continua = false;
        }
    }

    public void iniciaThreadJogoEnvio() {
        new Thread(new Runnable() {
            public void run() {
                while (continua) {
                    logica.executa();

                    enviaSituacao(0);
                    enviaSituacao(1);
                    enviaPlacar();

                    enviaSituacaoInvertido(1);
                    enviaSituacaoInvertido(0);
                    enviaPlacarInvertido();

                    forcaEnvio();
                    try {
                        Thread.sleep(200);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    void iniciaThreadJogadorRecebe(int numDoPlayer) {
        new Thread(new Runnable() {
            int numPlayerEsperando = numDoPlayer;

            public void run() {
                while (continua) {
                    recebeComandosEDadosDoJogador(numPlayerEsperando);
                }
            }
        }).start();
    }

    public void enviaSituacao(int numDoPlayer) {
        try{
            os[0].writeInt(players[numDoPlayer].x);
            os[0].writeInt(players[numDoPlayer].estado);
            os[0].writeBoolean(players[numDoPlayer].invertido);
        } catch (IOException e) {
            e.printStackTrace();
            continua = false;
        }
    }   

    public void enviaSituacaoInvertido(int numDoPlayer) {
        try {
            os[1].writeInt(LARG_JOGO - players[numDoPlayer].x);
            os[1].writeInt(players[numDoPlayer].estado);
            os[1].writeBoolean(!players[numDoPlayer].invertido);
        } catch (IOException e) {
            e.printStackTrace();
            continua = false;
        }
    }

    public void enviaPlacar() {
        try {
            os[0].writeInt(players[0].pontos);
            os[0].writeInt(players[1].pontos);
        } catch (IOException e) {
            e.printStackTrace();
            continua = false;;
        }
    }

    public void enviaPlacarInvertido() {
        try {
            os[1].writeInt(players[1].pontos);
            os[1].writeInt(players[0].pontos);
        } catch (IOException e) {
            e.printStackTrace();
            continua = false;
        }
    }
        
    void forcaEnvio() {
        try {
            os[0].flush();
            os[1].flush();
        } catch (IOException e) {
        }
    }

}

class Logica {
    Jogo jogo;
    Jogador jogador;
    Jogador adversario;
    List<Comida> listaComida;
  
    Logica(Jogo jogo) { 
      this.jogo = jogo;
      jogador = this.jogo.players[0];
      adversario = this.jogo.players[1];
    }
  
    public void executa() {
      verificaAnda(jogador, 10);
      verificaAnda(adversario, -10);
      trataColisao();
    }
  
    boolean trataColisaoLimites(Jogador jogador) {
      if (jogador.x + jogador.rectColisao.width + 5 > Jogo.LARG_JOGO) {
        jogador.posicao(Jogo.LARG_JOGO - jogador.rectColisao.width - 5);
        return true;
      } else if (jogador.x < 5) {
        jogador.posicao(5);
        return true;
      }
      return false;
    }

    boolean verificaColisaoComida(Jogador jogador){
        for(Comida c: listaComida){
            if(jogador.verificaColisao(c)){
                return true;
            }
        }
        return false;
    }

    void trataColisao() {
        if (verificaColisaoComida(jogador)){
            // atualizaPlacar(jogador);
            if (jogador.estado == Jogador.ESTADO_COME) {
                jogador.pontos += 5;
            }
        }
        if(verificaColisaoComida(adversario)){
            // atualizaPlacar(adversario);
            if (adversario.estado == Jogador.ESTADO_COME) {
                adversario.pontos += 5;
            }
        }
    }
  
    void verificaAnda(Jogador jogador, int tamPasso) {
      if (jogador.estado == Jogador.ESTADO_COME) {
        jogador.posicao(jogador.x + tamPasso);
      }
    }
}
  