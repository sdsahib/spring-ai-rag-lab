package com.sahib._1ragfundamentals.model;

public record DocumentChunk(String id,
                            String content,
                            String source,
                            int chunkIndex,
                            float[] embeddings) {

    public DocumentChunk withEmbeddings(float[] newEmbeddings) {
        return new DocumentChunk(this.id, this.content, this.source, this.chunkIndex, newEmbeddings);
    }
}
