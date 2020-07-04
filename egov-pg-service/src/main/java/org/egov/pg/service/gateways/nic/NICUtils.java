package org.egov.pg.service.gateways.nic;

import lombok.extern.slf4j.Slf4j;

import org.egov.pg.utils.Utils;
import org.egov.tracer.model.CustomException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@Slf4j
class NICUtils {

    // This is an array for creating hex chars
    private static final char[] HEX_TABLE = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    private NICUtils() {
    }

//  ----------------------------------------------------------------------------

    private static byte[] fromHexString(String s, int offset, int length) {
        if ((length % 2) != 0)
            return null;
        byte[] byteArray = new byte[length / 2];
        int j = 0;
        int end = offset + length;
        for (int i = offset; i < end; i += 2) {
            int high_nibble = Character.digit(s.charAt(i), 16);
            int low_nibble = Character.digit(s.charAt(i + 1), 16);
            if (high_nibble == -1 || low_nibble == -1) {
                // illegal format
                return null;
            }
            byteArray[j++] = (byte) (((high_nibble << 4) & 0xf0) | (low_nibble & 0x0f));
        }
        return byteArray;
    }
//  ----------------------------------------------------------------------------

    /**
     * Returns Hex output of byte array
     */
    private static String hex(byte[] input) {
        // create a StringBuffer 2x the size of the hash array
        StringBuilder sb = new StringBuilder(input.length * 2);

        // retrieve the byte array data, convert it to hex
        // and add it to the StringBuilder
        for (byte anInput : input) {
            sb.append(HEX_TABLE[(anInput >> 4) & 0xf]);
            sb.append(HEX_TABLE[anInput & 0xf]);
        }
        return sb.toString();
    }


    static String SHAhashAllFields(Map<String, String> fields, String secret) {

        // create a list and sort it
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        // create a buffer for the SHA256 input
        StringBuilder buf = new StringBuilder();


        // iterate through the list and add the remaining field values
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                buf.append(fieldName).append("=").append(fieldValue);
                if (itr.hasNext()) {
                    buf.append('&');
                }
            }
        }
        byte[] mac = null;
        try {
            byte[] b = fromHexString(secret, 0, secret.length());
            SecretKey key = new SecretKeySpec(b, "HmacSHA256");
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(key);

            m.update(buf.toString().getBytes("ISO-8859-1"));
            mac = m.doFinal();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            log.error("Error occurred while generating hash for fields "+fields.toString(), e);
            throw new CustomException("CHECKSUM_GEN_FAILED", "Hash generation failed, gateway redirect URI " +
                    "cannot be generated");
        }
        return hex(mac);

    } // end hashAllFields()

    static Map<String, List<String>> splitQuery(String params) {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        try {
            final String[] pairs = params.split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.get(key).add(value);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return query_pairs;
    }
    
    public static void validateTransaction(String txMsg, String secretKey) {
    	int splitIndex =txMsg.lastIndexOf("|");
    	String txChecksumvalue =  txMsg.substring(splitIndex+1) ;
    	String generatedChecksum =generateCRC32Checksum(txMsg.substring(0, splitIndex) , secretKey);
    	if(!generatedChecksum.equals( txChecksumvalue)){
    		throw new CustomException("CHECKSUM_VALIDATION_FAILED", "Fraud transaction, Checksum did not match");
    	}
    }
    
    
    public static String generateCRC32Checksum(String message, String secretKey) {
    	String msg = message + "|" + secretKey;
    	System.out.println("Input Key : "+ msg);
    	byte bytes[] = msg.getBytes();
    	Checksum checksum = new CRC32();
    	// update the current checksum with the specified array of bytes
    	checksum.update(bytes, 0, bytes.length);
    	// get the current checksum value
    	long checksumValue = checksum.getValue();
    	System.out.println("CRC32 checksum for input string is: " + 	checksumValue);
    	return String.valueOf(checksumValue);
    }
}
