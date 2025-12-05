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

    @Override                       //perché NetBeans me lo richiede?
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
                
                Thread.sleep(TP + r.nextInt(DP + 1));           //specificare
                rc.put(progressivo, a, tot);        //inserisce nel ResultCollector --> da implementare
                
            }
        } catch (InterruptedException e){
            System.out.println("ProcessorThread numero " + s + " --> terminato");
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
    private final Semaphore vuoti;                      //per gli spazi liberi o pieni
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
        
        for(int i = 0; i < K; i++)
            pieni.release();            //rilasciati i restanti ancora pieni
        
        return vett;
    }
    
    // qualcosa per le richieste finali da aggiungere
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