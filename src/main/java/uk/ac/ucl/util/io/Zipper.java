package uk.ac.ucl.util.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * This Zipper is not the zipper on the pants :)
 * It aims to provide gzip compression to bytes arrays.
 * The code is read from here: https://stackoverflow.com/questions/14777800/gzip-compression-to-a-byte-array
 */
public class Zipper {
    public static byte[] comrpess(byte[] targetData) {
        byte[] result = new byte[]{};
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream(targetData.length);
            GZIPOutputStream gos = new GZIPOutputStream(bos)){
                gos.write(targetData);
                // Have to close before using bos, otherwise it remains incomplete.
                gos.close();
                result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] uncompress(byte[] targetData) {
        byte[] result = new byte[]{};
        try(ByteArrayInputStream bis = new ByteArrayInputStream(targetData);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPInputStream gip = new GZIPInputStream(bis)
        ) {
            byte[] buffer = new byte[1024];
            int len;
            while((len = gip.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            result = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
