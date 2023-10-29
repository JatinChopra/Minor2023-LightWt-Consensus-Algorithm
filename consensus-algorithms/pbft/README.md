# Practical Byzantine Fault Tolerance (PBFT) Algorithm

## Overview

The PBFT algorithm is designed to achieve consensus in a distributed network, even when some nodes are faulty. It utilizes a multi-phase process, including Pre-Prepare, Prepare, and Commit messages, to reach an agreement among nodes.

## Code Structure

### PBFTNode Class

- A `PBFTNode` represents a node in the PBFT network. It has a name, a list of peer nodes, and counters for received Prepare and Commit messages. It can send and receive PBFT messages.

- The `addPeer` method allows a node to add other nodes (peers) to its list of peers.

- The `sendMessage` method is used to send PBFT messages to other nodes.

- The `receiveMessage` method handles the reception of PBFT messages, updates the message, and counts received messages.

### PBFTMessage Class

- A `PBFTMessage` represents a PBFT message with text and a phase (preprepare, prepare, or commit). It can be used to create, access, and update messages.

### Pbft Class

- The `Pbft` class serves as the main driver for the PBFT algorithm.

- It creates a list of PBFT nodes, defines a primary node, and simulates the Pre-Prepare, Prepare, and Commit phases.

- In the Pre-Prepare phase, the primary node sends a Pre-Prepare message to all backup nodes.

- In the Prepare phase, backup nodes receive the Pre-Prepare message, update it to a Prepare message, and send it to other nodes.

- In the Commit phase, nodes receive Prepare messages and, if conditions are met, forward Commit messages.

- When the primary node receives Commit messages from more than half of the nodes, it considers the transaction committed.

## Code Usage

- The code demonstrates the functioning of the PBFT algorithm in a simplified environment.

- You can experiment with different configurations, such as changing the number of nodes, message contents, and phases.

## Conclusion

This code provides a basic implementation of the PBFT consensus algorithm. It showcases how PBFT nodes communicate and reach consensus through Pre-Prepare, Prepare, and Commit phases. You can use this code as a starting point for further exploration and experimentation with the PBFT algorithm.
