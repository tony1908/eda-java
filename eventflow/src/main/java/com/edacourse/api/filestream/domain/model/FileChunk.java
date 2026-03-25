package com.edacourse.api.filestream.domain.model;

/**
 * Representa un fragmento de un archivo grande.
 * Cada chunk tiene metadata para reensamblado y verificacion.
 */
public record FileChunk(
    String fileId,
    String fileName,
    int partNumber,
    int totalParts,
    byte[] data,
    String checksum,
    String extension
) {
    public boolean isLast() {
        return partNumber == totalParts;
    }
}
