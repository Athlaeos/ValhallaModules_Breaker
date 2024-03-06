package me.athlaeos.vbreaking.utility;

import me.athlaeos.vbreaking.ValhallaModulesBreaker;

public class Catch {
    public static <T> T catchOrElse(Fetcher<T> c, T r){
        return catchOrElse(c, r, null);
    }

    public static <T> T catchOrElse(Fetcher<T> c, T r, String log){
        try {
            return c.get();
        } catch (Exception e){
            if (log != null) ValhallaModulesBreaker.logWarning(log);
            return r;
        }
    }
}
