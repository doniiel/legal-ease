package kz.legeal.ease.backend.service.rule.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class RankingResult {

    private Map<Long, Integer> scoreAdjustments;

    private Map<Long, String> notes;
}
