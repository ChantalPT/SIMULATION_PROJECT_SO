/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.model;

import edu.unimetsim.structures.LinkedList;
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
            System.out.println("[Clock " + globalClock + "] (RAM) - Nuevo Proceso: " + process.getName());
        } else{
            process.setStatus(ProcessStatus.READY_SUSPENDED);
            queueReadySuspended.enqueue(process);
            System.out.println("[Clock " + globalClock + "] (DISCO) - Memoria llena: " + process.getName());
        }
    }
    
    

    public void dispatch() { //De la cola de listos a la CPU.
        // Solo se despacha si la CPU está libre y hay alguien esperando en RAM
        if (currentProcess == null && !readyQueue.isEmpty()) {
            PCB next = readyQueue.dequeue();
            next.setStatus(ProcessStatus.RUNNING);
            currentProcess = next;
            System.out.println("[Clock " + globalClock + "] (CPU) - Dispatch: " + next.getName());
        }
    }

    public void runCycle() {
        globalClock++; 
        if (currentProcess != null) { //Se ejecuta 1 ciclo si hay un proceso en el CPU
            currentProcess.executeCycle();
            if (currentProcess.isFinished()) {
                currentProcess.setStatus(ProcessStatus.TERMINATED);
                System.out.println("[Clock " + globalClock + "] Terminado: " + currentProcess.getName());             
                currentProcess = null;
                checkSwapIn(); //Se checkea si existe un proceso que quiera entrar.
            }
        }
        if (currentProcess == null) {
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
            System.out.println("[Clock " + globalClock + "] (Disco->RAM) - Swap in: " + processFromDisk.getName());
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
