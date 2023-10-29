# Proof of Work (PoW) Algorithm

## Overview

In this project, we have implemented a PoW consensus algorithm. PoW is a crucial part of many blockchain systems, where miners compete to solve a computationally intensive puzzle to add a new block to the blockchain.

## Code Structure

The code is organized into the following sections:

- **Configuration**: Configuration properties are loaded from a `config.properties` file, specifying the target prefix (mining difficulty) and the number of mining threads.

- **Mining Threads**: Mining is performed by a specified number of threads. Each thread starts mining with a unique nonce.

- **Mining Function**: The `mine` function demonstrates the mining process. It repeatedly calculates a hash and checks if it meets the target prefix, indicating a valid solution.

- **Verification**: After mining, the code allows a user to input a nonce for verification. It calculates the hash using the user-provided nonce and validates it against the target prefix.

## Execution

- The code is executed by starting multiple mining threads.

- The threads run concurrently to find a nonce that, when combined with the transaction data and previous hash, results in a hash with a prefix that matches the mining difficulty.

- When a thread finds a valid solution, it prints the nonce and other performance metrics.

## Verification

- After mining, the code allows the user to input a nonce for verification. It calculates the hash with the user-provided nonce and checks if it satisfies the mining difficulty.

## Configuration

- Configuration properties are loaded from a `config.properties` file, making it easy to adjust the mining difficulty and the number of mining threads.

## Dependencies

- The code utilizes Java's `MessageDigest` for SHA-256 hashing and relies on a `config.properties` file for configuration.

## Usage

- To execute the code, provide a `config.properties` file with the desired configuration.

- The code demonstrates how PoW mining works, measures performance, and allows users to verify solutions.

## Conclusion

This code provides a basic implementation of the PoW consensus algorithm, illustrating how miners work together to secure a blockchain through computational work.
