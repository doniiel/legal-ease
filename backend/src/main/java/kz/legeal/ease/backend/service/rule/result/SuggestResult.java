package kz.legeal.ease.backend.service.rule.result;

import kz.legeal.ease.backend.service.rule.common.FieldSuggestion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuggestResult {

    private List<FieldSuggestion> suggestions;
}
