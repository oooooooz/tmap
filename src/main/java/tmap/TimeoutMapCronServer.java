package tmap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * User: oooooooz
 * Date: 2016/5/31
 * Time: 20:04
 */
public class TimeoutMapCronServer {

    private static Logger log = LoggerFactory.getLogger(TimeoutMapCronServer.class);

    private final AtomicIntegerFieldUpdater<TimeoutMapCronServer> WORKER_STATE_UPDATER;

    private final Thread workerThread;

    private static final int WORKER_STATE_INIT = 0;
    private static final int WORKER_STATE_STARTED = 1;
    private static final int WORKER_STATE_SHUTDOWN = 2;

    private volatile int workerState = WORKER_STATE_INIT;

    private static TimeoutMapCronServer timeoutMapCron;

    public synchronized static TimeoutMapCronServer getInstance(){
        if(timeoutMapCron  == null){
            timeoutMapCron = new TimeoutMapCronServer();
        }
        return timeoutMapCron;
    }

    private TimeoutMapCronServer(){
        WORKER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(TimeoutMapCronServer.class, "workerState");
        WORKER_STATE_UPDATER.compareAndSet(this,WORKER_STATE_STARTED,WORKER_STATE_INIT);
        Worker worker = new Worker();
        workerThread = Executors.defaultThreadFactory().newThread(worker);
    }

    public void stopTimeoutMapCronServer(){
        WORKER_STATE_UPDATER.compareAndSet(this,WORKER_STATE_STARTED,WORKER_STATE_SHUTDOWN);
        workerState = WORKER_STATE_SHUTDOWN;
        log.info("TimeoutMapCronServer stop ...");
    }

    public void startTimeoutMapCronServer(){
        WORKER_STATE_UPDATER.compareAndSet(this,WORKER_STATE_INIT,WORKER_STATE_STARTED);
        workerState = WORKER_STATE_STARTED;
        workerThread.start();
        log.info("TimeoutMapCronServer initialize ...");
    }


    private void expireTimeouts()throws Exception{


        int capacity = 100;

        log.info("TimeoutMapCronServer expireTimeouts start ...");

        TimeoutMapContext context = TimeoutMapContext.contextInstance();
        AbstractTimeoutMap head = context.getHead();
        AbstractTimeoutMap next = head.next;

        BlockingQueue<Object> queue = new ArrayBlockingQueue<Object>(capacity);

        while(next != null ){

            try {

                if (WORKER_STATE_UPDATER.get(this) == WORKER_STATE_SHUTDOWN) {
                    log.info("TimeoutMapCronServer stopped at " + new Date());
                    break;
                }

                Set<Map.Entry<Object, KeyContext>> entrySet = next.entrySet();

                //may be null
                if(entrySet != null) {
                    queue.clear();
                    //iterate key to expire
                    int factor = 0;
                    for(Map.Entry<Object,KeyContext> entry : entrySet){
                        Object key = entry.getKey();
                        KeyContext keyContext = entry.getValue();
                        if (context.currentTime() >= keyContext.getDeadline()) {
                            log.info("TimeoutMapCronServer ====> " + next + ",key=" + key + ", expire " + ",devi="+((context.currentTime() - keyContext.getDeadline())/1000000));
                            //next.expire(key);
                            if(!queue.offer(key)){ // fail
                                queue = resizeQueue(queue,capacity,++factor);
                                queue.offer(key);

                            }
                        }
                    }
                    //expire
                    while (queue.size() > 0){
                        next.expire(queue.poll());
                    }
                }

                AbstractTimeoutMap timeout = next.next; //may be loop to head

                if (next.isEmpty()) { //remove empty map
                    context.remove(next);
                }

                next = timeout;

                if(next == head){ //begin to head again
                    TimeUnit.SECONDS.sleep(1);
                }

            }catch (Throwable t){
                log.error("TimeoutMapCronServer error",t);
                TimeUnit.SECONDS.sleep(1);
            }

        }
    }


    private BlockingQueue<Object>  resizeQueue(BlockingQueue oldQueue,int capacity,int factor){
       return new ArrayBlockingQueue<Object>(capacity << factor,true,oldQueue);
    }


    //run cron
    private final class Worker implements Runnable{
        @Override
        public void run() {
            try{
                if(WORKER_STATE_UPDATER.get(TimeoutMapCronServer.this) == WORKER_STATE_STARTED){
                    log.info("TimeoutMapCron start ...");
                    TimeoutMapCronServer.this.expireTimeouts();
                }
            }catch (Throwable ex){
                log.error("TimeoutMapCronServer start fail" , ex);
            }
        }
    }


    static <T> Callable<T> toCallable(Runnable runnable, T result) {
        return new RunnableAdapter<T>(runnable, result);
    }

    private static final class RunnableAdapter<T> implements Callable<T> {
        final Runnable task;
        final T result;

        RunnableAdapter(Runnable task, T result) {
            this.task = task;
            this.result = result;
        }

        @Override
        public T call() {
            task.run();
            return result;
        }

        @Override
        public String toString() {
            return "Callable(task: " + task + ", result: " + result + ')';
        }
    }


}
