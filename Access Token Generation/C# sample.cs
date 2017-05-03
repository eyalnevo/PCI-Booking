using System;

namespace CreateAccessToken
{
    class Program
    {
        private static string[] helpLines =
        {
            "Usage: CreateAccessToken.exe <Certificate file path> <API Key> <Absolute time>",
            "Certificate file path: .cer as downloaded from https://service.pcibooking.net/api",
            "API Key: As generated in your PCI Booking account",
            "Absolute time: local time in format HH:MM:ss (up to 1 hour from now), e.g: 18:20:34"
        };

        static void Main(string[] args)
        {
            if (args.Length != 3)
                Usage("Invalid number of arguments; expected 3");

            // read certificate
            System.Security.Cryptography.X509Certificates.X509Certificate2 cert = null;
            try
            {
                cert = new System.Security.Cryptography.X509Certificates.X509Certificate2(args[0]);
            }
            catch (Exception ex)
            {
                Usage("Cannot load certificate file: " + args[0] + " Error: " + ex.Message);
            }

            // read time and convert to absolute date time in UTC
            DateTime expectedExpirationLocalTime;
            if (!DateTime.TryParse(args[2], out expectedExpirationLocalTime))
            {
                Usage("Invalid expected time: " + args[2]);
            }
            TimeSpan expiresIn = expectedExpirationLocalTime.Subtract(DateTime.Now);
            if (expiresIn > TimeSpan.FromHours(1) || expiresIn < TimeSpan.Zero)
            {
                Usage(string.Format("Invalid expected time [{0}]. Should be up to one hour ahead ", args[2]));
            }
            DateTime expectedExpirationUTC = expectedExpirationLocalTime.ToUniversalTime();

            // Generate a guid
            string guid = Guid.NewGuid().ToString().Replace("-", "");

            // Construct data block
            string dataBlock = string.Format("{0}#{1}#{2}", args[1], guid, expectedExpirationUTC.ToString("o"));


            // encode the string to clear bytes
            byte[] clearBytes = System.Text.Encoding.UTF8.GetBytes(dataBlock);

            // obtain crypto provider with public key
            System.Security.Cryptography.RSACryptoServiceProvider rsaCryptoServiceProvider = (System.Security.Cryptography.RSACryptoServiceProvider)cert.PublicKey.Key;

            // perform the encryption
            byte[] encryptedBytes = rsaCryptoServiceProvider.Encrypt(clearBytes, false);

            // convert byte array to hex string
            string accessToken = string.Concat(Array.ConvertAll(encryptedBytes, b => b.ToString("X2")));

            Console.WriteLine(accessToken);
        }

        private static void Usage(string message = "")
        {
            if (!string.IsNullOrEmpty(message))
                Console.WriteLine(message);

            foreach (var line in helpLines)
            {
                Console.WriteLine(line);
            }
            Environment.Exit(0);
        }
    }
}