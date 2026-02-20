/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.model;

import edu.unimetsim.structures.LinkedList;
import edu.unimetsim.structures.Node;
import edu.unimetsim.structures.Queue;

/**
 *
 * @author pinto
 */

public class ProcessManager {
    private Queue<PCB> readyQueue;
    private LinkedList<PCB> blockedQueue;
    private Queue<PCB> queueReadySuspended;
    private LinkedList<PCB> blockedSuspendedQueue;
    private int maxMemorySize;
    private int globalClock;
    private PCB currentProcess;

    public ProcessManager(int maxMemorySize) { //se hace así para evitar romper el encapsulamiento
        this.maxMemorySize = maxMemorySize;    //solo el PrMa es dueño de las colas.
        this.globalClock = 0; 
        this.currentProcess = null; 
        this.readyQueue = new Queue<>();
        this.blockedQueue = new LinkedList<>();
        this.queueReadySuspended = new Queue<>();
        this.blockedSuspendedQueue = new LinkedList<>();
    }
    
    //El proceso va a la RAM o al disco (ready o readySuspended)
    public void addProcess(PCB process){
        int processRam = readyQueue.getSize() + blockedQueue.getSize() + (currentProcess != null ? 1:0);
        if (processRam < maxMemorySize) {
            process.setStatus(ProcessStatus.READY);
            readyQueue.enqueue(process);
            System.out.println("[Reloj " + globalClock + "] (RAM) - Nuevo Proceso: " + process.getName());
        } else{
            process.setStatus(ProcessStatus.READY_SUSPENDED);
            queueReadySuspended.enqueue(process);
            System.out.println("[Reloj " + globalClock + "] (DISCO) - Memoria llena: " + process.getName());
        }
    }
    
    public void dispatch() { //De la cola de listos a la CPU.
        // Solo se despacha si la CPU está libre y hay alguien esperando en RAM
        if (currentProcess == null && !readyQueue.isEmpty()) {
            PCB next = readyQueue.dequeue();
            next.setStatus(ProcessStatus.RUNNING);
            currentProcess = next;
            System.out.println("[Reloj " + globalClock + "] (CPU) - Dispatch: " + next.getName());
        }
    }

    public void runCycle() {
        globalClock++; 
        checkBlocked();
        if (currentProcess != null) { //Se ejecuta 1 ciclo si hay un proceso en el CPU
            currentProcess.executeCycle();
            if (currentProcess.isFinished()) {
                currentProcess.setStatus(ProcessStatus.TERMINATED);
                System.out.println("[Reloj " + globalClock + "] Terminado: " + currentProcess.getName());             
                currentProcess = null;
                checkSwapIn(); //Se checkea si existe un proceso que quiera entrar.
            }
        }
        if (currentProcess == null) {
            dispatch();
        }
    }
    
    // Enviar el proceso actual a la cola de bloqueados porque pidió I/O
    public void blockCurrentProcess(int timeToWait) {
        if (currentProcess != null) {
            currentProcess.setIoWait(timeToWait); //tiempo de bloqueo
            currentProcess.setStatus(ProcessStatus.BLOCKED);
            blockedQueue.add(currentProcess); 
            
            System.out.println("[Reloj " + globalClock + "] (CPU -> Bloqueado) - Esperando I/O: " + currentProcess.getName());

            currentProcess = null; //Se libera el CPU y va el siguiente
            dispatch();
        }
    }

    //Verificar que haya espacio en la RAM para voler a ejecutar procesos suspendidos
    private void checkSwapIn() { 
        // Verificar espacio actual
        int processesInRam = readyQueue.getSize() + blockedQueue.getSize() + (currentProcess != null ? 1 : 0);
        while (processesInRam < maxMemorySize && !queueReadySuspended.isEmpty()) {
            // Traer del disco
            PCB processFromDisk = queueReadySuspended.dequeue();                 
            processFromDisk.setStatus(ProcessStatus.READY); //Cambiar el estatus 
            readyQueue.enqueue(processFromDisk);       
            processesInRam++; // Actualizar contador 
            System.out.println("[Reloj " + globalClock + "] (Disco->RAM) - Swap in: " + processFromDisk.getName());
        }
    }

