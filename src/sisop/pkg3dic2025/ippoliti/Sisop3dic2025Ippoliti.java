/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sisop.pkg3dic2025.ippoliti;

/**
 *
 * @author Gabriele
 */

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Sisop3dic2025Ippoliti {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        GeneratorThread gt = new GeneratorThread();
        ProcessorThread pt = new ProcessorThread();
        Queue q = new Queue(L);
        int N = System.in.writeln();

        while (true) {
            gt.start();
            for (int i = 0; i < N; i++)
                pt.start();

            Sleep(10000);
        }
    }
}

class GeneratorThread extends thread {

    private int TG = 0;
    private Queue q;

    public void run(int TG, Queue q) throw InterruptedException {
        this.TG = TG;
        this.q = q;

        try {
            while (true && q) {
                TG++;
                put(TG);
                Sleep(1000);
            }
        } catch {
            InterruptedException e
        }
    }
}

class PrecessorThread extends thread {

    private int N;
    private Queue q;
    private Semaphore mutex;
    public int v;

    public void run(Queue q, int N, Semaphore mutex) {
        this.q = q;
        this.n = n;
        this.v = v;

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

class Queue {

    public void Queue(int L) {
        this.L = L
        ArrayList<int> a = new ArraiList(L);
    }

    public void Put(int v) {
        this.v = v;
        a.add(v);
    }

    public void Remove() {
        a.remove(0);
    }
}

class ResultCollector {

    private Semaphore mutex = new Semaphore(1);

    public void ResultCollector(Queue q, Semaphore mutex) {
        this.q = q;

        mutex.aquire();
        mutex.relise();
    }
}