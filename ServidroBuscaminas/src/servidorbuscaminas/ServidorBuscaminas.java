package servidorbuscaminas;
import java.io.IOException;
import  servidorbuscaminas.Tablero;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorBuscaminas {
    
    static int numJugadorEntra = 0;
    static private Tablero a=null;
    static java.util.Timer timer;
    static String mensaje;
    
    public static void main(String[] args) throws Exception{
        System.out.println("Iniciando Servidor...");
        ExecutorService pool = Executors.newFixedThreadPool(100);
        try(ServerSocket listener = new ServerSocket(2001)){
            while(true){
                pool.execute(new Handler(listener.accept())  {});
            }
        }
    }//fin main
    
    private static class Handler implements Runnable{
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private static Map<Integer,Set> jugadoresMap = new HashMap<>();
        private static Map<Integer,Set> numeroJugadores = new HashMap<>();
        Jugador b;
        public Handler(Socket socket){
            this.socket = socket;
        }
        
        public void run(){
            try{
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(),true);
                
                numJugadorEntra++;
                if(numJugadorEntra==1 && a==null){
                    a = new Tablero();
                }
                b = new Jugador();

                /*regresa numero de partida y numero de jugador*/           
                String infoJugador = a.agregarJugador();
                int sala = Integer.valueOf(infoJugador.substring(0,infoJugador.indexOf(":")));
                int numJugadorSala = Integer.valueOf(infoJugador.substring(infoJugador.indexOf(":")+1));;
                
                b.setNumeroDeSalaEnLaQueEstoy(sala);
                b.setJugador(numJugadorSala);
                b.setBanderas(a.getNumMinas(b.getNumeroDeSalaEnLaQueEstoy()));
               
                if(numeroJugadores.get(b.getNumeroDeSalaEnLaQueEstoy())==null){
                    Set<Integer> setNumeroJugadores= new HashSet<>();
                    setNumeroJugadores.add(b.getJugador());
                    numeroJugadores.put(b.getNumeroDeSalaEnLaQueEstoy(),setNumeroJugadores);  
                }else{
                    Set<Integer> setNumeroJugadores= new HashSet<>();
                    setNumeroJugadores=numeroJugadores.get(b.getNumeroDeSalaEnLaQueEstoy());
                    setNumeroJugadores.add(b.getJugador());
                    numeroJugadores.replace(b.getNumeroDeSalaEnLaQueEstoy(), setNumeroJugadores);
                }
               
                if(jugadoresMap.get(b.getNumeroDeSalaEnLaQueEstoy())==null){
                    Set<PrintWriter> setSalidas= new HashSet<>();
                    setSalidas.add(out);
                    jugadoresMap.put(b.getNumeroDeSalaEnLaQueEstoy(),setSalidas);  
                }else{
                    Set<PrintWriter> setSalidas= new HashSet<>();
                    setSalidas=jugadoresMap.get(b.getNumeroDeSalaEnLaQueEstoy());
                    setSalidas.add(out);
                    jugadoresMap.replace(b.getNumeroDeSalaEnLaQueEstoy(), setSalidas);
                }
                Set<PrintWriter> writers =jugadoresMap.get(b.getNumeroDeSalaEnLaQueEstoy());
                for(PrintWriter escritor : writers){
                        escritor.println("NUEVOJUGADOR"+numeroJugadores.get(b.getNumeroDeSalaEnLaQueEstoy()));
                }
                if(a.validarTamañoYMinas(b.getNumeroDeSalaEnLaQueEstoy())){
                    System.out.println("Hay mas minas que casillas...");
                }
                
                if( (a.getTamañox(b.getNumeroDeSalaEnLaQueEstoy())-1) < 10 && (a.getTamañoy(b.getNumeroDeSalaEnLaQueEstoy())-2) < 10){
                    out.println("ACEPTADO JUGADOR" + b.getJugador() + "x0" + a.getTamañox(b.getNumeroDeSalaEnLaQueEstoy()) + ":y0" + a.getTamañoy(b.getNumeroDeSalaEnLaQueEstoy()));
                }else if(a.getTamañox(b.getNumeroDeSalaEnLaQueEstoy()) < 10){
                    out.println("ACEPTADO JUGADOR" + b.getJugador() + "x0" + a.getTamañox(b.getNumeroDeSalaEnLaQueEstoy()) + ":y" + a.getTamañoy(b.getNumeroDeSalaEnLaQueEstoy()));
                }else if(a.getTamañoy(b.getNumeroDeSalaEnLaQueEstoy()) < 10){
                    out.println("ACEPTADO JUGADOR" + b.getJugador() + "x" + a.getTamañox(b.getNumeroDeSalaEnLaQueEstoy()) + ":y0" + a.getTamañoy(b.getNumeroDeSalaEnLaQueEstoy()));
                }else{
                    out.println("ACEPTADO JUGADOR" + b.getJugador() + "x" + a.getTamañox(b.getNumeroDeSalaEnLaQueEstoy()) + ":y" + a.getTamañoy(b.getNumeroDeSalaEnLaQueEstoy()));
                }

                
                String line = in.nextLine();
                                
                
                while(true){
                    synchronized(String.valueOf(numJugadorEntra)){
                        line = in.nextLine();
                        if(line.contains("INICIARPARTIDA")){
                        try{                     
                        if(a.iniciarPartida(b.getNumeroDeSalaEnLaQueEstoy())){
                            a.generarMinas(b.getNumeroDeSalaEnLaQueEstoy());
                            mensaje = "INICIARPARTIDA";
                            cambiar(writers, b.getNumeroDeSalaEnLaQueEstoy(), b, a, out);
                        }else{
                            mensaje = "ERRORINICIARPARTIDA";
                        }
                        }catch(Exception w){
                            w.printStackTrace();
                        }
                        }else if(line.contains("ACTUALIZAME")){
                            b.setBanderas(b.getBanderas()+1);
                            mensaje="00:00-";
                        }else if(line.contains("NUEVONUMERO")){
                            String numerito=line.substring(line.indexOf("/")+1);
                            b.setJugador(Integer.valueOf(numerito));
                            mensaje="00:00-";
                        }else{
                        
                        int x = Integer.valueOf(line.substring(0,line.indexOf(":")));
                        int y = Integer.valueOf(line.substring(line.indexOf(":")+1,line.indexOf("-")));
                        
                        int valorCasilla = a.obtenerValores(x+1, y+1,b.getNumeroDeSalaEnLaQueEstoy());

                        if(line.contains("BANDERA_MENOS")){
                            if(a.getCuadriculaBanderas(x+1, y+1,b.getNumeroDeSalaEnLaQueEstoy())==0){
                                if(b.getBanderas()>0){
                                    b.setBanderas(b.getBanderas()-1);                                
                                    a.setCuadriculaBanderas(x+1, y+1, b.getJugador(),b.getNumeroDeSalaEnLaQueEstoy());
                                    mensaje = x+":"+y + "-PONER_BANDERA"+ b.getJugador();
                                }else{
                                    mensaje = "SINBANDERAS" + b.getJugador();
                                }
                            }else{
                                mensaje = x+":"+y + "-NEGADO";
                            }
                        }else if(line.contains("BANDERA_MAS")){
                            if(a.getCuadriculaBanderas(x+1, y+1,b.getNumeroDeSalaEnLaQueEstoy()) == b.getJugador()){
                                a.setCuadriculaBanderas(x+1, y+1, 0,b.getNumeroDeSalaEnLaQueEstoy());
                                b.setBanderas(b.getBanderas()+1);
                                mensaje = x+":"+y + "-QUITAR_BANDERA";      
                            }else{
                                mensaje = x+":"+y + "-NEGADO";
                            }
                        }else if(line.contains("-OBTENERNUMEROS")){
                            mensaje = String.valueOf(a.obtenerValores(x, y,b.getNumeroDeSalaEnLaQueEstoy()));
                        }else if(line.contains("CERRAR")){
                            if(a!=null){
                                 a.jugadorSalio(b.getJugador(), b.getNumeroDeSalaEnLaQueEstoy(), jugadoresMap, writers,b);
                                Set<Integer> nombres=numeroJugadores.get(b.getNumeroDeSalaEnLaQueEstoy());
                                 nombres.remove(b.getJugador());
                                if(!a.getPartidaIniciada(b.getNumeroDeSalaEnLaQueEstoy())){
                                 a.salioJugadorDisminuirNumeroJugador(b.getNumeroDeSalaEnLaQueEstoy(),b.getJugador());
                                 
                                 }else{
                                   a.cambiarEstadoJugador(b.getNumeroDeSalaEnLaQueEstoy(), b.getJugador()); 
                                 }
                                 numeroJugadores.replace(b.getNumeroDeSalaEnLaQueEstoy(),nombres);
                                 mensaje = "00:00-";
                                 if(nombres.size()==0){
                                   //a.cerrarPartida(b.getNumeroDeSalaEnLaQueEstoy());   
                                 }
                             }
                             if(out!=null){
                                Set<PrintWriter> wr=jugadoresMap.get(b.getNumeroDeSalaEnLaQueEstoy());
                                wr.remove(out);
                                jugadoresMap.replace(b.getNumeroDeSalaEnLaQueEstoy(), wr);
                                mensaje = "00:00-";
                             }
                             try{socket.close();}catch(IOException e){e.printStackTrace();};                        
                        }else if(a.esMina(x+1,y+1,b.getNumeroDeSalaEnLaQueEstoy())){
                            mensaje = "ESMINA"+x+":"+y + "|" + "-1" + " " + b.getJugador();
                            a.setCuadriculaBanderas(x+1, y+1, 5,b.getNumeroDeSalaEnLaQueEstoy());
                            a.cambiarEstadoJugador(b.getNumeroDeSalaEnLaQueEstoy(), b.getJugador());
                            b.setSigoJugando(false);
                        }else if(valorCasilla > 0){
                            mensaje = "ESMINA"+x+":"+y + "|" + valorCasilla + " " + b.getJugador();
                            a.setCuadriculaBanderas(x+1, y+1,6,b.getNumeroDeSalaEnLaQueEstoy());
                        }else if(valorCasilla == 0){
                            String ceros="";
                            String numeros="";
                            a.procesarCeros(x, y,"inicia",b.getNumeroDeSalaEnLaQueEstoy());
                            ceros = a.getCeros(b.getNumeroDeSalaEnLaQueEstoy());
                            numeros = a.getNumeros(b.getNumeroDeSalaEnLaQueEstoy());
                            if(numeros.length()>0){               
                                mensaje = "ESCERO:" + ceros + "-" + numeros;
                            }else{
                                mensaje = "ESCERO:" + ceros;
                            }
                        }
                        }
                    }
                    enviarMensajeATodaLaSala(writers, mensaje);       
                   if(!a.getEnviadoNoVivo(b.getNumeroDeSalaEnLaQueEstoy())){           
                    writers =jugadoresMap.get(b.getNumeroDeSalaEnLaQueEstoy());         
                    if(a.finalizarPartida_MinasEncontradas(b.getNumeroDeSalaEnLaQueEstoy())){
                      timer.cancel();
                      b.setSigoJugando(false);
                      String resultados=a.contarPuntos(b.getNumeroDeSalaEnLaQueEstoy(),numeroJugadores.get(b.getNumeroDeSalaEnLaQueEstoy()));
                        enviarMensajeATodaLaSala(writers,"FINPARTIDA-" , resultados);    
                       timer.cancel();
                        a.setEnviadoNoVivo(b.getNumeroDeSalaEnLaQueEstoy(), true);
                     
                    }       
                    if(a.finalizarPartida_JugadoresPierden(b.getNumeroDeSalaEnLaQueEstoy())){     
                        String resultados=a.contarPuntos(b.getNumeroDeSalaEnLaQueEstoy(),numeroJugadores.get(b.getNumeroDeSalaEnLaQueEstoy()));
                        enviarMensajeATodaLaSala(writers,"NOVIVOS-" , resultados);
                        timer.cancel();
                       a.setEnviadoNoVivo(b.getNumeroDeSalaEnLaQueEstoy(), true);
                    }
                  }
                }
                
                
                
            }catch(Exception e){
                //e.printStackTrace();
                        
            }
        }
        public void enviarMensajeATodaLaSala(Set<PrintWriter> writers, String protocolo, String mensajeAEnviar) {
            for (PrintWriter escritor : writers) {
                escritor.println(protocolo + mensajeAEnviar);
            }
        }
        public void enviarMensajeATodaLaSala(Set<PrintWriter> writers, String mensajeAEnviar) {
            for (PrintWriter escritor : writers) {
                escritor.println(mensajeAEnviar);
            }
        }
        public void cambiar(Set<PrintWriter> writers, int numJugadorSala, Jugador b, Tablero a, PrintWriter out){
                timer = new java.util.Timer();
                TimerTask task = new TimerTask() {
                    boolean band = false;
                    String seg="";
                    Integer segundos=0; 
                    @Override
                    public void run(){
                        segundos++;
                        if(segundos%30 == 0){
                            mensaje = a.aumentarMinasTiempo(numJugadorSala);
                            for(PrintWriter escritor : writers){
                                escritor.println(mensaje);
                            }
                            if(a.verSiHayEspacios(b.getNumeroDeSalaEnLaQueEstoy())==0){
                              timer.cancel();  
                            }
                            int x = Integer.valueOf(mensaje.substring(0,mensaje.indexOf(":")));
                            int y = Integer.valueOf(mensaje.substring(mensaje.indexOf(":")+1,mensaje.indexOf("-")));
                            if(x>0 && y>0){
                             mensaje="";
                            mensaje=a.actualizarValoresMinaAgregada(x-1, y-1, b.getNumeroDeSalaEnLaQueEstoy());
                            if(mensaje.length()>0){
                                enviarMensajeATodaLaSala(writers, "ESCERO:", mensaje);
                            }
                            }
                        }
                    }
                };
                // Empezamos dentro de 10ms y luego lanzamos la tarea cada 1000ms
                timer.schedule(task, 1, 1000);
        }
    }//FIN HANDLER
    


}//fin class ServidorBuscaminas