    //Revisar que los procesos  que estaban esperando en I/O hayan terminado
    private void checkBlocked() {
        //Revisar Bloqueados en RAM
        Queue<PCB> readyToUnblock = new Queue<>(); //cola temporal para guardar los procesos que ya terminaron
        Node<PCB> current = blockedQueue.getHead(); 
        while (current != null) { //Recorrer los bloqueados hasta que no hayan más
            PCB p = current.getData();
            p.decreaseIoWait(); 
            if (p.getIoWait() <= 0) { //Si el Tiemp. espera llega a 0, se crea una cola temporal para sacarlo
                readyToUnblock.enqueue(p); 
            }
            current = current.getNext(); 
        }
        // Sacamos de la cola de bloqueados y movemos a  la cola de listos
        while (!readyToUnblock.isEmpty()) {
            PCB p = readyToUnblock.dequeue();
            blockedQueue.remove(p);
            
            p.setStatus(ProcessStatus.READY);
            readyQueue.enqueue(p);
            System.out.println("[Reloj " + globalClock + "] (RAM) - Desbloqueado: " + p.getName());
        }

        //Revisar Bloqueados en Disco 
        Queue<PCB> readyToUnblockSuspended = new Queue<>();  //cola temporal de suspendidos   
        Node<PCB> currentSuspended = blockedSuspendedQueue.getHead();
        while (currentSuspended != null) { //
            PCB p = currentSuspended.getData();
            p.decreaseIoWait();
            if (p.getIoWait() <= 0) { //Tiempo = 0, 
                readyToUnblockSuspended.enqueue(p);
            }
            currentSuspended = currentSuspended.getNext();
        }
        while (!readyToUnblockSuspended.isEmpty()) {
            PCB p = readyToUnblockSuspended.dequeue(); //se mueve a la cola de listos del disco
            blockedSuspendedQueue.remove(p);
            
            //Como esta en el disco, pasa a Ready_Suspended
            p.setStatus(ProcessStatus.READY_SUSPENDED);
            queueReadySuspended.enqueue(p);
            System.out.println("[Reloj " + globalClock + "] (Disco) - Desbloqueado: " + p.getName());
        }
    }
    
    //Interrupciones de emergencia
    public void triggerEmergencyInterrupt() {
        if (this.currentProcess != null) {
            System.out.println("\n[ALERTA ROJA] Interrupción por micro-meteorito detectada!");
            System.out.println("Suspendiendo ejecución de: " + this.currentProcess.getName());
            
            this.currentProcess.setStatus(ProcessStatus.BLOCKED);
            this.blockedQueue.add(this.currentProcess); //Se cambia el proceso a la cola de bloqueados
            
            //Se vacia el CPU para que el planificador coloque otro proceso en el próximo ciclo
            this.currentProcess = null; 
            
        } else {
            System.out.println("\n[ALERTA ROJA] Interrupción detectada, pero el CPU esta vacío.");
        }
    }

    //Getters y Setters.
    public Queue<PCB> getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(Queue<PCB> readyQueue) {
        this.readyQueue = readyQueue;
    }

    public LinkedList<PCB> getBlockedQueue() {
        return blockedQueue;
    }

    public void setBlockedQueue(LinkedList<PCB> blockedQueue) {
        this.blockedQueue = blockedQueue;
    }

    public Queue<PCB> getQueueReadySuspended() {
        return queueReadySuspended;
    }

    public void setQueueReadySuspended(Queue<PCB> queueReadySuspended) {
        this.queueReadySuspended = queueReadySuspended;
    }

    public LinkedList<PCB> getBlockedSuspendedQueue() {
        return blockedSuspendedQueue;
    }

    public void setBlockedSuspendedQueue(LinkedList<PCB> blockedSuspendedQueue) {
        this.blockedSuspendedQueue = blockedSuspendedQueue;
    }

    public int getMaxMemorySize() {
        return maxMemorySize;
    }

    public void setMaxMemorySize(int maxMemorySize) {
        this.maxMemorySize = maxMemorySize;
    }

    public int getGlobalClock() {
        return globalClock;
    }

    public void setGlobalClock(int globalClock) {
        this.globalClock = globalClock;
    }

    public PCB getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(PCB currentProcess) {
        this.currentProcess = currentProcess;
    }
}
