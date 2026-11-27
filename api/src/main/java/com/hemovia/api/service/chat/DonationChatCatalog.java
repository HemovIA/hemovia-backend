package com.hemovia.api.service.chat;

import com.hemovia.api.dto.response.DonationChatResponse;
import com.hemovia.api.dto.response.DonationChatResponse.Source;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class DonationChatCatalog {

    static final int MAX_CARDS = 3;
    private static final String REFERRAL = "encaminhamento";
    private static final String CLARIFICATION = "esclarecimento";

    private final Map<String, Source> sources;
    private final Map<String, Card> cards;
    private final String choices;

    public DonationChatCatalog(InputStream input) {
        CatalogData data = new ObjectMapper().readValue(input, CatalogData.class);
        sources = Map.copyOf(data.sources());
        cards = data.cards().stream().collect(Collectors.toUnmodifiableMap(Card::id, Function.identity()));
        if (!cards.containsKey(REFERRAL) || !cards.containsKey(CLARIFICATION)
                || data.cards().stream().anyMatch(card -> card.sourceIds().isEmpty()
                        || !sources.keySet().containsAll(card.sourceIds()))) {
            throw new IllegalStateException("O catálogo de doação contém referências incompletas.");
        }
        choices = data.cards().stream()
                .map(card -> "- " + card.id() + " (" + card.topic() + "): " + card.appliesWhen())
                .collect(Collectors.joining("\n"));
    }

    public DonationChatResponse respond(List<String> selection) {
        List<String> chosen = valid(selection) ? selection : List.of(REFERRAL);
        List<Card> selected = chosen.stream().map(cards::get).toList();
        String answer = selected.stream().map(Card::answer).collect(Collectors.joining("\n\n"));
        List<Source> references = selected.stream()
                .flatMap(card -> card.sourceIds().stream()).distinct().map(sources::get).toList();
        return new DonationChatResponse(answer, references);
    }

    String choices() {
        return choices;
    }

    List<String> cardIds() {
        return cards.keySet().stream().sorted().toList();
    }

    private boolean valid(List<String> selection) {
        return selection != null && !selection.isEmpty() && selection.size() <= MAX_CARDS
                && selection.stream().allMatch(id -> id != null && cards.containsKey(id))
                && selection.stream().distinct().count() == selection.size()
                && (selection.size() == 1 || !selection.contains(REFERRAL) && !selection.contains(CLARIFICATION));
    }

    private record CatalogData(int version, String revisedOn, Map<String, Source> sources, List<Card> cards) { }

    private record Card(String id, String topic, String appliesWhen, String answer,
                        String referenceSection, List<String> sourceIds) { }
}

