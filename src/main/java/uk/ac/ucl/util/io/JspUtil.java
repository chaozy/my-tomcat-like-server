package uk.ac.ucl.util.io;

import org.apache.jasper.JasperException;
import org.apache.jasper.JspC;
import uk.ac.ucl.context.Context;
import uk.ac.ucl.util.Constant;
import uk.ac.ucl.util.core.StrUtil;

import java.io.File;

/**
 * This class is derived from the source code of Apache Tomcat.
 * JspUtil aims to translate JSP to .java and compile it.
 * The main class here is jasper.JspC
 */
public class JspUtil {
    private static final String javaKeywords[] = { "abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long",
            "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile",
            "while" };

    public static void compileJsp(Context context, File file) {
        String subFolder;
        String path = context.getPath();
        if ("/".equals(path))
            subFolder = "_";
        else
            subFolder = StrUtil.subAfter(path, "/", false);

        String workPath = new File(Constant.workFolder, subFolder).getAbsolutePath() + File.separator;
        String[] args = new String[] { "-webapp", context.getDocBase().toLowerCase(), "-d", workPath.toLowerCase(), "-compile"};

        JspC jspc = new JspC();
        try {
            jspc.setArgs(args);
        } catch (JasperException e) {
            e.printStackTrace();
        }
        jspc.execute(file);
    }

    public static final String makeJavaIdentifier(String identifier) {
        return makeJavaIdentifier(identifier, true);
    }

    public static final String makeJavaIdentifier(String identifier, boolean periodToUnderscore) {
        StringBuilder modifiedIdentifier = new StringBuilder(identifier.length());
        if (!Character.isJavaIdentifierStart(identifier.charAt(0))) {
            modifiedIdentifier.append('_');
        }
        for (int i = 0; i < identifier.length(); i++) {
            char ch = identifier.charAt(i);
            if (Character.isJavaIdentifierPart(ch) && (ch != '_' || !periodToUnderscore)) {
                modifiedIdentifier.append(ch);
            } else if (ch == '.' && periodToUnderscore) {
                modifiedIdentifier.append('_');
            } else {
                modifiedIdentifier.append(mangleChar(ch));
            }
        }
        if (isJavaKeyword(modifiedIdentifier.toString())) {
            modifiedIdentifier.append('_');
        }
        return modifiedIdentifier.toString();
    }

    public static final String mangleChar(char ch) {
        char[] result = new char[5];
        result[0] = '_';
        result[1] = Character.forDigit((ch >> 12) & 0xf, 16);
        result[2] = Character.forDigit((ch >> 8) & 0xf, 16);
        result[3] = Character.forDigit((ch >> 4) & 0xf, 16);
        result[4] = Character.forDigit(ch & 0xf, 16);
        return new String(result);
    }
//
    public static boolean isJavaKeyword(String key) {
        int i = 0;
        int j = javaKeywords.length;
        while (i < j) {
            int k = (i + j) / 2;
            int result = javaKeywords[k].compareTo(key);
            if (result == 0) {
                return true;
            }
            if (result < 0) {
                i = k + 1;
            } else {
                j = k;
            }
        }
        return false;
    }

    public static String getServletPath(String uri, String subFolder) {
        String tempPath = "org/apache/jsp/" + StrUtil.subAfter(uri, "/", true);
        File temp = new File(Constant.workFolder, subFolder);
        File tempFile = new File(temp, tempPath);
        String fileNameOnly = tempFile.getName();
        String classFileName = JspUtil.makeJavaIdentifier(fileNameOnly);

        File servletFile = new File(tempFile.getParent(), classFileName);

        return servletFile.getAbsolutePath();
    }

    public static String getServletClassPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".class";
    }

    public static String getServletJavaPath(String uri, String subFolder) {
        return getServletPath(uri, subFolder) + ".java";
    }

    public static String getJspServletClassName(String uri, String subFolder) {
        File tempFile = new File(Constant.workFolder, subFolder);
        String tempPath = tempFile.getAbsolutePath() + File.separator;
        String servletPath = getServletPath(uri, subFolder);
        String jsServletClassPath = StrUtil.subAfter(servletPath, tempPath, false);
        String jspServletClassName = jsServletClassPath.replaceAll( File.separator, ".");
        return jspServletClassName;
    }

    public static void main(String[] args) {
        try {
            Context context = new Context("/example", "/Users/chaozy/Desktop/example/web", null,true);
            File file = new File("/Users/chaozy/Desktop/example/web/index.jsp");
            compileJsp(context,file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
