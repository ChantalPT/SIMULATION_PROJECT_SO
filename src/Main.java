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

public class Main {

    public static void main(String[] args) {
        
        System.out.println("=== INICIANDO SIMULADOR ===");

        // 1. INICIALIZAR EL KERNEL
        // Parámetros: Memoria Máxima (4 procesos en RAM), Algoritmo Inicial ("SRT"), Quantum (2)
        ProcessManager kernel = new ProcessManager(4, "SRT", 2);

        System.out.println("--- GENERANDO PROCESOS INICIALES ALEATORIOS ---");

        // 2. CREAR PROCESOS AUTOMÁTICAMENTE (Cumpliendo la página 4 del PDF)
        int cantidadInicial = 6; // Arrancamos con 6 procesos para probar el Swapping y la RAM
        
        for (int i = 0; i < cantidadInicial; i++) {
            // Le enviamos "0" como arrivalTime porque están llegando en el ciclo 0
            PCB p = ProcessGenerator.generateRandomProcess(0); 
            
            System.out.println("Creado: " + p.getName() + 
                               " | Inst: " + p.getRemainingTime() + 
                               " | Prio: " + p.getPriority() + 
                               " | Deadline: " + p.getDeadline());
                               
            kernel.addProcess(p);
        }

        System.out.println("\n--- ARRANCANDO EL RELOJ DEL SISTEMA ---");

        // 3. ENCENDER EL RELOJ
        // Le pasamos el kernel y configuramos la velocidad: 1 ciclo = 1000 milisegundos (1 segundo)
        SystemClock clock = new SystemClock(kernel, 1000);
        
        // ¡Damos vida al simulador!
        clock.start();
        
        // El programa seguirá corriendo en segundo plano mostrando los tics en la consola
    }
}