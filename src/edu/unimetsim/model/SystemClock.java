/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.model;

/**
 *
 * @author pinto
 */

public class SystemClock extends Thread {
    
    private ProcessManager kernel;
    private int speedMs; //Velocidad en milisegundos 
    private boolean running;

    public SystemClock(ProcessManager kernel, int speedMs) {
        this.kernel = kernel;
        this.speedMs = speedMs;
        this.running = true;
    }

    @Override
    public void run() {
        System.out.println("Iniciando reloj del sistema");
        
        while (running) {
            kernel.runCycle(); //Se ejecuta el ciclo del CPU y memoria
            try {
                Thread.sleep(speedMs);  //Se pausa el hilo para simular el paso del tiempo real
            } catch (InterruptedException e) {
                System.out.println("(SISTEMA) El reloj fue interrumpido.");
                break;
            }
        }
        System.out.println("Reloj detenido");
    }

    public void stopClock() { //Apagar el reloj cuando se detenga la simulacion
        this.running = false;
    }
}