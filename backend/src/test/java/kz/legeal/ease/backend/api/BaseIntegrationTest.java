package kz.legeal.ease.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.legeal.ease.backend.domain.Role;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.domain.UserRole;
import kz.legeal.ease.backend.jwt.JwtUtils;
import kz.legeal.ease.backend.jwt.PersonDetails;
import kz.legeal.ease.backend.repository.AuditLogRepository;
import kz.legeal.ease.backend.repository.DocumentRepository;
import kz.legeal.ease.backend.repository.DocumentReviewRepository;
import kz.legeal.ease.backend.repository.DocumentShareRepository;
import kz.legeal.ease.backend.repository.DocumentVersionRepository;
import kz.legeal.ease.backend.repository.LawyerApplicationRepository;
import kz.legeal.ease.backend.repository.RefreshTokenRepository;
import kz.legeal.ease.backend.repository.RoleRepository;
import kz.legeal.ease.backend.repository.TemplateRepository;
import kz.legeal.ease.backend.repository.UserRepository;
import kz.legeal.ease.backend.repository.VerificationRepository;
import kz.legeal.ease.backend.service.EmailSenderService;
import kz.legeal.ease.backend.storage.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base class for all integration tests.
 *
 * <p>Starts a single shared PostgreSQL container (Testcontainers).
 * Creates test users for all three roles before each test and cleans them up after.
 * Provides helper methods for authenticated HTTP calls via MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // ── Shared Testcontainers PostgreSQL — singleton per JVM ──────────────────
    //
    // Using static initializer (singleton pattern) instead of @Testcontainers +
    // @Container so the same container instance is reused across ALL test classes
    // in a single Gradle test run. The container is stopped by Testcontainers'
    // JVM shutdown hook when the test process exits.

    static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("legalease_test")
                .withUsername("test")
                .withPassword("test");
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDataSourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    // ── Spring Beans ──────────────────────────────────────────────────────────

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtUtils jwtUtils;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    private DocumentReviewRepository documentReviewRepository;

    @Autowired
    private DocumentShareRepository documentShareRepository;

    @Autowired
    private DocumentVersionRepository documentVersionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private LawyerApplicationRepository lawyerApplicationRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    // ── Mocked External Services ──────────────────────────────────────────────

    @MockitoBean
    protected StorageService storageService;

    @MockitoBean
    protected EmailSenderService emailSenderService;

    // ── Test User State ───────────────────────────────────────────────────────

    protected User testUser;
    protected User testLawyer;
    protected User testAdmin;
    protected User testUser2;   // second user for IDOR tests

    protected String userToken;
    protected String lawyerToken;
    protected String adminToken;
    protected String user2Token;

    // ── Test Data Prefixes ────────────────────────────────────────────────────

    /** Unique suffix per test run to avoid unique constraint violations. */
    private String suffix;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeEach
    void baseSetUp() {
        suffix = UUID.randomUUID().toString().substring(0, 8);

        // Stub external services
        doNothing().when(emailSenderService).send(any());
        when(storageService.uploadFile(anyString(), any(), anyString()))
                .thenReturn("documents/test/test.pdf");
        when(storageService.downloadFile(anyString()))
                .thenReturn(new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF magic bytes
        when(storageService.generatePresignedUrl(anyString(), any()))
                .thenReturn("http://localhost:9000/test/doc.pdf");

        // Create test users
        testUser   = createTestUser("user_"   + suffix, "USER");
        testLawyer = createTestUser("lawyer_" + suffix, "LAWYER");
        testAdmin  = createTestUser("admin_"  + suffix, "ADMIN");
        testUser2  = createTestUser("user2_"  + suffix, "USER");

        // Generate JWT tokens
        userToken   = generateToken(testUser,   "USER");
        lawyerToken = generateToken(testLawyer, "LAWYER");
        adminToken  = generateToken(testAdmin,  "ADMIN");
        user2Token  = generateToken(testUser2,  "USER");
    }

    @AfterEach
    void baseTearDown() {
        // Delete in FK-safe order. Subclass @AfterEach runs first (JUnit 5 semantics),
        // so any test-specific cleanup happens before this runs.
        // Document children first, then documents, then templates, then users.
        documentReviewRepository.deleteAll();
        documentShareRepository.deleteAll();
        documentVersionRepository.deleteAll();
        auditLogRepository.deleteAll();
        documentRepository.deleteAll();
        templateRepository.deleteAll();
        lawyerApplicationRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        verificationRepository.deleteAll();

        List.of(testUser, testLawyer, testAdmin, testUser2).forEach(u -> {
            if (u != null && u.getId() != null) {
                userRepository.deleteById(u.getId());
            }
        });
    }

    // ── User Factory ──────────────────────────────────────────────────────────

    protected User createTestUser(String alias, String roleCode) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Role " + roleCode + " not seeded by Liquibase"));

        User user = User.builder()
                .iin(generateIin())
                .email(alias + "@test.com")
                .phone("+7700" + randomDigits(7))
                .fio("Test " + alias)
                .password(passwordEncoder.encode("Password1!"))
                .active(true)
                .deleted(false)
                .build();

        user = userRepository.save(user);

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .active(true)
                .assignedAt(LocalDateTime.now())
                .build();

        user.getUserRoles().add(userRole);
        return userRepository.save(user);
    }

    protected String generateToken(User user, String roleCode) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleCode));
        // Build a transient UserRole with the role loaded to avoid lazy-loading issues
        UserRole userRole = UserRole.builder()
                .user(user).role(role).active(true)
                .assignedAt(LocalDateTime.now()).build();
        return jwtUtils.generateAccessToken(new PersonDetails(user, userRole));
    }

    // ── Request Helpers ───────────────────────────────────────────────────────

    protected MockHttpServletRequestBuilder authGet(String url, String token) {
        return get(url).header("Authorization", "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder authPost(String url, String token, Object body) throws Exception {
        return post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder authPut(String url, String token, Object body) throws Exception {
        return put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    protected MockHttpServletRequestBuilder authPatch(String url, String token) {
        return patch(url).header("Authorization", "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder authDelete(String url, String token) {
        return delete(url).header("Authorization", "Bearer " + token);
    }

    protected ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mvc.perform(builder);
    }

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private String generateIin() {
        // Extract 12 decimal digits from a UUID — guaranteed unique per call
        String hex = UUID.randomUUID().toString().replace("-", "");
        StringBuilder digits = new StringBuilder(12);
        for (char c : hex.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            if (digits.length() == 12) return digits.toString();
        }
        // Pad with '0' if UUID had fewer than 12 digit chars (extremely rare)
        while (digits.length() < 12) digits.append('0');
        return digits.toString();
    }

    private String randomDigits(int count) {
        // Use UUID entropy instead of Math.random() to avoid per-test collisions
        String hex = UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder(count);
        for (char c : hex.toCharArray()) {
            if (Character.isDigit(c)) sb.append(c);
            if (sb.length() == count) return sb.toString();
        }
        while (sb.length() < count) sb.append('0');
        return sb.toString();
    }
}
