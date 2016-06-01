import com.cache.tmap.TimeoutMap;
import com.cache.tmap.TimeoutMapCronServer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * author: ff
 * Date: 2016/6/1
 */
public class TestTimeoutMap {

    public static void main(String[] args) throws Exception{

        TimeoutMapCronServer server =  TimeoutMapCronServer.getInstance();

        server.startTimeoutMapCronServer();


        int time1 = 0;
        for(;++time1 < 30;){
            final TimeoutMap<HashMap>  timeoutMap = new TimeoutMap<HashMap>(time1*2,HashMap.class,TimeUnit.SECONDS);
            new Thread(){
                public void run(){
                    for(int k = 0;k < 100;k++){
                        timeoutMap.put(k,k);
                    }
                }
            }.start();
        }

        TimeUnit.SECONDS.sleep(5);

        for(;++time1 < 45;){
            final TimeoutMap<HashMap>  timeoutMap = new TimeoutMap<HashMap>(time1*2,HashMap.class,TimeUnit.SECONDS);
            new Thread(){
                public void run(){
                    for(int k = 0;k < 10;k++){
                        timeoutMap.put(k,k);
                    }
                }
            }.start();
        }

        TimeoutMap<HashMap>  timeoutMap = new TimeoutMap<HashMap>(10,HashMap.class,TimeUnit.SECONDS);
        timeoutMap.put("[test add again]","test");
        int time2 = 0;
        while(++time2 < (time1*2 + 20)){
            try {
                System.out.println(time2 + " second pass ...");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        timeoutMap.put("[test add again]","test");
        TimeUnit.SECONDS.sleep(20);
        timeoutMap.put("[test add again]","test2");
        server.stopTimeoutMapCronServer();

    }

}
