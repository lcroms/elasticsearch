/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.analysis.common;

import org.apache.lucene.analysis.MockTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.IndexSettingsModule;
import org.elasticsearch.test.VersionUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

public class CommonAnalysisPluginTests extends ESTestCase {

    /**
     * Check that the deprecated "nGram" filter throws exception for indices created since 7.0.0 and
     * logs a warning for earlier indices when the filter is used as a custom filter
     */
    public void testNGramFilterInCustomAnalyzerDeprecationError() throws IOException {
        final Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_8_0_0, Version.CURRENT))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "my_ngram")
            .put("index.analysis.filter.my_ngram.type", "nGram")
            .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            Map<String, TokenFilterFactory> tokenFilters = createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings),
                    settings, commonAnalysisPlugin).tokenFilter;
            TokenFilterFactory tokenFilterFactory = tokenFilters.get("nGram");
            Tokenizer tokenizer = new MockTokenizer();
            tokenizer.setReader(new StringReader("foo bar"));

            IllegalArgumentException ex = expectThrows(IllegalArgumentException.class, () -> tokenFilterFactory.create(tokenizer));
            assertEquals("The [nGram] token filter name was deprecated in 6.4 and cannot be used in new indices. "
                    + "Please change the filter name to [ngram] instead.", ex.getMessage());
        }

        final Settings settingsPre7 = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
                .put(IndexMetaData.SETTING_VERSION_CREATED, VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.V_7_6_0))
                .put("index.analysis.analyzer.custom_analyzer.type", "custom")
                .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
                .putList("index.analysis.analyzer.custom_analyzer.filter", "my_ngram").put("index.analysis.filter.my_ngram.type", "nGram")
                .build();
        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            Map<String, TokenFilterFactory> tokenFilters = createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settingsPre7),
                    settingsPre7, commonAnalysisPlugin).tokenFilter;
            TokenFilterFactory tokenFilterFactory = tokenFilters.get("nGram");
            Tokenizer tokenizer = new MockTokenizer();
            tokenizer.setReader(new StringReader("foo bar"));
            assertNotNull(tokenFilterFactory.create(tokenizer));
            assertWarnings("The [nGram] token filter name is deprecated and will be removed in a future version. "
                    + "Please change the filter name to [ngram] instead.");
        }
    }

    /**
     * Check that the deprecated "edgeNGram" filter throws exception for indices created since 7.0.0 and
     * logs a warning for earlier indices when the filter is used as a custom filter
     */
    public void testEdgeNGramFilterInCustomAnalyzerDeprecationError() throws IOException {
        final Settings settings = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
            .put(IndexMetaData.SETTING_VERSION_CREATED,
                VersionUtils.randomVersionBetween(random(), Version.V_8_0_0, Version.CURRENT))
            .put("index.analysis.analyzer.custom_analyzer.type", "custom")
            .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
            .putList("index.analysis.analyzer.custom_analyzer.filter", "my_ngram")
            .put("index.analysis.filter.my_ngram.type", "edgeNGram")
            .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            Map<String, TokenFilterFactory> tokenFilters = createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settings),
                    settings, commonAnalysisPlugin).tokenFilter;
            TokenFilterFactory tokenFilterFactory = tokenFilters.get("edgeNGram");
            Tokenizer tokenizer = new MockTokenizer();
            tokenizer.setReader(new StringReader("foo bar"));

            IllegalArgumentException ex = expectThrows(IllegalArgumentException.class, () -> tokenFilterFactory.create(tokenizer));
            assertEquals("The [edgeNGram] token filter name was deprecated in 6.4 and cannot be used in new indices. "
                    + "Please change the filter name to [edge_ngram] instead.", ex.getMessage());
        }

        final Settings settingsPre7 = Settings.builder().put(Environment.PATH_HOME_SETTING.getKey(), createTempDir())
                .put(IndexMetaData.SETTING_VERSION_CREATED,
                    VersionUtils.randomVersionBetween(random(), Version.V_7_0_0, Version.V_7_6_0))
                .put("index.analysis.analyzer.custom_analyzer.type", "custom")
                .put("index.analysis.analyzer.custom_analyzer.tokenizer", "standard")
                .putList("index.analysis.analyzer.custom_analyzer.filter", "my_ngram")
                .put("index.analysis.filter.my_ngram.type", "edgeNGram")
                .build();

        try (CommonAnalysisPlugin commonAnalysisPlugin = new CommonAnalysisPlugin()) {
            Map<String, TokenFilterFactory> tokenFilters = createTestAnalysis(IndexSettingsModule.newIndexSettings("index", settingsPre7),
                    settingsPre7, commonAnalysisPlugin).tokenFilter;
            TokenFilterFactory tokenFilterFactory = tokenFilters.get("edgeNGram");
            Tokenizer tokenizer = new MockTokenizer();
            tokenizer.setReader(new StringReader("foo bar"));
            assertNotNull(tokenFilterFactory.create(tokenizer));
            assertWarnings("The [edgeNGram] token filter name is deprecated and will be removed in a future version. "
                    + "Please change the filter name to [edge_ngram] instead.");
        }
    }
}
