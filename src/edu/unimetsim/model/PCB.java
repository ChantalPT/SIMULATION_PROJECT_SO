/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.model;

/**
 *
 * @author pinto
 */
public class PCB {
    private static int idCounter = 1; 
    private int id;                 
    private String name;        
    private ProcessStatus status;
    private int programCounter;     
    private int mar;                //Direc. de memoria actual.
    private int totalInstructions;  
    private int priority;           
    private int deadline;           
    private int arrivalTime;        //Ciclo en el que llegó al sistema.
    private int burstTime;      // Para calcular el RST (tiempo más corto)
    private int remainingTime;      // Tiempo restante de ejecución

    public PCB(String name, int totalInstructions, int priority, int deadline, int arrivalTime) {
        this.id = idCounter++;
        this.name = name;
        this.totalInstructions = totalInstructions;
        this.priority = priority;
        this.deadline = deadline;
        this.arrivalTime = arrivalTime;
        this.programCounter = 0;
        this.mar = 0; 
        this.status = ProcessStatus.NEW; 
        this.remainingTime = totalInstructions; 
    }

    public void executeCycle() { //Ejecuta 1 ciclo de instrucción simulado.
        if (remainingTime > 0) {
            programCounter++; 
            mar++;            
            remainingTime--;
        }
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }
    
    //Getters y Setters.
    public int getId() { return id; }
    public String getName() { return name; }
    
    public ProcessStatus getStatus() { return status; }
    public void setStatus(ProcessStatus status) { this.status = status; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public int getDeadline() { return deadline; }
    
    public int getProgramCounter() { return programCounter; }
    public int getMar() { return mar; }
    
    public int getRemainingTime() { return remainingTime; }
    public int getArrivalTime() { return arrivalTime; }
    
    @Override
    public String toString() {
        return "PCB{ID=" + id + ", Name=" + name + ", Status=" + status + "}";
    }
}

