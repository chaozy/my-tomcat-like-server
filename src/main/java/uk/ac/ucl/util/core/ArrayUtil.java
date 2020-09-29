package uk.ac.ucl.util.core;

public class ArrayUtil {
    public static Object[] append(Object[] target, Object object) {
        for (int i = 0; i < target.length; i++) {
            if (target[i] == null) {
                target[i] = object;
                return target;
            }
        }
        Object[] newArray = new Object[target.length+1];
        System.arraycopy(target, 0, newArray, 0, target.length);
        newArray[target.length] = object;
        return newArray;
    }

}
