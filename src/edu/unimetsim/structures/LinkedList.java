/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.structures;

/**
 *
 * @author pinto
 */
public class LinkedList<T> {
    private Node<T> head;
    private int size;
    
    public LinkedList(){
        this.head = null;
        this.size = 0;
    }
    
    public boolean isEmpty(){
        return head == null;
    }
    
    // Agregar al final de la lista
    public void add(T data) { 
        Node<T> newNode = new Node<>(data);
        if (isEmpty()) {
            head = newNode;
        } else {
            Node<T> current = head;
            while (current.getNext() != null) {
                current = current.getNext();
            }
            current.setNext(newNode);
        }
        size++;
    }

    // Elimina el objeto que se especifique en la lista
    public void remove(T data) {
        if (isEmpty()) return;

        if (head.getData().equals(data)) {
            head = head.getNext();
            size--; //borrar head
            return;
        }


        Node<T> current = head;
        while (current.getNext() != null && !current.getNext().getData().equals(data)) {
            current = current.getNext(); //buscar para borrar.
        }

        if (current.getNext() != null) {
            current.setNext(current.getNext().getNext());
            size--; //borrarlo cuando lo encuentre.
        }
    }

    
    public T get(int index) {
        if (index < 0 || index >= size) return null; //obetener mediante index
        
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    public int getSize() {
        return size;
    }
    
    //obtener solo la cabeza
    public Node<T> getHead() {
        return head;
    }
}

