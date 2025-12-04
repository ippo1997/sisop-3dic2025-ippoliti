/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sisop.pkg3dic2025.ippoliti;

/**
 *
 * @author Gabriele
 * - visabilità aggiunta a tutte le classi
 * - aggiunti costruttori mancanti
 * - aggiunti semafori mancanti
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.Scanner;
import java.util.Random;

public class Sisop3dic2025Ippoliti {

    /**
     * @param args the command line arguments
     * 
     */
    public static void main(String[] args) throws InterruptedException {
        Scanner input = new Scanner(System.in);
        int N = input.nextInt();
        int L = input.nextInt();
        
        input.close(); 
        GeneratorThread gt = new GeneratorThread(q, TG);
        ProcessorThread[] pt = new ProcessorThread[N];
        Queue q = new Queue(L);


        /* while(true) {
            gt.start();
            for(int i = 0; i < N; i++)
                pt.start();

            Thread.sleep(10000);
        } */
    }
}

class GeneratorThread extends Thread {

    private int TG = 0;             
    private final Queue<Integer> q;
    private int n = 0;              //numero generato
    public int count = 0;           //numero progressivo di generazione del valore
    
    public GeneratorThread(Queue<Integer> q, int TG) {
        this.TG = TG;
        this.q = q;
    }

    @Override
    public void run() {
        /*this.TG = TG; ---> nel costruttore
        this.q = q;*/ 

        try {
            while(true) {
                q.put(n);
                n++;
                count++;
                Thread.sleep(TG);       
            }
        }
        catch(InterruptedException e) {
            System.out.println("GeneratorThread terminato");
        }
    }
}

class ProcessorThread extends Thread {

    private final int s;             //numero seriale del thread
    private final int K;
    private final int TP;
    private final int DP;
    private final Queue<Integer> q;
    private final ResultCollector rc;
    private final Random r = new Random();
    private int num = 0; 
    public int count = 0;
    
    public ProcessorThread(int s, int K, int TP, int DP, Queue<Integer> q, ResultCollector rc) {
        this.q = q;
        this.s = s;
        this.K = K;
        this.TP = TP;
        this.DP = DP;
        this.rc = rc;
    }
    
    public void run() {
        try {
            mutex.aquire();
            v = q.Get();
            q.Remove(0);
        } catch {
            InterruptedException e
        }
    }
}

class PrintThread {
    public void Print() {

    }
}

class Queue<T> {         //T serve per il tipo generico della coda
    private final ArrayList<T> a;
    private final int L;
    private final Semaphore mutex = new Semaphore(1);   //semafori necessari
    private final Semaphore vuoti;                            //per gli spazi, uguale riga sotto
    private final Semaphore pieni;
    
    public Queue(int L) {          //tolto void perché costruttore
        this.L = L;
        a = new ArrayList<>(L);    //sintassi sbagliata tolto ArrayList<int>
        vuoti = new Semaphore(L);  // specificare
        pieni = new Semaphore(0);
    }

    public void put(T v) throws InterruptedException {
        //this.v = v; non serve
        vuoti.acquire();        //aspetta se la coda è piena bloccando il GeneratorThread
        mutex.acquire();        //serve per far entrare un thread alla volta
        a.add(v);               
        mutex.release();
        pieni.release();        //c'è qualcosa in più da prelevare
    }

    public void remove() throws InterruptedException {
        pieni.acquire(); //l'inverso di quanto sopra, ci deve essere almeno qualcosa
        mutex.acquire();
        a.remove(0);
        mutex.release();
        vuoti.release(); //libera uno spazio
    }
    
    public T get() throws InterruptedException {
        pieni.acquire();
        mutex.acquire();
        T v = a.get(0);         //rende il valore inserito più vecchio
        mutex.release();
        vuoti.release(); // specificare
        
        return v;
    }
    
    // qualcosa per le richieste finali da aggiungere
}

class ResultCollector {

    private Semaphore mutex = new Semaphore(1);

    public void ResultCollector(Queue q, Semaphore mutex) {
        this.q = q;

        mutex.aquire();
        mutex.relise();
    }
}