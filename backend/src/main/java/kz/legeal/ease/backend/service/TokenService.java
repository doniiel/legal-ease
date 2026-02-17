package kz.legeal.ease.backend.service;

import kz.legeal.ease.backend.domain.RefreshToken;
import kz.legeal.ease.backend.domain.User;
import kz.legeal.ease.backend.jwt.PersonDetails;

public interface TokenService {

    String generateAccessToken(PersonDetails userDetails);

    String generateRefreshToken(PersonDetails userDetails);

    void revokeToken(RefreshToken token);

    RefreshToken findValidStoredToken(String refreshToken);

    void revokeAllByUser(User user);

    long getAccessExpMin();

    Long extractUserId(String token, boolean isRefreshToken);

    boolean isTokenValid(String token, PersonDetails userDetails, boolean isRefreshToken);
}
