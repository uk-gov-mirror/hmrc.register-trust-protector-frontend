/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pages.individual.mld5

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.register.individual.mld5.{NationalityUkYesNoPage, NationalityPage, NationalityYesNoPage}

class NationalityUkYesNoPageSpec extends PageBehaviours {

  "NationalityUkYesNoPage" must {

    beRetrievable[Boolean](NationalityUkYesNoPage(0))

    beSettable[Boolean](NationalityUkYesNoPage(0))

    beRemovable[Boolean](NationalityUkYesNoPage(0))

    "Yes selected - set NationalityPage to 'GB' " in {
      forAll(arbitrary[UserAnswers]) {
        initial =>
          val answers: UserAnswers = initial.set(NationalityYesNoPage(0), false).success.value
            .set(NationalityPage(0), "ES").success.value

          val result = answers.set(NationalityUkYesNoPage(0), true).success.value

          result.get(NationalityPage(0)).get mustBe "GB"
          result.get(NationalityUkYesNoPage(0)).get mustBe true
      }
    }

    "No selected" in {
      forAll(arbitrary[UserAnswers]) {
        initial =>
          val answers: UserAnswers = initial.set(NationalityYesNoPage(0), false).success.value
            .set(NationalityPage(0), "ES").success.value

          val result = answers.set(NationalityUkYesNoPage(0), false).success.value

          result.get(NationalityPage(0)).get mustBe "ES"
          result.get(NationalityUkYesNoPage(0)).get mustBe false
      }
    }

  }
}