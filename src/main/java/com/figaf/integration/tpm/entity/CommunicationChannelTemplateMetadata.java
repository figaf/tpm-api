package com.figaf.integration.tpm.entity;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CommunicationChannelTemplateMetadata implements Serializable {

    private String id;
    private String alias;
    private String name;
}
