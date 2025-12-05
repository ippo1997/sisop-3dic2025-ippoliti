/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sisop.pkg3dic2025.ippoliti;

/**
 *
 * @author Gabriele
 * - aggiunti costruttori e metodi mancanti
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
        System.out.print("Inserire numero ProcessorThread = ");
        int N = input.nextInt();
        System.out.print("Inserire dimensione della coda = ");
        int L = input.nextInt();
        System.out.print("Inserire dimensione dei messaggi = ");
        int K = input.nextInt();
        System.out.print("Inserire il tempo di generazione dei valori = ");
        int TG = input.nextInt();
        System.out.print("Inserire il tempo minimo di elaborazione del messaggio = ");
        int TP = input.nextInt();
        System.out.print("Inserire intervallo massimo rispeto al tempo minimo = ");
        int DP = input.nextInt();
        System.out.print("Inserire l'intervallo di tempo tra un messaggio e l'altro = ");
        int IT = input.nextInt();
        input.close(); 
        
        Queue<Integer> q = new Queue(L);
        GeneratorThread gt = new GeneratorThread(q, TG);
        ProcessorThread[] pt = new ProcessorThread[N];
        ResultCollector rc = new ResultCollector(N);
        
        /* 
        ---vecchio ciclo---
        while(true) {
            gt.start();
            for(int i = 0; i < N; i++)
                pt.start();

         --- sleep fuori ciclo ---   
        } */
        
        for(int i = 0; i < N; i++)
            pt[i] = new ProcessorThread(i+1, K, TP, DP, q, rc);
        
        PrintThread print1 = new PrintThread(rc, IT);
        PrintThread print2 = new PrintThread(rc, IT);

        gt.start();
        for(ProcessorThread processor : pt) //avvio
            processor.start();
        print1.start();
        print2.start();
        
        Thread.sleep(10000);
        
        gt.interrupt();
        for(ProcessorThread processor : pt) //interrompi
            processor.interrupt();
        print1.interrupt();
        print2.interrupt();
        
        gt.join();
        for(ProcessorThread processor : pt) //attendi (specificare perché)
            processor.join();
        print1.join();
        print2.join();
        
        System.out.println("GeneratorThread totali " + gt.getCount());
          
        for(int i = 0; i < N; i++) {
            System.out.println("ProcessorThread " + (i+1) + "--> processati " + pt[i].getCount());
        }
        
        System.out.println("PrintThread1 --> " + print1.getCount());
        System.out.println("PrintThread2 --> " + print2.getCount());
        
        try {
            System.out.println("Valori ancora in coda " + q.dimensione());
        } catch (InterruptedException e) {                                          //specificare
            System.out.println("Messaggi nel ResultCollector: " + rc.attesa());
        }
    }
}

class GeneratorThread extends Thread {

    private int TG = 0;             
    private final Queue<Integer> q;
    private int n = 0;              //numero generato attualmente
    public int count = 0;           //conteggio
    
    public GeneratorThread(Queue<Integer> q, int TG) {
        this.TG = TG;
        this.q = q;
    }

    @Override                       //perché NetBeans me lo richiede?
    public void run() {
        /*this.TG = TG; ---> nel costruttore
        this.q = q;*/ 

        try {
            while(true) {           //perché trovato meglio !isInterrupted() rispetto a true?
                q.put(n);
                n++;
                count++;
                Thread.sleep(TG);       
            }
        }
        catch(InterruptedException e) {}
        
        System.out.println("GeneratorThread terminato. Tot " + count); //specificare prodotti cosa
    }
    
