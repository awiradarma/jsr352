/*
 * Copyright (c) 2013-2015 Red Hat, Inc. and/or its affiliates.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.jberet.job.model;

/**
 * Enum type to hold all XML attribute names for job xml and batch.xml.
 *
 * @see XmlElement
 */
public enum XmlAttribute {
    UNKNOWN(null),

    //attributes from job xml & batch.xml, in alphabetical order
    ABSTRACT("abstract"),
    ALLOW_START_IF_COMPLETE("allow-start-if-complete"),
    CHECKPOINT_POLICY("checkpoint-policy"),
    CLASS("class"),
    EXIT_STATUS("exit-status"),
    ID("id"),
    ITEM_COUNT("item-count"),
    JSL_NAME("jsl-name"),
    MERGE("merge"),
    NAME("name"),
    NEXT("next"),
    ON("on"),
    PARENT("parent"),
    PARTITION("partition"),
    PARTITIONS("partitions"),
    REF("ref"),
    RESTART("restart"),
    RESTARTABLE("restartable"),
    RETRY_LIMIT("retry-limit"),
    SKIP_LIMIT("skip-limit"),
    SRC("src"),
    START_LIMIT("start-limit"),
    THREADS("threads"),
    TIME_LIMIT("time-limit"),
    TO("to"),
    TYPE("type"),
    VALUE("value");

    private final String name;

    XmlAttribute(final String name) {
        this.name = name;
    }

    /**
     * Get the local name of this attribute.
     *
     * @return the local name
     */
    public String getLocalName() {
        return name;
    }
}
