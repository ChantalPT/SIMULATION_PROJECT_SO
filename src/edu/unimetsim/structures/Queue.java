/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.structures;

/**
 *
 * @author pinto
 * @param <T> tipo de dato que se guarda.
 */
public class Queue<T> {
    private Node<T> head; // Inicio, final y tamaño de la cola
    private Node<T> tail;
    private int size; 
    
    public Queue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
    
    public boolean isEmpty () { // Confirma que la queue esta vacia
        return head == null;
    }
    
    public void enqueue(T data) { //Encolar. Agrega al final de la cola o si esta
                                  //vacia newNode se convierte en cabeza y cola
        Node<T>  newNode = new Node<>(data);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }
    
    public T dequeue() {
        if (isEmpty()){
            return null;
        }
        T data = head.getData(); // Guarda el dato antes de cambiar head
        head = head.getNext(); //El head.getNext se vuelve la principal
        
        if (head == null) { //Si queda vacia, todo debe ser null (cabeza y cola)
            tail = null;
        }
        
        size--;
        return data;
    }
    
    public int getSize() {
        return size;
    }
}
