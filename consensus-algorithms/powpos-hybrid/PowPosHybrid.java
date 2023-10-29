import java.io.FileInputStream;
import java.security.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class PowPosHybrid {

  private static final int MAX_AGE = 10; // maximum age for validators
  private static final int MAX_STAKE = 10000; // maximum stake that validators can hold

  private static final String CONFIG_FILE = "config.properties"; // config file name
  private static final Properties config = loadConfigurations(CONFIG_FILE); // load the properties from the config file

  private static final String targetPrefix = config.getProperty( // Adjust this to change mining difficulty
    "targetPrefix",
    "000"
  );

  private static final int THREAD_COUNT = Integer.parseInt( // number of miners involved
    config.getProperty("threadCount", "5")
  );

  private static volatile boolean solutionFound = false; // solutionFound flag used to stop mining operation
  static long startTime = System.currentTimeMillis(); // set the starttime before the mining operation begins
  static long hashesAttempted = 0; // will hold the total number of hashs calculated before reaching the solution

  public static void main(String[] args) {
    List<Validator> validators = new ArrayList<>(); // hold validators

    // Create 20 validators and add them to the validators arraylist
    for (int i = 1; i <= 20; i++) {
      try {
        String name = "Validator" + i; // define the validator name
        double stake = randomStakeGenerator(); // get randomly generated stake
        int age = randomAgeGenerator(); // get randomly generated age
        Validator validator = new Validator(name, stake, age); // create a new validator by calling the constructor function
        validators.add(validator); // add the newly created validator to list
        System.out.print(
          "\rCreating  " +
          20 +
          " random validators " +
          (int) (((double) i / 20.0) * 100) +
          "%"
        );
        // System.out.println("done : "+validator.getName());
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }

    // print newly created validators
    System.out.println("\n\nList of validators : ");
    for (Validator validator : validators) {
      System.out.println(
        "Name: " +
        validator.getName() +
        ", Stake: " +
        validator.getStake() +
        ", Age: " +
        validator.getAge()
      );
    }

    // select few validators from the validators list and then make them act as miner in pow way
    int numValidatorsToSelect = THREAD_COUNT; // nuber of validators that would be selected for pow like competition
    List<Validator> selectedValidators = selectValidators( // will select the specified number of validators from the list
      validators,
      numValidatorsToSelect
    );

    // print the selected validators

    System.out.println("\nSelected validators : ");
    for (Validator validator : selectedValidators) {
      System.out.println(
        "Selected: " +
        validator.getName() +
        ", Stake: " +
        validator.getStake() +
        ", Age: " +
        validator.getAge()
      );
    }

    // initial nonce, data and previous hash
    AtomicInteger nonce = new AtomicInteger(0);
    String data = "somedata";
    String previousHash = "00000000000000000000000000000000";

    // array for holding threads that will carry out pow mech.
    Thread[] miners = new Thread[selectedValidators.size()];

    // start the mining process for each validator
    for (int i = 0; i < selectedValidators.size(); i++) {
      miners[i] =
        new Thread(
          () -> mine(data, previousHash, nonce), // lambad function that would be called when thread starts running, calls the mine function
          selectedValidators.get(i).getName()
        );
      System.out.println(
        "Starting minin process : " + selectedValidators.get(i).getName()
      );
      miners[i].start(); // this would call the run() function which would
    }

    // wait for all the miner threads to stop
    for (Thread miner : miners) {
      try {
        miner.join();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // load the configurations from the config.properties file
  private static Properties loadConfigurations(String configFileName) {
    Properties config = new Properties();
    try (FileInputStream finput = new FileInputStream(configFileName)) {
      config.load(finput);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return config;
  }

  public static int randomAgeGenerator() {
    Random random = new Random();
    return random.nextInt(MAX_AGE + 1); // generates a random integer b/w 0 and MAX_AGE
  }

  public static double randomStakeGenerator() {
    Random random = new Random();
    return random.nextDouble() * MAX_STAKE; // generates a random double b/w 0 and MAX_STAKE
  }

  private static void mine(
    String data,
    String previousHash,
    AtomicInteger nonce
  ) {
    while (!solutionFound) {
      int currentnonce = nonce.getAndIncrement();
      String input = previousHash + data + currentnonce;
      String hash = applySHA256(input);
      hashesAttempted++;

      System.out.println(
        "[+] " +
        Thread.currentThread().getName() +
        " [ input ] : \"" +
        input +
        "\" | [ hash ] : " +
        hash
      );

      if (hash.substring(0, targetPrefix.length()).equals(targetPrefix)) {
        solutionFound = true;
        long endTime = System.currentTimeMillis();
        System.out.println(
          "POW Solved! by " +
          Thread.currentThread().getName() +
          " Nonce : " +
          (currentnonce)
        );
        System.out.println("Time elapsed (ms) : " + (endTime - startTime));
        System.out.println("Hashes Attempted : " + hashesAttempted);
        System.out.println(
          "Hash Rate (H/s): " +
          (int) (
            (double) hashesAttempted /
            (((double) endTime - (double) startTime) / (double) 1000)
          )
        );
        break;
      }
    }
  }

  private static String applySHA256(String input) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] byteHash = sha256.digest(input.getBytes("UTF-8"));
      String hexString = "";

      for (byte b : byteHash) {
        String hex = String.format("%02x", b);
        hexString = hexString + hex;
      }

      // System.out.println(hexString);
      return hexString;
    } catch (Exception e) {
      e.printStackTrace();
      return "nothing";
    }
  }

  public static List<Validator> selectValidators(
    List<Validator> validators,
    int numToSelect
  ) {
    List<Validator> selectedValidators = new ArrayList<>();
    Random random = new Random();

    for (int i = 0; i < numToSelect; i++) {
      // Calculate the weight for each validator based on stake and age
      double totalWeight = 0;
      List<Double> weights = new ArrayList<>();
      for (Validator validator : validators) {
        // You can adjust these weight factors as needed
        double stakeWeight = validator.getStake();
        double ageWeight = 1.0 / (validator.getAge() + 1); // +1 to avoid division by zero
        double weight = stakeWeight * ageWeight;
        weights.add(weight);
        totalWeight += weight;
      }

      // Randomly select a validator based on the weights
      double randomValue = random.nextDouble() * totalWeight;
      double cumulativeWeight = 0;
      int selectedValidatorIndex = -1;
      for (int j = 0; j < validators.size(); j++) {
        cumulativeWeight += weights.get(j);
        if (randomValue <= cumulativeWeight) {
          selectedValidatorIndex = j;
          break;
        }
      }

      if (selectedValidatorIndex != -1) {
        Validator selectedValidator = validators.get(selectedValidatorIndex);
        selectedValidators.add(selectedValidator);
      }
    }

    return selectedValidators;
  }
}

class Validator {

  private final String name;
  private final double stake;
  private final int age;

  private final KeyPair keyPair;

  public Validator(String name, double stake, int age)
    throws NoSuchAlgorithmException {
    this.name = name;
    this.stake = stake;
    this.age = age;
    this.keyPair = generateKeyPair();
  }

  public String getName() {
    return name;
  }

  public double getStake() {
    return stake;
  }

  public int getAge() {
    return age;
  }

  public KeyPair getKeyPair() {
    System.out.println("this is the key pair looks like : " + keyPair);
    return keyPair;
  }

  // generate key pair for digital signatures
  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  // calculate hash
  public String calculateBlockHash(String blockHeader)
    throws NoSuchAlgorithmException {
    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    byte[] byteHash = sha256.digest(blockHeader.getBytes());

    String hexHash = "";
    for (byte b : byteHash) {
      String hex = String.format("%02x", b);
      hexHash = hexHash + hex;
    }
    return hexHash;
  }

  // sign a message with the private key
  public byte[] sign(String data)
    throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(keyPair.getPrivate());
    signature.update(data.getBytes());
    return signature.sign();
  }

  // verify a signature with public key
  public boolean verifySignature(String data, byte[] signature)
    throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature verifier = Signature.getInstance("SHA256withRSA");
    verifier.initVerify(keyPair.getPublic());
    verifier.update(data.getBytes());
    return verifier.verify(signature);
  }
}
