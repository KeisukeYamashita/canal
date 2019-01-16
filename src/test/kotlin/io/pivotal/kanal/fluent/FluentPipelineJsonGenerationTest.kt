/*
 * Copyright 2018 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.kanal.fluent

import io.pivotal.kanal.json.FanOutMultistagePipeline
import io.pivotal.kanal.json.JsonAdapterFactory
import io.pivotal.kanal.model.*
import org.junit.jupiter.api.Test

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson

class FluentPipelineJsonGenerationTest {

    @Test
    fun `fluent stages DSL with fan out and fan in`() {
        val stages = Stages.first(CheckPreconditionsStage(
                "Check Preconditions",
                emptyList()
        )).andThen(WaitStage(
                "Server Group Timeout",
                420,
                "woah"
        )).fanOut(
                (1..3).map {
                    Stages.first(DestroyServiceStage(
                            "Destroy Service $it Before",
                            "cloudfoundry",
                            "creds1",
                            "dev > dev",
                            "serviceName$it",
                            ExpressionCondition("exp1")
                    )).andThen(DeployServiceStage(
                            "Deploy Service $it",
                            "cloudfoundry",
                            "deploy comment",
                            "creds1",
                            "serviceParam$it",
                            "dev > dev",
                            "serviceType$it",
                            "serviceName$it",
                            "servicePlan$it",
                            ExpressionCondition("exp2"),
                            "serviceTags$it"
                    ))
                }
        ).fanIn(ManualJudgmentStage(
                "Thumbs Up?",
                "Give a thumbs up if you like it."
        ))

        val json = JsonAdapterFactory().stageGraphAdapter().toJson(stages.stageGraph)
        assertThatJson(json).isEqualTo(FanOutMultistagePipeline.json)
    }

}
