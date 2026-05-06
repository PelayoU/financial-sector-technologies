# Financial Sector Technologies

A portfolio of **fintech infrastructure** and **capital-markets technology** projects covering the full stack a trading system relies on: latency analysis, low-latency messaging, in-memory data grids, market protocols, secure transport, applied cryptography, serialization, and on-chain settlement primitives. Each project is self-contained, builds with one Maven command and ships with a portfolio-grade README that explains the engineering trade-offs in production terms.

The work was developed during my **MSc in Financial Sector Technologies (UC3M)**.

---

## Project index

### Performance, latency and concurrency

| Project | Stack | Focus |
| :--- | :--- | :--- |
| **[Latency & Jitter Analysis](./latency-jitter-analysis)** | Java · HdrHistogram · Maven | Service time vs response time under high concurrency. Demonstrates the *coordinated omission* problem and the hockey-stick transition between healthy and saturated regimes. |
| **[Low-Latency Market Data Messenger](./low-latency-market-data-messenger)** | Java 21 · TCP · UDP Multicast · Wireshark | Custom binary market-data protocol over TCP (length-prefixed framing) and UDP multicast, with PCAP captures verifying the wire format. |
| **[Low-Latency Messaging Benchmarks with Aeron](./low-latency-messaging-benchmarks-with-aeron)** | Java · [Aeron](https://github.com/real-logic/aeron) · HdrHistogram | Publish/subscribe over unicast, multicast and IPC at three load points (1k/50k/100k msg/s). Shows why message integrity must beat raw mean latency in trading. |

### Market protocols and connectivity

| Project | Stack | Focus |
| :--- | :--- | :--- |
| **[FIX Protocol Market Data Client](./fix-protocol-market-data-client)** | Java 21 · QuickFIX/J · FIX 5.0SP2 | Acceptor + Initiator implementing `MarketDataRequest` / `MarketDataIncrementalRefresh` for TSLA & NVDA. Includes raw FIX wire logs and full session lifecycle. |

### In-memory data grids

| Project | Stack | Focus |
| :--- | :--- | :--- |
| **[Hazelcast IMDG — Market Volume Control](./hazelcast-imdg-market-volume-control)** | Java 21 · Hazelcast 5 | Two-part project: cluster fundamentals (TCP discovery, distributed `IMap`, CP `ICountDownLatch`) and trading-system patterns (real-time `EntryListener` for volume alerts, end-of-day `EntryProcessor` batch). |

### Secure messaging and applied cryptography

| Project | Stack | Focus |
| :--- | :--- | :--- |
| **[Spring Boot Basic Auth Lab](./spring-boot-basic-auth-lab)** | Java 17 · Spring Boot 3 · Spring Security · Actuator | Minimal HTTPS-ready microservice with HTTP Basic Auth and an Actuator health probe — the smallest footprint that's still recognisable as a financial-grade service. |
| **[Applied Cryptography — AES, RSA & TLS](./applied-cryptography-aes-rsa-tls)** | Java 21 · Spring Boot · `javax.crypto` · keytool | Self-signed PKCS#12 keystore + Spring Boot HTTPS, AES (symmetric) and RSA (asymmetric) helpers, plus a benchmark that quantifies why HTTPS is hybrid (RSA + AES) rather than purely one. |

### Serialization and data interchange

| Project | Stack | Focus |
| :--- | :--- | :--- |
| **[Serialization Benchmarks — Protobuf vs Kryo](./serialization-benchmarks-protobuf-kryo)** | Java 21 · Protocol Buffers · Kryo · HdrHistogram | 5M-iteration benchmark on a market reference-data payload. Proto wins on encode speed by ~5.5×; Kryo wins on payload size for small objects. Hot-path vs cold-path framing. |

### Generative AI & blockchain (research notes)

| Project | Topic | Focus |
| :--- | :--- | :--- |
| **[Generative AI in Finance](./generative-ai-in-finance-research-note)** | ChatGPT · Prompt engineering | Three case studies (IRR computation, investment-committee report, swap explained with cromos). Shows where LLMs *help* and where they *quietly fail* — and what to do about it. |
| **[Blockchain in Capital Markets](./blockchain-capital-markets-research-note)** | Solidity · Remix · Sepolia · MetaMask | End-to-end ERC-20 deployment on Sepolia testnet. Includes verifiable on-chain artefacts (contract address, transaction hash, Etherscan links). |

---

## Areas of focus

- **Market connectivity** — FIX 5.0SP2, custom binary protocols, TCP / UDP multicast.
- **Distributed systems** — Aeron pub/sub, Hazelcast IMDG, CP-subsystem coordination, Protobuf / Kryo serialization.
- **Performance discipline** — Service time vs response time, accumulated latency, message integrity under load, HdrHistogram.
- **Security and cryptography** — TLS / HTTPS via PKCS#12, symmetric (AES) and asymmetric (RSA) ciphers, hybrid cryptography rationale.
- **Emerging finance technology** — Smart contracts (Solidity / ERC-20), wallet derivation, generative AI in financial workflows.

---

## Author

**Pelayo Urzaiz**

- BSc in Applied Statistics — Universidad Complutense de Madrid (UCM)
- MSc in Financial Technologies (FinTech) — Universidad Carlos III de Madrid (UC3M)
- MSc in Quantitative Finance — Universidad Nacional de Educación a Distancia (UNED)

[LinkedIn Profile](https://www.linkedin.com/in/pelayourzaiz/)
