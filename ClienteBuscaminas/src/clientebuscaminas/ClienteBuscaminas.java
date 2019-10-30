package clientebuscaminas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.TimerTask;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

public class ClienteBuscaminas extends Thread implements ActionListener, MouseListener, WindowListener {

    //public static int tamañox=10,tamañoy=10;3
    public static JButton[][] cuadriculaBotones;
    public static Scanner in;
    public static PrintWriter out;
    public static String numJugador;
    public static boolean sigoJugando = true;
    public static boolean esElPrimerClick = true;
    public static boolean botonesPrimerClick = false;
    public static int tamañoTablerox;
    public static int tamañoTableroy;
    public DefaultListModel modelo = new DefaultListModel();
    static int contadorSegundos;
    JLabel cronometro = new JLabel("00:00");
    java.util.Timer timer;
    JFrame pantallaInicial;
    Socket socket;
    JFrame pantallaDeBotones;

    public static void main(String[] args) throws Exception {
        ClienteBuscaminas cliente = new ClienteBuscaminas();
        cliente.run();
    }

    @Override
    public void run() {

        try {
            socket = new Socket("localhost", 2001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            pantallaInicial();
            while (in.hasNextLine()) {
                String line = in.nextLine();
                System.out.println("Mensaje Entrante: "+line);
                if (line.startsWith("NUEVOJUGADOR")) {
                    agregarNuevoJugador(line);
                } else if (line.startsWith("ACEPTADO")) {
                    aceptarJugador(line);
                } else if (line.startsWith("INICIARPARTIDA")) {
                    iniciarPartida_Tablero();
                } else if (line.startsWith("ERRORINICIARPARTIDA")) {
                    imprimirErrorFaltaDeJugadores();
                } else if (line.contains("TIEMPO")) {
                    enviarQueMeActualice();
                } else if (line.startsWith("ESMINA")) {
                    verificarSiEsMina(line);
                } else if (line.startsWith("SINBANDERAS")) {
                    verificarSiSoyJugadorSinBanderas(line);
                } else if (line.startsWith("MODIFICANUMERO")) {
                    cambiarNumeroDeJugador(line);
                } else if (line.startsWith("ESCERO")) {
                    ponerCero(line);
                } else if (line.contains("FINPARTIDA")) {
                    terminarPartida_AglunJugdorGana(line);
                } else if (line.contains("NOVIVOS")) {
                    terminarPartida_JugadoresPierden(line);
                } else {
                    int columna = obtenerColumnaRecibida(line);
                    int renglon = obtenerRenglonRecibido(line);
                    if (line.contains("PONER_BANDERA")) {
                        ponerBandera(line, columna, renglon);
                    }
                    if (line.contains("QUITAR_BANDERA")) {
                        quitarBandera(columna, renglon);

                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public int obtenerColumnaRecibida(String line) {
        return Integer.parseInt(line.substring(0, line.indexOf(":")));
    }

    public int obtenerRenglonRecibido(String line) {
        return Integer.parseInt(line.substring(line.indexOf(":") + 1, line.indexOf("-")));
    }

    public void quitarBandera(int columna, int renglon) {
        cuadriculaBotones[columna][renglon].setIcon(null);
    }

    public void ponerBandera(String line, int columna, int renglon) {
        String jugadorQuePusoLaBandera = line.substring(line.length() - 1);
        ImageIcon icono = new ImageIcon(getClass().getResource(jugadorQuePusoLaBandera + ".png"));
        ImageIcon alTamaño = new ImageIcon(icono.getImage().getScaledInstance(cuadriculaBotones[columna][renglon].getSize().height - 20, cuadriculaBotones[columna][renglon].getSize().width - 20, java.awt.Image.SCALE_DEFAULT));
        cuadriculaBotones[columna][renglon].setIcon(alTamaño);
    }

    public void cambiarNumeroDeJugador(String line) {
        int numero = Integer.valueOf(line.substring(line.indexOf("-") + 1));
        int numJug = Integer.valueOf(numJugador);

        if (numJug > numero) {
            numJug--;
            numJugador = String.valueOf(numJug);

        }
        out.println("00:00-NUEVONUMERO/" + numJugador);
    }

    public void terminarPartida_JugadoresPierden(String line) {
        timer.cancel();
        sigoJugando = false;
        String resultados = line.substring(line.indexOf("-") + 1);
        int indiceInicialJugador = 0;
        int indiceInicial = 2;
        String n = "";
        String j = "";

        String mensaje = "";
        do {
            j = resultados.substring(indiceInicialJugador, indiceInicial);
            n = resultados.substring(indiceInicial, indiceInicial + 2);
            indiceInicialJugador += 4;
            indiceInicial += 4;
            if (!j.equalsIgnoreCase("JG")) {
                if (numJugador.equalsIgnoreCase(j.substring(1))) {
                    mensaje += "Tu puntaje: " + n + "\n";
                } else if (j.equalsIgnoreCase("J1")) {
                    mensaje += "Jugador 1: " + n + "\n";
                } else if (j.equalsIgnoreCase("J2")) {
                    mensaje += "Jugador 2: " + n + "\n";
                } else if (j.equalsIgnoreCase("J3")) {
                    mensaje += "Jugador 3: " + n + "\n";
                } else if (j.equalsIgnoreCase("J4")) {
                    mensaje += "Jugador 4: " + n + "\n";
                }
            }

        } while (indiceInicial < resultados.length() - 1);
        JOptionPane.showMessageDialog(null, "Todos los jugadores han perdido.\nPuntajes:\n" + mensaje, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    public void terminarPartida_AglunJugdorGana(String line) {
        timer.cancel();
        sigoJugando = false;
        String resultados = line.substring(line.indexOf("-") + 1);
        int indiceInicialJugador = 0;
        int indiceInicial = 2;
        String n = "";
        String j = "";

        String mensaje = "";
        do {
            j = resultados.substring(indiceInicialJugador, indiceInicial);
            n = resultados.substring(indiceInicial, indiceInicial + 2);
            indiceInicialJugador += 4;
            indiceInicial += 4;
            if (!j.equalsIgnoreCase("JG")) {
                if (numJugador.equalsIgnoreCase(j.substring(1))) {
                    mensaje += "Tu puntaje: " + n + "\n";
                } else if (j.equalsIgnoreCase("J1")) {
                    mensaje += "Jugador 1: " + n + "\n";
                } else if (j.equalsIgnoreCase("J2")) {
                    mensaje += "Jugador 2: " + n + "\n";
                } else if (j.equalsIgnoreCase("J3")) {
                    mensaje += "Jugador 3: " + n + "\n";
                } else if (j.equalsIgnoreCase("J4")) {
                    mensaje += "Jugador 4: " + n + "\n";
                }
            }

        } while (indiceInicial < resultados.length() - 1);
        mensajeMinasEncontradas(mensaje);
    }

    public void mensajeMinasEncontradas(String mensaje) {
        JOptionPane.showMessageDialog(null, "Todos los Hukies han sido encontrados\nPuntajes:\n" + mensaje, "Fin de la partida", JOptionPane.INFORMATION_MESSAGE);
    }

    public void ponerCero(String line) {
        String ceros = "";
        String numeros = "";
        if (line.contains("-")) {
            ceros = line.substring(line.indexOf(":") + 1, line.indexOf("-"));
            numeros = line.substring(line.indexOf("-") + 1);
        } else {
            ceros = line.substring(line.indexOf(":") + 1);
        }
        reventarCeros(ceros);
        if (line.contains("-")) {
            reventarCeros(numeros);
        }
    }

    public void agregarNuevoJugador(String line) {
        modelo.removeAllElements();
        int indiceInicio = 13;
        do {
            modelo.addElement("Jugador " + line.substring(indiceInicio, indiceInicio + 1));
            indiceInicio += 3;
        } while (indiceInicio < line.length() - 1);
    }

    public void aceptarJugador(String line) {
        System.out.println("El servidor te ha aceptado...");
        tamañoTablerox = Integer.valueOf(line.substring(line.indexOf("x") + 1, line.indexOf(":")));
        tamañoTableroy = Integer.valueOf(line.substring(line.indexOf("y") + 1));
        numJugador = line.substring(16, 17);
        out.println("Jugador " + line.substring(16, 17) + " con tablero en pantalla");
    }

    public void iniciarPartida_Tablero() {
        dibujarTablero(tamañoTablerox - 2, tamañoTableroy - 2);
        this.cambiar();
        pantallaDeBotones.setTitle("Buscahuskies: eres el jugador " + numJugador);
        pantallaInicial.setVisible(false);
    }

    public void imprimirErrorFaltaDeJugadores() {
        JOptionPane.showMessageDialog(null, "No pudiste iniciar la partida por falta de jugadores", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void enviarQueMeActualice() {
        out.println("ACTUALIZAME");
        System.out.println("Te han agregado una mina...");
    }

    public void verificarSiEsMina(String line) {
        String resultado;

        int x = Integer.parseInt(line.substring(line.indexOf("A") + 1, line.indexOf(":")));
        int y = Integer.parseInt(line.substring(line.indexOf(":") + 1, line.indexOf("|")));
        if (tieneMina(line)) {
            //ES MINA
            minaExplota(x, y);
            if (soyElJugadorQueDetonaLaMina(line)) {
                perdiste();
            }
        } else {
            //NO ES MINA
            resultado = verValorResultadoDelBoton(line);
            botonPresionado(x, y, resultado);
        }
    }

    public boolean tieneMina(String line) {
        if (line.contains("-1")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean soyElJugadorQueDetonaLaMina(String line) {
        if (line.substring(line.length() - 1).equalsIgnoreCase(numJugador)) {
            return true;
        } else {
            return false;
        }
    }

    public String verValorResultadoDelBoton(String line) {
        String resultado = line.substring(line.indexOf("|") + 1, line.indexOf(" "));
        return resultado;
    }

    public void verificarSiSoyJugadorSinBanderas(String line) {
        if (line.substring(line.length() - 1).equalsIgnoreCase(numJugador)) {
            JOptionPane.showMessageDialog(null, "Te quedaste sin banderas.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //ventana inicial 
    public void pantallaInicial() {
        //Crear JList listaJugadores en partida. Son los jugadores que están en la partida y esperan a que esta comienze
        JList listaJugadoresEnPartida = new JList();

        ponerPropiedadesListaJugadoresEnPartida(listaJugadoresEnPartida);

        //Crea JPanel panel donde se muestra listaJugadoresEnPartida
        crearPanelPantallaInicial(listaJugadoresEnPartida);

        //Crea y pone propiedades al JPanel panelBoton donde se pondrá un botón
        crearPanelBoton();

        //Crea y pone propiedades al JPanel panelBoton donde se pondrá un botón
        crearPantallaInicial();

        ponerPropiedadesPantallaInicialJPanel(crearPanelPantallaInicial(listaJugadoresEnPartida), crearPanelBoton());
    }

    public JPanel crearPanelBoton() {
        JPanel panelBoton = new JPanel();
        JButton boton = new JButton("Iniciar");
        boton.setName("Iniciar");
        boton.addActionListener(this);
        panelBoton.add(boton);
        return panelBoton;
    }

    public JPanel crearPanelPantallaInicial(JList listaJugadoresEnPartida) {
        JPanel panelPantallaInicial = new JPanel();
        panelPantallaInicial.add(listaJugadoresEnPartida);
        return panelPantallaInicial;
    }

    public void crearPantallaInicial() {
        pantallaInicial = new JFrame();
    }

    public void crearListaJugadoresEnPartida() {
        JList listaJugadoresEnPartida = new JList();
    }

    public void ponerPropiedadesListaJugadoresEnPartida(JList lista) {
        lista.setModel(modelo);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public void ponerPropiedadesPantallaInicialJPanel(JPanel panel, JPanel panelBton) {
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
        pantallaInicial.setTitle("Buscahuskies");
        pantallaInicial.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    //Dibuja el tablero en la pantalla
    public void dibujarTablero(int tamañoTablerox, int tamañoTableroy) {
        //verifica el tamaño minimo del tablero
        if (verificarSiTamañoTableroCumple(tamañoTablerox, tamañoTableroy)) {
            //Crea y pone las propiedades al JPanel panelCronometro
            JPanel panelCronometro = new JPanel();
            ponerPropiedades_PanelCronometro(panelCronometro);

            //Contstruye el arreglo de botones del tamaño deseado
            construirCuadriculaBotones(tamañoTablerox, tamañoTableroy);

            //Se le pone nombre a cada boton de la matriz y se le agregan propiedades
            JPanel panelMuestraTablero = new JPanel();
            ponerNombre_Propiedades_CuadriculaBotones(tamañoTablerox, tamañoTableroy, panelMuestraTablero);

            visualizarPanelPrincipal(panelMuestraTablero);

            //Se establece la distrubucion visual de los botones
            pantallaDeBotones = new JFrame();
            establecerDistribuciónVisualBotones(tamañoTablerox, tamañoTableroy, panelMuestraTablero, panelCronometro);

            //se establece el tamaño minimo de el frame
            establecerTamañoMínimoPantallaDeBotones();
        } else {
            JOptionPane.showMessageDialog(null, "Para tener una buena experiencia en el juego \nEl tamaño minimo es 7x7", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean verificarSiTamañoTableroCumple(int tamañoTablerox, int tamañoTableroy) {
        if (tamañoTablerox > 6 && tamañoTableroy > 6) {
            return true;
        } else {
            return false;
        }
    }

    public void ponerPropiedades_PanelCronometro(JPanel panelCronometro) {
        cronometro.setName("cronometro");
        cronometro.setFont(new Font(Font.SERIF, Font.BOLD, 50));
        panelCronometro.add(cronometro);
        panelCronometro.setPreferredSize(new Dimension(100, 100));
    }

    public void construirCuadriculaBotones(int x, int y) {
        cuadriculaBotones = new JButton[x][y];
    }

    public void visualizarPanelPrincipal(JPanel panelPrincipal) {
        panelPrincipal.setVisible(true);
    }

    public void ponerNombre_Propiedades_CuadriculaBotones(int tamañoTablerox, int tamañoTableroy, JPanel panelMuestraTablero) {
        for (int Columna = 0; Columna < tamañoTablerox; Columna++) {
            for (int Renglon = 0; Renglon < tamañoTableroy; Renglon++) {

                //Crea la cuadricula donde se almacenará la información de los botones
                cuadriculaBotones[Columna][Renglon] = new JButton();

                ponerPropiedadesCuadriculaBotones(Columna, Renglon);

                verificarDondeEmpiezaElJugador_1(Columna, Renglon);
                verificarDondeEmpiezaElJugador_2(Columna, Renglon, tamañoTableroy);
                verificarDondeEmpiezaElJugador_3(Columna, Renglon, tamañoTablerox);
                verificarDondeEmpiezaElJugador_4(Columna, Renglon);

                //Añade el botón al panel que muestra el tablero
                panelMuestraTablero.add(cuadriculaBotones[Columna][Renglon]);
            }
        }
    }

    public Color colorAzul() {
        Color azul = new Color(54, 58, 201);
        return azul;
    }

    public Color colorRojo() {
        Color rojo = new Color(197, 32, 32);
        return rojo;
    }

    public Color colorRosa() {
        Color rosa = new Color(225, 102, 189);
        return rosa;
    }

    public Color colorVerde() {
        Color verde = new Color(39, 208, 66);
        return verde;
    }

    public Color colorLight_Cryan() {
        Color light_cyan = new Color(206, 255, 255);
        return light_cyan;
    }

    public Color colorMedia_Cyan() {
        Color colorMedia_Cyan = new Color(31, 118, 144);
        return colorMedia_Cyan;
    }

    public void ponerPropiedadesCuadriculaBotones(int columna, int renglon) {
        cuadriculaBotones[columna][renglon].setVisible(true);
        cuadriculaBotones[columna][renglon].setName(columna + "," + renglon);
        cuadriculaBotones[columna][renglon].addActionListener(this);
        cuadriculaBotones[columna][renglon].addMouseListener(this);
        cuadriculaBotones[columna][renglon].setBackground(Color.WHITE);
        cuadriculaBotones[columna][renglon].setMaximumSize(new Dimension(40, 40));
        cuadriculaBotones[columna][renglon].setMinimumSize(new Dimension(40, 40));
        cuadriculaBotones[columna][renglon].setPreferredSize(new Dimension(40, 40));
        Font fuente = new Font("Arial", 3, 12);
        cuadriculaBotones[columna][renglon].setFont(fuente);
    }

    public void verificarDondeEmpiezaElJugador_1(int renglon, int tamañoTableroy) {
        if (numJugador.equalsIgnoreCase("1")) {
            if (renglon == 0) {
                cuadriculaBotones[renglon][tamañoTableroy].setBackground(colorAzul());
            }
        }
    }

    public void verificarDondeEmpiezaElJugador_2(int columna, int renglon, int tamañoTablerox) {
        if (numJugador.equalsIgnoreCase("2")) {
            if (renglon == tamañoTablerox - 1) {
                cuadriculaBotones[columna][renglon].setBackground(colorRojo());
            }
        }
    }

    public void verificarDondeEmpiezaElJugador_3(int columna, int renglon, int x) {
        if (numJugador.equalsIgnoreCase("3")) {
            if (columna == x - 1) {
                cuadriculaBotones[columna][renglon].setBackground(colorRosa());
            }
        }
    }

    public void verificarDondeEmpiezaElJugador_4(int columna, int renglon) {
        if (numJugador.equalsIgnoreCase("4")) {
            if (renglon == 0) {
                cuadriculaBotones[columna][renglon].setBackground(colorVerde());
            }
        }
    }

    public void establecerDistribuciónVisualBotones(int x, int y, JPanel panel, JPanel panelCronometro) {
        panel.setLayout(new GridLayout(x, y));
        panel.setPreferredSize(new Dimension(650, 650));
        pantallaDeBotones.add(panelCronometro, BorderLayout.NORTH);
        pantallaDeBotones.add(panel, BorderLayout.CENTER);
    }

    public void establecerTamañoMínimoPantallaDeBotones() {
        //pantallaDeBotones.setMinimumSize(new Dimension(720, 720));
        pantallaDeBotones.pack();
        pantallaDeBotones.setVisible(true);
        pantallaDeBotones.setLocationRelativeTo(null);
        pantallaDeBotones.addWindowListener(this);
        pantallaDeBotones.setResizable(false);
    }

    void cambiar() {
        timer = new java.util.Timer();
        TimerTask task = new TimerTask() {
            boolean band = false;
            String min = "", seg = "";
            Integer minutos = 0, segundos = 0;

            @Override
            public void run() {
                String cadenaCronometro = "";
                if (segundos == 59) {
                    minutos++;
                    segundos = 0;
                } else {
                    segundos++;
                }
                if (minutos < 10) {
                    cadenaCronometro = "0" + minutos.toString() + ":";
                } else {
                    cadenaCronometro = minutos.toString() + ":";
                }
                if (segundos < 10) {
                    cadenaCronometro += "0" + segundos.toString();
                } else {
                    cadenaCronometro += segundos.toString();
                }
                cronometro.setText(cadenaCronometro);
            }
        };
        // Empezamos dentro de 10ms y luego lanzamos la tarea cada 1000ms
        timer.schedule(task, 1, 1000);
    }

    public void perdiste() {
        JOptionPane.showMessageDialog(null, "Hiciste enojar al Hasky", "Perdiste", JOptionPane.ERROR_MESSAGE);
        sigoJugando = false;
    }

    public void minaExplota(int x, int y) {
        //ImageIcon icono=new ImageIcon(trimLink()+"\\src\\img\\explota.png");
        ImageIcon icono = new ImageIcon(getClass().getResource("explota.png"));
        ImageIcon alTamaño = new ImageIcon(icono.getImage().getScaledInstance(cuadriculaBotones[x][y].getSize().height, cuadriculaBotones[x][y].getSize().width, java.awt.Image.SCALE_DEFAULT));
        cuadriculaBotones[x][y].setIcon(alTamaño);

    }

    //accion al presionar el boton izquierdo
    @Override
    public void actionPerformed(ActionEvent ae) {
        JButton botonQuePresionaste = (JButton) ae.getSource();
        if (elBotonNoEsElDeIniciar(botonQuePresionaste)) {
            try {
                if (sigoJugando) {
                    int posicionx = obtenerPosicionx(botonQuePresionaste);
                    int posiciony = obtenerPosiciony(botonQuePresionaste);

                    if (esElPrimerClick) {
                        checarColores();
                    }

                    if (esElPrimerClick && botonesPrimerClick) {
                        quitarDondeEmpiezaJugador_1(posicionx, posiciony);
                        quitarDondeEmpiezaJugador_2(posicionx, posiciony);
                        quitarDondeEmpiezaJugador_3(posicionx, posiciony);
                        quitarDondeEmpiezaJugador_4(posicionx, posiciony);
                    } else {
                        if (elBotonNoTieneIcono(posicionx, posiciony)) {
                            enviarMensajeClick(posicionx, posiciony, "-");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                imprimirErrorObteniendoPosicion();
            }
        } else {
            iniciarPartida();
        }
    }

    public void imprimirErrorObteniendoPosicion() {
        JOptionPane.showMessageDialog(null, "Error al obtener la posicion presionada", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean elBotonNoEsElDeIniciar(JButton botonQuePresionaste) {
        if (botonQuePresionaste.getName().equalsIgnoreCase("Iniciar")) {
            return false;
        } else {
            return true;
        }
    }

    public void iniciarPartida() {
        out.println("00:00-INICIARPARTIDA");
    }

    public boolean elBotonNoTieneIcono(int posicionx, int posiciony) {
        if (cuadriculaBotones[posicionx][posiciony].getIcon() == null) {
            return true;
        } else {
            return false;
        }
    }

    public int obtenerPosicionx(JButton botonQuePresionaste) {
        return Integer.parseInt(botonQuePresionaste.getName().substring(0, botonQuePresionaste.getName().indexOf(",")));
    }

    public int obtenerPosiciony(JButton botonQuePresionaste) {
        return Integer.parseInt(botonQuePresionaste.getName().substring(botonQuePresionaste.getName().indexOf(",") + 1));
    }

    public void quitarDondeEmpiezaJugador_1(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("1") && posicionx == 0) {
            enviarMensajeClick(posicionx, posiciony, "-");
            for (int y = 0; y < tamañoTableroy - 2; y++) {
                if (cuadriculaBotones[0][y].isEnabled()) {
                    cuadriculaBotones[0][y].setBackground(Color.WHITE);
                }
            }
        }
    }

    public void quitarDondeEmpiezaJugador_2(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("2") && posiciony == (tamañoTableroy - 3)) {
            enviarMensajeClick(posicionx, posiciony, "-");
            for (int x = 0; x < tamañoTablerox - 2; x++) {
                if (cuadriculaBotones[x][tamañoTableroy - 3].isEnabled()) {
                    cuadriculaBotones[x][tamañoTableroy - 3].setBackground(Color.WHITE);
                }
            }
        }
    }

    public void quitarDondeEmpiezaJugador_3(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("3") && posicionx == (tamañoTablerox - 3)) {
            enviarMensajeClick(posicionx, posiciony, "-");
            for (int x = 0; x < tamañoTableroy - 2; x++) {
                if (cuadriculaBotones[tamañoTableroy - 3][x].isEnabled()) {
                    cuadriculaBotones[tamañoTableroy - 3][x].setBackground(Color.WHITE);
                }
            }
        }
    }

    public void quitarDondeEmpiezaJugador_4(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("4") && posiciony == 0) {
            enviarMensajeClick(posicionx, posiciony, "-");
            for (int x = 0; x < tamañoTablerox - 2; x++) {
                if (cuadriculaBotones[x][0].isEnabled()) {
                    cuadriculaBotones[x][0].setBackground(Color.WHITE);
                }
            }
        }
    }

    public void checarColor_Jugador_1() {
        int renglon = 0;
        do {
            if (cuadriculaBotones[0][renglon].isEnabled()) {
                botonesPrimerClick = true;
                break;
            } else {
                renglon++;
            }
        } while (renglon < tamañoTableroy - 2);
    }

    public void checarColor_Jugador_2() {
        int columna = 0;
        do {
            if (cuadriculaBotones[columna][tamañoTableroy - 3].isEnabled()) {
                botonesPrimerClick = true;
                break;
            } else {
                columna++;
            }
        } while (columna < tamañoTablerox - 2);
    }

    public void checarColor_Jugador_3() {
        int renglon = 0;
        do {
            if (cuadriculaBotones[tamañoTablerox - 3][renglon].isEnabled()) {
                botonesPrimerClick = true;
                break;
            } else {
                renglon++;
            }
        } while (renglon < tamañoTableroy - 2);
    }

    public void checarColor_Jugador_4() {
        int clumna = 0;
        do {
            if (cuadriculaBotones[clumna][0].isEnabled()) {
                botonesPrimerClick = true;
                break;
            } else {
                clumna++;
            }
        } while (clumna < tamañoTablerox - 2);
    }

    public void checarColores() {
        if (numJugador.equalsIgnoreCase("1")) {
            checarColor_Jugador_1();
        } else if (numJugador.equalsIgnoreCase("2")) {
            checarColor_Jugador_2();
        } else if (numJugador.equalsIgnoreCase("3")) {
            checarColor_Jugador_3();
        } else if (numJugador.equalsIgnoreCase("4")) {
            checarColor_Jugador_4();
        }
    }

    //accion al presionar el boton derecho
    @Override
    public void mousePressed(MouseEvent me) {
        if (sigoJugando) {
            if (esElPrimerClick) {
                checarColores();
            }

            if (me.getButton() == MouseEvent.BUTTON3) {
                JButton botonQuePresionaste = (JButton) me.getSource();
                int posicionx = -1;
                int posiciony = -1;
                try {
                    posicionx = obtenerPosicionx(botonQuePresionaste);
                    posiciony = obtenerPosiciony(botonQuePresionaste);
                } catch (NumberFormatException e) {
                    imprimirErrorObteniendoPosicion();
                }

                if (esElPrimerClick) {
                    checarColores();
                }

                if (esElPrimerClick && botonesPrimerClick) {
                    if (posicionx != -1 && posiciony != -1) {
                        if (elBotonEstaHabilitado(posicionx, posiciony)) {
                            if (elBotonNoTieneIcono(posicionx, posiciony)) {
                                poner_Bandera_Jugador_1(posicionx, posiciony);
                                poner_Bandera_Jugador_2(posicionx, posiciony);
                                poner_Bandera_Jugador_3(posicionx, posiciony);
                                poner_Bandera_Jugador_4(posicionx, posiciony);
                            }
                        }
                    }
                } else {
                    if (posicionx != -1 && posiciony != -1) {
                        if (elBotonEstaHabilitado(posicionx, posiciony)) {
                            if (elBotonNoTieneIcono(posicionx, posiciony)) {
                                enviarMensajeClick(posicionx, posiciony, "-BANDERA_MENOS");
                                //enviarMensaje_ClickDerecho_BanderaMenos(posicionx, posiciony, "-BANDERA_MENOS");
                            } else {
                                //enviarMensaje_ClickDerecho_BanderaMas(posicionx, posiciony, "BANDERA_MAS");
                                enviarMensajeClick(posicionx, posiciony, "-BANDERA_MAS");
                            }
                        }
                    }
                }
            }
        }
    }

    public void poner_Bandera_Jugador_1(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("1") && posicionx == 0) {
            enviarMensajeClick(posicionx, posiciony, "-BANDERA_MENOS");
            for (int y = 0; y < tamañoTableroy - 2; y++) {
                cuadriculaBotones[0][y].setBackground(Color.WHITE);
            }
        }
    }

    public void poner_Bandera_Jugador_2(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("2") && posiciony == (tamañoTableroy - 3)) {
            enviarMensajeClick(posicionx, posiciony, "-BANDERA_MENOS");
            for (int x = 0; x < tamañoTablerox - 2; x++) {
                cuadriculaBotones[x][tamañoTableroy - 3].setBackground(Color.WHITE);
            }
        }
    }

    public void poner_Bandera_Jugador_3(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("3") && posicionx == 0) {
            enviarMensajeClick(posicionx, posiciony, "-BANDERA_MENOS");
            for (int x = 0; x < tamañoTableroy - 2; x++) {
                cuadriculaBotones[tamañoTableroy - 3][x].setBackground(Color.WHITE);
            }
        }
    }

    public void poner_Bandera_Jugador_4(int posicionx, int posiciony) {
        if (numJugador.equalsIgnoreCase("4") && posiciony == 0) {
            enviarMensajeClick(posicionx, posiciony, "-BANDERA_MENOS");
            for (int x = 0; x < tamañoTablerox - 2; x++) {
                cuadriculaBotones[x][0].setBackground(Color.WHITE);
            }
        }
    }

    public boolean elBotonEstaHabilitado(int posicionx, int posiciony) {
        if (cuadriculaBotones[posicionx][posiciony].isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    public void enviarMensajeClick(int posicionx, int posiciony, String mensaje) {
        if (posicionx < 10 && posiciony < 10) {
            out.println("0" + posicionx + ":0" + posiciony + mensaje);
        } else if (posicionx < 10) {
            out.println("0" + posicionx + ":" + posiciony + mensaje);
        } else if (posiciony < 10) {
            out.println(posicionx + ":0" + posiciony + mensaje);
        } else if (posicionx > 9 && posiciony > 9) {
            out.println(posicionx + ":" + posiciony + mensaje);
        }
        esElPrimerClick = false;
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

    @Override
    public void mouseClicked(MouseEvent me) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void botonPresionado(int x, int y, String valor) {
        cuadriculaBotones[x][y].setEnabled(false);
        cuadriculaBotones[x][y].setText(valor);
        cuadriculaBotones[x][y].setBackground(colorLight_Cryan());
    }

    public void reventarCeros(String cadena) {
        int indiceInicioX = 0;
        int indiceFinalX = 0;
        int indiceInicioY = 0;
        int indiceFinalY = 0;
        int indiceInicioN = -1;
        do {
            try {
                indiceInicioX = indiceInicioN + 1;
                indiceFinalX = indiceInicioX + 2;
                indiceInicioY = indiceFinalX + 1;
                indiceFinalY = indiceInicioY + 2;
                indiceInicioN = indiceFinalY + 1;
                int columna = obtenerPosicionColumna_ReventarCeros(cadena, indiceInicioX, indiceFinalX);
                int renglon = obtenerPosicionRenglon_ReventarCeros(cadena, indiceInicioY, indiceFinalY);
                int valorEnLaPosicion = obtenerValorEnLaPosicion(cadena, indiceInicioN);

                if (valorEnLaPosicion == 0) {
                    botonPresionado(columna, renglon, "");
                } else if (valorEnLaPosicion > 0) {
                    botonPresionado(columna, renglon, String.valueOf(valorEnLaPosicion));
                }
            } catch (Exception e) {
               
            }
        } while (indiceInicioN < cadena.length() - 1);
    }

    public int obtenerPosicionColumna_ReventarCeros(String cadena, int indiceInicioX, int indiceFinalX) {
        return Integer.valueOf(cadena.substring(indiceInicioX, indiceFinalX));
    }

    public int obtenerPosicionRenglon_ReventarCeros(String cadena, int indiceInicioY, int indiceFinalY) {
        return Integer.valueOf(cadena.substring(indiceInicioY, indiceFinalY));
    }

    public int obtenerValorEnLaPosicion(String cadena, int indiceInicioN) {
        return Integer.valueOf(cadena.substring(indiceInicioN, indiceInicioN + 1));
    }

    @Override
    public void windowClosing(WindowEvent e) {
        JFrame frame = (JFrame) e.getSource();
        if (frame.getTitle().equalsIgnoreCase("Buscahuskies")) {
            out.println("00:00-CERRAR");
            pantallaInicial.setVisible(false);
            pantallaInicial.dispose();
            try {
                socket.close();
            } catch (IOException a) {
                a.printStackTrace();
            };
        } else {
            out.println("00:00-CERRAR");
            pantallaDeBotones.setVisible(false);
            pantallaDeBotones.dispose();
            pantallaInicial.setVisible(false);
            pantallaInicial.dispose();
            timer.cancel();
            try {
                socket.close();
            } catch (IOException a) {
                a.printStackTrace();
            };

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
