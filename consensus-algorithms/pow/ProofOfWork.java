import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class ProofOfWork {

  // configuration file name
  private static final String CONFIG_FILE = "config.properties";
  // load configuration properties
  private static final Properties config = loadConfigurations(CONFIG_FILE);

  // target prefix for PoW ( mining difficulty)
  private static final String targetPrefix = config.getProperty(
    "targetPrefix",
    "000"
  );
  // number of miners=threads involved
  private static final int THREAD_COUNT = Integer.parseInt(
    config.getProperty("threadCount", "5")
  );

  // volatile flag to stop mining process once the solution is found
  private static volatile boolean solutionFound = false;
  // start time for measuring time execution
  static long startTime = System.currentTimeMillis();
  // counter for number of attempted hashes
  static long hashesAttempted = 0;

  public static void main(String[] args) {
    // (value is not cached) for adjusting the nonce values by miner
    // unique nonce for every thread/miner
    AtomicInteger nonce = new AtomicInteger(0);

    // dummy transaction data
    String data = "transactiondata";
    // 32-bit initial previous hash
    String previousHash = "00000000000000000000000000000000";

    // a thread array that depicts miners in a PoW consensus mechanism
    Thread[] miners = new Thread[THREAD_COUNT];

    // Starting Miner Threads
    for (int i = 0; i < THREAD_COUNT; i++) {
      // create a new thread for each miner that would start executing the mine function
      miners[i] =
        new Thread(() -> mine(data, previousHash, nonce), "miner" + i);
      System.out.println("Starting thread: " + miners[i].getName());
      miners[i].start();
    }

    // wait for all miner threads to finish
    for (Thread miner : miners) {
      try {
        miner.join();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    // Verify the hash based on user input
    verifyHash();
  }

  // Function to verify the hash based on user input
  private static void verifyHash() {
    // scanner input to take input from user
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter nonce for verification: ");
    int userNonce = scanner.nextInt();
    scanner.nextLine(); // Consume newline character

    String userInput =
      "00000000000000000000000000000000" + "transactiondata" + userNonce;
    String userHash = applySHA256(userInput);

    System.out.println("User-Provided Nonce: " + userNonce);
    System.out.println("User-Provided Input: " + userInput);
    System.out.println("Calculated Hash: " + userHash);

    if (userHash.startsWith(targetPrefix)) {
      System.out.println(
        "Verification: Success. The user-provided nonce is valid."
      );
    } else {
      System.out.println(
        "Verification: Failed. The user-provided nonce is not valid."
      );
    }
  }

  // function for loading configurations from the config file
  // takes a config file name and return a config object
  private static Properties loadConfigurations(String configFileName) {
    Properties config = new Properties();
    try (FileInputStream finput = new FileInputStream(configFileName)) {
      config.load(finput);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return config;
  }

  // mine function for demonstrating the mining process
  // takes data, previous hash and nonce as required argumets
  private static void mine(
    String data,
    String previousHash,
    AtomicInteger nonce
  ) {
    while (!solutionFound) {
      int currentNonce = nonce.getAndIncrement();
      // concatenate the input arguments into a single string
      String input = previousHash + data + currentNonce;
      // apply sha256 hash over it
      String hash = applySHA256(input);
      hashesAttempted++; // increment total hashesAttempted

      System.out.println(
        "[" + Thread.currentThread().getName() + "] Input: \"" + input + "\""
      );
      System.out.println(
        "[" + Thread.currentThread().getName() + "] Hash: " + hash
      );

      // match if hash have the required number of leading zeroes and satisfies the mining difficulty
      if (hash.startsWith(targetPrefix)) {
        solutionFound = true;
        long endTime = System.currentTimeMillis(); // make a note of endTime

        // display information regarding perfomance and showing different useful parameters
        System.out.println(
          "POW Solved by " +
          Thread.currentThread().getName() +
          " Nonce: " +
          (currentNonce)
        );
        System.out.println("Time Elapsed (ms): " + (endTime - startTime));
        System.out.println("Hashes Attempted: " + hashesAttempted);
        double hashRate = (double) hashesAttempted /
        ((double) (endTime - startTime) / 1000);
        System.out.println(
          "Hash Rate (H/s): " + String.format("%.2f", hashRate)
        );
        break;
      }
    }
  }

  // helper function for applying sha256
  private static String applySHA256(String input) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] byteHash = sha256.digest(input.getBytes("UTF-8"));
      StringBuilder hexString = new StringBuilder();

      for (byte b : byteHash) {
        String hex = String.format("%02x", b);
        hexString.append(hex);
      }

      return hexString.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "nothing";
    }
  }
}
