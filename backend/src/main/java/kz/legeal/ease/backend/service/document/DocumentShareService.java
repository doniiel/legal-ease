package kz.legeal.ease.backend.service.document;

import kz.legeal.ease.backend.dto.document.DocumentShareResponse;

public interface DocumentShareService {

    /** Create a time-limited share link for a completed document. */
    DocumentShareResponse createShareLink(Long docId);

    /** Resolve a share token to the document's PDF bytes (validates expiry). */
    byte[] resolveShare(String token);
}
