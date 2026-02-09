package org.example.project

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ComposeAppCommonTest : FunSpec({
    test("basic arithmetic should work") {
        (1 + 2) shouldBe 3
    }
})