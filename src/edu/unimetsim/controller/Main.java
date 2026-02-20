package edu.unimetsim.controller;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author pinto
 */

import edu.unimetsim.model.PCB;
import edu.unimetsim.model.ProcessManager;
import edu.unimetsim.model.ProcessGenerator;
import edu.unimetsim.model.SystemClock;
import edu.unimetsim.view.SimulatorWindow;

public class Main {

    public static void main(String[] args) {
        
        System.out.println(" INICIANDO SIMULADOR UNIMET ");
        ProcessManager kernel = new ProcessManager(4, "SRT", 2); //Iniciar kernel

        System.out.println(" GENERANDO PROCESOS INICIALES ");
        int cantidadInicial = 6; 
        
        for (int i = 0; i < cantidadInicial; i++) {
            PCB p = ProcessGenerator.generateRandomProcess(0); 
            kernel.addProcess(p);
        }

        System.out.println("\n INICIANDO INTERFAZ GRÁFICA ");
        SimulatorWindow window = new SimulatorWindow(); //Crear ventana
        window.setKernel(kernel); //
        
        window.setLocationRelativeTo(null); //Centrar pantalla
        window.setVisible(true); //Mostrarlo

        System.out.println(" CORRIENDO RELOJ DEL SISTEMA ");

        //Encender reloj (kernel, la velocidad y la ventana)
        SystemClock clock = new SystemClock(kernel, 1000, window);
        clock.start();
    }
}