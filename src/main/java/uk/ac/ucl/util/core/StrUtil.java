package uk.ac.ucl.util.core;


public class StrUtil {
    private static final int INDEX_NOT_FOUND = -1;
    private static final String PLACE_HOLDER = "{}";

    public static boolean isEmpty(String s){
        return s == null || s.length() == 0;
    }

    /**
     *
     * @param str
     * @param start : the start label
     * @param end : the end label
     * @return : substring between start and end in str
     */
    public static String subBetween(String str, String start, String end){
        if (isEmpty(str) || start == null || end == null){
            return null;
        }
        int indexStart = str.indexOf(start);

        if (indexStart != INDEX_NOT_FOUND){
            int indexEnd = str.indexOf(end, indexStart + start.length());

            if (indexEnd != INDEX_NOT_FOUND){
                return str.substring(indexStart + start.length(), indexEnd);
            }
            else { return ""; }
        }
        else { return ""; }
    }

    public static String subBetween(String str, String startend){
        return subBetween(str, startend, startend);
    }

    /**
     *
     * @param str
     * @param before : the end label
     * @return : the substring from the beginning to before
     */
    public static String subBefore(String str, String before){
        if ( str == null ){ return null; }
        int indexBefore = str.indexOf(before);
        if (indexBefore != INDEX_NOT_FOUND){
            return str.substring(0, indexBefore);
        }
        return "";
    }

    /**
     * Substring behind first 'after' will be returned.
     * @param str
     * @param after
     * @return
     */
    public static String subAfter(String str, String after){
        if (str == null ) { return null; }
        int indexAfter = str.indexOf(after);
        if (indexAfter != INDEX_NOT_FOUND){
            return str.substring(indexAfter + after.length());
        }
        return str;
    }

    /**
     * Find the substring behind the delimiter 'after', 'last'
     * indicates weather the substring behind the last delimiter
     * or behind the first delimiter is returned.
     * @param str
     * @param after
     * @param last
     * @return
     */
    public static String subAfter(String str, String after, boolean last){
        if (!last) { return subAfter(str, after); }
        int indexAfter = str.indexOf(after);
        while (str.indexOf(after, indexAfter + after.length()) != INDEX_NOT_FOUND){
            indexAfter = str.indexOf(after, indexAfter + after.length());
        }
        if (indexAfter != INDEX_NOT_FOUND){
            return str.substring(indexAfter + after.length());
        }
        else{
            return str;
        }
    }

    /**
     * Replace the placeholder("{}") by the given argument list
     * format("hello {}", "hello"} returns "hello world"
     * @param pattern
     * @param argArray
     * @return
     */
    public static String format(final String pattern, final Object... argArray){
        if (argArray.length == 0) { return pattern; }

        StringBuilder sb = new StringBuilder();

        int handledIndex = 0;
        for (int argIndex = 0; argIndex < argArray.length; argIndex++){
            int placeHolder = pattern.indexOf(PLACE_HOLDER, handledIndex);
            if (placeHolder == -1){
                sb.append(pattern, handledIndex, pattern.length());
                return sb.toString();
            }
            sb.append(pattern, handledIndex, placeHolder);
            sb.append(argArray[argIndex].toString());
            handledIndex = placeHolder + 2;
        }
        sb.append(pattern, handledIndex, pattern.length());
        return sb.toString();
    }

    /**
     * Check if the given url is in root folder
     * @param url
     * @return
     */
    public static boolean isRootFolder(String url){
        int count = 0;
        int dealedIndex = url.indexOf("/");;
        while (dealedIndex != INDEX_NOT_FOUND){
            count += 1;
            dealedIndex += 1;
            dealedIndex = url.indexOf("/", dealedIndex);
        }
        return count <= 1;
    }

    public static boolean containsAny(String father, String son){
        return father.contains(son);
    }
}
