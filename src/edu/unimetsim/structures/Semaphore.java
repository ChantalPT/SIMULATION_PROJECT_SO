/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.structures;

/**
 *
 * @author pinto
 */
public class Semaphore {
    private int permits;
    public Semaphore(int initial) { this.permits = initial; }
    public synchronized void acquire() {
        while (permits <= 0) {
            try { wait(); } catch (InterruptedException e) {}
        }
        permits--;
    }
    public synchronized void release() {
        permits++;
        notifyAll();
    }
}
