/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.model;

import edu.unimetsim.view.SimulatorWindow;


/**
 *
 * @author pinto
 */

public class SystemClock extends Thread {
    private ProcessManager kernel;
    private int delay; //Velocidad en milisegundos 
    private boolean running;
    private SimulatorWindow window;

    public SystemClock(ProcessManager kernel, int delay, SimulatorWindow window) {
        this.kernel = kernel;
        this.delay = delay;
        this.running = true;
        this.window = window;
    }

    @Override
    public void run() {
        System.out.println("Iniciando reloj del sistema");
        
        while (running) {
            kernel.getMutex().acquire();
            try {
                kernel.runCycle(); //Se ejecuta el ciclo del CPU y memoria
                if (window != null) {
                window.refreshMissionControl();
            } 
            }finally {
                kernel.getMutex().release();
            }
            try {
                Thread.sleep(delay);  //Se pausa el hilo para simular el paso del tiempo real
            } catch (InterruptedException e) {
                System.out.println("(SISTEMA) El reloj fue interrumpido.");
                break;
            }
        }
        System.out.println("Reloj detenido");
    }
    
    public void setSpeedMs(int newSpeedMs) { //Cambiar velocidad
        this.delay = newSpeedMs;
        System.out.println("(SISTEMA) Velocidad del reloj actualizada a: " + newSpeedMs + " ms");
    }
    
    public void stopClock() { //Apagar el reloj cuando se detenga la simulacion
        this.running = false;
    }
}