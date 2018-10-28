package spartons.com.javalocationdistancetrackingapp.util;


import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRxSchedulers {

    public Scheduler disk = Schedulers.single();

    public Scheduler network = Schedulers.io();

    public Scheduler newThread = Schedulers.newThread();

    public Scheduler computation = Schedulers.computation();

    public Scheduler mainThread = AndroidSchedulers.mainThread();

    public Scheduler threadPoolSchedulers() {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService threadPoolExecutorService = Executors.newFixedThreadPool(threadCount);
        return Schedulers.from(threadPoolExecutorService);
    }
}