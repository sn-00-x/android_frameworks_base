/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view.textclassifier;

import static com.google.common.truth.Truth.assertThat;

import android.content.Intent;

import androidx.test.InstrumentationRegistry;
import androidx.test.filters.SmallTest;
import androidx.test.runner.AndroidJUnit4;

import com.google.android.textclassifier.AnnotatorModel;
import com.google.android.textclassifier.RemoteActionTemplate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class TemplateClassificationIntentFactoryTest {

    private static final String TEXT = "text";
    private static final String TITLE = "Map";
    private static final String DESCRIPTION = "Opens in Maps";
    private static final String ACTION = Intent.ACTION_VIEW;

    @Mock
    private IntentFactory mFallback;
    private TemplateClassificationIntentFactory mTemplateClassificationIntentFactory;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mTemplateClassificationIntentFactory = new TemplateClassificationIntentFactory(
                new TemplateIntentFactory(),
                mFallback);
    }

    @Test
    public void create_foreignText() {
        AnnotatorModel.ClassificationResult classificationResult =
                new AnnotatorModel.ClassificationResult(
                        TextClassifier.TYPE_ADDRESS,
                        1.0f,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        createRemoteActionTemplates());

        List<TextClassifierImpl.LabeledIntent> intents =
                mTemplateClassificationIntentFactory.create(
                        InstrumentationRegistry.getContext(),
                        TEXT,
                        /* foreignText */ true,
                        null,
                        classificationResult);

        assertThat(intents).hasSize(2);
        TextClassifierImpl.LabeledIntent labeledIntent = intents.get(0);
        assertThat(labeledIntent.getTitle()).isEqualTo(TITLE);
        Intent intent = labeledIntent.getIntent();
        assertThat(intent.getAction()).isEqualTo(ACTION);
        assertThat(intent.hasExtra(TextClassifier.EXTRA_FROM_TEXT_CLASSIFIER)).isTrue();

        labeledIntent = intents.get(1);
        intent = labeledIntent.getIntent();
        assertThat(intent.getAction()).isEqualTo(Intent.ACTION_TRANSLATE);
        assertThat(intent.hasExtra(TextClassifier.EXTRA_FROM_TEXT_CLASSIFIER)).isTrue();
    }

    @Test
    public void create_notForeignText() {
        AnnotatorModel.ClassificationResult classificationResult =
                new AnnotatorModel.ClassificationResult(
                        TextClassifier.TYPE_ADDRESS,
                        1.0f,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        createRemoteActionTemplates());

        List<TextClassifierImpl.LabeledIntent> intents =
                mTemplateClassificationIntentFactory.create(
                        InstrumentationRegistry.getContext(),
                        TEXT,
                        /* foreignText */ false,
                        null,
                        classificationResult);

        assertThat(intents).hasSize(1);
        TextClassifierImpl.LabeledIntent labeledIntent = intents.get(0);
        assertThat(labeledIntent.getTitle()).isEqualTo(TITLE);
        Intent intent = labeledIntent.getIntent();
        assertThat(intent.getAction()).isEqualTo(ACTION);
        assertThat(intent.hasExtra(TextClassifier.EXTRA_FROM_TEXT_CLASSIFIER)).isTrue();
    }

    private static RemoteActionTemplate[] createRemoteActionTemplates() {
        return new RemoteActionTemplate[]{
                new RemoteActionTemplate(
                        TITLE,
                        DESCRIPTION,
                        ACTION,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        };
    }
}