    public int getCount() {
        return count;
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
    private int p = 0;              //specificare numero progressivo indipendente
    public int count = 0;
    
    public ProcessorThread(int s, int K, int TP, int DP, Queue<Integer> q, ResultCollector rc) {
        this.q = q;
        this.s = s;
        this.K = K;
        this.TP = TP;
        this.DP = DP;
        this.rc = rc;
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                Integer[] a = q.getnum(K);            //aspetta K elementi per prenderli rimuovendo solo il primo
                int progressivo = p;
                p++;
                count++;
                
                int somma = 0;
                for (int v : a)
                    somma = somma + v;
                int tot = somma * s;                  //calcola il risultato
                
                Thread.sleep(TP + r.nextInt(DP + 1));           //tempo variabile
                Messaggio m = new Messaggio(progressivo, a, tot);
                rc.put(m);        //inserisce nel ResultCollector --> da implementare Messaggio
                
            }
        } catch (InterruptedException e){
            System.out.println("ProcessorThread numero " + s + " --> terminato");
        }
        System.out.println("ProcessorThread totali: " + count);
    }
    
    public int getCount() {
        return count;
    }
}

class PrintThread extends Thread {
    private final ResultCollector rc;
    private final int IT;
    private int count = 0;
    
    public PrintThread(ResultCollector rc, int IT) {
        this.rc = rc;
        this.IT = IT;
    }
    
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Messaggio m = rc.get(); // prende messaggio successivo se ce ne sono
                System.out.print("Messaggio " + m.k + ": [");
                for (Object o : m.v)
                    System.out.print(o + " ");
                System.out.println("] --> " + m.res);
                count++;
                Thread.sleep(IT);
            }
        } catch (InterruptedException e) {
            // terminazione richiesta
        }
        System.out.println("PrintThread terminato, stampati: " + count);
    }

    public int getCount() {
        return count;
    }
}

class Queue<T> {         //T serve per il tipo generico della coda
    private final ArrayList<T> a;
    private final int L;
    private final Semaphore mutex = new Semaphore(1);   //semafori necessari
    private final Semaphore vuoti;                      //per gli spazi liberi e pieni
    private final Semaphore pieni;
    
    public Queue(int L) {          //tolto void perché costruttore
        this.L = L;
        this.a = new ArrayList<>(L);    //sintassi sbagliata tolto ArrayList<int>
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
    
    public T[] getnum(int K) throws InterruptedException {
        for(int i = 0; i < K; i++)
            pieni.acquire();
        
        mutex.acquire();                //entra in sezione critica per leggere i valori
        T[] vett = (T[]) new Object[K]; //specificare
        for(int i = 0; i < K; i++)
            vett[i] = a.get(i);
        a.remove(0);                    //liberato lo spazio contenente il più vecchio
        mutex.release();
        vuoti.release();                //rilasciato il vuoto
        
        for(int i = 1; i < K; i++)
            pieni.release();            //rilasciati i restanti ancora pieni
        
        return vett;
    }
    
    public int dimensione() throws InterruptedException {
        mutex.acquire();
        int d = a.size();
        mutex.release();
        
        return d;
    }
}

class ResultCollector {

    private final Semaphore mutex = new Semaphore(1);
    private final Semaphore[] libera;
    private final Semaphore[] piena;

    private final int N;
    private final Messaggio[] messaggio;
    private int successiva = 0;
    
    public ResultCollector(int N) {
        this.N = N;
        this.messaggio = new Messaggio[N];
        this.piena = new Semaphore[N];
        this.libera = new Semaphore[N];
        
        for(int i = 0; i < N; i++) {
            libera[i] = new Semaphore(1);
            piena[i] = new Semaphore(0);
        }
    }
    
    public void put(Messaggio m) throws InterruptedException {
        int pos = m.k % N;
        
        libera[pos].acquire();      //attende posizione libera
        messaggio[pos] = m;                 //scrive
        piena[pos].release();       //ora è piena
    }
    
    public Messaggio get() throws InterruptedException {
        mutex.acquire();
        int pos = successiva;
        successiva = (successiva + 1) % N;
        
        piena[pos].acquire();
        Messaggio m = messaggio[pos];
        libera[pos].release();
        
        mutex.release();
        return m;
    }
    
    public int attesa() {
        int c = 0;
        for (Messaggio m : messaggio)
            if (m != null) c++;
        return c;
    }
}

class Messaggio {
    public final Object[] v;        //vettore di K valori
    public final int k;             //numero del k-esimo valore del vettore
    public final int res;           //risultato di Precessor
    
    public Messaggio(int k, Object[] v, int res) {
        this.k = k;
        this.v = v;
        this.res = res;
    }
}