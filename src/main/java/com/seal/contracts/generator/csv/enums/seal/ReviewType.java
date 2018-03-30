package com.seal.contracts.generator.csv.enums.seal;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

/**
 * Created by jantonak on 06/09/17.
 */
public enum ReviewType {
    PRIMARY_DOCUMENT("Primary Review Contract Document", false),
    SUPPORTING_DOCUMENT("Supporting Document", false),
    REVIEWTYPE_SKIP("Do not bring to Ariba", true),
    UNKNOWN("UNKNOWN", true);

    @Getter
    private final String label;

    @Getter
    private final boolean skip;

    private static final ImmutableMap<String, ReviewType> lookup;

    static {
        lookup = Maps.uniqueIndex(Lists.newArrayList(values()), new Function<ReviewType, String>() {
            @Override
            public String apply(ReviewType reviewType) {
                return reviewType.label;
            }
        });
    }

    ReviewType(String label, boolean skip) {
        this.label = label;
        this.skip = skip;
    }

    public static ReviewType lookup(String label) {
        return Optional.fromNullable(lookup.get(Strings.nullToEmpty(label))).or(UNKNOWN);
    }
}
