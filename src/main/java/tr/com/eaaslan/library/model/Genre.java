package tr.com.eaaslan.library.model;

import com.fasterxml.jackson.annotation.JsonValue;


public enum Genre {
    FICTION,
    SCIENCE_FICTION,
    FANTASY,
    MYSTERY,
    THRILLER,
    HORROR,
    ROMANCE,
    HISTORICAL_FICTION,
    BIOGRAPHY,
    AUTOBIOGRAPHY,
    MEMOIR,
    POETRY,
    DRAMA,
    COMEDY,
    CHILDREN,
    YOUNG_ADULT,
    NON_FICTION,
    SCIENCE,
    HISTORY,
    PHILOSOPHY,
    RELIGION,
    PSYCHOLOGY,
    SELF_HELP,
    BUSINESS,
    ECONOMICS,
    POLITICS,
    TRAVEL,
    COOKING,
    ART,
    MUSIC,
    REFERENCE,
    TEXTBOOK,
    OTHER

//    @JsonValue
//    public String toDisplayName(){
//        return WordUtils.toTitleCase(this.name());
//    }
}
