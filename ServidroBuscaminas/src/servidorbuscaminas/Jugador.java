//
package servidorbuscaminas;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Jugador {
    private int puntaje;
    private PrintWriter out;
    private int banderas;
    private int numJugador;
    private boolean SigoJugando = true;
    private int numeroDeSalaEnLaQueEstoy;
    
    
    /*
    public Jugador(int númeroJugador,int puntaje, PrintWriter out, int banderas) {
        this.númeroJugador = númeroJugador;
        this.puntaje = puntaje;
        this.out = out;
        this.banderas = banderas;
    }*/

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void setBanderas(int banderas) {
        this.banderas = banderas;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public PrintWriter getOut() {
        return out;
    }

    public int getBanderas() {
        return banderas;
    }
    
    public int getJugador(){
        return numJugador;
    }
    
    public void setJugador(int numJugador){
        this.numJugador = numJugador;
    }

    public void setSigoJugando(boolean SigoJugando) {
        this.SigoJugando = SigoJugando;
    }

    public boolean isSigoJugando() {
        return SigoJugando;
    }

    public void setNumeroDeSalaEnLaQueEstoy(int numeroDeSalaEnLaQueEstoy) {
        this.numeroDeSalaEnLaQueEstoy = numeroDeSalaEnLaQueEstoy;
    }

    public int getNumeroDeSalaEnLaQueEstoy() {
        return numeroDeSalaEnLaQueEstoy;
    }
       
    
    
}
