/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.unimetsim.structures;

/**
 *
 * @author pinto
 * @param <T> Tipo de dato que tendrá el nodo.
 */
public class Node <T>{
    private T data;  // contenido del nodo
    private Node<T> next; // apuntador al siguiente nodo
    
    public Node(T data) {
        this.data = data;
        this.next = null;
    }
    
    public T getData(){
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }
    
    
}
