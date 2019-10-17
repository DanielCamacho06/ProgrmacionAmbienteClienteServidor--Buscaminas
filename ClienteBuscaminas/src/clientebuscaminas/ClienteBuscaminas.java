
package clientebuscaminas;

import com.sun.java.accessibility.util.AWTEventMonitor;
import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


public class ClienteBuscaminas implements ActionListener,MouseListener,WindowListener {
    
    //public static int tamañox=10,tamañoy=10;3
    public static JButton[][] botones; 
    public static Scanner in;
    public static PrintWriter out;
    public static String numJugador;
    public static boolean sigoJugando = true;    
    public static boolean primerClick = true;
    public static boolean botonesPrimerClick = false;
    public static int tamañoTablerox;
    public static int tamañoTableroy;
    public DefaultListModel modelo= new DefaultListModel();
    JFrame pantallaInicial;
     Socket socket;
    JFrame frameBotones;
    
    //ventana inicial 
    public void pantallaInicial(){          
        pantallaInicial = new JFrame();
        JList lista= new JList();
        lista.setModel(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel panel = new JPanel();
        panel.add(lista);
        JPanel panelBton = new JPanel();
        JButton boton=new JButton("Iniciar");
        boton.setName("Iniciar");
        boton.addActionListener(this);
        panelBton.add(boton);
        FlowLayout flow = new FlowLayout();
        flow.setHgap(20);
        pantallaInicial.setLayout(flow);
        pantallaInicial.add(panel);
        pantallaInicial.add(panelBton);
        pantallaInicial.setMinimumSize(new Dimension(240, 240));
        pantallaInicial.pack();
        pantallaInicial.setLocationRelativeTo(null);
        pantallaInicial.setVisible(true);
        pantallaInicial.addWindowListener(this);
        pantallaInicial.setTitle("Buscaminas");
        pantallaInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    //dibuja el tablero en la pantalla
    public void dibujarTablero(int x,int y){
        //verifica el tamaño minimo del tablero
        if(x>6 && y>6){
        frameBotones= new JFrame();
        JPanel panel = new JPanel();
        //contstruye el arreglo de botones del tamañod deseado
        botones= new JButton[x][y];
            
        //se le pone nombre a cada boton de la matriz y se le agregan propiedades
        for (int i = 0; i < x; i++) {
            for (int z = 0; z < y; z++) {
                botones[i][z]= new JButton();
                botones[i][z].setVisible(true);
                botones[i][z].setName(i+","+z);
                botones[i][z].addActionListener(this);
                botones[i][z].addMouseListener(this);
                
                if(numJugador.equalsIgnoreCase("1")){
                    if(i == 0){
                        botones[i][z].setBackground(Color.blue);
                    }
                }
                if(numJugador.equalsIgnoreCase("2")){
                    if(z == y-1){
                        botones[i][z].setBackground(Color.red);
                    }
                }
                if(numJugador.equalsIgnoreCase("3")){
                    if(i == x-1){
                        botones[i][z].setBackground(Color.pink);
                    }
                }
                if(numJugador.equalsIgnoreCase("4")){
                    if(z == 0){
                        botones[i][z].setBackground(Color.green);
                    }
                }
                
                panel.add(botones[i][z]);
            }  
        }
        panel.setVisible(true);
        //Se establece la distrubucion visual de los botones
        panel.setLayout(new GridLayout(x, y));      
        frameBotones.add(panel);
        //se establece el tamaño minimo de el frame
        frameBotones.setMinimumSize(new Dimension(720,720));
        frameBotones.pack();
        frameBotones.setVisible(true);
        frameBotones.setLocationRelativeTo(null);
        frameBotones.addWindowListener(this);
        }else{
            JOptionPane.showMessageDialog(null, "Para tener una buena experiencia en el juego \nEl tamaño minimo es 7x7", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void run() throws IOException{
        try{
            socket = new Socket("127.0.0.1",2001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            pantallaInicial();
            while(in.hasNextLine()){                  
                String line = in.nextLine();
                
                if(line.startsWith("NUEVOJUGADOR")){
                   modelo.removeAllElements();
                   int indiceInicio=13;
                   do{
                     modelo.addElement("Jugador "+line.substring(indiceInicio,indiceInicio+1));
                        indiceInicio+=3;
                   }while(indiceInicio<line.length()-1);
               }else if(line.startsWith("ACEPTADO")){
                    System.out.println("El servidor te ha aceptado...");
                    tamañoTablerox = Integer.valueOf(line.substring(line.indexOf("x")+1,line.indexOf(":")));
                    tamañoTableroy = Integer.valueOf(line.substring(line.indexOf("y")+1));
                    numJugador = line.substring(16,17);
                    out.println("Jugador " + line.substring(16,17) + " con tablero en pantalla");
                }else if(line.startsWith("INICIARPARTIDA")){
                   dibujarTablero(tamañoTablerox-2, tamañoTableroy-2);
                   frameBotones.setTitle("Buscaminas: eres el jugador "+numJugador);
                   pantallaInicial.setVisible(false);
                }else if(line.startsWith("ERRORINICIARPARTIDA")){
                    JOptionPane.showMessageDialog(null, "No pudiste iniciar la partida por falta de jugadores", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(line.startsWith("ESMINA")){
                    String resultado;
                    
                    
                    int x=Integer.parseInt(line.substring(line.indexOf("A")+1,line.indexOf(":")));
                    int y=Integer.parseInt(line.substring(line.indexOf(":")+1, line.indexOf("|")));
                    if(line.contains("-1")){
                        //ES MINA
                        minaExplota(x, y);
                        if(line.substring(line.length()-1).equalsIgnoreCase(numJugador)){
                            perdiste();
                        }
                    }else{
                        //NO ES MINA
                        resultado = line.substring(line.indexOf("|")+1,line.indexOf(" "));
                        numero(x, y, resultado);
                    }
                }else if(line.startsWith("SINBANDERAS")){
                    if(line.substring(line.length()-1).equalsIgnoreCase(numJugador)){
                        JOptionPane.showMessageDialog(null, "Te quedaste sin banderas." ,"Error", JOptionPane.ERROR_MESSAGE);
                    }
                }else if(line.startsWith("MODIFICANUMERO")){
                    int numero=Integer.valueOf(line.substring(line.indexOf("-")+1));
                    int numJug=Integer.valueOf(numJugador);
                    
                    if(numJug>numero){
                       numJug--;
                       numJugador=String.valueOf(numJug);
                       
                    }
                    out.println("00:00-NUEVONUMERO/"+numJugador);
                }else if(line.startsWith("ESCERO")){
                    String ceros = line.substring(line.indexOf(":")+1,line.indexOf("-"));
                    String numeros = line.substring(line.indexOf("-")+1);
                    reventarCeros(ceros);
                    reventarCeros(numeros);
                }else if(line.contains("FINPARTIDA")){
                    
                    
                    sigoJugando=false;
                    String resultados=line.substring(line.indexOf("-")+1);
                      int indiceInicialJugador=0;
                      int indiceInicial=2;
                      String n="";  
                      String j="";
                      
                      String mensaje="";
                    do{
                        j=resultados.substring(indiceInicialJugador,indiceInicial);
                        n=resultados.substring(indiceInicial,indiceInicial+2);
                        indiceInicialJugador+=4;
                        indiceInicial+=4;
                        if(!j.equalsIgnoreCase("JG")){
                           if(numJugador.equalsIgnoreCase(j.substring(1))){
                             mensaje+="Tu puntaje: "+n+"\n";    
                           }else if(j.equalsIgnoreCase("J1")){
                               mensaje+="Jugador 1: "+n+"\n";
                           }else if(j.equalsIgnoreCase("J2")){
                               mensaje+="Jugador 2: "+n+"\n";
                           }else if(j.equalsIgnoreCase("J3")){
                               mensaje+="Jugador 3: "+n+"\n";
                           }else if(j.equalsIgnoreCase("J4")){
                               mensaje+="Jugador 4: "+n+"\n";
                           } 
                        }
                        
                      }while(indiceInicial<resultados.length()-1);
                   JOptionPane.showMessageDialog(null, "Todas las minas han sido encontradas\nPuntajes:\n"+mensaje, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
                }else if(line.contains("NOVIVOS")){
                    
                    sigoJugando=false;
                    String resultados=line.substring(line.indexOf("-")+1);
                    int indiceInicialJugador=0;
                    int indiceInicial=2;
                    String n="";  
                    String j="";
                    
                    String mensaje="";
                    do{
                        j=resultados.substring(indiceInicialJugador,indiceInicial);
                        n=resultados.substring(indiceInicial,indiceInicial+2);
                        indiceInicialJugador+=4;
                        indiceInicial+=4;
                        if(!j.equalsIgnoreCase("JG")){
                           if(numJugador.equalsIgnoreCase(j.substring(1))){
                             mensaje+="Tu puntaje: "+n+"\n";    
                           }else if(j.equalsIgnoreCase("J1")){
                               mensaje+="Jugador 1: "+n+"\n";
                           }else if(j.equalsIgnoreCase("J2")){
                               mensaje+="Jugador 2: "+n+"\n";
                           }else if(j.equalsIgnoreCase("J3")){
                               mensaje+="Jugador 3: "+n+"\n";
                           }else if(j.equalsIgnoreCase("J4")){
                               mensaje+="Jugador 4: "+n+"\n";
                           } 
                        }
                        
                    }while(indiceInicial<resultados.length()-1);
                    
                   JOptionPane.showMessageDialog(null, "Todos los jugadores han perdido.\nPuntajes:\n"+mensaje, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
                }else{
                    
                    int x=Integer.parseInt(line.substring(0,line.indexOf(":")));
                    int y=Integer.parseInt(line.substring(line.indexOf(":")+1, line.indexOf("-")));
                    if(line.contains("PONER_BANDERA")){
                        String jugadorQuePusoLaBandera = line.substring(line.length()-1);
                        ImageIcon icono=new ImageIcon(trimLink()+"\\src\\img\\" + jugadorQuePusoLaBandera + ".png");
                        ImageIcon alTamaño= new ImageIcon(icono.getImage().getScaledInstance(botones[x][y].getSize().height, botones[x][y].getSize().width, java.awt.Image.SCALE_DEFAULT));
                        botones[x][y].setIcon(alTamaño);
                    }
                    if(line.contains("QUITAR_BANDERA")){
                        botones[x][y].setIcon(null);
                    } 
                }
                /*
                else{
                    out.println("Error: jugador " + line.substring(17) + " sin tablero en pantalla");
                }*/
            }
        }catch(IOException e){
            
            System.out.println(e);
        }
    }
    
    
    
    public void perdiste(){
        JOptionPane.showMessageDialog(null, "Detonaste una mina", "Terminaste", JOptionPane.ERROR_MESSAGE);
        sigoJugando = false;
    }
    
    public void numero(int x,int y,String valor){
        botones[x][y].setEnabled(false);
        botones[x][y].setText(valor);
    }
    
    public void minaExplota(int x, int y){
        ImageIcon icono=new ImageIcon(trimLink()+"\\src\\img\\explota.png");
        ImageIcon alTamaño= new ImageIcon(icono.getImage().getScaledInstance(botones[x][y].getSize().height, botones[x][y].getSize().width, java.awt.Image.SCALE_DEFAULT));
        botones[x][y].setIcon(alTamaño);
        
    }
    //accion al presionar el boton izquierdo
    @Override
    public void actionPerformed(ActionEvent ae) {
        JButton boton =(JButton)ae.getSource();
       if(!boton.getName().equalsIgnoreCase("Iniciar")){
        try{
            if(sigoJugando == true){
                int posicionx=Integer.parseInt(boton.getName().substring(0,boton.getName().indexOf(",")));
                
                int posiciony=Integer.parseInt(boton.getName().substring(boton.getName().indexOf(",")+1));
                
                 if(primerClick){   
                    checarColores();
                 }
                if(primerClick && botonesPrimerClick){
                    
                    if(numJugador.equalsIgnoreCase("1") && posicionx==0){
                        if(posiciony < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posicionx < 10){
                            out.println("0" + posicionx + ":" + posiciony + "-");
                        }else if(posiciony < 10 && posicionx < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posiciony > 9 && posicionx > 9){
                            out.println(posicionx + ":" + posiciony + "-");
                        }
                        for(int y = 0; y < tamañoTableroy-2; y++){
                            botones[0][y].setBackground(new JButton().getBackground());
                        }
                        primerClick = false;
                    }
                   if(numJugador.equalsIgnoreCase("2") && posiciony==(tamañoTableroy-3)){
                        if(posiciony < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posicionx < 10){
                            out.println("0" + posicionx + ":" + posiciony + "-");
                        }else if(posiciony < 10 && posicionx < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posiciony > 9 && posicionx > 9){
                            out.println(posicionx + ":" + posiciony + "-");
                        }
                        for(int x = 0; x < tamañoTablerox-2; x++){
                            botones[x][tamañoTableroy-3].setBackground(new JButton().getBackground());
                        }
                        primerClick = false;
                   }
                   if(numJugador.equalsIgnoreCase("3") && posicionx==(tamañoTablerox-3)){
                        if(posiciony < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posicionx < 10){
                            out.println("0" + posicionx + ":" + posiciony + "-");
                        }else if(posiciony < 10 && posicionx < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posiciony > 9 && posicionx > 9){
                            out.println(posicionx + ":" + posiciony + "-");
                        }
                        for(int x = 0; x < tamañoTableroy-2; x++){
                            botones[tamañoTableroy-3][x].setBackground(new JButton().getBackground());
                        }
                        primerClick = false;
                   }
                   if(numJugador.equalsIgnoreCase("4") && posiciony==0){
                       
                        if(posiciony < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posicionx < 10){
                            out.println("0" + posicionx + ":" + posiciony + "-");
                        }else if(posiciony < 10 && posicionx < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posiciony > 9 && posicionx > 9){
                            out.println(posicionx + ":" + posiciony + "-");
                        }
                        for(int x = 0; x < tamañoTablerox-2; x++){
                            botones[x][0].setBackground(new JButton().getBackground());
                        }
                        primerClick = false;
                        
                   }
                }else{
                    if(botones[posicionx][posiciony].getIcon()==null){
                        //funciones del esqueleto
                        if(posicionx < 10 && posiciony < 10){
                            out.println("0" + posicionx + ":0" + posiciony + "-");
                        }else if(posicionx < 10){
                            out.println("0" + posicionx + ":" + posiciony + "-");
                        }else if(posiciony < 10){
                            out.println(posicionx + ":0" + posiciony + "-");
                        }else if(posicionx > 9 && posiciony > 9){
                            out.println(posicionx + ":" + posiciony + "-");
                        }
                      primerClick = false;
                    }
                }
            }
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(null, "Error al obtener la posicion presionada", "Error", JOptionPane.ERROR_MESSAGE);
        }
       }else{
           out.println("00:00-INICIARPARTIDA");
       }
    }
    
    public void checarColores(){
        if(numJugador.equalsIgnoreCase("1")){
            int y = 0;
            do{
                if(botones[0][y].isEnabled()){
                     botonesPrimerClick = true;
                    break;
                }else{
                    y++;
                }
            }while(y<tamañoTableroy-2);
        }else if(numJugador.equalsIgnoreCase("2")){
            int x = 0;
            do{
                if(botones[x][tamañoTableroy-3].isEnabled()){
                     botonesPrimerClick = true;
                    break;
                }else{
                    x++;
                }
            }while(x<tamañoTablerox-2);
        }else if(numJugador.equalsIgnoreCase("3")){
            int y = 0;
            do{
                if(botones[tamañoTablerox-3][y].isEnabled()){
                     botonesPrimerClick = true;
                    break;
                }else{
                    y++;
                }
            }while(y<tamañoTableroy-2);
        }else if(numJugador.equalsIgnoreCase("4")){
            int x = 0;
            do{
                if(botones[x][0].isEnabled()){
                     botonesPrimerClick = true;
                    break;
                }else{
                    x++;
                }
            }while(x<tamañoTablerox-2);
        }  
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    //accion al presionar el boton derecho
    @Override
    public void mousePressed(MouseEvent me) {
        if(sigoJugando == true){    
            //System.out.println("primerClick: -"+primerClick);
            //System.out.println("checarColores: -"+botonesPrimerClick);
            if(primerClick){   
              checarColores();
            }
            
            
                if(me.getButton()==MouseEvent.BUTTON3){
                    JButton boton = (JButton)me.getSource();
                    int posicionx=-1;
                    int posiciony=-1;
                    try{
                        posicionx=Integer.parseInt(boton.getName().substring(0,boton.getName().indexOf(",")));
                        posiciony=Integer.parseInt(boton.getName().substring(boton.getName().indexOf(",")+1));
                    }catch(NumberFormatException e){
                        JOptionPane.showMessageDialog(null, "Error al obtener la posicion presionada", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                    if(primerClick){   
                       checarColores();
                    }
                    if(primerClick && botonesPrimerClick){
                        if(posicionx!=-1 && posiciony!=-1){
                            if(botones[posicionx][posiciony].isEnabled()){                                
                                if(botones[posicionx][posiciony].getIcon()==null){
                                                                
                                    if(numJugador.equalsIgnoreCase("1") && posicionx==0){
                                        if(posicionx < 10 && posiciony < 10){
                                            out.println("0" + posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx < 10){
                                            out.println("0" + posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }else if(posiciony < 10){
                                            out.println(posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx > 9 && posiciony > 9){
                                            out.println(posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }
                                        for(int y = 0; y < tamañoTableroy-2; y++){
                                            botones[0][y].setBackground(new JButton().getBackground());
                                        }
                                        primerClick=false;
                                    }
                                    if(numJugador.equalsIgnoreCase("2") && posiciony==(tamañoTableroy-3)){
                                        if(posicionx < 10 && posiciony < 10){
                                            out.println("0" + posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx < 10){
                                            out.println("0" + posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }else if(posiciony < 10){
                                            out.println(posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx > 9 && posiciony > 9){
                                            out.println(posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }
                                        for(int x = 0; x < tamañoTablerox-2; x++){
                                            botones[x][tamañoTableroy-3].setBackground(new JButton().getBackground());
                                        }
                                        primerClick=false;
                                    }
                                    if(numJugador.equalsIgnoreCase("3") && posicionx==0){
                                        if(posicionx < 10 && posiciony < 10){
                                            out.println("0" + posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx < 10){
                                            out.println("0" + posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }else if(posiciony < 10){
                                            out.println(posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx > 9 && posiciony > 9){
                                            out.println(posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }
                                        for(int x = 0; x < tamañoTableroy-2; x++){
                                            botones[tamañoTableroy-3][x].setBackground(new JButton().getBackground());
                                        }
                                        primerClick=false;
                                    }
                                    if(numJugador.equalsIgnoreCase("4") && posiciony==0){
                                        if(posicionx < 10 && posiciony < 10){
                                            out.println("0" + posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx < 10){
                                            out.println("0" + posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }else if(posiciony < 10){
                                            out.println(posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                        }else if(posicionx > 9 && posiciony > 9){
                                            out.println(posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                        }
                                        for(int x = 0; x < tamañoTablerox-2; x++){
                                            botones[x][0].setBackground(new JButton().getBackground());
                                        }
                                        primerClick=false;
                                    }
                                }
                            }
                        }
                    }else{
                        if(posicionx!=-1 && posiciony!=-1){
                            if(botones[posicionx][posiciony].isEnabled()){                                
                                if(botones[posicionx][posiciony].getIcon()==null){
                            
                                    if(posicionx < 10 && posiciony < 10){
                                        out.println("0" + posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                    }else if(posicionx < 10){
                                        out.println("0" + posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                    }else if(posiciony < 10){
                                        out.println(posicionx + ":0" + posiciony + "-BANDERA_MENOS");
                                    }else if(posicionx > 9 && posiciony > 9){
                                        out.println(posicionx + ":" + posiciony + "-BANDERA_MENOS");
                                    }
                                    
                                    primerClick=false;
                                }else{
                                    
                                    if(posicionx < 10 && posiciony < 10){
                                        out.println("0" + posicionx + ":0" + posiciony + "-BANDERA_MAS");
                                    }else if(posicionx < 10){
                                        out.println("0" + posicionx + ":" + posiciony + "-BANDERA_MAS");
                                    }else if(posiciony < 10){
                                        out.println(posicionx + ":0" + posiciony + "-BANDERA_MAS");
                                    }else if(posicionx > 9 && posiciony > 9){
                                        out.println(posicionx + ":" + posiciony + "-BANDERA_MAS");
                                    }
                                    primerClick=false;
                                }
                            }
                        }
                    }
                }
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public String getLink(){
      URL link=this.getClass().getProtectionDomain().getCodeSource().getLocation(); 
      return link.toString();
    }
    
    public static String trimLink(){
        String linkEntero="";
                    try {
                       ClienteBuscaminas clase = new ClienteBuscaminas();
                       linkEntero=clase.getLink();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
               
                int endIndex= linkEntero.length()-26;
                String link=linkEntero.substring(6,endIndex);
                link=link.replaceAll("%20", " ");
           return link;
    }
    
    public void botonPresionado(int x,int y,String valor){
        botones[x][y].setEnabled(false);
        botones[x][y].setText(valor);
        botones[x][y].setBackground(new JButton().getBackground());
    }
    
    public void reventarCeros(String cadena){
        int indiceInicioX=0;
        int indiceFinalX=0;
        int indiceInicioY=0;
        int indiceFinalY=0;
        int indiceInicioN=-1;
        do{
            indiceInicioX=indiceInicioN+1;
            indiceFinalX=indiceInicioX+2;
            indiceInicioY=indiceFinalX+1;
            indiceFinalY=indiceInicioY+2;
            indiceInicioN=indiceFinalY+1;
            int x=Integer.valueOf(cadena.substring(indiceInicioX,indiceFinalX));
            int y=Integer.valueOf(cadena.substring(indiceInicioY,indiceFinalY));
            int n=Integer.valueOf(cadena.substring(indiceInicioN,indiceInicioN+1));
            if(n==0){
                botonPresionado(x, y, "");
            }else if(n>0){
                botonPresionado(x, y, String.valueOf(n));    
            }
        }while(indiceInicioN<cadena.length()-1);
    }
    
    public static void main(String[] args) throws Exception{
        ClienteBuscaminas cliente = new ClienteBuscaminas();
        cliente.run();
    } 


    @Override
    public void windowClosing(WindowEvent e) {
       JFrame frame=(JFrame)e.getSource();
       if(frame.getTitle().equalsIgnoreCase("Buscaminas")){
            out.println("00:00-CERRAR");
            pantallaInicial.setVisible(false);
            pantallaInicial.dispose();
            try{socket.close();}catch(IOException a){a.printStackTrace();};
       }else{
        out.println("00:00-CERRAR");
        frameBotones.setVisible(false);
        frameBotones.dispose();
        pantallaInicial.setVisible(false);
        pantallaInicial.dispose();
        try{socket.close();}catch(IOException a){a.printStackTrace();};
        
       }
    }

    @Override
    public void windowOpened(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowClosed(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowIconified(WindowEvent e) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
       // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
