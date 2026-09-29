# Manual 10-Document RAG Pipeline (Java + Spring AI)

This module is Milestone 1 of a RAG learning track: build a complete retrieval pipeline manually over ten synthetic support documents.

Spring AI is used only for model calls:
- `EmbeddingModel` for chunk and query vectors
- `ChatClient` for answer generation

Everything else is plain Java:
- deterministic chunking
- in-memory index
- cosine similarity
- top-K ranking
- prompt assembly
- citation mapping

## Pipeline

```text
OFFLINE
documents -> chunks + metadata -> embeddings -> in-memory index

ONLINE
question -> query embedding -> cosine similarity -> top 3 chunks
         -> grounded prompt -> LLM answer -> citations
```

## Stack and Versions

- Java 25
- Spring Boot 4.1.1
- Spring AI BOM 2.0.0
- Spring AI OpenAI starter: `spring-ai-starter-model-openai`

Current model config in [src/main/resources/application.yaml](src/main/resources/application.yaml):
- Chat model: `bedrock-claude-v4.5_Sonnet`
- Embedding model: `text-embedding-3-large`
- Base URL: OpenAI-compatible endpoint

## Corpus

Ten synthetic AcmeFlow support docs live in [src/main/resources/corpus](src/main/resources/corpus).

Examples of known facts used for validation:
- password reset link validity: 30 minutes ([doc-02.md](src/main/resources/corpus/doc-02.md))
- free plan project limit: 10 active projects ([doc-03.md](src/main/resources/corpus/doc-03.md))
- API limit on free plan: 100 requests per rolling hour ([doc-08.md](src/main/resources/corpus/doc-08.md))
- workspace deletion recovery period: 30 days ([doc-10.md](src/main/resources/corpus/doc-10.md))

## Run Locally

Prerequisites:
- Java 25
- network access to your configured model endpoint
- `OPENAI_API_KEY` exported in environment

Start the app:

```sh
export OPENAI_API_KEY=your-key
./mvnw spring-boot:run
```

At startup, the app:
- loads all `classpath:corpus/*.md` files
- splits/pack paragraphs into chunks (target max 800 chars unless a single paragraph exceeds it)
- generates chunk embeddings
- publishes an immutable in-memory snapshot

If corpus loading or embedding fails, startup fails fast.

## API

Endpoint:

```http
POST /questions
Content-Type: application/json
```

Request:

```json
{
  "question": "How long can a deleted workspace be recovered?"
}
```

Example:

```sh
curl -X POST http://localhost:8080/questions \
  -H 'Content-Type: application/json' \
  -d '{"question":"How long can a deleted workspace be recovered?"}'
```

Response shape:

```json
{
  "answer": "A deleted workspace can be recovered for 30 days [doc-10.md#0].",
  "citations": [
    {
      "source": "doc-10.md",
      "chunkIndex": 0,
      "score": 0.82
    }
  ]
}
```

Error mapping:
- `400` for blank/invalid question
- `503` when index is not ready
- `502` when embedding/chat model calls fail

## Testing and Evaluation

Automated tests (mocked model boundaries, no paid calls):

```sh
./mvnw -q test
```

Detailed test strategy and coverage map: [TESTING.md](TESTING.md)

Manual live-evaluation worksheet and scoring table: [results.md](results.md)

## Current Limitations

- no `VectorStore` yet (in-memory only)
- no relevance threshold or reranker
- no hybrid search
- no persistence across restarts
- unsupported-question refusal depends on prompt behavior, so live verification is required

## Next Step

After this manual milestone is fully validated, build a second version with Spring AI `VectorStore` + advisor flow and compare transparency vs productivity.
