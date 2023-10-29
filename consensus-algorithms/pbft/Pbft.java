import java.util.ArrayList;
import java.util.List;

class PBFTNode {

  private String name;
  public List<PBFTNode> peers;
  public int totalPrepareMessagesReceived;
  public int totalCommitMessagesReceived;
  private int preprepareMessages = 0;
  private PBFTMessage message;
  private int totalNodes = 10;

  public PBFTNode(String name) {
    this.name = name;
    peers = new ArrayList<>();
  }

  // method for adding other peer nodes ( except self )
  public void addPeer(PBFTNode peer) {
    peers.add(peer);
  }

  public void setMessage(PBFTMessage message) { // setter method for message
    this.message = message;
  }

  public PBFTMessage getMessage() {
    return message;
  }

  public String getName() {
    return name;
  }

  // send message from one node to another
  public void sendMessage(PBFTMessage message, PBFTNode node) {
    node.receiveMessage(message, this);
  }

  // for handling received messages
  public void receiveMessage(PBFTMessage message, PBFTNode primaryNode) { // takes message and primaryNode node argument
    String phaseName = "";

    // sets the phaseName of the variable
    if (message.getPhase().equals("preprepare")) {
      phaseName = "preprepare";
    } else if (message.getPhase().equals("prepare")) {
      phaseName = "prepare";
    } else {
      phaseName = "commit";
    }

    // display detailed log about message transfer b/w nodes, what phase it is , and how many message does a node has received till now
    System.out.println(
      "- [" +
      phaseName +
      " Phase] " +
      "[Node " +
      name +
      "] " +
      "received " +
      phaseName +
      " message from " +
      "[Node " +
      primaryNode.getName() +
      "] " +
      "(" +
      name +
      "_total_" +
      phaseName.toLowerCase() +
      ": " +
      (
        phaseName.equals("preprepare")
          ? ++preprepareMessages
          : (
            phaseName.equals("prepare")
              ? totalPrepareMessagesReceived + 1 // this is just for displaying the correct value ( value is updated in following code)
              : totalCommitMessagesReceived + 1
          )
      ) +
      ")"
    );

    setMessage(message); // sets the received message
    // prevNodeList.add(primaryNode); // Add primaryNode to the prevNodeList

    // increment the number of messages based on
    if (message.getPhase().equals("prepare")) {
      // prepareMessagesReceivedFrom.add(message);
      totalPrepareMessagesReceived++;
    } else if (message.getPhase().equals("commit")) {
      // commitMessagesReceivedFrom.add(message);
      totalCommitMessagesReceived++;
      if (name.equals("N1") && totalCommitMessagesReceived > totalNodes / 2) { // check if the commit message increment is being done on primary nodes
        System.out.println(
          "[+] " +
          name +
          " received commit message from more than half of nodes therefore."
        );
        System.out.println("Transaction has been committed!");
        System.exit(0);
        return; // Exit the method
      }
    }
  }

  public List<PBFTNode> getPeers() {
    return peers;
  }
}

// Class for creating a message
class PBFTMessage {

  private String text; // stores the text of the message
  private String phase; // stores phase information ( pre-prepare , prepared or commit ) tells the type of message

  // constructor function to initialize the message object
  // with text and phase type
  public PBFTMessage(String text, String phase) {
    this.text = text;
    this.phase = phase;
  }

  public String getText() { // getter method to return the text of the message
    return text;
  }

  public String getPhase() { // getter method to return the phase of the message
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }
}

// pBFT algorithm : that shows how the messages flow through the nodes in pBFT to achieve consensus
public class Pbft {

  public static void main(String[] args) {
    int totalNodes = 10; // set the total number of nodes
    List<PBFTNode> nodes = new ArrayList<>(); // create an array list for storing PBFTnodes

    // fill the arraylist with PBFTNode's
    for (int i = 1; i <= totalNodes; i++) {
      String nodeName = "N" + i; // N1 to N100
      nodes.add(new PBFTNode(nodeName)); // add the new node to arraylist
    }

    // create a peer list
    // each node have a peer-nodes-list ( this list contain all nodes except itself )
    for (PBFTNode node : nodes) { // iterate over all the nodes
      for (PBFTNode peer : nodes) { // and for all of them add peers to peers-list except themself
        if (node != peer) {
          node.addPeer(peer);
        }
      }
      // System.out.println("peer list : " + node.getName());
      // for (PBFTNode peer : node.peers) {
      //   System.out.println("-" + peer.getName());
      // }
    }

    // setups up the primary node (node at index 0)
    PBFTNode primaryNode = nodes.get(0);

    PBFTMessage message = new PBFTMessage("Hello boi", "preprepare"); // create a per-prepared message

    // Start the preprepared phase and send message from primary node to all other backup nodes
    System.out.println("---- Preprepare Phase Started ----");
    for (PBFTNode node : nodes) {
      if (node != primaryNode) { // initially primaryNode was 1st node so send message from primaryNode to all nodes except itself
        primaryNode.sendMessage(message, node);
      }
    }
    System.out.println("---- Preprepare Phase Completed ----");

    System.out.println("---- Prepare Phase Started ----");
    for (PBFTNode node : nodes) {
      if (node != primaryNode) {
        // takethe message received from prev node and update its state
        node.getMessage().setPhase("prepare");
        for (PBFTNode peer : node.getPeers()) { // send the message with updated state to rest of the nodes
          if (peer != primaryNode && peer != node) {
            node.sendMessage(node.getMessage(), peer);
          }
        }
      }
    }
    System.out.println("---- Prepare Phase Completed ----");

    // commit phase : all nodes would send message to each other except themselves, as soon as the primary
    // node receives the message from more than half of the nodes it consider it to be valid .
    System.out.println("---- Commit Phase Started ----");
    // start the commit phase , loop over all the nodes
    for (PBFTNode node : nodes) {
      // the node must not send message to itself
      // and the totalnumber of preparemessages received should be more than 50% of the nodes only then it could send a commit message

      if (
        node != primaryNode && // primary node wont be able to send commit message to other nodes
        node.totalPrepareMessagesReceived >= totalNodes / 2
      ) {
        // get the message associated with the current node and update its phase
        node.getMessage().setPhase("commit");
        // then forward commit message to the rest of the nodes on the network
        for (PBFTNode peer : node.getPeers()) {
          if (peer != node) {
            node.sendMessage(node.getMessage(), peer);
          }
        }
      }
    }

    System.out.println("---- Commit Phase Completed ----");
  }
}
