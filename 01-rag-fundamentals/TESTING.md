# Testing Guide and Evidence

This document explains how this module is tested, what is covered, and what must still be validated live before claiming milestone completion.

## Test Status Snapshot

Latest local run (2026-09-29):
- command: `./mvnw -q test`
- total tests: 31
- failures: 0
- errors: 0
- skipped: 0
- suite time (Surefire aggregate): 1.552s

These are unit and API-slice tests only. They do not call paid/remote models.

## How to Run Tests

Run full suite:

```sh
./mvnw -q test
```

Run selected suites:

```sh
./mvnw -q -Dtest=CorpusLoaderTest,CorpusIndexRunnerTest,InMemoryEmbeddingIndexTest,CosineSimilarityTest,RetrievalServiceTest,PromptBuilderTest,RagServiceTest,QuestionControllerTest test
```

## Coverage Map

### Corpus loading and chunking
- `CorpusLoaderTest`
- Validates:
  - all 10 corpus files are discovered
  - chunk IDs are unique and deterministic (`source#index`)
  - `source`, `chunkIndex`, and content integrity are preserved
  - paragraph split behavior across mixed line endings
  - 800-char packing rule and oversized paragraph handling

### Embedding index safety
- `InMemoryEmbeddingIndexTest`
- Validates:
  - embeddings are attached and published as immutable snapshot
  - empty corpus is rejected
  - inconsistent dimensions are rejected
  - previous valid snapshot survives failed re-index attempt

### Math correctness
- `CosineSimilarityTest`
- Validates:
  - identical vectors -> ~1.0
  - opposite vectors -> ~-1.0
  - orthogonal vectors -> ~0.0
  - mismatched dimensions -> exception
  - zero-vector policy -> 0.0

### Retrieval behavior
- `RetrievalServiceTest`
- Validates:
  - ranked ordering and top-K limiting
  - top-K larger than corpus returns available chunks
  - blank question and invalid top-K reject early
  - uninitialized index produces controlled error
  - embedding failures/dimension mismatch map to model exceptions

### Prompt grounding
- `PromptBuilderTest`
- Validates:
  - refusal rule is present
  - all retrieved chunk labels are included
  - question is appended to prompt

### RAG orchestration and citation trust
- `RagServiceTest`
- Validates:
  - returned citations come from retrieval metadata, not generated answer text
  - chat failures are sanitized to application-level model errors
  - blank model output is treated as error

### API contract and error mapping
- `QuestionControllerTest`
- Validates:
  - `POST /questions` success payload shape
  - blank question -> HTTP 400
  - index not ready -> HTTP 503
  - model failure -> HTTP 502 with sanitized message

### Startup indexing flow
- `CorpusIndexRunnerTest`
- Validates:
  - runner loads corpus and triggers indexing
  - loader failures fail startup flow
  - embedding failures are propagated

## What Automated Tests Do Not Prove

- They do not verify real embedding quality.
- They do not verify live model refusal behavior for unsupported questions.
- They do not verify retrieval quality under production latency/scale.
- They do not verify prompt-token truncation behavior for larger corpora.

Those require manual, credentialed, live evaluation.

## Manual Live Evaluation Protocol

Use this with a valid `OPENAI_API_KEY` and reachable model endpoint.

1. Start app:

```sh
export OPENAI_API_KEY=your-key
./mvnw spring-boot:run
```

2. Ask at least five questions (direct, paraphrase, related-topic, unsupported, ambiguous).

3. Record each run in [results.md](results.md) using:
- expected source
- retrieved top-3
- top score
- answer rating: `Correct`, `Partial`, `Wrong`, `Refused correctly`
- earliest failing stage (if any):
  1. corpus/content gap
  2. chunking problem
  3. embedding/retrieval problem
  4. top-K/context selection problem
  5. prompt/generation problem
  6. citation/presentation problem

## Suggested LinkedIn Evidence Pack

For a credible milestone post, include:
- architecture diagram from [architecture.md](architecture.md)
- one retrieval trace (rank + source + score)
- one failed/edge query and the classified failure stage
- one sentence on what Spring AI handled vs what was manual
- link to this module and test evidence ([README.md](README.md), [TESTING.md](TESTING.md), [results.md](results.md))

Avoid claiming production readiness until live evaluation rows in [results.md](results.md) are complete.
