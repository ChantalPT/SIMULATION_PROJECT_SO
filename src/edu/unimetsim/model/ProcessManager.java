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
    private String currentPolicy; // Puede ser "FCFS, RR, SRT, Prioridad, RMS y EDF"
    private int quantum;          // Solo se usa si es "RR"
    private int currentQuantumTicks; //Tiempo que lleva el proceso actual en el CPU

    public ProcessManager(int maxMemorySize, String initialPolicy, int quantum) { //se hace así para evitar romper el encapsulamiento
        this.maxMemorySize = maxMemorySize;    //solo el PrMa es dueño de las colas.
        this.globalClock = 0; 
        this.currentProcess = null; 
        this.readyQueue = new Queue<>();
        this.blockedQueue = new LinkedList<>();
        this.queueReadySuspended = new Queue<>();
        this.blockedSuspendedQueue = new LinkedList<>();
        this.currentPolicy = initialPolicy;
        this.quantum = quantum;
        this.currentQuantumTicks = 0;
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
            PCB next = extractBestProcess();
            next.setStatus(ProcessStatus.RUNNING);
            currentProcess = next;
            currentQuantumTicks = 0;
            System.out.println("[Reloj " + globalClock + "] (CPU) - Dispatch: " + next.getName());
        }
    }
    
    private void preemptCurrentProcess() { //Expulsar del CPU por el fin de Quantum
        if (currentProcess != null) {
            System.out.println("[Reloj " + globalClock + "] (CPU -> RAM) - Fin de Quantum (RR): " + currentProcess.getName());
            currentProcess.setStatus(ProcessStatus.READY);
            readyQueue.enqueue(currentProcess); //Va al final de la cola de listos
            currentProcess = null; //Se libera el CPU y 
            currentQuantumTicks = 0;//reinicia el contador
        }
    }

    public void runCycle() {
        globalClock++; 
        checkBlocked(); //checkear si alguien termino su I/O
        checkSwapOut(); //checkear si hay que sacar a alguien de bloqueado al disco para ganar espacio
        checkSwapIn();  //checkear si no hay espacio en la RAM para buscar a alguien del disco
        if (currentProcess != null) { //Se ejecuta 1 ciclo si hay un proceso en el CPU
            currentProcess.executeCycle();
            currentQuantumTicks++; //Tiempo que lleva en el CPU
            if (currentProcess.isFinished()) {
                currentProcess.setStatus(ProcessStatus.TERMINATED);
                System.out.println("[Reloj " + globalClock + "] Terminado: " + currentProcess.getName());              
                currentProcess = null;
                currentQuantumTicks = 0; //Se reinicia el contador
                checkSwapIn(); //Se checkea si existe un proceso que quiera entrar.
            }
            //Round Robin
            else if (currentPolicy.equals("RR") && currentQuantumTicks >= quantum) {
                preemptCurrentProcess(); //Si se acabo el tiempo se fuerza su salida
            }
            //SRT, Priority y EDF
            else if (shouldPreemptCurrentProcess()) {
                System.out.println("[Reloj " + globalClock + "] ¡INTERRUPCIÓN! Apropiación por política: " + currentPolicy);
                preemptCurrentProcess(); // Lo botamos de la CPU
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
    
    private void checkSwapOut() {
        //verificar espacio actual, se expulsan los procesos al disco si la RAM 
        //está llena y hay procesos esperando por entrar
        int processesInRam = readyQueue.getSize() + blockedQueue.getSize() + (currentProcess != null ? 1 : 0);
        while (processesInRam >= maxMemorySize && !queueReadySuspended.isEmpty() && blockedQueue.getHead() != null) {

            PCB process = blockedQueue.getHead().getData(); //se elige el primer p de la cola de bloqueados en RAM
            blockedQueue.remove(process); //se saca de la RAM
            process.setStatus(ProcessStatus.BLOCKED_SUSPENDED);
            blockedSuspendedQueue.add(process);
            processesInRam--; //Se libera un espacio

            System.out.println("[Reloj " + globalClock + "] (RAM->Disco) - Swap out: " + process.getName() + " para liberar espacio.");
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
    
    private PCB extractBestProcess() {
        if (readyQueue.isEmpty()) return null;

        // Si es FCFS o Round Robin, se saca al primero 
        if (currentPolicy.equals("FCFS") || currentPolicy.equals("RR")) {
            return readyQueue.dequeue(); 
        }

        // Para SRT, Priority, EDF tenemos se evalua cada proceso
        PCB best = readyQueue.dequeue();
        Queue<PCB> tempQueue = new Queue<>();

        while (!readyQueue.isEmpty()) {
            PCB candidate = readyQueue.dequeue();
            boolean isBetter = false;

            switch (currentPolicy) {
                case "SRT":
                    if (candidate.getRemainingTime() < best.getRemainingTime()) isBetter = true;
                    break;
                case "PRIORITY": //1>prioridad // 5<prioridad
                    if (candidate.getPriority() < best.getPriority()) isBetter = true;
                    break;
                case "EDF":
                    if (candidate.getDeadline() < best.getDeadline()) isBetter = true;
                    break;
            }

            if (isBetter) {
                tempQueue.enqueue(best); 
                best = candidate;        
            } else {
                tempQueue.enqueue(candidate);
            }
        }

        //Se devuelve a todos los demás a la cola de listos original
        while (!tempQueue.isEmpty()) {
            readyQueue.enqueue(tempQueue.dequeue());
        }

        return best;
    }
    
    private boolean shouldPreemptCurrentProcess() { //Se debe cambiar quien usa el CPU?
        if (readyQueue.isEmpty() || currentProcess == null) return false; 
        //FCFS no expulsa a nadie, y RR se expulsa por Quantum, no por quién está en la cola.
        if (currentPolicy.equals("FCFS") || currentPolicy.equals("RR")) return false;
                                                    //Se revisa la cola de listos
        boolean needsPreemption = false;            //si existe alguien que merezca mas el CPU
        Queue<PCB> tempQueue = new Queue<>();       //que el proceso actual.

        //Se revisam a todos los procesos
        while (!readyQueue.isEmpty()) {
            PCB candidate = readyQueue.dequeue();
            
            if (currentPolicy.equals("SRT") && candidate.getRemainingTime() < currentProcess.getRemainingTime()) needsPreemption = true;
            if (currentPolicy.equals("PRIORITY") && candidate.getPriority() < currentProcess.getPriority()) needsPreemption = true;
            if (currentPolicy.equals("EDF") && candidate.getDeadline() < currentProcess.getDeadline()) needsPreemption = true;
            
            tempQueue.enqueue(candidate);
        }

        while (!tempQueue.isEmpty()) {
            readyQueue.enqueue(tempQueue.dequeue());
        }

        return needsPreemption;
    }

    //Interrupciones de emergencia
    public void triggerEmergencyInterrupt() {
        if (this.currentProcess != null) {
            System.out.println("\n[ALERTA ROJA] Interrupción por micro-meteorito detectada!");
            System.out.println("Suspendiendo ejecución de: " + this.currentProcess.getName());
            this.currentProcess.setIoWait(10);//Se queda por 10 ciclos para verlo en interfaz
            this.currentProcess.setStatus(ProcessStatus.BLOCKED);
            this.blockedQueue.add(this.currentProcess); //Se cambia el proceso a la cola de bloqueados
            
            //Se vacia el CPU para que el planificador coloque otro proceso en el próximo ciclo
            this.currentProcess = null; 
            
        } else {
            System.out.println("\n[ALERTA ROJA] Interrupción detectada, pero el CPU esta vacío.");
        }
    }

    //Getters y Setters.
    public void setPolicy(String newPolicy, int newQuantum) {
        this.currentPolicy = newPolicy;
        this.quantum = newQuantum;
        this.currentQuantumTicks = 0; // Reiniciamos la cuenta al cambiar
        System.out.println("(SISTEMA) Cambio de política de planificación a: " + newPolicy);
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
