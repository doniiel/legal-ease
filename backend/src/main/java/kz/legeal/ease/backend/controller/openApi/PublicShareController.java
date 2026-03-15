package kz.legeal.ease.backend.controller.openApi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.legeal.ease.backend.service.document.DocumentShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoint — no authentication required.
 * Serves the PDF of a shared document via a time-limited token.
 */
@RestController
@RequestMapping("/api/public/share")
@RequiredArgsConstructor
@Tag(
        name = "Public - Document Sharing",
        description = "Public endpoint for downloading shared documents via time-limited tokens. No authentication required."
)
public class PublicShareController {

    private final DocumentShareService documentShareService;

    @Operation(
            summary = "Download shared document",
            description = "Downloads the PDF of a shared document using a time-limited secure token. " +
                    "Returns 404 if the token has expired or is invalid."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PDF returned successfully"),
            @ApiResponse(responseCode = "404", description = "Share token not found or expired")
    })
    @GetMapping(value = "/{token}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(
            @Parameter(description = "Secure share token", required = true)
            @PathVariable String token
    ) {
        final byte[] pdfBytes = documentShareService.resolveShare(token);

        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("shared-document.pdf")
                        .build()
        );
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
