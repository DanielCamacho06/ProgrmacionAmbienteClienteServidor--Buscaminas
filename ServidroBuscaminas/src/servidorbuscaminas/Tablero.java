package servidorbuscaminas;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Tablero {
    private int[][] cuadriculaMinas;
    private int[][] perimetro;    
    private int[][] cuadriculaBanderas;
    private int tamañox=15;
    private int tamañoy=15;
    private final int numMinas = 10;
    private int numJugadores = 0; 
    int rotacionx[] = {-1, 0, 1, -1, 1, -1, 0, 1};
    int rotaciony[] = {-1, -1, -1, 0, 0, 1, 1, 1};
    private int salas = 0;
    private boolean partidaIniciada=false;
    private Map<Integer,Tablero> partidasMultiples = new HashMap();
    private Map<Integer,String> estadoJugador = new HashMap();
    private int numPartidas=1;
    private Set<Integer> tamañoJugadores=new HashSet<>();;
    
    
    //Constructor crea la matriz y la llena de 0 excepto las orillas que las llena de 9
    public Tablero() {
        //+2 para tener espacio para las columnas de 9 para delimitar y el tamaño de la matriz quede del tamaño deseado
        this.tamañox = tamañox+2;
        this.tamañoy = tamañoy+2;
        cuadriculaMinas = new int[tamañox][tamañoy];
        perimetro=new int[tamañox-2][tamañoy-2];
        tableroBanderas();
        for (int i = 0; i < tamañox; i++) {
            for (int j = 0; j < tamañoy; j++) {
                //si es orilla se le da el valor 9 y si no 0
                if(i == 0 || i == tamañox-1 || j == 0 || j == tamañoy-1){
                    cuadriculaMinas[i][j] = 9;
                }else{
                    cuadriculaMinas[i][j] = 0;
                }
            }
        }
    }
    
    public void tableroBanderas(){
        cuadriculaBanderas = new int[tamañox][tamañoy];
        for (int i = 0; i < tamañox; i++) {
            for (int j = 0; j < tamañoy; j++) {
                //si es orilla se le da el valor 9 y si no 0
                if(i == 0 || i == tamañox-1 || j == 0 || j == tamañoy-1){
                    cuadriculaBanderas[i][j] = 9;
                }else{
                    cuadriculaBanderas[i][j] = 0;
                }
            }
        } 
    }
    
    /*public void nuevaSala(){
        if(getSalas() == 0){
            System.out.println("numero de salas igual a 0: " + getSalas());
            setSalas(getSalas()+1);
            generarMinas();
            System.out.println("numero de salas despues de sumarle 1: " + getSalas());
        }
    }*/
    
    //se colocan las minas en la matriz
    
public void generarMinas(int numPartida){
        cuadriculaMinas=partidasMultiples.get(numPartida).cuadriculaMinas;
        //GENERA LAS MINAS
        int minasColocadas=0;
        do{
            int x=generarAleatorio(partidasMultiples.get(numPartida).tamañox);
            int y=generarAleatorio(partidasMultiples.get(numPartida).tamañoy);
            //se verifica si es orilla
            if(!partidasMultiples.get(numPartida).esOrilla(x,y,numPartida)){
                //se verifica si ya hay una mina en esa posicion
                if(!partidasMultiples.get(numPartida).esMina(x,y,numPartida,1)){
                    cuadriculaMinas[x][y]=-1;
                    minasColocadas++;  
                }
            }    
           
        }while(minasColocadas!=partidasMultiples.get(numPartida).numMinas);
        System.out.println("Minas Colocadas");
        perimetro=partidasMultiples.get(numPartida).perimetro;
        //GENERA LOS NÚMEROS ALREDEDOR DE LA MINA
        for (int i = 0; i < partidasMultiples.get(numPartida).tamañox-2; i++) {
            for (int j = 0; j < partidasMultiples.get(numPartida).tamañoy-2; j++) {
                if(cuadriculaMinas[i+1][j+1]!=9){
                    perimetro[i][j] = obtenerValores(i+1, j+1,numPartida);
                }
            }
        }
        for (int i = 0; i < tamañox-2; i++) {
            for (int j = 0; j < tamañoy-2; j++) {
                System.out.print(perimetro[i][j]);          
            }
            System.out.println("");
        }  
        partidasMultiples.get(numPartida).cuadriculaMinas=cuadriculaMinas;
        partidasMultiples.get(numPartida).perimetro=perimetro;
    }



   public void imprimirCuadricula(int numPartida){
        for (int i = 0; i < partidasMultiples.get(numPartida).tamañox; i++) {
            for (int j = 0; j <partidasMultiples.get(numPartida).tamañoy; j++) {
                System.out.print(partidasMultiples.get(numPartida).cuadriculaMinas[i][j]+"|");
            }
            System.out.println("");
        }
        System.out.println("////////////////////");
    }
    
    //devuelve un entero aleatorio del entre 0 y tamaño-1
    public int generarAleatorio(int tamaño){
         return (int)(Math.random()*tamaño);
    }
  
    //imprime la cuadricula Banderas
    public void imprimirCuadriculaBanderas(int numPartida){
        for (int i = 0; i < tamañox; i++) {
            for (int j = 0; j <tamañoy; j++) {
                System.out.print(partidasMultiples.get(numPartida).cuadriculaBanderas[i][j]+"|");
            }
            System.out.println("");
        }
        System.out.println("////////////////////");
    }
    
   
    //verifica si la posicion indicada es una orilla
    public boolean esOrilla(int x,int y,int numPartida){
        boolean orilla=false;       
         if(x==0 || x==tamañox-1 || y==0 || y==tamañoy-1){
            orilla=true; 
        }else{
            orilla=false;
        }
        return orilla;
    }
    
    //verifica si hay una mina en la posicion especificada 
    public boolean esMina(int x,int y,int numPartida,int m){
        boolean mina=false;
        if(m==1){
        if(cuadriculaMinas[x][y]==-1){
            mina=true;  
        }
        }else{
            if(partidasMultiples.get(numPartida).cuadriculaMinas[x][y]==-1){
                mina=true;  
            }    
        }
        return mina;
    }
  
    //devuelve la suma de el valor total de minas al rededor de la posicion indicada
    public int obtenerValores(int x,int y,int numPartida){
        int total=0;
        for (int i = 0; i < 8; i++) {
           if(partidasMultiples.get(numPartida).cuadriculaMinas[x+rotacionx[i]][y+rotaciony[i]]==-1){
                total++; 
           }   
        }
        return total;
    }
    
////////////////////////////////

    String ceros="";
    String numeros="";
    public void procesarCeros(int x, int y,String cadena,int numPartida){
       if(cadena!=null){
           if(cadena.equalsIgnoreCase("inicia")){
               cuadriculaMinas=partidasMultiples.get(numPartida).cuadriculaMinas;
               cuadriculaBanderas=partidasMultiples.get(numPartida).cuadriculaBanderas;
               perimetro=partidasMultiples.get(numPartida).perimetro;
               ceros=partidasMultiples.get(numPartida).ceros;
               numeros=partidasMultiples.get(numPartida).numeros;
               if(x<10 && y<10){
                 ceros="0"+x+":0"+y+",0";  
               }else if(x<10){
                 ceros="0"+x+":"+y+",0";  
               }else if(y<10){
                 ceros=x+":0"+y+",0";  
               }else if(x>9 && y>9){
                 ceros=x+":"+y+",0";    
               }
               numeros="";
           }
       }
         for (int a = 0;a<8;a++){
            if (cuadriculaMinas[x+1+rotacionx[a]][y+1+rotaciony[a]] == 9){
 
            } else if ((perimetro[x+rotacionx[a]][y+rotaciony[a]] == 0) && (cuadriculaMinas[x+1+rotacionx[a]][y+1+rotaciony[a]] == 0) && (cuadriculaBanderas[x+1+rotacionx[a]][y+1+rotaciony[a]])==0){
               // if(!ceros.contains((x+rotacionx[a])+":"+(y+rotaciony[a])) && !ceros.contains(("0"+x+rotacionx[a])+":0"+(y+rotaciony[a])) && !ceros.contains(("0"+x+rotacionx[a])+":"+(y+rotaciony[a])) && !ceros.contains((x+rotacionx[a])+":0"+(y+rotaciony[a])) ){
                    int sumax=(x+rotacionx[a]);
                    int sumay=(y+rotaciony[a]);
                    cuadriculaBanderas[sumax+1][sumay+1]=6;
                    if(sumax<10 && sumay<10){
                     ceros+="0"+sumax+":0"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];    
                    }else if(sumax<10){
                     ceros+="0"+sumax+":"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];    
                    }else if(sumay<10){
                      ceros+=sumax+":0"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];       
                    }else if(sumax>9 && sumay>9){
                        ceros+=sumax+":"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];
                    }
                   procesarCeros(x+rotacionx[a], y+rotaciony[a],"",numPartida);  
               //}
            } else if ((perimetro[x+rotacionx[a]][y+rotaciony[a]] != 0) && (cuadriculaMinas[x+1+rotacionx[a]][y+1+rotaciony[a]] == 0) && (cuadriculaBanderas[x+1+rotacionx[a]][y+1+rotaciony[a]])==0){
               //if(!numeros.contains((x+rotacionx[a])+":"+(y+rotaciony[a])) && !numeros.contains(("0"+x+rotacionx[a])+":0"+(y+rotaciony[a])) && !numeros.contains(("0"+x+rotacionx[a])+":"+(y+rotaciony[a])) && !numeros.contains((x+rotacionx[a])+":0"+(y+rotaciony[a]))){
                    int sumax=(x+rotacionx[a]);
                    int sumay=(y+rotaciony[a]);
                    cuadriculaBanderas[sumax+1][sumay+1]=6;
                    if(sumax<10 && sumay<10){
                     numeros+="0"+sumax+":0"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];    
                    }else if(sumax<10){
                     numeros+="0"+sumax+":"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];    
                    }else if(sumay<10){
                     numeros+=sumax+":0"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];       
                    }else if(sumax>9 && sumay>9){
                      numeros+=sumax+":"+sumay+","+perimetro[x+rotacionx[a]][y+rotaciony[a]];
                    }
               //}
            }
        }
        partidasMultiples.get(numPartida).ceros=ceros;
        partidasMultiples.get(numPartida).numeros=numeros;
    }
    
    public boolean finalizarPartida_MinasEncontradas(int numPartida){
        int minasEncontradas = 0;
        try{
            for (int i = 1; i < partidasMultiples.get(numPartida).tamañox-1; i++) {
                for (int j = 1; j < partidasMultiples.get(numPartida).tamañoy-1; j++) {
                    if(partidasMultiples.get(numPartida).cuadriculaMinas[i][j] == -1 && partidasMultiples.get(numPartida).cuadriculaBanderas[i][j] != 0){
                        minasEncontradas ++;
                    }
                }
            }
        }catch(Exception e){
           e.printStackTrace();
        }
        
        if(minasEncontradas == partidasMultiples.get(numPartida).numMinas){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean finalizarPartida_JugadoresPierden(int numPartida){
        boolean estado=false;
        int contadorMuertos=0,contadorNumeroJugadores=0;
        try{
            Map<Integer,String> map=partidasMultiples.get(numPartidas).estadoJugador;
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                Integer key = entry.getKey();
                String value = entry.getValue();
                if(value.equalsIgnoreCase("muerto")){
                    contadorMuertos++;
                }
                contadorNumeroJugadores++;
            }
        }catch(Exception e){
           e.printStackTrace();
        }
        if(contadorMuertos==contadorNumeroJugadores){
            estado=true;
        }
        return estado;
    }
    
    public boolean validarTamañoYMinas(int numPartida){
        int tamañomatriz = partidasMultiples.get(numPartida).tamañox * partidasMultiples.get(numPartida).tamañoy;
        if(partidasMultiples.get(numPartida).numMinas > tamañomatriz){
            return false;
        }else{
            return true;
        }
    }

    
    public int getTamañox(int numPartida) {
        return partidasMultiples.get(numPartida).tamañox;
    }
    public int getTamañoy(int numPartida) {
        return partidasMultiples.get(numPartida).tamañoy;
    }
    public int getNumMinas(int numPartida) {
        return partidasMultiples.get(numPartida).numMinas;
    }
    public void setCuadriculaBanderas(int x, int y, int numJugador,int numPartida) {
        partidasMultiples.get(numPartida).cuadriculaBanderas[x][y] = numJugador;
    }
    public int getCuadriculaBanderas(int x, int y,int numPartida) {
        return partidasMultiples.get(numPartida).cuadriculaBanderas[x][y];
    }
    public String getCeros(int numPartida){
        return partidasMultiples.get(numPartida).ceros;   
    }
    public String getNumeros(int numPartida){
        return partidasMultiples.get(numPartida).numeros;
    } 
    public int getValorPosicion(int x,int y,int numPartida){
        return partidasMultiples.get(numPartida).cuadriculaMinas[x][y];
    }
    public void setSalas(int salas) {
        this.salas = salas;
    }
    public int getSalas() {
        return salas;
    }
    
/////////////////////////PARTIDAS MULTIPLES
    public String agregarJugador(){
       try{    
        if(partidasMultiples.get(numPartidas)!=null){
            if(partidasMultiples.get(numPartidas).tamañoJugadores.size()<4 && !partidasMultiples.get(numPartidas).partidaIniciada){
                partidasMultiples.get(numPartidas).numJugadores++;
                partidasMultiples.get(numPartidas).tamañoJugadores.add(partidasMultiples.get(numPartidas).numJugadores);
                partidasMultiples.get(numPartidas).estadoJugador.put(partidasMultiples.get(numPartidas).numJugadores, "vivo");
            }else{  
                partidasMultiples.get(numPartidas).partidaIniciada=true;
                numPartidas++;    
                Tablero temporal=new Tablero();
                temporal.numJugadores=1;
                temporal.estadoJugador.put(1, "vivo");
                partidasMultiples.get(numPartidas).tamañoJugadores.add(1);
                partidasMultiples.put(numPartidas,temporal);    
            }
        }else{
            Tablero temporal=new Tablero();
            temporal.numJugadores=1;
            temporal.estadoJugador.put(1, "vivo");
            partidasMultiples.get(numPartidas).tamañoJugadores.add(1);
            partidasMultiples.put(numPartidas,temporal);
        }
       }catch(Exception e){
           if(e.toString().contains("java.lang.NullPointerException")){
            Tablero temporal=new Tablero();
            temporal.numJugadores=1;
            partidasMultiples.put(numPartidas,temporal);
            partidasMultiples.get(numPartidas).estadoJugador.put(1, "vivo");
            partidasMultiples.get(numPartidas).tamañoJugadores.add(1);
           }
           
       }
       String numPartidas="";
       if(this.numPartidas<10){
          numPartidas="0"+this.numPartidas;
       }else if(this.numPartidas>9){
           numPartidas=String.valueOf(this.numPartidas);
       }
       String numJ=String.valueOf(partidasMultiples.get(this.numPartidas).numJugadores);
       return numPartidas+":"+numJ;
    }
   
    public void cambiarEstadoJugador(int numPartida,int numJugador){
        partidasMultiples.get(numPartidas).estadoJugador.put(numJugador, "muerto");
    }
    public void salioJugadorDisminuirNumeroJugador(int numPartida,int numJugador){
       partidasMultiples.get(numPartidas).tamañoJugadores.remove(numJugador);
    }
    
    public boolean iniciarPartida(int numPartida){
        if(partidasMultiples.get(numPartida).numJugadores>1){
            partidasMultiples.get(numPartida).partidaIniciada=true;
        }
        return partidasMultiples.get(numPartida).partidaIniciada;
    }
    public void cerrarPartida(int numPartida){
        partidasMultiples.get(numPartida).partidaIniciada=false;
    }
    public boolean getPartidaIniciada(int numPartida){
        return partidasMultiples.get(numPartida).partidaIniciada;
    }
    public void jugadorSalio(int numJugador,int numPartida, Map<Integer,Set> jugadoresMap, Set<PrintWriter> writers, Jugador b){
        cuadriculaBanderas=partidasMultiples.get(numPartida).cuadriculaBanderas;
        for (int i = 0; i < partidasMultiples.get(numPartida).tamañox-1; i++) {
            for (int j = 0; j < partidasMultiples.get(numPartida).tamañoy-1; j++) {
                if(cuadriculaBanderas[i][j]==numJugador){
                    cuadriculaBanderas[i][j]=0;
                    String mensaje = (i-1) + ":" + (j-1) + "-QUITAR_BANDERA";
                    writers =jugadoresMap.get(b.getNumeroDeSalaEnLaQueEstoy());
                    for(PrintWriter escritor : writers){
                        escritor.println(mensaje);
                    }
                }
            }
        }
       partidasMultiples.get(numPartida).cuadriculaBanderas=cuadriculaBanderas;
    }

    
    public String contarPuntos(int numPartida,Set<Integer> numeroJugador){
        int jugador1=0;
        int jugador2=0;
        int jugador3=0;
        int jugador4=0;
        for (int i = 0; i < partidasMultiples.get(numPartida).tamañox-1; i++) {
            for (int j = 0; j < partidasMultiples.get(numPartida).tamañox-1; j++) {
                if(partidasMultiples.get(numPartida).cuadriculaBanderas[i][j]==1){
                    if(partidasMultiples.get(numPartida).cuadriculaMinas[i][j]==-1){
                        jugador1++;
                    }else{
                        jugador1--;
                    }
                }else if(partidasMultiples.get(numPartida).cuadriculaBanderas[i][j]==2){
                    if(partidasMultiples.get(numPartida).cuadriculaMinas[i][j]==-1){
                        jugador2++;
                    }else{
                        jugador2--;
                    }
                }else if(partidasMultiples.get(numPartida).cuadriculaBanderas[i][j]==3){
                    if(partidasMultiples.get(numPartida).cuadriculaMinas[i][j]==-1){
                        jugador3++;
                    }else{
                        jugador3--;
                    }
                }else if(partidasMultiples.get(numPartida).cuadriculaBanderas[i][j]==4){
                    if(partidasMultiples.get(numPartida).cuadriculaMinas[i][j]==-1){
                        jugador4++;
                    }else{
                        jugador4--;
                    }
                }
            }
        }
        String resultado="";
        int mayor1=Math.max(jugador1, jugador2);
        int mayor2=Math.max(jugador3, jugador4);
        int mayormayor=Math.max(mayor1, mayor2);
        if(mayormayor<10 && mayormayor>=0){
            resultado="JG0"+mayormayor;
        }else{
            resultado="JG"+mayormayor;
        }
       if(numeroJugador.contains(1)){
         if(jugador1<10 && jugador1>=0){
             resultado+="J10"+jugador1;
         }else{
             resultado+="J1"+jugador1;
         }
       }
      if(numeroJugador.contains(2)){
         if(jugador2<10 && jugador2>=0){
             resultado+="J20"+jugador2;
         }else{
             resultado+="J2"+jugador2;
         }
      }
       if(numeroJugador.contains(3)){
         if(jugador3<10 && jugador3>=0){
            resultado+="J30"+jugador3;
         }else{
             resultado+="J30"+jugador3;
         }
       }
        if(numeroJugador.contains(4)){
         if(jugador4<10 && jugador4>=0){
             resultado+="J40"+jugador4;
         }else{
             resultado+="J40"+jugador4;
         }
        }
        return resultado;
    }
    
    
}//FIN CLASS
