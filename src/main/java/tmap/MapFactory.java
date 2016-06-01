package tmap;

import java.util.AbstractMap;

/**
 * User: oooooooz
 * Date: 2016/5/31
 * Time: 16:28
 */
public interface MapFactory<T extends AbstractMap>  {

    T newTimeoutMap();

}
