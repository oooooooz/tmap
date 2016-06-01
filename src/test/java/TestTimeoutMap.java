import tmap.TimeoutMap;
import tmap.TimeoutMapCronServer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * author: oooooooz
 * Date: 2016/6/2
 */
public class TestTimeoutMap {

    public static void main(String[] args) throws Exception{

        //start server
        TimeoutMapCronServer.getInstance().startTimeoutMapCronServer();

        int time1 = 0;
        for(;++time1 < 10;){
            final TimeoutMap<HashMap> timeoutMap = new TimeoutMap<HashMap>(time1*2,HashMap.class, TimeUnit.SECONDS);
            new Thread(){
                public void run(){
                    for(int k = 0; k < 30;k++){
                        timeoutMap.put(k,k);
                    }
                }
            }.start();
        }

        TimeoutMap<HashMap> timeoutMap = new TimeoutMap<HashMap>(20,HashMap.class, TimeUnit.SECONDS);
        timeoutMap.put("[test put again]","again1");
        TimeUnit.SECONDS.sleep(10);
        int time = 0;
        while (++time < ( time1 + 30 )){
            System.out.println(time + " second pass...");
            TimeUnit.SECONDS.sleep(1);
        }

        timeoutMap.put("[test put again]","again2");

        TimeUnit.SECONDS.sleep(30);
        //stop
        TimeoutMapCronServer.getInstance().stopTimeoutMapCronServer();
    }

}
