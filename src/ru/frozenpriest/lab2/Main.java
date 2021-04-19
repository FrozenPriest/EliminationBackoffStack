package ru.frozenpriest.lab2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    PrintWriter out;
    int N = 200000;
    Random random;


    public Main() {
        try {
            out = new PrintWriter(new FileOutputStream("output.txt"), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        random = new Random();
    }

    public static void main(String[] args) {
        Main main = new Main();
        for (int i = 1; i < 200; i++) {
            main.testBench(i);
        }
    }

    private static void log(String msg) {
        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getId() + " " + msg);
    }

    private void testBench(int threadCount) {
        double time = 0;
        var N = this.N / threadCount;
        for (int k = 0; k < 10; k++) {
            EliminationBackoffStack<Integer> concurrentStack = new EliminationBackoffStack<>();
            for (int i = 0; i < 1000000; i++) {
                concurrentStack.push(i);
            }

            var timer = new BarrierTimer();
            final CyclicBarrier barrier = new CyclicBarrier(threadCount, timer);
            ExecutorService svc = Executors.newFixedThreadPool(threadCount);

            for (int threadI = 0; threadI < threadCount; threadI++) {
                svc.execute(() -> {
                    try {
                        //log("At run()");
                        barrier.await();
                        //log("Do work");

                        try {
                            for (int i = 0; i < N*0.5; i++)
                                concurrentStack.push(i + random.nextInt(100));
                            Thread.sleep(1);
                            for (int i = 0; i < N*0.5; i++)
                                concurrentStack.pop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                        //log("Wait for end");
                        barrier.await();
                        //log("Done");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }

            try {
                svc.shutdown();
                svc.awaitTermination(10000, TimeUnit.MILLISECONDS);
                time += timer.time;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        out.println(time / 10.0);
    }

    private static class BarrierTimer implements Runnable {
        public long time;
        private long start;

        public void run() {
            if (start == 0) {
                start = System.currentTimeMillis();
            } else {
                time = (System.currentTimeMillis() - start);
                System.out.println("Completed in " + time + " ms");
            }

        }

    }

}
