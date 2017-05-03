
import java.io.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import javax.crypto.Cipher;

public class CreateAccessToken {

    private static final String[] helpLines
            = {
                "Usage: CreateAccessToken.exe <Certificate file path> <API Key> <Absolute time>",
                "Certificate file path: .cer as downloaded from https://service.pcibooking.net/api",
                "API Key: As generated in your PCI Booking account",
                "Absolute time: local time in format HH:MM:ss (up to 1 hour from now), e.g: 18:20:34"
            };

    public static void main(String[] args) {
       
    	if (args.length != 3) {
            Usage("Invalid number of arguments; expected 3");
        }
        // read certificate
        Certificate cert = null;
        try {
            InputStream fis = new FileInputStream(args[0]);
            BufferedInputStream bis = new BufferedInputStream(fis);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(bis);
        } catch (Exception ex) {
            Usage("Cannot load certificate file: " + args[0] + " Error: " + ex);
        }
        // read time and convert to absolute date time in UTC
        Date expectedExpirationLocalTime = null;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String cDate=formatter.format(date);
        formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            expectedExpirationLocalTime = formatter.parse(cDate+" "+args[2]);
        } catch (Exception ex) {
            Usage("Invalid expected time: " + args[2] + " Error: " + ex);
        }
        Calendar cal = Calendar.getInstance();
        long expiresIn = expectedExpirationLocalTime.getTime() - cal.getTime().getTime();
        if (expiresIn > 3600000 || expiresIn < 0) {
            Usage("Invalid expected time [" + args[2] + "]. Should be up to one hour ahead ");
        }
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expectedExpirationUTC = null;
        try {
            expectedExpirationUTC = formatter.parse(formatter.format(expectedExpirationLocalTime));
        } catch (Exception ex) {
            Usage("Cannot convert time to UniversalTime: Error: " + ex);
        }

		// format to ISO 8601
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String asISO8601 = df.format(expectedExpirationUTC);


        // Generate a guid
        String guid = UUID.randomUUID().toString().replace("-", "");
        // Construct data block
        String dataBlock = String.format("%1$s#%2$s#%3$s", args[1], guid, asISO8601);
        // encode the string to clear bytes
        byte[] clearBytes = null;
        try {
            clearBytes = dataBlock.getBytes("UTF-8");
        } catch (Exception ex) {
            Usage("Cannot encode to UTF8: Error: " + ex);
        }
        byte[] encryptedBytes = null;
        // perform the encryption
        try {
            // get an RSA cipher object and print the provider
            final Cipher cipher = Cipher.getInstance("RSA");
            // encrypt the plain text using the public key
            cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
            encryptedBytes = cipher.doFinal(clearBytes);
        } catch (Exception ex) {
            Usage("Cannot encrypt: Error: " + ex);
        }
        // convert byte array to hex string
        String accessToken = byteArrayToHex(encryptedBytes);

		// display the access token
		System.out.println(accessToken);
    }

    private static void Usage(String message) {
        if (!message.isEmpty()) {
            System.out.println(message);
        }

        for (String line : helpLines) {
            System.out.println(line);
        }
        System.exit(0);
    }

    private static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a) {
            sb.append(String.format("%02X", b & 0xff));
        }
        return sb.toString();
    }
}