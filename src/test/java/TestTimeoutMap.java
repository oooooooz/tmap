
import com.cache.tmap.TimeoutMap;
import com.cache.tmap.TimeoutMapCronServer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * author: ff
 * Date: 2016/6/1
 */
public class TestTimeoutMap {

    public static void main(String[] args)throws Exception{

        TimeoutMapCronServer server =  TimeoutMapCronServer.getInstance();

        server.startTimeoutMapCronServer();



        int time1 = 0;


        for(; ++time1 < 12;) {


            final TimeoutMap<HashMap> timeoutMap = new TimeoutMap<>(time1 * 5 + 10, HashMap.class, TimeUnit.SECONDS);
            new Thread(

            ){
                @Override
                public void run() {
                    for (int i = 0; i < 30; i++) {
                        timeoutMap.put(i, i);
                    }
                }
            }.start();
        }
        //master modify1
        //master modify2
        TimeoutMap<HashMap> timeoutMap = new TimeoutMap<>(10, HashMap.class, TimeUnit.SECONDS);

        timeoutMap.put("12",server);
        System.out.println("---------value="+timeoutMap.get("12"));


        int time = 0;
        while(++time < (time1 * 5 + 60)){
            try {
                System.out.println(time + " second pass ...");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        timeoutMap.put("12",server);

        TimeUnit.SECONDS.sleep(15);

        System.out.println("---------value="+timeoutMap.get("12"));

        server.stopTimeoutMapCronServer();

    }
}
