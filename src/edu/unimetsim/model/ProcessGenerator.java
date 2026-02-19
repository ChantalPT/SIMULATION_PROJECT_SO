/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.model;

import java.util.Random;
/**
 *
 * @author pinto
 */
public class ProcessGenerator {
private static int nameNumber = 1;     
    //Tiempo actual (reloj) para guardarlo como arrivalTime
    public static PCB generateRandomProcess(int currentTime) {
        Random rand = new Random();
        
        String name = "P_Aleatorio_" + nameNumber;
        nameNumber++; 
        
        int instructions = rand.nextInt(11) + 2; //Entre 2 y 12
        int priority = rand.nextInt(5) + 1;      //Entre 1 y 5
        int deadline = instructions + rand.nextInt(20) + 5; 
        int arrivalTime = currentTime; 
        
        PCB newProcess = new PCB(name, instructions, priority, deadline, arrivalTime);
        return newProcess;
    }
}
