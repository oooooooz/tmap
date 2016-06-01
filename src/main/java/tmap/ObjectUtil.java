package tmap;

/**
 * User: oooooooz
 * Date: 2016/5/31
 * Time: 18:11
 */
public class ObjectUtil {


    public static String simpleClassName(Class<?> clazz){

        if(clazz == null){
            throw new NullPointerException("clazz");
        }

        String className = clazz.getName();

        int lastDotIdx = className.lastIndexOf('.');

        if(lastDotIdx > -1){
            return className.substring(lastDotIdx + 1);
        }

        return className;

    }
}
