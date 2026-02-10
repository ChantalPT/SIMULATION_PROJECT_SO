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
