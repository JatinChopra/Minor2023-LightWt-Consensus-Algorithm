import java.security.*;
import java.util.ArrayList;
import java.util.List;

public class ProofOfStake {

  private static List<Validator> validators = new ArrayList<>();

  public static void main(String[] args) {
    try {
      // Create and register 7 validators
      Validator validator1 = new Validator("V1", 1000, 2);
      Validator validator2 = new Validator("V2", 1500, 3);
      Validator validator3 = new Validator("V3", 800, 1);
      Validator validator4 = new Validator("V4", 1200, 4);
      Validator validator5 = new Validator("V5", 600, 5);
      Validator validator6 = new Validator("V6", 1100, 3);
      Validator validator7 = new Validator("V7", 900, 2);

      // Assuming you have a method called registerValidator to register each validator.
      registerValidator(validator1);
      registerValidator(validator2);
      registerValidator(validator3);
      registerValidator(validator4);
      registerValidator(validator5);
      registerValidator(validator6);
      registerValidator(validator7);

      System.out.println("List of validators : ");
      for (Validator v : validators) {
        System.out.println(
          "[>] " +
          v.getName() +
          " | stake : " +
          v.getStake() +
          " | age : " +
          v.getAge()
        );
      }

      // select valiadtor to proposethe next block
      Validator selectedValidator = selectValidator(validators);
      System.out.println(selectedValidator.getName());

      // simulate block proposal and signing
      String data = "block data";
      String previousBlockHash = "00000000000000000000000000000000"; // In a real blockchain, this would be the previous block's hash.
      String blockHeader =
        previousBlockHash + data + selectedValidator.getName();
      String blockHash = selectedValidator.calculateBlockHash(blockHeader);

      // Sign the block header
      byte[] signature = selectedValidator.sign(blockHeader);

      // Verify the signature
      boolean isSignatureValid = selectedValidator.verifySignature(
        blockHeader,
        signature
      );

      System.out.println("Block Hash: " + blockHash);
      System.out.println("Signature Verification: " + isSignatureValid);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void registerValidator(Validator validator) {
    validators.add(validator);
  }

  // selects a validator from the list based on weighted randomization considering stake and age
  public static Validator selectValidator(List<Validator> validators) {
    double totalStake = 0.0; // init totalStake
    double totalAge = 0; // init totalAge

    // calculate the totalStake and totalAge
    // the values will be cumulative sums of stakes and ages of all validators
    for (Validator validator : validators) {
      totalStake += validator.getStake();
      totalAge += validator.getAge();
    }

    // Generate a random value b/w range 0 to (totalStake+totalAge)
    double randomValue = Math.random() * (totalStake + totalAge);
    double cumulativeValue = 0.0; // init cumulative value

    System.out.println("\nTotal Stake: " + totalStake);
    System.out.println("Total Age: " + totalAge);
    System.out.println("Random Value: " + randomValue + "\n");

    for (Validator validator : validators) {
      // calculate cumulative Value for each validator
      // cumulative value is simply sum of validator age and stake
      // it doesnt hold any special mathematical significance
      cumulativeValue += validator.getStake() + validator.getAge();
      System.out.println(
        ">> Cumulative Value for " +
        validator.getName() +
        ": " +
        cumulativeValue
      );

      // if randomValue falls withing range of cumulativeValue thats associated with a validator then that validaor is selected.
      if (randomValue <= cumulativeValue) {
        System.out.println("[+] " + validator.getName() + " accepted.");
        return validator;
      } else {
        System.out.println("[!] " + validator.getName() + " rejected.");
      }
    }
    return validators.get(0);
  }
}

// class demonstrates data members and functions that are asociated with a validator in PoS consensus mechanism
class Validator {

  private final String name;
  private final double stake;
  private final int age;

  private final KeyPair keyPair; // key pair for digital signatures

  public Validator(String name, double stake, int age)
    throws NoSuchAlgorithmException {
    this.name = name;
    this.stake = stake;
    this.age = age;
    this.keyPair = generateKeyPair(); // generating a key-pair for the digital signature
  }

  // getter methods

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

  // generate and return KeyPair
  private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA"); // uses RSA algorithm
    keyPairGenerator.initialize(2048); //
    return keyPairGenerator.generateKeyPair();
  }

  // calculate hash of block header
  public String calculateBlockHash(String blockHeader)
    throws NoSuchAlgorithmException {
    // initialize sha md instance to create a hash
    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    // compte the hash
    byte[] byteHash = sha256.digest(blockHeader.getBytes());

    String hexHash = "";
    // conver the bytehash to hex
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

  // verify signature with public key
  public boolean verifySignature(String data, byte[] signature)
    throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature verifier = Signature.getInstance("SHA256withRSA");
    verifier.initVerify(keyPair.getPublic());
    verifier.update(data.getBytes());
    return verifier.verify(signature);
  }
}
