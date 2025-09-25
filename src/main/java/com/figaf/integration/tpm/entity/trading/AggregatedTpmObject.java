package com.figaf.integration.tpm.entity.trading;

import com.figaf.integration.tpm.entity.AdministrativeData;
import com.figaf.integration.tpm.entity.trading.verbose.TpmObjectDetails;
import lombok.*;

import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class AggregatedTpmObject {

    private TpmObjectDetails tpmObjectDetails;
    private List<System> systems;
    private List<Identifier> identifiers;
    private Map<String, List<Channel>> systemIdToChannels;
    private ProfileConfiguration profileConfiguration;

    public List<System> getSystems() {
        if (systems == null) {
            systems = new ArrayList<>();
        }
        return systems;
    }

    public List<Identifier> getIdentifiers() {
        if (identifiers == null) {
            identifiers = new ArrayList<>();
        }
        return identifiers;
    }

    public Map<String, List<Channel>> getSystemIdToChannels() {
        if (systemIdToChannels == null) {
            systemIdToChannels = new LinkedHashMap<>();
        }
        return systemIdToChannels;
    }

    public AdministrativeData resolveLatestAdministrativeData() {
        return Stream.of(
                Stream.of(tpmObjectDetails.getAdministrativeData()),
                systems.stream().map(System::getAdministrativeData),
                identifiers.stream().map(Identifier::getAdministrativeData),
                systemIdToChannels.values().stream()
                    .flatMap(channels -> channels.stream().map(Channel::getAdministrativeData)),
                profileConfiguration == null ? Stream.<AdministrativeData>empty() : Stream.of(profileConfiguration.getAdministrativeData())
            )
            .flatMap(s -> s)
            .filter(Objects::nonNull)
            .max(Comparator.comparing(AdministrativeData::getModifiedAt))
            .orElseThrow();
    }

}
